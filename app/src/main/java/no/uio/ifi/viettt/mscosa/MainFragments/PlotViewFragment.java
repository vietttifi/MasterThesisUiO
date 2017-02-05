package no.uio.ifi.viettt.mscosa.MainFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.DataRecord;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.BeNotifiedComingSample;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.TimeAxisValueFormatter;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.PlotMarkerView;

/**
 * Created by viettt on 20/12/2016.
 */

public class PlotViewFragment extends Fragment implements BeNotifiedComingSample{
    SensorSource observedSource;
    public static final int NR_ENTRIES_WINDOW = 100;

    View v;

    AlertDialog.Builder alertdialogbuilder;
    String[] alertDialogItems;
    List<String> itemsIntoList;
    boolean[] selectedSensors;
    private boolean swichFragment = false;

    //For Plot
    LineChart lineChart;
    ArrayList<ILineDataSet> lineDataSetsPLOT = new ArrayList<>();
    HashMap<String,Integer> channelLineID = new HashMap<>();
    PlotMarkerView mv;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    public void setObservedSource(SensorSource sensorSource){
        if(observedSource != null && observedSource.getClient_thread() != null){
            observedSource.getClient_thread().removeBeNotifiedComingSampleObject();
        }
        observedSource = sensorSource;
        if(sensorSource == null) return;
        int i = 0;
        for(String channel: observedSource.getChannelsOfThisSource().keySet()){
            channelLineID.put(channel,i++);
        }
        for(Channel channel : observedSource.getChannelsOfThisSource().values()) {
            String legendName = channel.getChannel_name()+"(" +((channel.getPhysical_dimension().equals("")) ? "": channel.getPhysical_dimension().trim())+")";
            ILineDataSet dataSet = createSet(legendName, Color.rgb((int) (Math.random() * Integer.MAX_VALUE),
                    (int) (Math.random() * Integer.MAX_VALUE), (int) (Math.random() * Integer.MAX_VALUE)));
            lineDataSetsPLOT.add(channelLineID.get(channel.getChannel_ID()), dataSet);
        }
        if(observedSource.getClient_thread() != null)
            observedSource.getClient_thread().setBeNotifiedComingSampleObject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        swichFragment = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.ro_plot_layout, container, false);

        lineChart = (LineChart) v.findViewById(R.id.lineChart);
        //first time, no need to draw lineChart
        if(selectedSensors != null) registerDataFromSensorSource();

        final ImageButton ibtn_rec = (ImageButton) v.findViewById(R.id.btnRec_RO);
        final ImageButton ibtn_save = (ImageButton) v.findViewById(R.id.btnSave_RO);

        //get button status from observed source.
        if(observedSource == null) {
            ibtn_save.setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
            ibtn_save.invalidate();
        } else {
            if(observedSource.getRecFlag()) {
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

        TextView selectSensors = (TextView) v.findViewById(R.id.lblPlot_RO);

        selectSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(observedSource == null) return;
                readSensorInfoForSelectList();
                popUpSelectSensors();
                lineChart.invalidate();
            }
        });

        ibtn_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(observedSource == null){
                    Toast.makeText(getContext(),"No data for storing",Toast.LENGTH_SHORT).show();
                    return;
                }

                //if source is not active, return
                if(!observedSource.source_status.equals(SensorSource.ACTIVESTATUS)){
                    Toast.makeText(getContext(),"Source is not active",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!observedSource.getRecFlag()){
                    final SensorSource ss = observedSource;
                    //if source is active, we need to pop-up an view to collect patient and clinic info before saving.
                    View popUpView = getActivity().getLayoutInflater().inflate(R.layout.patient_clinic_info, null); // inflating popup layout
                    final PopupWindow mpopup = new PopupWindow(popUpView, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, true); // Creation of popup
                    mpopup.setAnimationStyle(android.R.style.Animation_Dialog);
                    mpopup.showAtLocation(popUpView, Gravity.CENTER, 0, 0); // Displaying popup

                    popUpView.setBackground(new ColorDrawable(Color.WHITE));

                    Button btnPatientClinicOK = (Button) popUpView.findViewById(R.id.btnPatientClinicOK);

                    btnPatientClinicOK.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            for(ILineDataSet ids: lineDataSetsPLOT) {
                                ids.clear();
                            }
                            lineChart.notifyDataSetChanged();
                            lineChart.moveViewToX(0);
                            ss.setRecFlag(true);
                            mpopup.dismiss();
                        }
                    });

                    view.getBackground().setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                    view.invalidate();
                    ibtn_save.getBackground().clearColorFilter();
                    ibtn_save.invalidate();
                }
            }
        });

        ibtn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(observedSource == null){
                    return;
                }
                if(observedSource.getRecFlag()){
                    observedSource.setRecFlag(false);
                    view.getBackground().setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                    view.invalidate();
                    ibtn_rec.getBackground().clearColorFilter();
                    ibtn_rec.invalidate();
                }
            }
        });

        return v;
    }

    public void resetPlotView(){
        selectedSensors = null;
        itemsIntoList = null;
        alertDialogItems = null;
        initPlot(-600,600);
    }

    public void registerDataFromSensorSource(){
        //Create Entry from observedSource
        //for each channel
        if(observedSource == null) {
            resetPlotView();
            return;
        }

        for(Channel channel : observedSource.getChannelsOfThisSource().values()) {
            ILineDataSet dataSet = lineDataSetsPLOT.get(channelLineID.get(channel.getChannel_ID()));
            //For each Sample in DataRecord buff, we create entries
            DataRecord dataRecord = observedSource.getbufferDataRecord();

            Sample sample = dataRecord.getSamplePerChannel().get(channel.getChannel_ID());

            for (int i = 0; i < sample.sizeOfArraySample(); i++) {
                float x_axis_converted = i + dataRecord.getData_record_ID()*sample.MAXSAMPLE;
                dataSet.addEntry(new Entry(x_axis_converted, sample.getArrayPhysicalSample()[i]));
            }

        }

        float yMin = -600f, yMax = 600f;

        if(selectedSensors != null){
            int a = 0;
            //foreach selected sensors
            for(ILineDataSet iLineDataSet: lineDataSetsPLOT){
                iLineDataSet.setVisible(false);
            }
            while(a < selectedSensors.length)
            {
                boolean value = selectedSensors[a];

                //the selected sensor
                if(value){
                    lineDataSetsPLOT.get(channelLineID.get(itemsIntoList.get(a))).setVisible(true);
                    if(yMin > (float) observedSource.getChannelsOfThisSource().get(alertDialogItems[a]).getPhysical_min()){
                        yMin = (float) observedSource.getChannelsOfThisSource().get(alertDialogItems[a]).getPhysical_min();
                    }
                    if(yMax < (float) observedSource.getChannelsOfThisSource().get(alertDialogItems[a]).getPhysical_max()){
                        yMax = (float) observedSource.getChannelsOfThisSource().get(alertDialogItems[a]).getPhysical_max();
                    }
                }
                a++;
            }
        }
        initPlot(yMin, yMax);
    }

    private void initPlot(float yMin, float yMax){
        if(lineChart == null) return;
        //lineChart.setOnChartValueSelectedListener(this);
        // enable description text
        lineChart.getDescription().setEnabled(true);
        Description d = new Description();
        d.setText("BITalino "+observedSource.getSensor_source_ID());
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
        lineChart.setPinchZoom(true);
        lineChart.setAutoScaleMinMaxEnabled(true);

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
        //leftAxis.setAxisMaximum(yMax);
        //leftAxis.setAxisMinimum(yMin);
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

    void popUpSelectSensors(){
        alertdialogbuilder = new AlertDialog.Builder(getActivity());

        itemsIntoList = Arrays.asList(alertDialogItems);
        String nameSensors[] = new String[alertDialogItems.length];

        for(int i = 0; i < alertDialogItems.length; i ++) {
            nameSensors[i] = observedSource.getChannelsOfThisSource().get(alertDialogItems[i]).getChannel_name();
        }

        alertdialogbuilder.setMultiChoiceItems(nameSensors, selectedSensors, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

            }
        });

        alertdialogbuilder.setCancelable(false);

        alertdialogbuilder.setTitle("Select sensors");

        alertdialogbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                lineChart = (LineChart) v.findViewById(R.id.lineChart);
                registerDataFromSensorSource();
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


    void readSensorInfoForSelectList(){
        if(alertDialogItems != null) return;
        observedSource.getChannelsOfThisSource().keySet().size();
        alertDialogItems = new String[observedSource.getChannelsOfThisSource().keySet().size()];
        selectedSensors = new boolean[observedSource.getChannelsOfThisSource().keySet().size()];

        int i = 0;
        for(Channel s : observedSource.getChannelsOfThisSource().values()){
            alertDialogItems[i] = s.getChannel_ID();
            selectedSensors[i] = false;
            i++;
        }
    }

    @Override
    public synchronized void addNewSample(final String channel_ID, float sample_data, long created_date){
        //System.out.println(" PlotView ---------> isNewRecord:"+isNewDataRecord+" sensorID:"+channel_ID+" sample:"+sample_data+" date:"+created_date);

        DataRecord dataRecord = observedSource.getbufferDataRecord();
        Sample sample = dataRecord.getSamplePerChannel().get(channel_ID);
        // minus 1 because the addition to SAMPLE has advanced index by 1
        final float x_axis_converted = (sample.getNr_of_sample() - 1) + dataRecord.getData_record_ID()*sample.MAXSAMPLE;
        final Entry new_entry = new Entry(x_axis_converted,sample_data);

        //not initialised yet
        if(v == null) return;
        v.post(new Runnable() {
            @Override
            public void run() {

                int i = channelLineID.get(channel_ID);
                LineData data = lineChart.getData();

                if (data != null && i >= 0) {
                    ILineDataSet set = data.getDataSetByIndex(i);

                    if (set == null) {
                        return;
                    }

                    //MAKE SURE THAT WE LIMIT THE WINDOWS OF ENTRY TO AVOID EATING A LOT OF MEMORY
                    if(set.getEntryCount() > NR_ENTRIES_WINDOW*3){
                        set.removeEntry(0);
                        set.removeEntry(1);
                        set.removeEntry(2);
                    }

                    set.addEntry(new_entry);
                    data.notifyDataChanged();

                    // let the chart know it's data has changed
                    lineChart.notifyDataSetChanged();
                    // limit the number of visible entries
                    lineChart.setVisibleXRangeMaximum(NR_ENTRIES_WINDOW);
                    // move to the latest entry
                    if((int)x_axis_converted-(NR_ENTRIES_WINDOW+1) > 0)
                        lineChart.moveViewToX((int)x_axis_converted-(NR_ENTRIES_WINDOW+1));
                    else lineChart.invalidate();
                }
            }
        });

    }
}
