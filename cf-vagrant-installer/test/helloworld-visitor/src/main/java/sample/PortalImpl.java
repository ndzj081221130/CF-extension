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
import org.oasisopen.sca.annotation.Reference;

import sock.Send;
 public class PortalImpl implements Portal {
    private Auth auth;
    @Reference	
    public void setAuth(Auth h){
       this.auth = h;
    }	
    public Auth getAuth(){
       return auth;
    }
    private Proc proc;
    @Reference
    public void setProc(Proc p){
    	this.proc = p;
    }
    
    public Proc getProc(){
    	return proc;
    }
    public String portal(String hi) {
    	Send  send = new Send ("/home/vcap/logs/env.log");///home/zhang/call-helloworld/
    	String authName = "AuthComponent";
     	String procName = "ProcComponent";
    	
    	String futures[] = new String[]{ authName,procName};
    	String pasts[] = new String[]{};
    	
    	String[]deps  = new String[]{authName, procName};
    	String[]indeps = new String []{ }; 
    	
    	String name ="PortalComponent";
    	
    	
    	String root = send.transactionStart(name,futures,pasts,deps,indeps );
    	
    	
//    	-------------
    	
    	send.firstRequestService(name,futures,pasts,deps,indeps,8000);
    	String idString = send.getTxID();
    	
    	
    	String aString = auth.getToken(hi+"|"+ root+"|"+ idString);
    	System.out.println(hi);
    	
    	
    	
     	
    	 
    	
    	futures = new String[]{procName};
    	pasts = new String[]{authName };
    	send.dependencesChanged(name,futures,pasts,deps,indeps);
    	
    	
    	
    	
    	
    	
    	
//    	---- second
    	send.firstRequestService(name,futures,pasts,deps,indeps,8001);
     	
    	
    	String cString = proc.process(aString +"|"+ root+"|"+ idString);
    	System.out.println(cString);
    	
    	
    	
     	
    	 
    	
    	futures = new String[]{};
    	pasts = new String[]{procName,authName };
    	send.dependencesChanged(name,futures,pasts,deps,indeps);
    	
     
    	
    	
    	
    	send.transactionEnd(name,futures,pasts,deps,indeps);
        return "Portal.  " + cString;
    }

}
