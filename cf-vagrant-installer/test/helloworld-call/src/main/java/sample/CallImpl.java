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
 public class CallImpl implements Call {
    private Helloworld auth;
    @Reference	
    public void setAuth(Helloworld h){
       this.auth = h;
    }	
    public Helloworld getAuth(){
       return auth;
    }
     
    private DBService db;
    @Reference 
    public void setDb(DBService d){
    	this.db = d;
    }
    
    public DBService getDb() {
		return db;
	}
    
    
    public String call(String hi) {
    	Send  send = new Send ("/home/vcap/logs/env.log"); 
    	String HelloName = "HelloworldComponent";
    	String DBName = "DBComponent";		
    	String futures[] = new String[]{HelloName,DBName };
     	String pasts[] = new String[]{};
    	
    	String[]deps  = new String[]{HelloName,DBName};
     	
    	String[]indeps = new String []{"PaPaComponent"}; 
    	String name ="CallComponent";
    	
    	
    	String root = send.transactionStart(name,futures,pasts,deps,indeps );
    	            
    	String authInvocation = send.firstRequestService(name,futures,pasts,deps,indeps,8000);
    	String idString = send.getTxID();
    	String hString = auth.sayHello(hi +"|"+ root+"|"+ idString +"|" + name +"|" +
    							send.getPort() + "|" + HelloName +"|" + authInvocation);
    	
    	//|root|parentTx|parent|port|subComp|invocationCtxString
    	
    	//sub_port is dynamic chose by router
    	
    	
 
    	
    	futures = new String[]{DBName};
    	pasts = new String[]{HelloName};

    	send.dependencesChanged(name, futures  , pasts , deps, indeps);
    	
    	String dbInvocation = send.firstRequestService(name, futures , pasts , deps, indeps, 8002);
    	String idString2 = send.getTxID();
    	String dString = db.dbOperation(hString +"|"+ root+"|"+ idString2 + "|" + name +"|"
    			+ send.getPort() +"|" + DBName +"|" + dbInvocation);
     	futures = new String[]{};
    	pasts = new String[]{HelloName,DBName};
    	
    	
    	
    	send.transactionEnd(name,futures,pasts,deps,indeps);
        return "Proc.  " +dString;
    }

}
