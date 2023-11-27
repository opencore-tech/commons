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
package tech.opencore.commons.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.opencore.commons.nio.ssl.SSLChannel;

/**
 *
 * @author Eric Boukobza
 */
public class SSLTest {
    
    public SSLTest() {
        System.out.println("SSLTest created.");
    }
    
    @BeforeAll
    public static void setUpClass() {
        System.out.println("SSLTest: setupClass");
    }
    
    @AfterAll
    public static void tearDownClass() {
        System.out.println("SSLTest: tearDownClass");
    }
    
    @BeforeEach
    public void setUp() {
        System.out.println("SSLTest: setUp");
    }
    
    @AfterEach
    public void tearDown() {
        System.out.println("SSLTest: tearDown");
    }
    
    @Test
    public void sslClientTest() {
        System.out.println("SSLTest: sslClientTest");
        try {
            SocketChannel channel = SocketChannel.open();
            channel.connect(new InetSocketAddress("www.infundity.vc", 443));
            System.out.println("Connected to: " + channel.getRemoteAddress());
            SSLChannel sslChannel = new SSLChannel(channel);

            ByteBuffer buf = ByteBuffer.allocate(65536);
            buf.put("HTTP\n".getBytes());
            buf.flip();
            int len = 0;
            while (buf.hasRemaining()) {
                len += sslChannel.write(buf);
                System.out.println(len + " Bytes written.");
            }
            
            System.out.println("Local principal: " + sslChannel.getSSLEngine().getSession().getLocalPrincipal());
            System.out.println("Peer principal: " + sslChannel.getSSLEngine().getSession().getPeerPrincipal());
            
            Assertions.assertTrue(true);
//            sslChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.assertTrue(false);
        }
    }
}
