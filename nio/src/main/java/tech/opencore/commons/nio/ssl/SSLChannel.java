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
package tech.opencore.commons.nio.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

/**
 *
 * @author Eroc Boukobza
 */
public class SSLChannel implements ByteChannel {
    public static final ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
    private Executor executor = null;
    private SSLEngine sslEngine;
    private ByteChannel networkChannel;
    private ByteBuffer encryptedInput = ByteBuffer.allocate(65536);
    private ByteBuffer encryptedOutput = ByteBuffer.allocate(65536);

    public SSLChannel(ByteChannel channel) throws NoSuchAlgorithmException, SSLException {
        this(channel, null, null);
    }
    
    public SSLChannel(ByteChannel channel, SSLEngine engine, Executor executor) throws NoSuchAlgorithmException, SSLException {
        this.networkChannel = channel;
        this.sslEngine = engine;
        this.executor = executor;
        
        if (sslEngine == null) {
            sslEngine = SSLContext.getDefault().createSSLEngine();
            sslEngine.setUseClientMode(true);
        }
    }
    
    public SSLEngine getSSLEngine() {
        return sslEngine;
    }
    
    public boolean isHandshaking() throws IOException {
        System.out.println("HS status: " + sslEngine.getHandshakeStatus());
        switch (sslEngine.getHandshakeStatus()) {
            case FINISHED:
                return false;
            case NEED_TASK:
                Runnable task;
                while ((task = sslEngine.getDelegatedTask()) != null) {
                    if (executor == null) {
                        task.run();
                    } else {
                        executor.execute(task);
                    }
                }
                
                return true;
            case NEED_UNWRAP:
            case NEED_UNWRAP_AGAIN:
                networkChannel.read(encryptedInput);
                encryptedInput.flip();
                sslEngine.unwrap(encryptedInput, emptyBuffer);
                encryptedInput.compact();
                return true;
            case NEED_WRAP:
                sslEngine.wrap(emptyBuffer, encryptedOutput);
                encryptedOutput.flip();
                networkChannel.write(encryptedOutput);
                encryptedOutput.compact();
                return true;
            case NOT_HANDSHAKING:
                return false;
        }
        
        return false;
    }

    @Override
    public int read(ByteBuffer clearInput) throws IOException {
        if (!isHandshaking()) {
            if (networkChannel.read(encryptedInput) > 0) {
                encryptedInput.flip();
                SSLEngineResult result = sslEngine.unwrap(encryptedInput, clearInput);
                encryptedInput.compact();
                return result.bytesProduced();
            }
        }
        
        return 0;
    }

    @Override
    public boolean isOpen() {
        return networkChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        sslEngine.closeInbound();
        sslEngine.closeOutbound();
        networkChannel.close();
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (!isHandshaking()) {
            SSLEngineResult result = sslEngine.wrap(src, encryptedOutput);
            encryptedOutput.flip();
            networkChannel.write(encryptedOutput);
            encryptedOutput.compact();
            return result.bytesConsumed();
        }
        
        return 0;
    }
}
