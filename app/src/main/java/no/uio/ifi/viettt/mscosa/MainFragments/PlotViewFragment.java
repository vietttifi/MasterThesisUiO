package no.uio.ifi.viettt.mscosa.MainFragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.ClinicAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.PersonAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.RecordAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SensorSourceAdapter;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Physician;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.BeNotifiedComingSample;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.BitalinoDataSample;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ClientThread;

/**
 * Created by viettt on 20/12/2016.
 */

public class PlotViewFragment extends Fragment implements BeNotifiedComingSample{

    ClientThread visualiseSource;
    private HashMap<String,Channel> channels = new HashMap<>();
    public static final int NR_ENTRIES_WINDOW = 210;

    View v;
    GraphView graph;
    AlertDialog.Builder alertdialogbuilder;
    String[] alertDialogItems;
    boolean[] selectedChannels;
    HashMap<String, LineGraphSeries<DataPoint>> channelLines;
    long timestampPlot;

    private boolean isReady = false;

    public void setVisualiseSource(ClientThread clientThread){
        timestampPlot = System.currentTimeMillis();
        channels = clientThread.getChannels();
        visualiseSource = clientThread;
        channelLines = new HashMap<>();
        isReady = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isReady = false;
        graph.removeAllSeries();
        channelLines = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.ro_plot_layout, container, false);
        graph = (GraphView) v.findViewById(R.id.lineChart);
        TextView lblPlot_RO = (TextView)v.findViewById(R.id.lblPlot_RO);

        final ImageButton ibtn_rec = (ImageButton) v.findViewById(R.id.btnRec_RO);
        final ImageButton ibtn_save = (ImageButton) v.findViewById(R.id.btnSave_RO);

        //get button status from observed source.
        if(visualiseSource == null) {
            ibtn_save.setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
            ibtn_save.invalidate();
        } else {
            if(visualiseSource.isStoring()) {
                ibtn_rec.setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                ibtn_rec.invalidate();
                ibtn_save.setColorFilter(0x00000000,PorterDuff.Mode.SRC_ATOP);
                ibtn_save.invalidate();
            }else{
                ibtn_save.setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                ibtn_save.invalidate();
                ibtn_rec.setColorFilter(0x00000000,PorterDuff.Mode.SRC_ATOP);
                ibtn_rec.invalidate();
            }
        }

        ibtn_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(visualiseSource == null || visualiseSource.isDisconnected()){
                    Toast.makeText(getContext(),"Source has not been chosen or disconnected.",Toast.LENGTH_SHORT).show();
                }else if(visualiseSource.isStoring()) {
                    Toast.makeText(getContext(),"Stop the current storing before taking new record.",Toast.LENGTH_SHORT).show();
                }else{
                    ibtn_rec.setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                    ibtn_rec.invalidate();
                    ibtn_save.setColorFilter(0x00000000,PorterDuff.Mode.SRC_ATOP);
                    ibtn_save.invalidate();
                    savePersonClinicInfo(visualiseSource);
                }
            }
        });

        ibtn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(visualiseSource == null || visualiseSource.isDisconnected()){
                    Toast.makeText(getContext(),"Source has not been chosen or disconnected.",Toast.LENGTH_SHORT).show();
                }else if(!visualiseSource.isStoring()) {
                    Toast.makeText(getContext(),"Source is currently stopping.",Toast.LENGTH_SHORT).show();
                }else{
                    ibtn_save.setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                    ibtn_save.invalidate();
                    ibtn_rec.getBackground().setColorFilter(0x00000000,PorterDuff.Mode.SRC_ATOP);
                    ibtn_rec.invalidate();
                    visualiseSource.setStoring(false);
                    Toast.makeText(getContext(),"Source is stopped.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        lblPlot_RO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpSelectSensors();
            }
        });
        initGraph();
        isReady = true;
        return v;
    }

    private void initGraph(){
        if(visualiseSource == null) return;
        selectedChannels = new boolean[channels.size()];
        alertDialogItems = new String[channels.size()];

        for(String ch_nr : channels.keySet()){
            if(!channelLines.containsKey(ch_nr))channelLines.put(ch_nr,new LineGraphSeries<DataPoint>());
            graph.addSeries(channelLines.get(ch_nr));
            channelLines.get(ch_nr).setTitle(channels.get(ch_nr).getCh_name()+"(" +((channels.get(ch_nr).getDimension()==null) ? "": channels.get(ch_nr).getDimension().trim())+")");
            channelLines.get(ch_nr).setColor(Color.rgb((int) (Math.random() * Integer.MAX_VALUE),
                    (int) (Math.random() * Integer.MAX_VALUE), (int) (Math.random() * Integer.MAX_VALUE)));
        }
        int inx = 0;
        for(String c : channels.keySet()){
            alertDialogItems[inx] = c+": "+channels.get(c).getCh_name();
            selectedChannels[inx++] = true;
        }


        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setScalable(true);
        viewport.setScalableY(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMaxX(0);
        viewport.setMaxX(210);
        viewport.setMinY(-600);
        viewport.setMaxY(600);
        viewport.setBackgroundColor(Color.BLACK);
        graph.getGridLabelRenderer().setGridColor(Color.DKGRAY);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time from visualising in second/10");
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.GREEN);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Metric in legend");
        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.GREEN);
        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        v.post(new Runnable() {
            @Override
            public void run() {
                graph.invalidate();
            }
        });
    }

    @Override
    public synchronized void addNewSample(final BitalinoDataSample samples[]) {
        if(!isReady) return;

        for(BitalinoDataSample sample : samples){
            LineGraphSeries<DataPoint> series = channelLines.get(sample.getChannel_nr());
            if(series == null) return;
            final int scale = (int)(sample.getCreatedDate()-timestampPlot)/100;
            if(scale == 0 || scale <= channels.get(sample.getChannel_nr()).getLastXRealtime()) {
                return;
            }
            channels.get(sample.getChannel_nr()).setLastXRealtime(scale);
        }

        v.post(new Runnable() {
            @Override
            public void run() {
                for(BitalinoDataSample sample: samples){
                    LineGraphSeries<DataPoint> tmp = channelLines.get(sample.getChannel_nr());
                    if(tmp != null && isReady)
                        tmp.appendData(new DataPoint(channels.get(sample.getChannel_nr()).getLastXRealtime(),sample.getSample_data()),true,NR_ENTRIES_WINDOW);
                }

            }
        });


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

        final TextView firstTimeSpinnerCheck = (TextView) popUpView.findViewById(R.id.lblPatientID);
        sPatient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(firstTimeSpinnerCheck.getText().toString().equals("Patient ID:")){
                    firstTimeSpinnerCheck.setText(String.valueOf("Patient ID: "));
                    return;
                }
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

        final TextView firstTimeSpinnerCheckPhys = (TextView) popUpView.findViewById(R.id.lblPhyID);
        sPhysician.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(firstTimeSpinnerCheckPhys.getText().toString().equals("Physician ID:")){
                    firstTimeSpinnerCheckPhys.setText(String.valueOf("Physician ID: "));
                    return;
                }

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

        final TextView firstTimeSpinnerCheckClinic = (TextView) popUpView.findViewById(R.id.lblClinicID);
        sClinic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(firstTimeSpinnerCheckClinic.getText().toString().equals("Clinic ID:")){
                    firstTimeSpinnerCheckClinic.setText(String.valueOf("Clinic ID: "));
                    return;
                }
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

    void popUpSelectSensors(){
        alertdialogbuilder = new AlertDialog.Builder(getActivity());

        String nameSensors[] = new String[alertDialogItems.length];

        for(int i = 0; i < alertDialogItems.length; i ++) {
            nameSensors[i] = alertDialogItems[i];
        }

        alertdialogbuilder.setMultiChoiceItems(nameSensors, selectedChannels, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            }
        });

        alertdialogbuilder.setCancelable(false);

        alertdialogbuilder.setTitle("Select sensors");

        alertdialogbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                graph.removeAllSeries();
                for(int i = 0; i < selectedChannels.length;i++){
                    if(selectedChannels[i]){
                        String ch_nr = alertDialogItems[i].split(": ")[0];
                        graph.addSeries(channelLines.get(ch_nr));
                    }
                }
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        graph.invalidate();
                    }
                });
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

    @Override
    public void unRegisterRunningSource(ClientThread clientThread){
        if(clientThread == visualiseSource){
            isReady = false;
            visualiseSource = null;
        }
    }

}
