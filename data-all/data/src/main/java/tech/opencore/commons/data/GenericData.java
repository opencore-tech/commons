/*
 * Copyright 2022 opencore.tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.opencore.commons.data;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ericb
 */
public class GenericData implements Data, DataFactory, Serializable {
    static public final Data NULL = new GenericData(false);
    static private final Set<String> NO_KEYS = new HashSet<>();
    
    private enum ValueType {
        booleanType,
        stringType,
        integralType,
        numericType,
        object,
        array,
        nullType
    }
    
    private ValueType valueType = ValueType.nullType;
    private boolean booleanValue = false;
    private String stringValue = null;
    private long integralValue = 0;
    private double numericValue = 0;
    private Map<String,Data> content = new LinkedHashMap<>();
    private Data[] array = null;

    private boolean isMutable = true;
    
    public GenericData() {}
    
    public GenericData(boolean mutable) {
        isMutable = mutable;
    }
    
    public GenericData(Data data) {
        this(data, true);
    }
    
    public GenericData(Data data, boolean mutable) {
        set(data, mutable);
    }
    
    @Override
    public boolean isAtomic() {
        switch (valueType) {
            case nullType:
                return false;
            case booleanType:
            case stringType:
            case integralType:
            case numericType:
                return true;
            default:
                return size() == 1 && get(0).isAtomic();
        }
    }
    
    @Override
    public boolean isBoolean() {
        switch (valueType) {
            case nullType:
                return false;
            case booleanType:
                return true;
            case integralType:
            case numericType:
                return false;
            case stringType:
                try {
                    Boolean.parseBoolean(stringValue);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            default:
                return size() == 1 && get(0).isBoolean();
        }
    }
    
    @Override
    public boolean isIntegral() {
        switch (valueType) {
            case nullType:
                return false;
            case booleanType:
                return false;
            case integralType:
                return true;
            case numericType:
                return false;
            case stringType:
                try {
                    Long.parseLong(stringValue);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            default:
                return size() == 1 && get(0).isIntegral();
        }
    }
    
    public boolean isNumeric() {
        switch (valueType) {
            case nullType:
                return false;
            case booleanType:
                return false;
            case integralType:
                return false;
            case numericType:
                return true;
            case stringType:
                try {
                    Double.parseDouble(stringValue);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            default:
                return size() == 1 && get(0).isNumeric();
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        switch (valueType) {
            case nullType:
                return 0;
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                return 1;
            case object:
                return content.size();
            case array:
                return array.length;
        }
        
        return 0;
    }
    
    @Override
    public Set<String> keySet() {
        if (valueType != ValueType.object) {
            return NO_KEYS;
        }
        
        return content.keySet();
    }

    @Override
    public Data get(String property) {
        Data result = content.get(property);
        
        return result == null ? NULL : result;
    }

    @Override
    public Data get(int index) {
        switch (valueType) {
            case nullType:
                return NULL;
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                if (index == 0) {
                    return this;
                }
                
                return NULL;
            case object:
                if (index > size()) {
                    return NULL;
                }
                
                int i = 0;
                for (Data d : content.values()) {
                    if (i++ == index) {
                        return d;
                    }
                }
                
                return NULL;
            case array:
                if (index < 0 && index > array.length) {
                    return NULL;
                }

                return array[index] == null ? NULL : array[index];
        }
        
        return NULL;
    }
    
    @Override
    public boolean getBoolean() {
        switch (valueType) {
            case nullType:
                return false;
            case booleanType:
                return booleanValue;
            case integralType:
                return integralValue != 0;
            case numericType:
                return numericValue != 0;
            case stringType:
                return Boolean.parseBoolean(stringValue);
            default:
                return get(0).getBoolean();
        }
    }
    
    @Override
    public boolean getBoolean(String property) {
        return get(property).getBoolean();
    }
    
    @Override
    public boolean getBoolean(int index) {
        return get(index).getBoolean();
    }
    
    @Override
    public String getString() {
        switch (valueType) {
            case nullType:
                return null;
            case booleanType:
                return Boolean.toString(booleanValue);
            case integralType:
                return Long.toString(integralValue);
            case numericType:
                return Double.toString(numericValue);
            case stringType:
                return stringValue;
            default:
                return get(0).getString();
        }
    }
    
    @Override
    public String getString(String property) {
        return get(property).getString();
    }
    
    @Override
    public String getString(int index) {
        return get(index).getString();
    }
    
    @Override
    public long getLong() {
        switch (valueType) {
            case nullType:
                return 0;
            case booleanType:
                return booleanValue ? 1 : 0;
            case integralType:
                return integralValue;
            case numericType:
                return (long) numericValue;
            case stringType:
                return Long.parseLong(stringValue);
            default:
                return get(0).getLong();
        }
    }
    
    @Override
    public long getLong(String property) {
        return get(property).getLong();
    }
    
    @Override
    public long getLong(int index) {
        return get(index).getLong();
    }
    
    @Override
    public int getInt() {
        switch (valueType) {
            case nullType:
                return 0;
            case booleanType:
                return booleanValue ? 1 : 0;
            case integralType:
                return (int) integralValue;
            case numericType:
                return (int) numericValue;
            case stringType:
                return Integer.parseInt(stringValue);
            default:
                return get(0).getInt();
        }
    }
    
    @Override
    public int getInt(String property) {
        return get(property).getInt();
    }
    
    @Override
    public int getInt(int index) {
        return get(index).getInt();
    }
    
    @Override
    public short getShort() {
        switch (valueType) {
            case nullType:
                return 0;
            case booleanType:
                return (short) (booleanValue ? 1 : 0);
            case integralType:
                return (short) integralValue;
            case numericType:
                return (short) numericValue;
            case stringType:
                return Short.parseShort(stringValue);
            default:
                return get(0).getShort();
        }
    }
    
    @Override
    public short getShort(String property) {
        return get(property).getShort();
    }
    
    @Override
    public short getShort(int index) {
        return get(index).getShort();
    }
    
    @Override
    public byte getByte() {
        switch (valueType) {
            case nullType:
                return 0;
            case booleanType:
                return (byte) (booleanValue ? 1 : 0);
            case integralType:
                return (byte) integralValue;
            case numericType:
                return (byte) numericValue;
            case stringType:
                return Byte.parseByte(stringValue);
            default:
                return get(0).getByte();
        }
    }
    
    @Override
    public byte getByte(String property) {
        return get(property).getByte();
    }
    
    @Override
    public byte getByte(int index) {
        return get(index).getByte();
    }
    
    @Override
    public double getDouble() {
        switch (valueType) {
            case nullType:
                return 0;
            case booleanType:
                return (double) (booleanValue ? 1 : 0);
            case integralType:
                return (double) integralValue;
            case numericType:
                return numericValue;
            case stringType:
                return Double.parseDouble(stringValue);
            default:
                return get(0).getDouble();
        }
    }
    
    @Override
    public double getDouble(String property) {
        return get(property).getDouble();
    }
    
    @Override
    public double getDouble(int index) {
        return get(index).getDouble();
    }
    
    @Override
    public float getFloat() {
        switch (valueType) {
            case nullType:
                return 0;
            case booleanType:
                return (float) (booleanValue ? 1 : 0);
            case integralType:
                return (float) integralValue;
            case numericType:
                return (float) numericValue;
            case stringType:
                return Float.parseFloat(stringValue);
            default:
                return get(0).getFloat();
        }
    }
    
    @Override
    public float getFloat(String property) {
        return get(property).getFloat();
    }
    
    @Override
    public float getFloat(int index) {
        return get(index).getFloat();
    }
    
    @Override
    public boolean isMutable() {
        return isMutable;
    }

    @Override
    public void clear() {
    	if (isNull()) {
    		return;
    	}
    	
        checkMutable();
        valueType = ValueType.nullType;
        content.clear();
        array = null;
    }
    
    @Override
    public Data set(Data value) {
        return set(value, isMutable);
    }
    
    @Override
    public Data set(Data value, boolean mutable) {
        clear();
        
        if (value.isNull()) {
          return this;  
        } 
        
        if (!value.keySet().isEmpty()) {
            for (String key : value.keySet()) {
                set(key, new GenericData(value.get(key), mutable));
            }
        } else if (value.size() > 1) {
            valueType = ValueType.array;
            array = new Data[value.size()];
            for (int i = 0 ; i < array.length ; i++) {
                array[i] = new GenericData(value.get(i), mutable);
            }
        } else {
            if (value.isIntegral()) {
                valueType = ValueType.integralType;
                integralValue = value.getLong();
            } else if (value.isNumeric()) {
                valueType = ValueType.numericType;
                numericValue = value.getDouble();
            } else if (value.isBoolean()) {
                valueType = ValueType.booleanType;
                booleanValue = value.getBoolean();
            } else {
                valueType = ValueType.stringType;
                stringValue = value.getString();
            }
        }
        
        isMutable = mutable;
        
        return this;
    }
    
    @Override
    public Data set(String property, Data value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                clear();
            case nullType:
                valueType = ValueType.object;
            case object:
                content.put(property, value);
                break;
            case array:
                get(0).set(property, value);
                break;
        }
        
        return this;
    }
    
    @Override
    public Data set(int index, Data value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
            case object:
                if (index == 0) {
                    set(value);
                    return this;
                }
            case nullType:
                array = new Data[index];
                valueType = ValueType.array;
                Data data = new GenericData(this);
                array[0] = data;
            case array:
                ensureCapacity(index+1);
                array[index] = new GenericData(value);
        }
        
        return this;
    }
    
    @Override
    public Data setBoolean(boolean value) {
        clear();
        valueType = ValueType.booleanType;
        booleanValue = value;
        
        return this;
    }
    
    @Override
    public Data setBoolean(String property, boolean value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                clear();
            case nullType:
                valueType = ValueType.object;
            case object:
                content.put(property, new GenericData().setBoolean(value));
                break;
            case array:
                get(0).set(property, new GenericData().setBoolean(value));
                break;
        }
        
        return this;
    }
    
    @Override
    public Data setBoolean(int index, boolean value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
            case object:
                if (index == 0) {
                    setBoolean(value);
                    return this;
                }
            case nullType:
                array = new Data[index];
                valueType = ValueType.array;
                Data data = new GenericData(this);
                array[0] = data;
            case array:
                ensureCapacity(index+1);
                array[index] = new GenericData().setBoolean(value);
        }
        
        return this;
    }
    
    @Override
    public Data setString(String value) {
        clear();
        valueType = ValueType.stringType;
        stringValue = value;
        
        return this;
    }
    
    @Override
    public Data setString(String property, String value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                clear();
            case nullType:
                valueType = ValueType.object;
            case object:
                content.put(property, new GenericData().setString(value));
                break;
            case array:
                get(0).set(property, new GenericData().setString(value));
                break;
        }
        
        return this;
    }
    
    @Override
    public Data setString(int index, String value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
            case object:
                if (index == 0) {
                    setString(value);
                    return this;
                }
            case nullType:
                array = new Data[index];
                valueType = ValueType.array;
                Data data = new GenericData(this);
                array[0] = data;
            case array:
                ensureCapacity(index+1);
                array[index] = new GenericData().setString(value);
        }
        
        return this;
    }
    
    @Override
    public Data setLong(long value) {
        clear();
        valueType = ValueType.integralType;
        integralValue = value;
        
        return this;
    }
    
    @Override
    public Data setLong(String property, long value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                clear();
            case nullType:
                valueType = ValueType.object;
            case object:
                content.put(property, new GenericData().setLong(value));
                break;
            case array:
                get(0).set(property, new GenericData().setLong(value));
                break;
        }
        
        return this;
    }
    
    @Override
    public Data setLong(int index, long value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
            case object:
                if (index == 0) {
                    setLong(value);
                    return this;
                }
            case nullType:
                array = new Data[index];
                valueType = ValueType.array;
                Data data = new GenericData(this);
                array[0] = data;
            case array:
                ensureCapacity(index+1);
                array[index] = new GenericData().setLong(value);
        }
        
        return this;
    }
    
    @Override
    public Data setInt(int value) {
        clear();
        valueType = ValueType.integralType;
        integralValue = value;
        
        return this;
    }
    
    @Override
    public Data setInt(String property, int value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                clear();
            case nullType:
                valueType = ValueType.object;
            case object:
                content.put(property, new GenericData().setInt(value));
                break;
            case array:
                get(0).set(property, new GenericData().setInt(value));
                break;
        }
        
        return this;
    }
    
    @Override
    public Data setInt(int index, int value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
            case object:
                if (index == 0) {
                    setInt(value);
                    return this;
                }
            case nullType:
                array = new Data[index];
                valueType = ValueType.array;
                Data data = new GenericData(this);
                array[0] = data;
            case array:
                ensureCapacity(index+1);
                array[index] = new GenericData().setInt(value);
        }
        
        return this;
    }
    
    @Override
    public Data setShort(short value) {
        clear();
        valueType = ValueType.integralType;
        integralValue = value;
        
        return this;
    }
    
    @Override
    public Data setShort(String property, short value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                clear();
            case nullType:
                valueType = ValueType.object;
            case object:
                content.put(property, new GenericData().setShort(value));
                break;
            case array:
                get(0).set(property, new GenericData().setShort(value));
                break;
        }
        
        return this;
    }
    
    @Override
    public Data setShort(int index, short value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
            case object:
                if (index == 0) {
                    setShort(value);
                    return this;
                }
            case nullType:
                array = new Data[index];
                valueType = ValueType.array;
                Data data = new GenericData(this);
                array[0] = data;
            case array:
                ensureCapacity(index+1);
                array[index] = new GenericData().setShort(value);
        }
        
        return this;
    }
    
    @Override
    public Data setByte(byte value) {
        clear();
        valueType = ValueType.integralType;
        integralValue = value;
        
        return this;
    }
    
    @Override
    public Data setByte(String property, byte value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                clear();
            case nullType:
                valueType = ValueType.object;
            case object:
                content.put(property, new GenericData().setByte(value));
                break;
            case array:
                get(0).set(property, new GenericData().setByte(value));
                break;
        }
        
        return this;
    }

    @Override
    public Data setByte(int index, byte value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
            case object:
                if (index == 0) {
                    setByte(value);
                    return this;
                }
            case nullType:
                array = new Data[index];
                valueType = ValueType.array;
                Data data = new GenericData(this);
                array[0] = data;
            case array:
                ensureCapacity(index+1);
                array[index] = new GenericData().setByte(value);
        }
        
        return this;
    }
    
    @Override
    public Data setDouble(double value) {
        clear();
        valueType = ValueType.numericType;
        numericValue = value;
        
        return this;
    }
    
    @Override
    public Data setDouble(String property, double value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                clear();
            case nullType:
                valueType = ValueType.object;
            case object:
                content.put(property, new GenericData().setDouble(value));
                break;
            case array:
                get(0).set(property, new GenericData().setDouble(value));
                break;
        }
        
        return this;
    }
    
    @Override
    public Data setDouble(int index, double value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
            case object:
                if (index == 0) {
                    setDouble(value);
                    return this;
                }
            case nullType:
                array = new Data[index];
                valueType = ValueType.array;
                Data data = new GenericData(this);
                array[0] = data;
            case array:
                ensureCapacity(index+1);
                array[index] = new GenericData().setDouble(value);
        }
        
        return this;
    }
    
    @Override
    public Data setFloat(float value) {
        clear();
        valueType = ValueType.numericType;
        numericValue = value;
        
        return this;
    }
    
    @Override
    public Data setFloat(String property, float value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
                clear();
            case nullType:
                valueType = ValueType.object;
            case object:
                content.put(property, new GenericData().setFloat(value));
                break;
            case array:
                get(0).set(property, new GenericData().setFloat(value));
                break;
        }
        
        return this;
    }
    
    @Override
    public Data setFloat(int index, float value) {
        checkMutable();
        
        switch (valueType) {
            case booleanType:
            case integralType:
            case numericType:
            case stringType:
            case object:
                if (index == 0) {
                    setFloat(value);
                    return this;
                }
            case nullType:
                array = new Data[index];
                valueType = ValueType.array;
                Data data = new GenericData(this);
                array[0] = data;
            case array:
                ensureCapacity(index+1);
                array[index] = new GenericData().setFloat(value);
        }
        
        return this;
    }
    
    private void ensureCapacity(int capacity) {
        if (array.length < capacity) {
            Data[] newArray = new Data[capacity];
            Arrays.fill(newArray, NULL);
            System.arraycopy(array, 0, newArray, 0, array.length);
        }
    }
    
    private void checkMutable() {
        if (!isMutable) {
            throw new IllegalStateException("Data is immutable");
        }
    }
    
    @Override
    public DataFactory getDataFactory() {
        return this;
    }

    @Override
    public Data createData() {
        return new GenericData();
    }

    @Override
    public Data deserialize(InputStream in) throws Exception {
        ObjectInputStream input = new ObjectInputStream(in);
        
        Object data = input.readObject();
        
        return (Data) data;
    }

    @Override
    public void serialize(Data data, OutputStream out) throws Exception {
        ObjectOutputStream output = new ObjectOutputStream(out);
        
        output.writeObject(data);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return isNull();
        }
        
        if (!(o instanceof Data)) {
            return false;
        }
        
        Data d = (Data) o;
        
        if (isEmpty()) {
            return d.isEmpty();
        }
        
        if (isAtomic()) {
            return getString().equals(d.getString());
        }
        
        if (size() != d.size()) {
            return false;
        }
        
        if (!keySet().isEmpty()) {
            if (d.keySet().isEmpty()) {
                return false;
            }
            
            for (String key : keySet()) {
                if (!get(key).equals(d.get(key))) {
                    return false;
                }
            }
            
            return true;
        }
        
        for (int i=0 ; i < size() ; i++) {
            if (!get(i).equals(d.get(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        if (isEmpty()) {
            return 0;
        }
        
        if (isAtomic()) {
            return getInt();
        }
        
        if (!keySet().isEmpty()) {
            int result = 0;
            
            for (String key : keySet()) {
                result += key.hashCode();
                result += get(key).hashCode();
            }
            
            return result;
        }
        
        int result = 0;
        for (int i=0 ; i < size() ; i++) {
            result += get(i).hashCode();
        }
        
        return result;
    }
}
