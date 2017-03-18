package no.uio.ifi.viettt.mscosa.MainFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.ClinicAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.PersonAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.RecordAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SensorSourceAdapter;
import no.uio.ifi.viettt.mscosa.MainActivity;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Physician;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ClientThread;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ConnectedListAdapter;

/**
 * Created by viettt on 18/12/2016.
 */

public class ServerFragment extends Fragment {
    //MAIN ATTRIBUTES
    List<ClientThread> connectedSources;

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

    //lock object used for add things to list
    private final Object lock = new Object();

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
                        ClientThread clientConnected = new ClientThread(socClient,CONTEXT,serverUpdateUI, selv);
                        addNewSource(clientConnected);
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
        }
    }

    private void stopAllConnection(){
        for(ClientThread s : connectedSources){
            s.closeConnection();
        }
        stopListeningServerButton();
        stopAll.setEnabled(false);
        invalidateSourceList();
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

    public void addNewSource(ClientThread newClient){
        synchronized (lock){
            this.connectedSources.add(newClient);
        }
        invalidateSourceList();
    }

    public void removeSourceFromList(ClientThread oldClient){
        synchronized (lock){
            connectedSources.remove(oldClient);
            if(plotViewFragment != null) plotViewFragment.unRegisterRunningSource(oldClient);
        }
        invalidateSourceList();
    }

    public boolean checkAndRemoveSourceNewSource(String source_ID, ClientThread newConnected){
        boolean ret = false;
        synchronized (lock){
            ClientThread tmp = null;
            for(ClientThread c : connectedSources){
                if(c.getThread_ID().equals(source_ID)){
                    tmp = c; break;
                }
            }

            if(tmp != null){
                if(tmp.isDisconnected()) connectedSources.remove(tmp);
                else {
                    newConnected.closeConnection();
                    connectedSources.remove(newConnected);
                    ret = true;
                }
            }
        }
        return ret;
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
            case R.id.visualiseSelected:
                Toast.makeText(getContext(),"I will implement it when I have time - low priority",Toast.LENGTH_SHORT).show();
                if(!((ClientThread)listView.getItemAtPosition(currentSelected)).isReadyToUse() || ((ClientThread)listView.getItemAtPosition(currentSelected)).isDisconnected()){
                    Toast.makeText(getContext(),"Source is not ready to use",Toast.LENGTH_SHORT).show();
                } else{
                    ClientThread ss = (ClientThread) listView.getItemAtPosition(adapterContextMenuInfo.position);
                    for(ClientThread s : connectedSources){
                        if (s != ss) s.setPlotting(false,plotViewFragment);
                    }
                    if(!ss.isPlotting()){
                        plotViewFragment.setVisualiseSource(ss);
                        ss.setPlotting(true,plotViewFragment);
                    }
                    mainActivity.bottomBar.selectTabWithId(R.id.action_current_connected);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,plotViewFragment).commit();
                }

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
                            ClientThread ss = (ClientThread) listView.getItemAtPosition(currentSelected);
                            ss.closeConnection();
                            removeSourceFromList(ss);
                            invalidateSourceList();
                            Toast.makeText(getContext(),ss.getThread_ID()+" has unregistered.",Toast.LENGTH_SHORT).show();

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

                    builder.setMessage("Delete "+((ClientThread) listView.getItemAtPosition(currentSelected)).getThread_ID()+" ?");
                    builder.setTitle("Warning...");

                    AlertDialog dialog_show = builder.create();
                    dialog_show.show();
                }
                break;
            case R.id.startCollecting:
                if(!((ClientThread)listView.getItemAtPosition(currentSelected)).isReadyToUse()){
                    Toast.makeText(getContext(),"Source is not ready to use",Toast.LENGTH_SHORT).show();
                } else if(((ClientThread)listView.getItemAtPosition(currentSelected)).isStoring()){
                    Toast.makeText(getContext(),"Source is currently used, stop it if you want to record new.",Toast.LENGTH_SHORT).show();
                } else{
                    savePersonClinicInfo(((ClientThread)listView.getItemAtPosition(currentSelected)));
                    invalidateSourceList();
                }
                break;
            case R.id.stopCollecting:
                if(!((ClientThread)listView.getItemAtPosition(currentSelected)).isStoring()){
                    Toast.makeText(getContext(),"Source is not used, start it if you want to record new.",Toast.LENGTH_SHORT).show();
                } else{
                    SensorSource record = ((ClientThread)listView.getItemAtPosition(currentSelected)).getSensorSource();
                    ((ClientThread)listView.getItemAtPosition(currentSelected)).setStoring(false);
                    invalidateSourceList();
                    if(record != null) Toast.makeText(getContext(),record.getS_name()+" has been saved to database",Toast.LENGTH_SHORT).show();
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

    private void savePersonClinicInfo(final ClientThread clientThread){
        View popUpView = getActivity().getLayoutInflater().inflate(R.layout.person_clinic_info, null); // inflating popup layout
        final PopupWindow mpopup = new PopupWindow(popUpView, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, true); // Creation of popup
        mpopup.showAtLocation(popUpView, Gravity.CENTER, 0, 0); // Displaying popup

        popUpView.setBackground(new ColorDrawable(Color.WHITE));

        final Button btnPatientClinicOK = (Button) popUpView.findViewById(R.id.btnPatientClinicOK);
        Button btnPatientClinicCancel = (Button) popUpView.findViewById(R.id.btnPatientClinicCancel);
        Button btnChooseChannels = (Button) popUpView.findViewById(R.id.btnChooseChannels);

        final EditText txtPatientID = (EditText) popUpView.findViewById(R.id.txtPatientID);
        final EditText txtPatientName = (EditText) popUpView.findViewById(R.id.txtPatientName);
        final EditText txtPatCity = (EditText) popUpView.findViewById(R.id.txtPatCity);
        final EditText txtPatPhone = (EditText) popUpView.findViewById(R.id.txtPatPhone);
        final EditText txtPatEmail = (EditText) popUpView.findViewById(R.id.txtPatEmail);
        final EditText txtPatGender = (EditText) popUpView.findViewById(R.id.txtPatGender);
        final EditText txtPatDOB = (EditText) popUpView.findViewById(R.id.txtPatDOB);
        final EditText txtPatAge = (EditText) popUpView.findViewById(R.id.txtPatAge);
        final EditText txtPatHeight = (EditText) popUpView.findViewById(R.id.txtPatHeight);
        final EditText txtPatWeight = (EditText) popUpView.findViewById(R.id.txtPatWeight);
        final EditText txtPatBMI = (EditText) popUpView.findViewById(R.id.txtPatBMI);
        final EditText txtPatOtherHealthIss = (EditText) popUpView.findViewById(R.id.txtPatOtherHealthIss);

        final EditText txtPhyID = (EditText) popUpView.findViewById(R.id.txtPhyID);
        final EditText txtPhyName = (EditText) popUpView.findViewById(R.id.txtPhyName);
        final EditText txtPhyCity = (EditText) popUpView.findViewById(R.id.txtPhyCity);
        final EditText txtPhyPhoneNr = (EditText) popUpView.findViewById(R.id.txtPhyPhoneNr);
        final EditText txtPhyEmail = (EditText) popUpView.findViewById(R.id.txtPhyEmail);
        final EditText txtPhyGender = (EditText) popUpView.findViewById(R.id.txtPhyGender);
        final EditText txtPhyDateOfBirth = (EditText) popUpView.findViewById(R.id.txtPhyDateOfBirth);
        final EditText txtPhyAge = (EditText) popUpView.findViewById(R.id.txtPhyAge);
        final EditText txtPhyTitle = (EditText) popUpView.findViewById(R.id.txtPhyTitle);

        final EditText txtClinicID = (EditText) popUpView.findViewById(R.id.txtClinicID);
        final EditText txtClinicName = (EditText) popUpView.findViewById(R.id.txtClinicName);
        final EditText txtClinicAddress = (EditText) popUpView.findViewById(R.id.txtClinicAddress);
        final EditText txtClinicPhone = (EditText) popUpView.findViewById(R.id.txtClinicPhone);
        final EditText txtClinicEmail = (EditText) popUpView.findViewById(R.id.txtClinicEmail);

        final EditText txtFragmentDuration = (EditText) popUpView.findViewById(R.id.txtFragmentDuration);

        Spinner sPatient = (Spinner) popUpView.findViewById(R.id.spinnerPatientTable);
        Spinner sClinic = (Spinner) popUpView.findViewById(R.id.spinnerClinicTable);
        Spinner sPhysician = (Spinner) popUpView.findViewById(R.id.spinnerPhyTable);

        ClinicAdapter clinicAdapter = new ClinicAdapter(getContext());
        ArrayList<String> listClinics = clinicAdapter.getAllClinicIDs();
        clinicAdapter.close();
        PersonAdapter personAdapter = new PersonAdapter(getContext());
        final ArrayList<String> listPatients = personAdapter.getAllPatientIDs();
        ArrayList<String> listPhysicians = personAdapter.getAllPhysicianIDs();
        personAdapter.close();

        sClinic.setAdapter(new ArrayAdapter<String>(getContext(),R.layout.support_simple_spinner_dropdown_item,listClinics));
        sPhysician.setAdapter(new ArrayAdapter<String>(getContext(),R.layout.support_simple_spinner_dropdown_item,listPhysicians));
        sPatient.setAdapter(new ArrayAdapter<String>(getContext(),R.layout.support_simple_spinner_dropdown_item,listPatients));
        if(listClinics.isEmpty()) sClinic.setEnabled(false);
        if(listPhysicians.isEmpty()) sPhysician.setEnabled(false);
        if(listPatients.isEmpty()) sPatient.setEnabled(false);

        sPatient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                PersonAdapter personAdapterGet1Per = new PersonAdapter(getContext());
                Patient patient = personAdapterGet1Per.getPatientByIds(adapterView.getItemAtPosition(i).toString());
                personAdapterGet1Per.close();
                txtPatientID.setText(patient.getP_id());
                txtPatientName.setText(patient.getName());
                txtPatCity.setText(patient.getCity());
                txtPatPhone.setText(patient.getPhone());
                txtPatEmail.setText(patient.getEmail());
                txtPatGender.setText(patient.getGender());
                txtPatDOB.setText(patient.getDayOfBirth());
                txtPatAge.setText(String.valueOf(patient.getAge()));
                txtPatHeight.setText(String.valueOf(patient.getHeight()));
                txtPatWeight.setText(String.valueOf(patient.getWeight()));
                txtPatBMI.setText(String.valueOf(patient.getBMI()));
                txtPatOtherHealthIss.setText(patient.getOtherHealthIssues());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        sPhysician.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PersonAdapter personAdapterGet1Per = new PersonAdapter(getContext());
                Patient patient = personAdapterGet1Per.getPatientByIds(adapterView.getItemAtPosition(i).toString());
                personAdapterGet1Per.close();
                txtPhyID.setText(patient.getP_id());
                txtPhyName.setText(patient.getName());
                txtPhyCity.setText(patient.getCity());
                txtPhyPhoneNr.setText(patient.getPhone());
                txtPhyEmail.setText(patient.getEmail());
                txtPhyGender.setText(patient.getGender());
                txtPhyDateOfBirth.setText(patient.getDayOfBirth());
                txtPhyAge.setText(String.valueOf(patient.getAge()));
                txtPhyTitle.setText(patient.getOtherHealthIssues());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sClinic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ClinicAdapter clinicAdapterGet1Clinic = new ClinicAdapter(getContext());
                Clinic clinic = clinicAdapterGet1Clinic.getClinicByIds(adapterView.getItemAtPosition(i).toString());
                clinicAdapterGet1Clinic.close();

                txtClinicID.setText(clinic.getCl_id());
                txtClinicName.setText(clinic.getName());
                txtClinicAddress.setText(clinic.getAddress());
                txtClinicPhone.setText(clinic.getPhone_nr());
                txtClinicEmail.setText(clinic.getEmail());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnPatientClinicOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtPatientID.getText().toString().equals("") ||
                        txtPhyID.getText().toString().equals("") ||
                        txtClinicID.getText().toString().equals("")) {
                    Toast.makeText(getContext(),"CANNOT STORE WITHOUT INFO OF PATIENT ID, PHYSICIAN ID AND CLINIC ID",Toast.LENGTH_SHORT).show();
                }else {
                    Patient patient = new Patient();
                    patient.setP_id(txtPatientID.getText().toString());
                    patient.setName(txtPatientName.getText().toString());
                    patient.setCity(txtPatCity.getText().toString());
                    patient.setPhone(txtPatPhone.getText().toString());
                    patient.setEmail(txtPatEmail.getText().toString());
                    patient.setGender(txtPatGender.getText().toString());
                    patient.setDayOfBirth(txtPatDOB.getText().toString());
                    patient.setAge(txtPatAge.getText().toString().equals("")? -1 : Integer.parseInt(txtPatAge.getText().toString()));
                    patient.setHeight(txtPatHeight.getText().toString().equals("") ? 0: Float.parseFloat(txtPatHeight.getText().toString()));
                    patient.setHeight(txtPatWeight.getText().toString().equals("") ? 0: Float.parseFloat(txtPatWeight.getText().toString()));
                    patient.setBMI(txtPatBMI.getText().toString().equals("") ? 0: Float.parseFloat(txtPatBMI.getText().toString()));
                    patient.setOtherHealthIssues(txtPatOtherHealthIss.getText().toString());
                    patient.setClinic_code(txtClinicID.getText().toString());

                    Physician physician = new Physician();
                    physician.setP_id(txtPhyID.getText().toString());
                    physician.setName(txtPhyName.getText().toString());
                    physician.setCity(txtPhyCity.getText().toString());
                    physician.setPhone(txtPhyPhoneNr.getText().toString());
                    physician.setEmail(txtPhyEmail.getText().toString());
                    physician.setGender(txtPhyGender.getText().toString());
                    physician.setDayOfBirth(txtPhyDateOfBirth.getText().toString());
                    physician.setAge(txtPhyAge.getText().toString().equals("") ? -1 : Integer.parseInt(txtPhyAge.getText().toString()));
                    physician.setTitle(txtPhyTitle.getText().toString());
                    physician.setClinic_code(txtClinicID.getText().toString());

                    Clinic clinic = new Clinic();
                    clinic.setCl_id(txtClinicID.getText().toString());
                    clinic.setName(txtClinicName.getText().toString());
                    clinic.setAddress(txtClinicAddress.getText().toString());
                    clinic.setPhone_nr(txtClinicPhone.getText().toString());
                    clinic.setEmail(txtClinicEmail.getText().toString());

                    ClinicAdapter clinicAdapterSave = new ClinicAdapter(getContext());
                    clinicAdapterSave.storeNewClinic(clinic);
                    clinicAdapterSave.close();

                    PersonAdapter personAdapterSave = new PersonAdapter(getContext());
                    personAdapterSave.storeNewPerson(patient);
                    personAdapterSave.storeNewPerson(physician);
                    personAdapterSave.close();

                    SensorSourceAdapter sensorSourceAdapter = new SensorSourceAdapter(getContext());
                    sensorSourceAdapter.saveSensorSourceToDB(clientThread.getSensorSource());
                    sensorSourceAdapter.close();

                    HashMap<String,Channel> channels = clientThread.getChannels();
                    ChannelAdapter channelAdapter = new ChannelAdapter(getContext());
                    for(Channel c : channels.values()){
                        channelAdapter.saveChannelToDB(c);
                    }
                    channelAdapter.close();

                    RecordAdapter recordAdapter = new RecordAdapter(getContext());
                    HashMap<String,Record> records = clientThread.getRecords();
                    for(Channel c : channels.values()){
                        Record record = new Record();
                        record.setS_id(clientThread.getSensorSource().getS_id());
                        record.setPhysician_id(physician.getP_id());
                        record.setPatient_id(physician.getP_id());
                        record.setTimestamp(System.currentTimeMillis());
                        record.setDescriptions(c.getDescription());
                        record.setFrequency(-1);

                        record.setR_id(recordAdapter.saveRecordToDB(record));
                        records.put(c.getCh_nr(),record);
                    }
                    recordAdapter.close();

                    clientThread.setFrag_duration(1000*(Integer.parseInt(txtFragmentDuration.getText().toString())));
                    Toast.makeText(getContext(),"INFO have saved/updated to database",Toast.LENGTH_SHORT).show();
                    clientThread.setStoring(true);
                    mpopup.dismiss();
                }
            }
        });

        btnPatientClinicCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpopup.dismiss();
            }
        });

        btnChooseChannels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(getActivity());
                final String[] alertDialogItems = new String[clientThread.getChannels().keySet().size()];
                final boolean[] selectedChannels = new boolean[alertDialogItems.length];

                int cnt = 0;
                for(String c : clientThread.getChannels().keySet()){
                    alertDialogItems[cnt++] = c + ": "+clientThread.getChannels().get(c).getCh_name();
                }

                alertdialogbuilder.setMultiChoiceItems(alertDialogItems, selectedChannels, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    }
                });

                alertdialogbuilder.setCancelable(false);

                alertdialogbuilder.setTitle("Select channels");

                alertdialogbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0; i < selectedChannels.length;i++){
                            if(selectedChannels[i]){
                                String[] selected = alertDialogItems[i].split(": ");
                                clientThread.getChannels().get(selected[0]).setSelectedToSaveSample(true);
                            }
                        }
                        btnPatientClinicOK.setEnabled(true);
                    }
                });

                alertdialogbuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = alertdialogbuilder.create();
                dialog.show();
            }
        });
    }

}
