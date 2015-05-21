package sock;

import gvjava.org.json.JSONArray;
import gvjava.org.json.JSONException;
import gvjava.org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import util.GetEnvUtil;

public class Send {
	public String ip = "192.168.12.34";// 服务器地址
	public int port = 8000;// 服务器端口号
	public GetEnvUtil envUtil;
	private String txID = "";

	public Send(String filename ) {
		envUtil = new GetEnvUtil(filename);
		 
		this.port = Integer.parseInt(envUtil.collect_port);
		// LOGGER.info("port:" + port);
		// LOGGER.info("ip:"+ip);
 	}
	public Send(String filename,int port) {
		envUtil = new GetEnvUtil(filename);
		 
//		this.port = Integer.parseInt(envUtil.collect_port);
		// LOGGER.info("port:" + port);
		// LOGGER.info("ip:"+ip);
		this.port = port;
	}

	public Send(int port) {
//		envUtil = new GetEnvUtil(filename);
		// ip = ""
		this.port = port;//Integer.parseInt(envUtil.collect_port);
		// LOGGER.info("port:" + port);
		// LOGGER.info("ip:"+ip);
	}
	/**
	 * @param args
	 */
//	public void sendMsg(String msg) {
//
//		Socket socket = null;
//		try {
//			// 创建一个流套接字并将其连接到指定主机上的指定端口号
//			socket = new Socket(ip, port);
//
//			// 读取服务器端数据
//			DataInputStream input = new DataInputStream(socket.getInputStream());
//			// 向服务器端发送数据
//			DataOutputStream out = new DataOutputStream(
//					socket.getOutputStream());
//			// System.out.print("请输入: \t");
//			out.writeUTF(msg);
//			out.close();
//			input.close();
//		} catch (Exception e) {
//			System.out.println("客户端异常:" + e.getMessage());
//		} finally {
//			if (socket != null) {
//				try {
//					socket.close();
//				} catch (IOException e) {
//					socket = null;
//					System.out.println("客户端 finally 异常:" + e.getMessage());
//				}
//			}
//		}
//
//	}

	public String sendSyncMsg(String msg) {

		Socket socket = null;
		StringBuffer getString = new StringBuffer();

		try {
			// 创建一个流套接字并将其连接到指定主机上的指定端口号
			socket = new Socket(ip, port);

			// 读取服务器端数据
			DataInputStream input = new DataInputStream(socket.getInputStream());
			// 向服务器端发送数据
			DataOutputStream out = new DataOutputStream(
					socket.getOutputStream());
			out.writeUTF(msg);
			// System.out.println("after write, before read");
			// String ret = input.readUTF();
			// byte[]b = new byte[1024];
			// System.out.println(socket.isOutputShutdown()+ ","+
			// socket.getReceiveBufferSize());//false,43690
			// input.re
			int re = 0;

			while (re != -1) {
				re = input.readByte();
				// System.out.println(re);
				Character c = new Character((char) re);
				// System.out.print(c);
				getString.append(c);
				// re
			}
			//
			//

			out.close();
			input.close();
			// System.out.println("closed");
		} catch (Exception e) {
			// System.out.println("客户端异常:" + e.getMessage());
		} finally {
			if (socket != null) {
				try {
					socket.close();
					// System.out.println("close socket")
					;
				} catch (IOException e) {
					socket = null;
					// System.out.println("客户端 finally 异常:" + e.getMessage());
				}
			}
		}

		// System.out.println(getString);
		return getString.toString();

	}

	 
	public String transactionStart(String name, String[] futures,
			String[] pasts, String[] deps, String[] indeps) {
		// txDepMointor.notify(TxEventType.FirstRequestService, transactionID);

		//
		if (txID == null || txID.equals("")) {
			txID = UUID.randomUUID().toString();
		} else {
			System.out.println("well this is a bug!!!");
		}

		JSONObject resultJsonObject = null;
		try {
			resultJsonObject = generateJSONObject(name, futures, pasts, deps,
					indeps, TxEventType.TransactionStart);
		} catch (JSONException e) {
 			e.printStackTrace();
		}

//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
// 			e.printStackTrace();
//		}

		String root = sendSyncMsg(resultJsonObject.toString()); 
		 
		return root;
	}

	public void transactionEnd(String name, String[] futures, String[] pasts,
			String[] deps, String[] indeps) {
		// txDepMointor.notify(TxEventType.FirstRequestService, transactionID);

		JSONObject resultJsonObject = null;
		try {
			resultJsonObject = generateJSONObject(name, futures, pasts, deps,
					indeps, TxEventType.TransactionEnd);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendSyncMsg(resultJsonObject.toString());// ("lddm: "+ +","+
												// transactionID +str +
												// "\n");
	}

	public void firstRequestService(String name, String[] futures,
			String[] pasts, String[] deps, String[] indeps,int other_port) {
		// txDepMointor.notify(TxEventType.FirstRequestService, transactionID);

		JSONObject resultJsonObject = null;
		try {
			resultJsonObject = generateJSONObject(name, futures, pasts, deps,
					indeps, TxEventType.FirstRequestService);
			resultJsonObject.put("other_dea_port", other_port);
			resultJsonObject.put("other_dea_ip", "192.168.12.34");
			resultJsonObject.put("target_comp", "HelloworldComponent");
		} catch (JSONException e) {
 			e.printStackTrace();
		}

		 String res = sendSyncMsg(resultJsonObject.toString());// ("lddm: "+ +","+
												// transactionID +str +
												// "\n");
	}

	public String getTxID() {
		return txID;
	}

	// 
	public void dependencesChanged(String name, String[] futures,
			String[] pasts, String[] deps, String[] indeps) {
		// txDepMointor.notify(TxEventType.FirstRequestService, transactionID);

	 	//

		JSONObject resultJsonObject = null;
		try {
			resultJsonObject = generateJSONObject(name, futures, pasts, deps,
					indeps, TxEventType.DependencesChanged);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendSyncMsg(resultJsonObject.toString());// ("lddm: "+ +","+
												// transactionID +str +
												// "\n");
	}

	private JSONObject generateJSONObject(String name, String[] futures,
			String[] pasts, String[] deps, String[] indeps, TxEventType event)
			throws JSONException {
		JSONObject resultJsonObject = new JSONObject();
		JSONArray futuresArray = new JSONArray();
		for (String futureString : futures)
			futuresArray.put(futureString);
		resultJsonObject.put("FutureComps", futuresArray);

		JSONArray pastsArray = new JSONArray();
		for (String past : pasts)
			pastsArray.put(past);// add(past);//
		resultJsonObject.put("PastComps", pastsArray);

		JSONArray depsArray = new JSONArray();
		for (String dep : deps)
			depsArray.put(dep);
		resultJsonObject.put("deps", depsArray);

		JSONArray indepsArray = new JSONArray();
		for (String indep : indeps)
			indepsArray.put(indep);
		resultJsonObject.put("indeps", indepsArray);

		resultJsonObject.put("name", name);
		resultJsonObject.put("instance_id", envUtil.instace_id);
		resultJsonObject.put("event_type", event);
		resultJsonObject.put("transaction_id", txID);
		return resultJsonObject;
	}

	public static void main(String[] args) {
		Send client = new Send("logs/env.log",8001);
		client.sendSyncMsg("msg");

	}

}
