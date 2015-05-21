package sample;

import org.oasisopen.sca.annotation.Reference;

import sock.Send;

public class PaPaImpl implements PaPa{

	private Helloworld auth;
    @Reference	
    public void setAuth(Helloworld h){
       this.auth = h;
    }	
    public Helloworld getAuth(){
       return auth;
    }
    private Call  proc ;
    @Reference 
    public void setProc(Call  c){
    	this.proc = c;
    }
    public Call getProc(){
    	return proc;
    }
	public String doPaPa(String me) {
 		Send  send = new Send ("/home/vcap/logs/env.log"); 
		String helloName = "HelloworldComponent";
		String callName = "CallComponent";
		
		
    	String future[] = new String[]{helloName,callName};
    	String past[] = new String[]{};
    	String[]deps  = new String[]{helloName,callName};
    	String[]indeps = new String []{}; 
    	
    	
    	String name ="PaPaComponent";
    	String root = send.transactionStart(name,future,past,deps,indeps );
    	
    	String invocationCtx = send.firstRequestService(name,future,past,deps,indeps,8000,helloName);
    	String idString = send.getTxID();
    	String token = auth.sayHello(me +"|"+ root+"|"+ idString +"|" + name+"|" + send.getPort()+"|"+ helloName
    												+"|"+invocationCtx); 
//    	|root|parentTx|parent|port|sub|invocationCtxString
    	
     	 
    	future = new String[]{callName};
    	past = new String[]{helloName};
    	send.dependencesChanged(name,future,past,deps,indeps);
    	
    	
    	String invocationCall  = send.firstRequestService(name,future,past,deps,indeps,8004,callName);// 
     	String call_ret_String = proc.call(token +"|"+ root+"|"+ idString +"|" + name + "|" +
     										send.getPort() + "|" + callName + "|" + invocationCall);
      
    	
    	future = new String[]{};
    	past = new String[]{callName,helloName};
    	send.dependencesChanged(name,future,past,deps,indeps);
    	
    	send.transactionEnd(name,future,past,deps,indeps);
        return "Portal.  " + call_ret_String;
        
 	}

}
