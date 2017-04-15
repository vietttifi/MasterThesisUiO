package no.uio.ifi.viettt.mscosa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADBHelper;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADataBaseManager;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.RecordAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleAdapter;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;

public class DatabaseVisualisationActivity extends AppCompatActivity implements View.OnClickListener{

    RadioButton rdSource, rdPatient;
    EditText keySearch, annotations;
    TextView lblSelectedSource, selectChannelRP;
    Button btnApply;
    ImageButton ibtnPlay, ibtnStop, ibtnAddAnns, ibtnSaveAnns;
    TableLayout tblLayout;

    //GUI
    GraphView graph;
    AlertDialog.Builder alertdialogbuilder;
    String[] alertDialogItems;
    boolean[] selectedChannels;
    HashMap<String, LineGraphSeries<DataPoint>> channelLines = new HashMap<>();

    final int NR_ENTRIES_WINDOW = 300;

    String source_ID, patient_ID;
    long timestamp = 0;

    //Thread query and update
    //ConsumerDataRecord updateGUI;
    //ProducerDataRecord queryDataForGUI;
    final int BUFF_SIZE = 20;

    Thread updateGUI = null;
    boolean pauseGUI = true;

    private final Object lock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initThing();
    }

    private void initThing(){
        setContentView(R.layout.activity_database_visualisation);
        rdSource = (RadioButton)findViewById(R.id.rdBtnSource);
        rdPatient = (RadioButton)findViewById(R.id.rdBtnPatient);
        graph = (GraphView)findViewById(R.id.graphview);

        keySearch = (EditText)findViewById(R.id.txtSearchRP);
        lblSelectedSource = (TextView)findViewById(R.id.lblSelectedSource);
        annotations = (EditText)findViewById(R.id.txtAnnRP);
        btnApply = (Button)findViewById(R.id.btnApply);
        tblLayout = (TableLayout)findViewById(R.id.tblSources);
        ibtnPlay = (ImageButton)findViewById(R.id.btnPlayRP);
        ibtnStop = (ImageButton)findViewById(R.id.btnStopRP);
        ibtnAddAnns = (ImageButton)findViewById(R.id.btnAddAnnRP);
        ibtnSaveAnns = (ImageButton)findViewById(R.id.btnSaveAnnRP);
        selectChannelRP = (TextView)findViewById(R.id.selectChannelRP);

        selectChannelRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpSelectSensors();
            }
        });

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


    private void manageRdSource(View view){
        manageKeySearch(keySearch.getText(),0,0,0);
    }

    private void manageRdPatient(View view){
        manageKeySearch(keySearch.getText(),0,0,0);
    }


    private void manageKeySearch(CharSequence charSequence, int i, int i1, int i2){
        //Toast.makeText(getApplication(),charSequence.toString(),Toast.LENGTH_SHORT).show();
        if(charSequence.toString().length() < 3) return;

        String queryString = "SELECT DISTINCT "
                + " p_owner as Patient, s_id as Source , timestamp"
                + " FROM RECORD ";

        if(rdSource.isChecked()){
            queryString += " WHERE s_id like '%"+ charSequence.toString()+"%'";
        } else {
            queryString += " WHERE p_owner like '%"+ charSequence.toString()+"%'";
        }

        OSADataBaseManager.initializeInstance(new OSADBHelper(getApplication()));
        OSADataBaseManager osaDataBaseManager = null;
        tblLayout.removeAllViews();

        try {
            osaDataBaseManager = OSADataBaseManager.getInstance();
            SQLiteDatabase mDatabase = osaDataBaseManager.openDatabase();
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
                        column.setText(String.valueOf(cursor.getLong(k)));
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

        if(osaDataBaseManager != null) osaDataBaseManager.closeDatabase();
    }

    private void manageBtnApply(View view){
        if(updateGUI != null) updateGUI.interrupt();
        pauseGUI = true;
        Toast.makeText(getApplication(),"APPLY "+source_ID+" "+patient_ID,Toast.LENGTH_SHORT).show();
        btnApply.setEnabled(false);
        RecordAdapter recordAdapter = new RecordAdapter(getApplication());
        ArrayList<Record> records = recordAdapter.getAllRecordForSourse(source_ID, timestamp);
        recordAdapter.close();
        ChannelAdapter channelAdapter = new ChannelAdapter(getApplication());
        ArrayList<Channel> channels = channelAdapter.getChannelsFromSource(source_ID);
        channelAdapter.close();
        //for(Record r : records) System.out.println(r.getR_id()+" "+r.getCh_nr()+" "+r.getTimestamp());
        //for(Channel c : channels) System.out.println(c.getCh_name() + " "+c.getCh_nr());
        initGraph(records,channels);
    }

    private void manageIbtnPlay(View view){
        Toast.makeText(getApplication(),"Play click",Toast.LENGTH_SHORT).show();
            synchronized (lock){
                pauseGUI = false;
                lock.notify();
            }

    }

    private void manageIbtnStop(View view){
        Toast.makeText(getApplication(),"Stop click",Toast.LENGTH_SHORT).show();
        pauseGUI = true;
    }

    private void manageIbtnAddAnns(View view){
        Toast.makeText(getApplication(),"Future work, where "+ annotations.getText().toString() +" will be added to annotation list at the current timestamp",Toast.LENGTH_SHORT).show();
    }

    private void manageIbtnSaveAnns(View view){
        Toast.makeText(getApplication(),"Future work, where the annotation list will be saved into DB",Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view) {
        if(view instanceof TableRow) {
            TableRow tr = (TableRow)view;
            patient_ID = ((TextView) tr.getChildAt(0)).getText().toString();
            source_ID = ((TextView) tr.getChildAt(1)).getText().toString();
            timestamp = Long.parseLong(((TextView) tr.getChildAt(2)).getText().toString());
            lblSelectedSource.setText(source_ID);
            btnApply.setEnabled(true);
        }
    }

    private void popUpSelectSensors(){
        final boolean tempPause;
        synchronized (lock){
            tempPause = pauseGUI;
            pauseGUI = true;
        }
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
                graph.removeAllSeries();
                for(int i = 0; i < selectedChannels.length;i++){
                    if(selectedChannels[i]){
                        String ch_nr = alertDialogItems[i].split(": ")[0];
                        graph.addSeries(channelLines.get(ch_nr));
                    }
                }
                synchronized (lock){
                    pauseGUI = tempPause;
                    lock.notify();
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

    private void initGraph(ArrayList<Record> records, ArrayList<Channel> channels){
        initThing();
        channelLines.clear();
        selectedChannels = new boolean[channels.size()];
        alertDialogItems = new String[channels.size()];

        for(Channel c : channels){
            channelLines.put(c.getCh_nr(),new LineGraphSeries<DataPoint>());
            graph.addSeries(channelLines.get(c.getCh_nr()));
            channelLines.get(c.getCh_nr()).setTitle(c.getCh_name()+"(" +((c.getDimension()==null) ? "": c.getDimension().trim())+")");
            channelLines.get(c.getCh_nr()).setColor(Color.rgb((int) (Math.random() * Integer.MAX_VALUE),
                    (int) (Math.random() * Integer.MAX_VALUE), (int) (Math.random() * Integer.MAX_VALUE)));
        }
        int inx = 0;
        for(Channel c : channels){
            alertDialogItems[inx] = c.getCh_nr()+": "+c.getCh_name();
            selectedChannels[inx++] = true;
        }

        drawGraph(-10,10);
        updateGUI = new UpdateGui(records,channels);
        updateGUI.start();
    }

    private void drawGraph(int minY, int maxY){
        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setScalable(true);
        viewport.setScalableY(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMaxX(0);
        viewport.setMaxX(210);
        viewport.setMinY(minY);
        viewport.setMaxY(maxY);
        viewport.setBackgroundColor(Color.BLACK);
        graph.getGridLabelRenderer().setGridColor(Color.DKGRAY);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time from visualising in nano second");
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.GREEN);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Metric in legend");
        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.GREEN);
        graph.getGridLabelRenderer().setHumanRounding(true);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    private class UpdateGui extends Thread{
        private ArrayList<Record> records;
        private ArrayList<Channel> channels;
        private long waitTime = 0;
        long timestampPlot = -1;
        int maxYY = 300, minYY = -300;

        UpdateGui(ArrayList<Record> records, ArrayList<Channel> channels){
            this.records = records;
            this.channels = channels;
            for(Record r : records){
                System.out.println(r.getFrequency()+" "+(1.0/r.getFrequency())*1000);
                if(waitTime<(1.0/r.getFrequency())*1000) waitTime = (int)((1.0/r.getFrequency())*1000)+1;
                timestampPlot = r.getTimestamp();
            }
        }

        @Override
        public void run() {

            Thread manageGUIData = new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock){
                        boolean bk = pauseGUI;
                        pauseGUI = true;
                        HashMap<String,LineGraphSeries<DataPoint>> channel2 = channelLines;
                        for(Record r: records){
                            ArrayList<Sample> samples = r.getSamplesbuffer();
                            while (!samples.isEmpty() && timestampPlot >= samples.get(0).getTimestamp()){
                                Sample sample = samples.remove(0);
                                if(minYY > sample.getSample_data()) minYY = ((int) sample.getSample_data())-1;
                                if(maxYY < sample.getSample_data()) maxYY = ((int) sample.getSample_data())+1;
                                LineGraphSeries<DataPoint> tmp = channel2.get(String.valueOf(r.getCh_nr()));
                                if(tmp != null && (sample.getTimestamp()-r.getTimestamp()) > tmp.getHighestValueX()){
                                    tmp.appendData(new DataPoint(sample.getTimestamp()-r.getTimestamp(),sample.getSample_data()),true,NR_ENTRIES_WINDOW);
                                    //System.out.println(tmp.getHighestValueX()+ " "+sample.getSample_data() + ": "+sample.getTimestamp() + " :" + r.getTimestamp());
                                }

                            }
                        }

                        timestampPlot += waitTime;
                        graph.getViewport().setMinY(minYY);
                        graph.getViewport().setMaxY(maxYY);
                        pauseGUI = bk;
                        lock.notifyAll();
                    }
                }
            });

            boolean stop = false;
            while(!stop){
                boolean allEmpty = true;
                //query data for each records if empty
                SampleAdapter sampleAdapter = new SampleAdapter(getApplication());
                for(Record r : records){
                    if(r.getSamplesbuffer().size() ==  0){
                        r.setSamplesbuffer(sampleAdapter.getSamples(r.getR_id(),r.getOffset(),r.getOffset()+r.getLimit()));
                        r.setOffset(r.getOffset()+r.getLimit());
                    }
                    if(!r.getSamplesbuffer().isEmpty()) allEmpty = false;
                }
                sampleAdapter.close();

                if(allEmpty) break;
                try {
                    while(pauseGUI) {
                        synchronized (lock) {
                            lock.wait();
                        }
                    }
                    sleep(waitTime);
                    runOnUiThread(manageGUIData);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    stop = true;
                }

            }
            System.out.println("IAM DIE "+this.toString());

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(updateGUI != null) updateGUI.interrupt();
    }
}
