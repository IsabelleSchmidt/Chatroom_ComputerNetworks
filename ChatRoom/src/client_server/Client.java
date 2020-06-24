package client_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class Client {
	
	Socket socket;
    BufferedReader read;
    BufferedWriter output;
    

    public void startClient() throws UnknownHostException, IOException{
        //Create socket connection
        socket = new Socket("localhost", 1234);

        //create printwriter for sending login to server
        output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        //prompt for user name
        String username = JOptionPane.showInputDialog(null, "Enter User Name:");

        //send user name to server
        output.write(username);

        //prompt for password
        String password = JOptionPane.showInputDialog(null, "Enter Password");

        //send password to server
        output.write(password);
        output.flush();

        //create Buffered reader for reading response from server
        read = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //read response from server
        String response = read.readLine();
        System.out.println("This is the response: " + response);

        //display response
        JOptionPane.showMessageDialog(null, response);
    }
    
    public static void main(String args[]){
        Client client = new Client();
        try {
            client.startClient();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
