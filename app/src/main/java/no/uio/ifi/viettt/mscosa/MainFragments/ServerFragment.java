package no.uio.ifi.viettt.mscosa.MainFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import no.uio.ifi.viettt.mscosa.MainActivity;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ClientThread;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ConnectedListAdapter;

/**
 * Created by viettt on 18/12/2016.
 */

public class ServerFragment extends Fragment {
    public MainActivity mMainActivity;
    PlotViewFragment plotViewFragment;
    final ServerFragment selv;
    
    //This fragment view
    View v;

    //For manage the connections
    public HashMap<String, SensorSource> clientConnectedList;
    public HashMap<Socket,ClientThread> connectedThread = new HashMap<>();
    public int MAXCONN = 8;

    //GUI
    private Button startServer, stopServer_NOSaveData, stopServer_SAVEData;
    public EditText txtMAXCONN, txtServerPort;
    ListView listView;
    private int currentSelected = 0;
    private List<SensorSource> listSensorsForView;

    //Elements to control server
    String serverIPAddress;
    private int serverPort = 24000;
    private Thread serverListeningThread;
    public ServerSocket socServer;
    

    //The handler for manage GUI
    private Handler serverUpdateUI = new Handler();

    public ServerFragment(){
        getIPofThisDeviceAsServerIP();
        clientConnectedList = new HashMap<>();
        listSensorsForView = new ArrayList<>();
        selv = this;
    }

    private void getIPofThisDeviceAsServerIP(){
        //Try to get the IP address of the device
        try {
            //Check all the network interface devices
            Enumeration<NetworkInterface> iter = NetworkInterface.getNetworkInterfaces();
            while(iter.hasMoreElements()){
                NetworkInterface networkInterface = iter.nextElement();

                //ip addresses of this network interface
                Enumeration<InetAddress> iterIpAddr = networkInterface.getInetAddresses();
                while(iterIpAddr.hasMoreElements()){
                    InetAddress ip_address = iterIpAddr.nextElement();
                    System.out.println(ip_address.getHostAddress() + "<-------");
                    //only IPv4 (not loop back) is interested.
                    if (!ip_address.isLoopbackAddress() && ip_address.getAddress().length == 4) {
                        //Assign IP address to TextView and make a copy of IP to Object Fragment
                        serverIPAddress = ip_address.getHostAddress();
                    }
                }

            }

        } catch (SocketException e) {
            Log.e("ERROR:", e.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.server_info_layout, container, false);

        //Set IP and port number
        ((TextView)(v.findViewById(R.id.txtServerIP))).setText(serverIPAddress);
        ((TextView)(v.findViewById(R.id.txtServerPort))).setText(String.valueOf(serverPort));

        startServer = (Button) v.findViewById(R.id.btnStartServer);
        stopServer_NOSaveData = (Button) v.findViewById(R.id.btnStopServerNoSaveData);
        stopServer_SAVEData = (Button) v.findViewById(R.id.btnStopServerSaveData);
        txtMAXCONN = (EditText) v.findViewById(R.id.txtMAXCONN);
        txtServerPort = (EditText) v.findViewById(R.id.txtServerPort);
        txtMAXCONN.setText(String.valueOf(MAXCONN));

        listView = (ListView) v.findViewById(R.id.listViewCurrentConnected);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ConnectedListAdapter adapter = new ConnectedListAdapter(getContext(),listSensorsForView);
        listView.setAdapter(adapter);

        registerForContextMenu(listView);

        if(clientConnectedList != null) txtMAXCONN.setEnabled(false);

        //----------------- Start server ---------
        if(serverListeningThread != null) {
            startServer.setEnabled(false);
            stopServer_NOSaveData.setEnabled(true);
            stopServer_SAVEData.setEnabled(true);
        }
        startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startServerButton();
            }
        });

        //------------ STOP LISTENING SERVER --------
        stopServer_NOSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopListeningServerButton();
            }
        });

        //------------ STOP All CONNECTIONS SERVER --------
        stopServer_SAVEData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAllConnection();
            }
        });

        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void setmMainActivityAndOther(MainActivity mMainActivity, PlotViewFragment plotViewFragment){
        this.mMainActivity = mMainActivity;
        this.plotViewFragment = plotViewFragment;
    }

    private void startServerButton(){
        serverPort = Integer.parseInt(txtServerPort.getText().toString());
        txtServerPort.setEnabled(false);
        MAXCONN = Integer.parseInt(txtMAXCONN.getText().toString());

        if(clientConnectedList.size() != 0){
            txtMAXCONN.setEnabled(false);
        }

        if(serverListeningThread == null){
            serverListeningThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Create a server socket object, bind it to server_port
                        socServer = new ServerSocket(serverPort);
                        System.out.println("---> port NR "+ serverPort);

                        //Create server side with client socket reference
                        //Multi clients management
                        while (true) {
                            //Accept the client connection, then give it to ServerThread with client socket
                            Socket socClient = socServer.accept();

                            final String clientIP = socClient.getRemoteSocketAddress().toString();
                            System.out.println("------->"+serverPort+" "+MAXCONN+ " "+clientIP);

                            if(clientConnectedList.size() >= MAXCONN){
                                serverUpdateUI.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mMainActivity,"FULL AND REJECTED: "+clientIP,Toast.LENGTH_SHORT).show();
                                    }
                                });
                                PrintWriter out = new PrintWriter(socClient.getOutputStream(),true);
                                out.println("SERVER IS BUSY NOW. WELCOME BACK LATER!");
                                out.flush();
                                out.close();
                                socClient.close();
                            } else{
                                serverUpdateUI.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mMainActivity,"Got connect from: "+clientIP,Toast.LENGTH_SHORT).show();
                                        txtMAXCONN.setText(String.valueOf(MAXCONN));
                                    }
                                });
                                ClientThread clientConnected = new ClientThread(socClient,getContext(),selv, serverUpdateUI);
                                connectedThread.put(socClient,clientConnected);
                                clientConnected.start();
                            }

                        }
                    } catch (IOException e) {
                        serverUpdateUI.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mMainActivity,"SERVER IS SHUT DOWN",Toast.LENGTH_SHORT).show();
                            }
                        });
                        //WHEN socServer close, it will be here
                        serverListeningThread = null;
                    }
                }
            });
            serverListeningThread.start();
        }
        startServer.setEnabled(false);
        stopServer_NOSaveData.setEnabled(true);
        stopServer_SAVEData.setEnabled(true);
    }

    private void stopListeningServerButton(){
        if(!socServer.isClosed()){
            try {
                socServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        serverListeningThread = null;
        startServer.setEnabled(true);
        stopServer_NOSaveData.setEnabled(false);
        if(clientConnectedList.size() == 0){
            stopServer_SAVEData.setEnabled(false);
        }else{
            stopServer_SAVEData.setEnabled(true);
        }
    }

    public void stopAllConnection(){
        for(Socket client_sock : connectedThread.keySet()){
            //If collected data, must save
            connectedThread.get(client_sock).stop = true;
            try {
                PrintWriter out = new PrintWriter(client_sock.getOutputStream(),true);
                out.println("SEE YOU NEXT TIME");
                out.flush();
                out.close();
                client_sock.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        connectedThread.clear();

        if(!socServer.isClosed()){
            try {
                socServer.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        serverListeningThread = null;
        startServer.setEnabled(true);
        stopServer_NOSaveData.setEnabled(false);
        stopServer_SAVEData.setEnabled(false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.current_con_context_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.observeSelected:
                SensorSource ss = (SensorSource)listView.getItemAtPosition(adapterContextMenuInfo.position);
                Toast.makeText(getContext(),ss.getSensor_source_ID(),Toast.LENGTH_SHORT).show();
                plotViewFragment.setObservedSource(ss);
                plotViewFragment.resetPlotView();

                mMainActivity.bottomBar.selectTabWithId(R.id.action_current_connected);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,plotViewFragment).commit();

                break;
            case  R.id.deleteSource:
                if(listView == null || listView.getItemAtPosition(currentSelected) == null){
                    Toast.makeText(getContext(),"Invalid selection.",Toast.LENGTH_SHORT).show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            SensorSource ss = (SensorSource)listView.getItemAtPosition(currentSelected);
                            unregisterSourceFromUI(ss);
                            plotViewFragment.setObservedSource(ss);
                            plotViewFragment.resetPlotView();
                            plotViewFragment.setObservedSource(null);
                            Toast.makeText(getContext(),ss.getSensor_source_ID()+" has unregistered.",Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });

                    builder.setMessage("Delete "+((SensorSource) listView.getItemAtPosition(currentSelected)).getSensor_source_ID()+" ?");
                    builder.setTitle("Warning...");

                    AlertDialog dialog_show = builder.create();
                    dialog_show.show();

                }
                break;
            default:break;
        }

        return super.onContextItemSelected(item);
    }

    public synchronized void unregisterSourceFromUI(SensorSource ss) {
        //For GUI
        listSensorsForView.remove(ss);
        clientConnectedList.remove(ss.getSensor_source_ID());
        //if source is active
        if(ss.getClient_thread() != null) {
            close_a_socket(ss.getClient_thread().clientsSocket);
        }
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.invalidateViews();
                }
            });
        }
        System.out.println(connectedThread.size()+", "+ clientConnectedList.size() +" = "+listSensorsForView.size()+"<----------- delete from UI");
    }

    public void registerNewConnectionUI(SensorSource incomingSource){
        clientConnectedList.put(incomingSource.getSensor_source_ID(),incomingSource);
        listSensorsForView.add(incomingSource);
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.invalidateViews();
                }
            });
        }
    }

    public synchronized SensorSource getDisconnectedSensorSource(String source_ID){
        return clientConnectedList.get(source_ID);
    }

    public  synchronized void close_a_socket(Socket socket){
        if(socket == null) return;
        ClientThread c = connectedThread.remove(socket);
        if(c!= null) c.stop = true;
        try {
            if(!socket.isClosed()){
                PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
                out.println("SEE YOU NEXT TIME");
                out.flush();
                out.close();
                socket.close();
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        System.out.println(connectedThread.size()+", "+ clientConnectedList.size() +" = "+listSensorsForView.size()+"<----------- close socket");
    }

    public List<SensorSource> getListSensorsForView() {
        return listSensorsForView;
    }

    public void invalidateSourceList(){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.invalidateViews();
                }
            });
        }
    }

    public synchronized boolean activeDisconnectedSource(String source_ID, Socket socket){
        if(clientConnectedList.get(source_ID) == null) return false;

        if(clientConnectedList.get(source_ID).source_status.equals(SensorSource.UNACTIVESTATUS)){
            clientConnectedList.get(source_ID).source_status = SensorSource.ACTIVESTATUS;
            clientConnectedList.get(source_ID).setReferenceThread(connectedThread.get(socket));
            connectedThread.get(socket).handlingSource = clientConnectedList.get(source_ID);
            invalidateSourceList();
            return true;
        } else{
            close_a_socket(socket);
            return true;
        }
    }
}
