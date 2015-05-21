/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.tuscany.sca.Node;
import org.apache.tuscany.sca.TuscanyRuntime;
import org.junit.Assert;
import org.junit.Test;
import org.oasisopen.sca.NoSuchServiceException;

public class HelloworldTestCase {

    @Test
    public void testSayHello() throws NoSuchServiceException, IOException {

        // Run the SCA composite in a Tuscany runtime
        Node node = TuscanyRuntime.runComposite("helloworld.composite", "target/classes");
        try {
            
            // Get the Helloworld service proxy
            Helloworld helloworld = node.getService(Helloworld.class, "HelloworldComponent");
            
            // test that it works as expected
            Assert.assertEquals("Hello Amelia", helloworld.sayHello("Amelia"));
            
            // test that has exposed an HTTP endpoint that works as expected
            // JSONRPC args are base64 encoded, ["World"] = WyJXb3JsZCJd  
            URL url = new URL("http://localhost:8080/HelloworldComponent/Helloworld?method=sayHello&params=WyJXb3JsZCJd&id=1");
            String response = read(url.openStream());
            System.out.println(response);
            Assert.assertTrue(response.contains("\"id\":1,\"result\":\"Hello World\""));

        } finally {
            // Stop the Tuscany runtime Node
            node.stop();        
        }
    }

    private static String read(InputStream is) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
