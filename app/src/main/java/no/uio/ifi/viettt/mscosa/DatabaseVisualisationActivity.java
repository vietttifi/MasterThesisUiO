package no.uio.ifi.viettt.mscosa;
/*
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import java.util.HashMap;
import java.util.List;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.ClinicAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADBHelper;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.PersonAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SensorSourceAdapter;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ProducerConsumerBuffer;

public class DatabaseVisualisationActivity extends AppCompatActivity implements View.OnClickListener{

    RadioButton rdSource, rdPatient, rdClinic;
    EditText keySearch, annotations;
    TextView lblSelectedSource;
    Button btnApply;
    ImageButton ibtnPlay, ibtnStop, ibtnAddAnns, ibtnSaveAnns;
    TableLayout tblLayout;

    //HELP VARIABLES
    private String patient_ID, source_ID, clinic_ID;
    private SensorSource ss;
    private Patient patient;
    private Clinic clinic;
    private Record[] records = new Record[10];
    private List<Channel> channelList;

    //GUI
    AlertDialog.Builder alertdialogbuilder;
    String[] alertDialogItems;
    boolean[] selectedChannels;

    //CHART
    //For Plot
    LineChart lineChartRP;
    ArrayList<ILineDataSet> lineDataSetsPLOT;
    HashMap<String, ILineDataSet> forUpdateNewSamples;
    final int NR_ENTRIES_WINDOW = 300;

    //Thread query and update
    //ConsumerDataRecord updateGUI;
    //ProducerDataRecord queryDataForGUI;
    final int BUFF_SIZE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_database_visualisation);


        rdSource = (RadioButton)findViewById(R.id.rdBtnSource);
        rdPatient = (RadioButton)findViewById(R.id.rdBtnPatient);
        rdClinic = (RadioButton)findViewById(R.id.rdBtnClinic);

        keySearch = (EditText)findViewById(R.id.txtSearchRP);
        lblSelectedSource = (TextView)findViewById(R.id.lblSelectedSource);
        annotations = (EditText)findViewById(R.id.txtAnnRP);

        btnApply = (Button)findViewById(R.id.btnApply);

        tblLayout = (TableLayout)findViewById(R.id.tblSources);
        if (lineChartRP == null) lineChartRP = (LineChart)findViewById(R.id.lineChartRP);
        else initPlot(-600,600);

        ibtnPlay = (ImageButton)findViewById(R.id.btnPlayRP);
        ibtnStop = (ImageButton)findViewById(R.id.btnStopRP);
        ibtnAddAnns = (ImageButton)findViewById(R.id.btnAddAnnRP);
        ibtnSaveAnns = (ImageButton)findViewById(R.id.btnSaveAnnRP);

        rdSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageRdSource(view);
            }
        });

        rdPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageRdPatient(view);
            }
        });

        rdClinic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageRdClinic(view);
            }
        });

        keySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                manageKeySearch(charSequence,i,i1,i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageBtnApply(view);
            }
        });

        ibtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageIbtnPlay(view);
            }
        });

        ibtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageIbtnStop(view);
            }
        });

        ibtnAddAnns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageIbtnAddAnns(view);
            }
        });

        ibtnSaveAnns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageIbtnSaveAnns(view);
            }
        });


    }

    /*=======================  FUNCTIONS  ==========================*/

    /*

    private void manageRdSource(View view){
        manageKeySearch(keySearch.getText(),0,0,0);
    }

    private void manageRdPatient(View view){
        manageKeySearch(keySearch.getText(),0,0,0);
    }

    private void manageRdClinic(View view){
        manageKeySearch(keySearch.getText(),0,0,0);
    }

    private void manageKeySearch(CharSequence charSequence, int i, int i1, int i2){
        //Toast.makeText(getApplication(),charSequence.toString(),Toast.LENGTH_SHORT).show();
        if(charSequence.toString().length() < 3) return;

        String queryString = "SELECT DISTINCT " +
                " sensor_source.source_id as source_id, " +
                " patient.patient_id as patient_id, patient.last_name as p_name, clinic.clinic_id as clinic_id from data_record " +
                " JOIN sensor_source ON sensor_source.source_id = data_record.source_id " +
                " JOIN patient ON patient.patient_id = data_record.patient_id " +
                " JOIN clinic ON clinic.clinic_id = data_record.clinic_id ";

        if(rdSource.isChecked()){
            queryString += " where sensor_source.source_id like '%"+ charSequence.toString()+"%'";
        }else if(rdPatient.isChecked()){
            queryString += " where patient.last_name like '%"+ charSequence.toString()+"%'";
        }else{
            queryString += " where clinic.clinic_code like '%"+ charSequence.toString()+"%'";
        }

        OSADBHelper mDbHelper = new OSADBHelper(getApplication());
        SQLiteDatabase mDatabase = mDbHelper.getReadableDatabase();

        tblLayout.removeAllViews();

        try {
            Cursor cursor = mDatabase.rawQuery(queryString, null);
            cursor.moveToFirst();

            int columnCount = cursor.getColumnCount();
            TableRow tr = new TableRow(getApplication());
            TableRow trSep = new TableRow(getApplication());
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            trSep.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            //add columns
            for (int k =0; k<columnCount; k++) {
                TextView column = new TextView(getApplication());
                TextView separator = new TextView(getApplication());
                column.setGravity(Gravity.CENTER);
                separator.setGravity(Gravity.CENTER);
                separator.setText("---");
                column.setText(String.valueOf(cursor.getColumnName(k)));
                column.setPadding(5, 5, 5, 0);
                tr.addView(column);
                trSep.addView(separator);
            }

            tblLayout.addView(tr, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            tblLayout.addView(trSep, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            while (!cursor.isAfterLast()) {
                tr = new TableRow(getApplication());
                tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                for (int k=0; k<columnCount; k++) {
                    TextView column = new TextView(getApplication());
                    column.setGravity(Gravity.CENTER);
                    if(cursor.getType(k) == Cursor.FIELD_TYPE_BLOB){
                        column.setText("UNPRINTABLE");
                    }else if(cursor.getType(k) == Cursor.FIELD_TYPE_NULL) {
                        column.setText("NULL");
                    }else if(cursor.getType(k) == Cursor.FIELD_TYPE_FLOAT) {
                        column.setText(String.valueOf(cursor.getFloat(k)));
                    }else if(cursor.getType(k) == Cursor.FIELD_TYPE_INTEGER) {
                        column.setText(String.valueOf(cursor.getInt(k)));
                    }else {
                        column.setText(cursor.getString(k));
                    }

                    column.setPadding(5, 5, 5, 0);
                    tr.addView(column);
                }

                tr.setOnClickListener(this);

                tblLayout.addView(tr, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();

        }catch (Exception e){
            //clear result table
            tblLayout.removeAllViews();
            TableRow tr = new TableRow(getApplication());
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            TextView column = new TextView(getApplication());
            column.setGravity(Gravity.CENTER);
            column.setText("Query syntax fail. " + "And fail is: "+e.getMessage());
            tr.addView(column);
            tblLayout.addView(tr, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            System.out.println(e.getMessage());
        }

        mDatabase.close();
        mDbHelper.close();
    }

    private void manageBtnApply(View view){
        //Stop provious update and query thread
        if(updateGUI != null) updateGUI.stop = true;
        if(queryDataForGUI != null) queryDataForGUI.stop = true;

        SensorSourceAdapter sensorSourceAdapter = new SensorSourceAdapter(getApplication());
        ss = sensorSourceAdapter.getSensorSourceById(source_ID);
        sensorSourceAdapter.close();

        PersonAdapter personAdapter = new PersonAdapter(getApplication());
        patient = personAdapter.getPatientById(patient_ID);
        personAdapter.close();

        ClinicAdapter clinicAdapter = new ClinicAdapter(getApplication());
        clinic = clinicAdapter.getClinicById(clinic_ID);
        clinicAdapter.close();

        ChannelAdapter channelAdapter = new ChannelAdapter(getApplication(),false);
        channelList = channelAdapter.getallChannelsOfSource(source_ID);
        channelAdapter.close();

        TextView selectSensors = (TextView) findViewById(R.id.selectChannelRP);
        selectSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ss == null) return;
                //read info for select channel list
                alertDialogItems = new String[channelList.size()];
                for(Channel c : channelList){
                    alertDialogItems[Integer.parseInt(c.getChannel_ID())] = c.getChannel_name();
                }
                popUpSelectSensors();
            }
        });

        btnApply.setEnabled(false);
        initPlot(-600,600);

        //Start producer and consumer threads
        List<String> channelIDs = new ArrayList<>();
        for(Channel c : channelList) channelIDs.add(c.getChannel_ID());
        ProducerConsumerBuffer buff = new ProducerConsumerBuffer(BUFF_SIZE);
        queryDataForGUI = new ProducerDataRecord(buff, patient.getPatient_ID(),clinic.getClinic_ID(),ss.getSource_id(),channelIDs);
        updateGUI = new ConsumerDataRecord(buff);
        queryDataForGUI.start();
        updateGUI.execute("");
    }

    private void manageIbtnPlay(View view){
        Toast.makeText(getApplication(),"Play click",Toast.LENGTH_SHORT).show();
    }

    private void manageIbtnStop(View view){
        Toast.makeText(getApplication(),"Stop click",Toast.LENGTH_SHORT).show();
    }

    private void manageIbtnAddAnns(View view){
        Toast.makeText(getApplication(),"Annotation added click",Toast.LENGTH_SHORT).show();
    }

    private void manageIbtnSaveAnns(View view){
        Toast.makeText(getApplication(),"All annotations have saved click",Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view) {
        if(view instanceof TableRow) {
            TableRow tr = (TableRow)view;
            source_ID = ((TextView) tr.getChildAt(0)).getText().toString();
            patient_ID = ((TextView) tr.getChildAt(1)).getText().toString();
            clinic_ID = ((TextView) tr.getChildAt(3)).getText().toString();
            lblSelectedSource.setText(source_ID);
            btnApply.setEnabled(true);
        }
    }

    private void popUpSelectSensors(){
        alertdialogbuilder = new AlertDialog.Builder(DatabaseVisualisationActivity.this);

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

    private void initPlot(float yMin, float yMax){
        if(ss == null) return;
        lineDataSetsPLOT = new ArrayList<>();
        forUpdateNewSamples = new HashMap<>();

        Utils.init(getApplication());

        if(forUpdateNewSamples.size() == 0){
            forUpdateNewSamples = new HashMap<>();
            for(Channel channel : channelList) {
                String legendName = channel.getChannel_name()+"(" +((channel.getPhysical_dimension().equals("")) ? "": channel.getPhysical_dimension().trim())+")";
                ILineDataSet dataSet = createSet(legendName, Color.rgb((int) (Math.random() * Integer.MAX_VALUE),
                        (int) (Math.random() * Integer.MAX_VALUE), (int) (Math.random() * Integer.MAX_VALUE)));
                forUpdateNewSamples.put(channel.getChannel_ID(),dataSet);
            }
        }

        selectedChannels = new boolean[channelList.size()];

        for(ILineDataSet iLineDataSet : forUpdateNewSamples.values()) lineDataSetsPLOT.add(iLineDataSet);

        lineChartRP.getDescription().setEnabled(true);
        Description d = new Description();
        d.setText("BITalino "+ss.getSource_id());
        d.setTextColor(Color.rgb((int)(Math.random()*Integer.MAX_VALUE),
                (int)(Math.random()*Integer.MAX_VALUE),(int)(Math.random()*Integer.MAX_VALUE)));
        lineChartRP.setDescription(d);

        // enable touch gestures
        lineChartRP.setTouchEnabled(true);

        // enable scaling and dragging
        lineChartRP.setDragEnabled(true);
        lineChartRP.setScaleEnabled(true);
        lineChartRP.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        //lineChart.setPinchZoom(true);
        //lineChart.setAutoScaleMinMaxEnabled(true);

        // set an alternative background color
        lineChartRP.setBackgroundColor(Color.BLACK);

        // get the legend (only possible after setting data)
        Legend l = lineChartRP.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
        l.setWordWrapEnabled(true);

        //------FORMAT DATE BOTTOM----
        //IAxisValueFormatter xAxisFormatter = new TimeAxisValueFormatter(lineChart,observedSource);



        XAxis xl = lineChartRP.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xl.setValueFormatter(xAxisFormatter);//-----------------

        //mv = new PlotMarkerView(getContext(), R.layout.ro_plot_marker_view,observedSource);
        //mv.setChartView(lineChart); // For bounds control
        //lineChart.setMarker(mv); // Set the marker to the chart



        YAxis leftAxis = lineChartRP.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(yMax);
        leftAxis.setAxisMinimum(yMin);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChartRP.getAxisRight();
        rightAxis.setEnabled(false);

        LineData init = new LineData(lineDataSetsPLOT);
        init.setValueTextColor(Color.WHITE);
        lineChartRP.setData(init);

        init.notifyDataChanged();
        // let the chart know it's data has changed
        lineChartRP.notifyDataSetChanged();
        // limit the number of visible entries
        lineChartRP.setVisibleXRangeMaximum(NR_ENTRIES_WINDOW);
        lineChartRP.moveViewToX(0);
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

    class ConsumerDataRecord extends AsyncTask<String,String,String>{
        boolean stop = false;
        ProducerConsumerBuffer buff;
        int cnt = 0;

        public ConsumerDataRecord(ProducerConsumerBuffer buff){
            this.buff = buff;
        }

        @Override
        protected String doInBackground(String... voids) {
            while (!stop){
                Record record = buff.getFromBuff();
                if(record == null) break;
                System.out.println("GET BUFF");
                for(Sample s : record.getSampleSetList()) s.samplesByteToFloat(false,0,0,false);
                for(Sample s : record.getSampleSetList()){

                    ILineDataSet channel_line = forUpdateNewSamples.get(s.getChannel_id());
                    System.out.println(s.getChannel_id()+" <---------- CID " +channel_line+" "+s.getSample_data().length);
                    if(channel_line == null) continue;
                    for(int i = 0; i < s.getSample_data().length; i++){
                        //MAKE SURE THAT WE LIMIT THE WINDOWS OF ENTRY TO AVOID EATING A LOT OF MEMORY
                        if(channel_line.getEntryCount() > NR_ENTRIES_WINDOW*3){
                            channel_line.removeFirst();
                        }

                        Entry new_entry = new Entry(cnt,s.getSample_data()[i]);
                        channel_line.addEntry(new_entry);

                        publishProgress("");

                        cnt++;

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
            System.out.println("EATING ALL --- ");
            return "";
        }

        @Override
        protected void onProgressUpdate(String... voids) {
            lineChartRP.getData().notifyDataChanged();

            // let the chart know it's data has changed
            lineChartRP.notifyDataSetChanged();
            // limit the number of visible entries
            lineChartRP.setVisibleXRangeMaximum(NR_ENTRIES_WINDOW);
            //System.out.println(" PlotView "+ss.getSource_id()+"---------> channel :" +s.getChannel_id()+" sample:"+s.getSample_data()[i]);

            // move to the latest entry
            if((cnt - (NR_ENTRIES_WINDOW + 1) > 0)){
                lineChartRP.moveViewToX(cnt - NR_ENTRIES_WINDOW - 1);
            } else lineChartRP.invalidate();
        }

    }

    class ProducerDataRecord extends Thread{
        boolean stop = false;
        ProducerConsumerBuffer buff;
        final int RESULT_SIZE;
        int offset = 0;

        String patientID, clinicID,sourceID;
        List<String> channelIDs;

        String queryString;

        public ProducerDataRecord(ProducerConsumerBuffer buff, String patientID, String clinicID, String sourceID, List<String> channelIDs){
            this.buff = buff;
            RESULT_SIZE = buff.getSize();
            this.patientID = patientID;
            this.channelIDs = channelIDs;
            this.clinicID = clinicID;
            this.sourceID = sourceID;
        }

        @Override
        public void run() {
            while (!stop){
                queryString = "SELECT distinct data_record.dr_id, data_record.createDate " +
                        " FROM data_record " +
                        " JOIN sensor_source ON sensor_source.source_id = data_record.source_id " +
                        " JOIN patient ON patient.patient_id = data_record.patient_id " +
                        " JOIN clinic ON clinic.clinic_id = data_record.clinic_id " +
                        " WHERE patient.patient_id = '" + this.patientID + "' AND clinic.clinic_id = '" + this.clinicID +
                        "' AND sensor_source.source_id = '" + this.sourceID + "'" +
                        " AND data_record.dr_id >= "+offset+" AND data_record.dr_id <"+(offset+RESULT_SIZE)+
                        " ORDER BY data_record.dr_id ASC";

                OSADBHelper mDbHelper = new OSADBHelper(getApplication());
                SQLiteDatabase mDatabase = mDbHelper.getReadableDatabase();
                Cursor cursor = mDatabase.rawQuery(queryString, null);
                cursor.moveToFirst();

                if(cursor.getCount() == 0) {
                    stop = true;
                    cursor.close();
                    mDatabase.close();
                    mDbHelper.close();
                    System.out.println("FEEDING ALL --- ");
                    buff.putToBuff(null);
                    return;
                }

                Record[] records = new Record[cursor.getCount()];

                for(int i = 0; i < cursor.getCount(); i++){
                    //System.out.println(cursor.getCount()+" "+cursor.getLong(0)+" "+cursor.getLong(1));
                    records[i] = new Record(cursor.getLong(0),this.sourceID,this.patientID, this.clinicID, cursor.getLong(1));
                    records[i].initSampleSet(channelIDs);

                    cursor.moveToNext();
                }

                cursor.close();


                //GET SAMPLE FOR THOSE DATA RECORDS
                for(int i = 0; i < records.length; i++){
                    for(String channel_ID: channelIDs){
                        queryString = "Select sample_data from sampleset " +
                                " WHERE source_id = '"+this.sourceID
                                +"' AND channel_id = '" +channel_ID
                                +"' AND patient_id = '"+this.patientID
                                +"' AND clinic_id = '"+this.clinicID
                                +"' AND dr_id = '"+ records[i].getData_record_ID()+"' ";
                        cursor = mDatabase.rawQuery(queryString, null);
                        cursor.moveToFirst();

                        if(cursor.getCount() != 0){
                            byte[] data = cursor.getBlob(0);
                            records[i].getSampleSetList().get(Integer.parseInt(channel_ID)).setSamples(data);
                        }

                        cursor.close();
                    }

                }

                for(int i = 0; i < records.length; i++){
                    buff.putToBuff(records[i]);
                    System.out.println("SET BUFF");
                }

                mDatabase.close();
                mDbHelper.close();
                offset += RESULT_SIZE;
            }

            System.out.println("FEEDING ALL --- ");
        }
    }



}
*/