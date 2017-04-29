import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.net.NetworkInterface;

public class ServerTest{

	public static void main(String[] args) {
		ServerTest s = new ServerTest();
		s.test();
	}

	public void test(){
		//Try to get the IP address of the device
		String serverIPAddress = "";
        try {
            //Check all the network interface devices
            Enumeration<NetworkInterface> iter = NetworkInterface.getNetworkInterfaces();
            while(iter.hasMoreElements()){
                NetworkInterface networkInterface = iter.nextElement();

                //ip addresses of this network interface
                Enumeration<InetAddress> iterIpAddr = networkInterface.getInetAddresses();
                while(iterIpAddr.hasMoreElements()){
                    InetAddress ip_address = iterIpAddr.nextElement();
                    //System.out.println(ip_address.getHostAddress() + "<-------");
                    //only IPv4 (not loop back) is interested.
                    if (!ip_address.isLoopbackAddress() && ip_address.getAddress().length == 4) {
                        //Assign IP address to TextView and make a copy of IP to Object Fragment
                        serverIPAddress = ip_address.getHostAddress();
                    }
                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        System.out.println("IP: "+serverIPAddress + " port: "+24000);
        ServerSocket sockServer;
		try {
            //Create a server socket object, bind it to server_port
            sockServer = new ServerSocket(24000);
            //Multi clients management
            while (true) {
                //Accept the client connection, then give it to ServerThread with client socket
                Socket socClient = sockServer.accept();
                ClientThread clientConnected = new ClientThread(socClient);
                clientConnected.start();
            }
        } catch (IOException e) {
            //WHEN sockServer close, it will be here
            sockServer = null;
        }
	}

	private class ClientThread extends Thread{
		private Socket clientsSocket;
		ClientThread(Socket clientsSocket){
			this.clientsSocket = clientsSocket;
		}

		@Override
	    public void run(){
	        final String clientIP = clientsSocket.getRemoteSocketAddress().toString();
	        System.out.println("CLIENT IP "+clientIP);
	        int cnt = 0;
	        try{
	            String jsonStringFromBITalino = null;
	            BufferedReader bf = new BufferedReader(new InputStreamReader(clientsSocket.getInputStream()));
	            while ((jsonStringFromBITalino = bf.readLine()) != null && !jsonStringFromBITalino.equals("END")){
	                System.out.println("have gotten "+cnt++ +" "+jsonStringFromBITalino);
	            }

	        }catch (IOException e){
	            e.printStackTrace();
	        }
	    }
	}
}