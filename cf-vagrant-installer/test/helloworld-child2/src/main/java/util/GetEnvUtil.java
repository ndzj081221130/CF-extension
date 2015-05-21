package util;
import gvjava.org.json.JSONArray;
import gvjava.org.json.JSONException;
import gvjava.org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
 

public class GetEnvUtil {

	public String instace_id="";
	public String instance_index="";
	public String application_version ="";
	public String application_name="";
	public String [] application_uris;
	public String port = "";
	public String collect_port="";
	public String jsonData="";
	public GetEnvUtil(String file) {
		
		
		readEnvFile(file);
	}
	public  void readEnvFile(String filename){
		InputStream is;
		try {
			is = new FileInputStream(filename);
			String line;        // 用来保存每行读取的内容
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        line = reader.readLine();
	        String [] part;
	        while (line != null) {   
	        	//System.out.println("line=" + line + "\n");
	        	
        		if(line.startsWith("VCAP_COLLECT_PORT"))
        		{
        			 part = line.split("=");
        			 collect_port = part[1];
        		}else if(line.startsWith("VCAP_APP_PORT")){
        			part = line.split("=");
        			port = part[1];
        		}
        		else if(line.startsWith("VCAP_APPLICATION")){
        			part = line.split("=");
        			String jsonApp = part[1];
        			JSONObject obj = new JSONObject(jsonApp);
        			instace_id = obj.getString("instance_id");
        			instance_index = obj.getString("instance_index");
        			application_version = obj.getString("application_version");
        			application_name = obj.getString("application_name");
        			JSONArray jsonArray =  obj.getJSONArray("application_uris");
        			application_uris = new String[jsonArray.length()];
        			for (int i = 0; i < jsonArray.length(); i++) {
						application_uris[i] = jsonArray.getString(i);
					}
        			//obj.put("stats", stats);
        			jsonData = obj.toString();
        			break;
        		}
	            line = reader.readLine();   // 读取下一行
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String toString() {
//		
//		for(String uri: application_uris)
//			System.out.print(uri+" ");
//		System.out.println();
//		return "{\"id\":\"" + instace_id+"\"," + "\"name\":\"" + application_name + "\"," + 
//		"\"index\":\"" + instance_index+"\""
////				+ "," 
////		+ "\"port\":" + port+",\"collect_port\":" + collect_port
//		+",\"stats\":\"" + stats +"\""
//		+"}";
		return jsonData;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GetEnvUtil env = new GetEnvUtil("logs/env.log");
//		env.readEnvFile("logs/env.log");
		System.out.println(env);
	}

}
