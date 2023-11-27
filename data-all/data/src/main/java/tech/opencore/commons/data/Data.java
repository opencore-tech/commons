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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

/**
 * This interface represents an abstraction of the concept of data
 * independent from any specific syntax or data types. It is therefore loosely typed.
 * 
 * Data can be empty (i.e. contains nothing).
 * Atomic data contains exactly one atomic value. Non atomic data are either multiple values (array)
 * or multiple named values (object).
 * 
 * Data holding a single object have a size of 1 regardless the number of named values.
 * 
 * @author Eric Boukobza
 */
public interface Data {

    /**
     * Check if the data is atomic. Data is atomic if it contains only one value.
     * 
     * @return true if this instance of data is atomic
     */
    public boolean isAtomic();
    
    public default boolean isObject() {
        return !keySet().isEmpty();
    }
    
    public boolean isBoolean();
    public boolean isIntegral();
    public boolean isNumeric();
    
    /**
     * Check if this data has no values. If this returns true, <code>size()</code> must return 0.
     * 
     * @return true is this data is empty.
     */
    public boolean isEmpty();
    
    /**
     * Check if this data represents a null value. Equivalent to <code>isEmpty()</code>.
     * 
     * @return true is this data is empty.
     */
    public default boolean isNull() { return isEmpty(); }
    
    /**
     * Get the size of this data. is it is primitive it returns 1, else it returns the number of first level
     * values in this data.
     * 
     * @return The size of this data.
     */
    public int size();
    
    public Set<String> keySet();
    
    /**
     * Get the factory that created this instance to create instances of the same implementation.
     *
     * @return The DataFactory that created this instance
     */
    public DataFactory getDataFactory();
    
    public Data get(String property);
    public Data get(int index);
    
    public boolean getBoolean();
    public boolean getBoolean(String property);
    public boolean getBoolean(int index);
    
    public String getString();
    public String getString(String property);
    public default String getString(String property, String defaultValue) {
        String result = getString(property);
        return result == null ? defaultValue : result;
    }
    public String getString(int index);
    
    public long getLong();
    public long getLong(String property);
    public default long getLong(String property, long defaultValue) {
        Data propData = get(property);
        if (propData == null || propData.isNull()) {
            return defaultValue;
        }
        return getLong(property);
    }
    public long getLong(int index);
    
    public int getInt();
    public int getInt(String property);
    public int getInt(int index);
    
    public short getShort();
    public short getShort(String property);
    public short getShort(int index);
    
    public byte getByte();
    public byte getByte(String property);
    public byte getByte(int index);
    
    public double getDouble();
    public double getDouble(String property);
    public double getDouble(int index);
    
    public float getFloat();
    public float getFloat(String property);
    public float getFloat(int index);
    
    public boolean isMutable();
    
    public void clear();
    
    public Data set(Data value);
    public Data set(Data value, boolean mutable);
    public Data set(String property, Data value);
    public Data set(int index, Data value);
    
    public Data setBoolean(boolean value);
    public Data setBoolean(String property, boolean value);
    public Data setBoolean(int index, boolean value);
    
    public Data setString(String value);
    public Data setString(String property, String value);
    public Data setString(int index, String value);
    
    public Data setLong(long value);
    public Data setLong(String property, long value);
    public Data setLong(int index, long value);
    
    public Data setInt(int value);
    public Data setInt(String property, int value);
    public Data setInt(int index, int value);
    
    public Data setShort(short value);
    public Data setShort(String property, short value);
    public Data setShort(int index, short value);
    
    public Data setByte(byte value);
    public Data setByte(String property, byte value);
    public Data setByte(int index, byte value);
    
    public Data setDouble(double value);
    public Data setDouble(String property, double value);
    public Data setDouble(int index, double value);
    
    public Data setFloat(float value);
    public Data setFloat(String property, float value);
    public Data setFloat(int index, float value);
}
