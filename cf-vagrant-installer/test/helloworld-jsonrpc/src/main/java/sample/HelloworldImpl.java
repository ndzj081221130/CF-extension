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


import sock.Send;

public class HelloworldImpl implements Helloworld {

    public String sayHello(String name) {
    	
    	Send send = new Send("/home/vcap/logs/env.log");///home/zhang/call-helloworld/
    	String callString = "CallComponent";
    	String papaString = "PaPaComponent";
    	String helloString = "HelloworldComponent";
    	String future[] = new String[]{};
    	String past[] = new String[]{};
    	String[]deps  = new String[]{};
    	String[]indeps = new String []{callString,papaString}; 
    	String root = send.transactionStart(helloString,future,past,deps,indeps );
    	 
    	System.out.println("root=" + root);
    	send.transactionEnd(helloString,future,past,deps,indeps);
    	
        return "Auth.v1 " + name;
    }

}
