package client_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {
	
	Socket socket;
    BufferedReader read;
    BufferedWriter output;
    

    public void startClient() throws UnknownHostException, IOException{
    	
    	try{
		    Socket socket = new Socket("127.0.0.1",8888);
		    DataInputStream inStream = new DataInputStream(socket.getInputStream());
		    DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
		    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    String clientMessage="",serverMessage="";
		    
		    
		    while(!clientMessage.equals("bye")){
		    	if(clientMessage == "") {
		    		System.out.println("Möchtest du dich (e)inloggen oder (r)egistrieren?");
		    	}
		    
		      clientMessage=br.readLine();
		      outStream.writeUTF(clientMessage);
		      outStream.flush();
		      serverMessage = inStream.readUTF();
		      System.out.println(serverMessage);
		    }
		    outStream.close();
		    outStream.close();
		    socket.close();
		    
		  }catch(Exception e){
		    System.out.println("Exception" + e);
		  }
    }
    
    public static void main(String args[]){
        Client client = new Client();
        try {
            client.startClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
