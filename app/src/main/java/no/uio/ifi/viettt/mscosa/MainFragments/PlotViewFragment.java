package no.uio.ifi.viettt.mscosa.MainFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pools;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ClinicAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.PatientAdapter;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.DataRecord;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SampleSet;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.BeNotifiedComingSample;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.PlotMarkerView;

/**
 * Created by viettt on 20/12/2016.
 */

public class PlotViewFragment extends Fragment implements BeNotifiedComingSample{

    SensorSource visualiseSource;
    public static final int NR_ENTRIES_WINDOW = 100;
    List<Channel> channelList;

    View v;

    AlertDialog.Builder alertdialogbuilder;
    String[] alertDialogItems;
    boolean[] selectedChannels;

    //For Plot
    LineChart lineChart;
    ArrayList<ILineDataSet> lineDataSetsPLOT;
    HashMap<String, ILineDataSet> forUpdateNewSamples;
    PlotMarkerView mv;


    public void setVisualiseSource(SensorSource sensorSource){
        if(sensorSource == null) return;
        //if(lineChart != null) lineChart.removeAllViews();
        this.visualiseSource = sensorSource;
        channelList = sensorSource.getClient_thread().getChannelList();
        lineDataSetsPLOT = new ArrayList<>();
        forUpdateNewSamples = new HashMap<>();
        selectedChannels = new boolean[channelList.size()];
        alertDialogItems = new String[channelList.size()];
    }

    private LineDataSet createSet(String lineName, int color) {
        LineDataSet set = new LineDataSet(null, lineName);
        set.setDrawCircles(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setLineWidth(2f);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawValues(false);
        return set;
    }


    private void initPlot(float yMin, float yMax){
        if(visualiseSource == null) return;

        //lineChart.setOnChartValueSelectedListener(this);
        // enable description text
        Utils.init(getContext());

        if(forUpdateNewSamples.size() == 0){
            forUpdateNewSamples = new HashMap<>();
            for(Channel channel : channelList) {
                String legendName = channel.getChannel_name()+"(" +((channel.getPhysical_dimension().equals("")) ? "": channel.getPhysical_dimension().trim())+")";
                ILineDataSet dataSet = createSet(legendName, Color.rgb((int) (Math.random() * Integer.MAX_VALUE),
                        (int) (Math.random() * Integer.MAX_VALUE), (int) (Math.random() * Integer.MAX_VALUE)));
                forUpdateNewSamples.put(channel.getChannel_ID(),dataSet);
            }
        }



        lineChart.getDescription().setEnabled(true);
        Description d = new Description();
        d.setText("BITalino "+visualiseSource.getSource_id());
        d.setTextColor(Color.rgb((int)(Math.random()*Integer.MAX_VALUE),
                (int)(Math.random()*Integer.MAX_VALUE),(int)(Math.random()*Integer.MAX_VALUE)));
        lineChart.setDescription(d);

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        //lineChart.setPinchZoom(true);
        //lineChart.setAutoScaleMinMaxEnabled(true);

        // set an alternative background color
        lineChart.setBackgroundColor(Color.BLACK);

        // get the legend (only possible after setting data)
        Legend l = lineChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
        l.setWordWrapEnabled(true);

        //------FORMAT DATE BOTTOM----
        //IAxisValueFormatter xAxisFormatter = new TimeAxisValueFormatter(lineChart,observedSource);



        XAxis xl = lineChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xl.setValueFormatter(xAxisFormatter);//-----------------

        //mv = new PlotMarkerView(getContext(), R.layout.ro_plot_marker_view,observedSource);
        //mv.setChartView(lineChart); // For bounds control
        //lineChart.setMarker(mv); // Set the marker to the chart



        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(yMax);
        leftAxis.setAxisMinimum(yMin);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        LineData init = new LineData(lineDataSetsPLOT);
        init.setValueTextColor(Color.WHITE);
        lineChart.setData(init);

        init.notifyDataChanged();
        // let the chart know it's data has changed
        lineChart.notifyDataSetChanged();
        // limit the number of visible entries
        lineChart.setVisibleXRangeMaximum(NR_ENTRIES_WINDOW);
        lineChart.moveViewToX(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.ro_plot_layout, container, false);

        lineChart = (LineChart) v.findViewById(R.id.lineChart);
        initPlot(-600,600);

        final TextView selectSensors = (TextView) v.findViewById(R.id.lblPlot_RO);
        selectSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(visualiseSource == null) return;
                //read info for select channel list
                int cnt = 0;
                for(Channel c : channelList){
                    alertDialogItems[cnt++] = c.getChannel_name();
                }
                popUpSelectSensors();
            }
        });

        final ImageButton ibtn_rec = (ImageButton) v.findViewById(R.id.btnRec_RO);
        final ImageButton ibtn_save = (ImageButton) v.findViewById(R.id.btnSave_RO);

        //get button status from observed source.
        if(visualiseSource == null) {
            ibtn_save.setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
            ibtn_save.invalidate();
        } else {
            if(visualiseSource.isRecFlag()) {
                ibtn_rec.setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                ibtn_rec.invalidate();
                ibtn_save.getBackground().clearColorFilter();
                ibtn_save.invalidate();
            }else{
                ibtn_save.setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                ibtn_save.invalidate();
                ibtn_rec.getBackground().clearColorFilter();
                ibtn_rec.invalidate();
            }
        }

        ibtn_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(visualiseSource == null){
                    Toast.makeText(getContext(),"No data for storing",Toast.LENGTH_SHORT).show();
                    return;
                }

                //if source is not active, return
                if(!visualiseSource.source_status.equals(SensorSource.ACTIVESTATUS)){
                    Toast.makeText(getContext(),"Source is not active",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!visualiseSource.isRecFlag()){
                    final SensorSource ss = visualiseSource;
                    //if source is active, we need to pop-up an view to collect patient and clinic info before saving.
                    View popUpView = getActivity().getLayoutInflater().inflate(R.layout.patient_clinic_info, null); // inflating popup layout
                    final PopupWindow mpopup = new PopupWindow(popUpView, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, true); // Creation of popup
                    mpopup.setAnimationStyle(android.R.style.Animation_Dialog);
                    mpopup.showAtLocation(popUpView, Gravity.CENTER, 0, 0); // Displaying popup

                    popUpView.setBackground(new ColorDrawable(Color.WHITE));

                    Button btnPatientClinicOK = (Button) popUpView.findViewById(R.id.btnPatientClinicOK);
                    //Patient
                    final EditText txtPatientID = (EditText) popUpView.findViewById(R.id.txtPatientID);
                    final EditText txtPatientName = (EditText) popUpView.findViewById(R.id.txtPatientName);
                    final EditText txtPatientSex = (EditText) popUpView.findViewById(R.id.txtPatientSex);
                    final EditText txtPatientBirthday = (EditText) popUpView.findViewById(R.id.txtPatientBirthday);
                    final EditText txtPatientAddress = (EditText) popUpView.findViewById(R.id.txtPatientAddress);
                    final EditText txtPatientTlf = (EditText) popUpView.findViewById(R.id.txtPatientTlf);
                    final EditText txtPatientEmail = (EditText) popUpView.findViewById(R.id.txtPatientEmail);

                    //Clinic
                    final EditText txtClinicID = (EditText) popUpView.findViewById(R.id.txtClinicID);
                    final EditText txtClinicName = (EditText) popUpView.findViewById(R.id.txtClinicName);
                    final EditText txtClinicTechnician = (EditText) popUpView.findViewById(R.id.txtClinicTechnician);
                    EditText txtClinicUsedEquipment = (EditText) popUpView.findViewById(R.id.txtClinicUsedEquipment);
                    final EditText txtClinicAddress = (EditText) popUpView.findViewById(R.id.txtClinicAddress);
                    final EditText txtClinicEmail = (EditText) popUpView.findViewById(R.id.txtClinicEmail);
                    EditText txtClinicStartDate = (EditText) popUpView.findViewById(R.id.txtClinicStartDate);

                    final View viewF = view;
                    btnPatientClinicOK.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!txtClinicID.getText().toString().trim().equals("")
                                    && !txtClinicID.getText().toString().trim().equals("")) {
                                Toast.makeText(getContext(),"STORE PATIENT AND CLINIC; THEN REC",Toast.LENGTH_SHORT).show();
                                PatientAdapter patientAdapter = new PatientAdapter(getContext());
                                patientAdapter.storeNewPatient(
                                        txtPatientID.getText().toString(),
                                        txtPatientSex.getText().toString(),
                                        txtPatientName.getText().toString(),
                                        txtPatientName.getText().toString(),
                                        txtPatientBirthday.getText().toString(),
                                        txtPatientAddress.getText().toString(),
                                        txtPatientTlf.getText().toString(),
                                        txtPatientEmail.getText().toString());
                                patientAdapter.close();

                                ClinicAdapter clinicAdapter = new ClinicAdapter(getContext());
                                clinicAdapter.storeNewClinic(
                                        txtClinicID.getText().toString(),
                                        txtClinicTechnician.getText().toString(),
                                        txtClinicAddress.getText().toString(),
                                        txtClinicName.getText().toString(),
                                        txtClinicEmail.getText().toString());
                                clinicAdapter.close();
                                viewF.getBackground().setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                                viewF.invalidate();
                                ibtn_save.getBackground().clearColorFilter();
                                ibtn_save.invalidate();
                                List<String> channelIDs = new ArrayList<>();
                                for(int i = 0; i < selectedChannels.length; i++){
                                    if(selectedChannels[i]) channelIDs.add(""+(i+1));
                                }
                                visualiseSource.setRecFlag(true, txtPatientID.getText().toString(), txtClinicID.getText().toString(),channelIDs);
                                mpopup.dismiss();
                            }else {
                                Toast.makeText(getContext(),"CANNOT STORE WITHOUT INFO OF PATIENT AND CLINIC",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        ibtn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(visualiseSource == null){
                    return;
                }
                Toast.makeText(getContext(),"STOP CLICK",Toast.LENGTH_SHORT).show();
                if(visualiseSource.isRecFlag()){
                    visualiseSource.setRecFlag(false, null, null,null);
                    view.getBackground().setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                    view.invalidate();
                    ibtn_rec.getBackground().clearColorFilter();
                    ibtn_rec.invalidate();
                    Toast.makeText(getContext(),"STOP STORING",Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
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
                for(int i = 0; i < selectedChannels.length;i++){
                    ILineDataSet iLineDataSet = forUpdateNewSamples.get(""+(i+1));
                    lineDataSetsPLOT.remove(iLineDataSet);
                    if(selectedChannels[i]){
                        lineDataSetsPLOT.add(iLineDataSet);
                    }
                }
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
    public void addNewSample(String channel_ID, float sample_data, long created_date, final float cnt){
        if(v == null) return;
        if(lineChart == null) return;
        ILineDataSet channel_line = forUpdateNewSamples.get(channel_ID);
        if (channel_line == null) return;

        //MAKE SURE THAT WE LIMIT THE WINDOWS OF ENTRY TO AVOID EATING A LOT OF MEMORY
        if(channel_line.getEntryCount() > NR_ENTRIES_WINDOW*3){
            channel_line.removeEntry(0);
            channel_line.removeEntry(1);
            channel_line.removeEntry(2);
        }


        Entry new_entry = new Entry(cnt,sample_data);

        channel_line.addEntry(new_entry);
        lineChart.getData().notifyDataChanged();

        // let the chart know it's data has changed
        lineChart.notifyDataSetChanged();
        // limit the number of visible entries
        lineChart.setVisibleXRangeMaximum(NR_ENTRIES_WINDOW);
        //System.out.println(" PlotView "+visualiseSource.getSource_id()+"---------> channel :" +channel_ID+" sample:"+sample_data);
        v.post(new Runnable() {
            @Override
            public void run() {
                // move to the latest entry
                if((cnt - (NR_ENTRIES_WINDOW + 1) > 0)){
                    lineChart.moveViewToX(cnt - NR_ENTRIES_WINDOW - 1);
                } else lineChart.invalidate();
            }
        });
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

}
