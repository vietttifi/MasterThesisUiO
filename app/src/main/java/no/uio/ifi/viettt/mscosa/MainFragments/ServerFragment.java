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
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.MonitorUpdatePlot;

/**
 * Created by viettt on 18/12/2016.
 */

public class ServerFragment extends Fragment {
    //MAIN ATTRIBUTES
    List<SensorSource> connectedSources;

    /*OTHERS ATTRIBUTES*/
    //Elements to control server
    String serverIPAddress;
    private int serverPort = 24000;
    public ServerSocket sockServer;

    //This fragment view GUI
    View v;
    private Button startServer, stopListening, stopAll;
    private EditText txtServerPort;
    ListView listView;
    private int currentSelected = 0;
    //The handler for manage GUI
    private Handler serverUpdateUI = new Handler();

    //The object of this class
    public final ServerFragment selv;

    //Pointer to other fragments
    private MainActivity mainActivity;
    private PlotViewFragment plotViewFragment;
    private MonitorUpdatePlot monitorUpdatePlot = new MonitorUpdatePlot();

    public ServerFragment(){
        selv = this;
        connectedSources = new ArrayList<>();
        getIPofThisDeviceAsServerIP();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.server_info_layout, container, false);

        //Set IP and port number
        ((TextView)(v.findViewById(R.id.txtServerIP))).setText(serverIPAddress);
        ((TextView)(v.findViewById(R.id.txtServerPort))).setText(String.valueOf(serverPort));

        //Get buttons
        startServer = (Button) v.findViewById(R.id.btnStartServer);
        stopListening = (Button) v.findViewById(R.id.btnStopServerNoSaveData);
        stopAll = (Button) v.findViewById(R.id.btnStopServerSaveData);
        txtServerPort = (EditText) v.findViewById(R.id.txtServerPort);

        listView = (ListView) v.findViewById(R.id.listViewCurrentConnected);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ConnectedListAdapter adapter = new ConnectedListAdapter(getContext(),connectedSources);
        listView.setAdapter(adapter);

        registerForContextMenu(listView);

        //----------------- Start server ---------
        if(sockServer != null && !sockServer.isClosed()) {
            startServer.setEnabled(false);
            stopListening.setEnabled(true);
            stopAll.setEnabled(true);
        }
        startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startServerButton();
            }
        });

        //------------ STOP LISTENING SERVER --------
        stopListening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopListeningServerButton();
            }
        });

        //------------ STOP All CONNECTIONS SERVER --------
        stopAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAllConnection();
            }
        });

        return v;
    }

    private void startServerButton(){
        serverPort = Integer.parseInt(txtServerPort.getText().toString());
        final Context CONTEXT = getContext();

        Thread server = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Create a server socket object, bind it to server_port
                    sockServer = new ServerSocket(serverPort);

                    //Create server side with client socket reference
                    //Multi clients management
                    while (true) {
                        //Accept the client connection, then give it to ServerThread with client socket
                        Socket socClient = sockServer.accept();

                        final String clientIP = socClient.getRemoteSocketAddress().toString();
                        //System.out.println("------->"+serverPort+" "+clientIP);
                        serverUpdateUI.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CONTEXT,"Got connect from: "+clientIP,Toast.LENGTH_SHORT).show();
                            }
                        });
                        ClientThread clientConnected = new ClientThread(socClient,CONTEXT,serverUpdateUI);
                        SensorSource new_source = new SensorSource("", "bitalino", clientConnected);
                        clientConnected.registerSensorSource(new_source);
                        selv.addNewSource(new_source);
                        clientConnected.start();
                    }
                } catch (IOException e) {
                    serverUpdateUI.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"SERVER IS SHUT DOWN",Toast.LENGTH_SHORT).show();
                        }
                    });
                    //WHEN sockServer close, it will be here
                    sockServer = null;
                }
            }
        });

        startServer.setEnabled(false);
        stopListening.setEnabled(true);
        stopAll.setEnabled(true);
        server.start();
    }

    private void stopListeningServerButton(){
        try{
            sockServer.close();
            sockServer = null;
            startServer.setEnabled(true);
            stopListening.setEnabled(false);
            if(connectedSources.size() == 0){
                stopAll.setEnabled(false);
            }else{
                stopAll.setEnabled(true);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void stopAllConnection(){
        for(SensorSource s : connectedSources){
            s.closeConnection();
            s.setSource_status(SensorSource.UNACTIVESTATUS);
        }
        stopListeningServerButton();
        stopAll.setEnabled(false);
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
                    //System.out.println(ip_address.getHostAddress() + "<-------");
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

    public synchronized void addNewSource(SensorSource newClient){
        this.connectedSources.add(newClient);
        invalidateSourceList();
    }

    public synchronized void removeSourceFromList(SensorSource oldClient){
        connectedSources.remove(oldClient);
        invalidateSourceList();
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
                Toast.makeText(getContext(),ss.getSource_id(),Toast.LENGTH_SHORT).show();
                monitorUpdatePlot.setStopUpdateThread(true);
                for(SensorSource s : connectedSources){
                    if (s.getClient_thread() != null) {
                        s.getClient_thread().stopThreadPlotUpdate();
                    }
                }

                if(ss.getClient_thread() != null) ss.getClient_thread().setMonitorUpdatePlot(monitorUpdatePlot,plotViewFragment);

                plotViewFragment.setVisualiseSource(ss);
                mainActivity.bottomBar.selectTabWithId(R.id.action_current_connected);
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
                            /*----mm----*/
                            Toast.makeText(getContext(),ss.getSource_id()+" has unregistered.",Toast.LENGTH_SHORT).show();
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

                    builder.setMessage("Delete "+((SensorSource) listView.getItemAtPosition(currentSelected)).getSource_id()+" ?");
                    builder.setTitle("Warning...");

                    AlertDialog dialog_show = builder.create();
                    dialog_show.show();
                }
                break;
            default:
                break;
        }

        return super.onContextItemSelected(item);
    }


    public void setmMainActivityAndOther(MainActivity mMainActivity, PlotViewFragment plotViewFragment){
        this.mainActivity = mMainActivity;
        this.plotViewFragment = plotViewFragment;
    }

}
