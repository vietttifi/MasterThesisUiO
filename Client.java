import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Scanner;

public class Client{
	String ip = "10.0.0.134";
	int port = 24000;
	float frequency = 1f;
	boolean running = false;
	String bitalino_ID = "BITdefault1";

	public static void main(String[] args) {
		Client c = new Client();
		System.out.println("\n\n");
		System.out.println("SIMULATE WITH: <SERVER_IP> <PORT> <BITalino ID> <frequency>\n");
		if(args.length >= 4){
			c.ip = args[0];
			c.port = Integer.parseInt(args[1]);
			c.bitalino_ID = args[2];
			c.frequency = Float.parseFloat(args[3]);
			System.out.println("Connect to to "+c.ip+" port "+c.port
				+", BITid: "+c.bitalino_ID +", frequency: "+c.frequency);
			System.out.println("Period:"+((int)((1/c.frequency)*1000)));
		} else {
			System.out.println("Default connect to "+c.ip+" port "+c.port
				+", BITid: "+c.bitalino_ID +", frequency: "+c.frequency);
			System.out.println("Period:"+((int)((1/c.frequency)*1000))+" miliseconds.");
		}

		System.out.println("\n\n");

		c.connectToAndroidServer();
	}

	public void connectToAndroidServer(){
		try {
		    //Create a client socket
		    Socket socket = new Socket(ip,port);
		    //Input stream of the client socket
		    InputStream is = socket.getInputStream();
		    //Buffer the data coming from server
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
		    
		    //Output stream of the client socket
		    PrintWriter out = new PrintWriter(socket.getOutputStream(),true);

			new Thread(new Runnable() {
                    @Override
                    public void run() {
                    	System.out.println("s - for stop.");
                        Scanner sc = new Scanner(System.in);
                        String ss = sc.nextLine();
                        while(!ss.equals("s")) ss = sc.nextLine();
                        System.out.println("BITalino is shuting down ...");

		                running = false;
		                 //Read confirm message from server
		                try {

		                	out.println("END");
		                	
				    		String result = br.readLine();
				    		System.out.println("Message from server: "+result);


				    		is.close();
					    	out.close();
					    	//Close the client socket
					    	socket.close();
				    	} catch (IOException e) {
					    	e.printStackTrace();
					    }

                    }
             }).start();


		    simulateBITalino(out);
		    
	   } catch (NumberFormatException e) {
	    	System.out.println(e.getMessage());
	   } catch (UnknownHostException e) {
	    	System.out.println(e.getMessage());
	   } catch (IOException e) {
	    	System.out.println(e.getMessage());
	   }

	}

	public void simulateBITalino(PrintWriter out){
		//THIS METHOD WILL SIMULATE BITALINO BOARDS.
		String id1 = bitalino_ID;
		int[] ids1 = {1,2,3,4};
		String[] dataTypes1 = {"ECG","EEG","EMG","RESP"};
		String[] metrics1 = {"mV","mV","mV","CM"};
		String[] descriptions1 = {"On chest", "On head", "On somewhere", "On chest"};
		JSONObject bitalino1_metadata = metadata(id1,ids1,dataTypes1,metrics1,descriptions1);

		String id2 = bitalino_ID;
		int[] ids2 = {1,2,3,4};
		String[] dataTypes2 = {"ACG","ECG","EEG","BTN"};
		String[] metrics2 = {"mV","mV","mV","V"};
		String[] descriptions2 = {"Applied somewhere", "On the chest", "By Dr. N", "Click or not click"};
		JSONObject bitalino2_metadata = metadata(id2,ids2,dataTypes2,metrics2,descriptions2);

		String id3 = bitalino_ID;
		int[] ids3 = {1,2,3,4};
		String[] dataTypes3 = {"EFG","FFF","ECG","RSP"};
		String[] metrics3 = {"C","K","mV","mm"};
		String[] descriptions3 = {"Just test", "Experiment", "Applied by dr. T", "On night"};
		JSONObject bitalino3_metadata = metadata(id3,ids3,dataTypes3,metrics3,descriptions3);

		JSONObject bit_metadata[] = {bitalino1_metadata,bitalino2_metadata,bitalino3_metadata};

		//RANDOM 1 OF 3 metadata
		int chosen = (int)(Math.random()*2);
		out.println(bit_metadata[chosen].toString()); 

		int period = ((int)((1/frequency)*1000));
    	running = true;
    	while(running){
    		//Generate random samples
    		Double d1 = new Double(Math.random()*1000 - 500);
    		Integer i1 = new Integer((int)(Math.random()*1000 - 500));
    		Integer i2 = new Integer((int)(Math.random()*100 - 50));
    		Double d2 = new Double(Math.random()*200 - 100);

    		int[] ids = {1,2,3,4};
    		Object[] values = {d1,i1,d2,i2};

    		JSONObject data_sample = construct(bitalino_ID,ids,values);

    		try {
    			Thread.sleep(period);
    			out.println(data_sample.toString());
    		} catch (InterruptedException e) {
    			 System.out.println(e.getMessage());
    			 running = false;
    		}

    		if(out.checkError()){
    			running = false;
    			System.out.println("\nSERVER DISCONNET\n");
		    	System.exit(0);
    		}
    	}


	}

	//----------------------------METHOD FROM GJOBY-------------------------------

	public static JSONObject construct(String id, int[] ids, Object[] values) {
        // TODO: Check if lenght of ids and values match
        long millis = Calendar.getInstance().getTimeInMillis();
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;
        millis = millis % 1000;

        String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);

        JSONObject res = new JSONObject();
        try {
            res.put("type", "data");
            res.put("id", id);
            res.put("time", time);

            JSONArray data = new JSONArray();
            for (int i = 0; i < ids.length; i++) {
                JSONObject element = new JSONObject();
                element.put("id", ids[i]);
                element.put("value", values[i]);
                data.put(element);
            }
            res.put("data", data);
        }catch(JSONException je){
            je.printStackTrace();
        }
        return res;
    }

    //----------------------------METHOD FROM GJOBY-------------------------------

	public static JSONObject metadata(String id, int[] ids, String[] dataTypes,
                                      String[] metrics, String[] descriptions) {

        JSONObject res = new JSONObject();
        try{
            res.put("type", "meta");
            res.put("id", id);
            res.put("name", "BITalino"+id);

            JSONArray channels = new JSONArray();
            for(int i = 0; i <ids.length; i++){
                JSONObject element = new JSONObject();
                element.put("id", ids[i]);
                element.put("type", dataTypes[i]);
                element.put("metric", metrics[i]);
                element.put("description", descriptions[i]);
                channels.put(element);
            }
            res.put("channels", channels);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }

    public void testWithTerminal(){
		String id1 = "1";
		int[] ids1 = {1,2,3,4};
		String[] dataTypes1 = {"ECG","EEG","EMG","RESP"};
		String[] metrics1 = {"mV","mV","mV","CM"};
		String[] descriptions1 = {"On chest", "On head", "On somewhere", "On chest"};
		JSONObject bitalino1_metadata = metadata(id1,ids1,dataTypes1,metrics1,descriptions1);

		String id2 = "2";
		int[] ids2 = {1,2,3,4};
		String[] dataTypes2 = {"ACG","ECG","EEG","BTN"};
		String[] metrics2 = {"mV","mV","mV","V"};
		String[] descriptions2 = {"Applied somewhere", "On the chest", "By Dr. N", "Click or not click"};
		JSONObject bitalino2_metadata = metadata(id2,ids2,dataTypes2,metrics2,descriptions2);

		String id3 = "3";
		int[] ids3 = {1,2,3,4};
		String[] dataTypes3 = {"EFG","FFF","ECG","RSP"};
		String[] metrics3 = {"C","K","mV","mm"};
		String[] descriptions3 = {"Just test", "Experiment", "Applied by dr. T", "On night"};
		JSONObject bitalino3_metadata = metadata(id3,ids3,dataTypes3,metrics3,descriptions3);

		Runtime.getRuntime().addShutdownHook(new Thread() {
	        public void run() {
	            try {
	                Thread.sleep(200);
	                System.out.println("BITalino is shuting down ...");

	                running = false;

	                Thread.sleep(1000);	                
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
    	});


		System.out.print(bitalino1_metadata.toString()+"\n"); 
		System.out.print(bitalino2_metadata.toString()+"\n"); 
		System.out.print(bitalino3_metadata.toString()+"\n"); 
    	running = true;
    	while(running){
    		//Generate random samples
    		Double d1 = new Double(Math.random()*1000 - 500);
    		Integer i1 = new Integer((int)(Math.random()*1000 - 500));
    		Integer i2 = new Integer((int)(Math.random()*100 - 50));
    		Double d2 = new Double(Math.random()*200 - 100);

    		int[] ids = {1,2,3,4};
    		Object[] values = {d1,i1,d2,i2};

    		JSONObject data_sample = construct(""+(int)(Math.random()*3),ids,values);

    		System.out.print(data_sample.toString()+"\n");

    		try {
    			Thread.sleep(1000);
    		} catch (InterruptedException e) {
    			 System.out.println(e.getMessage());
    		}
    	}
    	

	}

}
