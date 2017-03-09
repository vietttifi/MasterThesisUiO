package no.uio.ifi.viettt.mscosa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.ClinicAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADBHelper;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.PersonAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SensorSourceAdapter;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFElementSize;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFHeader;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFWriter;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Clinic;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

public class EDFExportActivity extends AppCompatActivity{

    EditText searchText;
    Button btnExport;
    ListView tblSource, tblChannel;

    //final SourceClickListener sourceListener = new SourceClickListener();

    String[] sourceIDs, patinetIDs, clinicIDs;

    private String sourceID, patientID, clinicID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edf_export_fullscreen);

        searchText = (EditText)findViewById(R.id.txtSearch);
        tblSource = (ListView) findViewById(R.id.tblSources);
        tblChannel = (ListView) findViewById(R.id.tblChannel);

        btnExport = (Button)findViewById(R.id.btnExport);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length()<2) return;
                //manageKeySearch(charSequence,i,i1,i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnExport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //exportBtn();
            }
        });
    }
/*
    private void exportBtn(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplication());
        edittext.setText(sourceID);
        edittext.setSelection(sourceID.length());
        alert.setTitle("Enter file name");
        alert.setView(edittext);
        alert.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                SparseBooleanArray checked = tblChannel.getCheckedItemPositions();
                String channelIDS[] = new String[tblChannel.getCheckedItemCount()];

                int cnt = 0;
                for (int i = 0; i < tblChannel.getAdapter().getCount(); i++) {
                    if (checked.get(i)) {
                        channelIDS[cnt++] = tblChannel.getAdapter().getItem(i).toString().trim().split(" ")[1];
                    }
                }

                btnExport.setEnabled(false);
                tblSource.setAdapter(null);
                tblChannel.setAdapter(null);
                File path = new File(Environment.getExternalStorageDirectory().getPath()+"/EDF_FILES/");
                if(!path.exists()) path.mkdir();
                String fileName = path.getPath()+"/"+edittext.getText().toString() + ".edf";
                Toast.makeText(getApplication(),"File save to: "+fileName,Toast.LENGTH_SHORT).show();
                //Start thread that will export data to EDF file.
                (new ExportSourceToEDF(sourceID,patientID,clinicID,channelIDS,fileName)).start();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();


    }

    private void manageKeySearch(CharSequence charSequence, int i, int i1, int i2){
        String[] choiceList = null;

        String queryString = "SELECT DISTINCT " +
                " sensor_source.source_id as S_ID, " +
                " patient.patient_id as P_ID, patient.last_name as P_NAME, clinic.clinic_id as C_ID , clinic.clinic_code as C_CODE" +
                " from data_record " +
                " JOIN sensor_source ON sensor_source.source_id = data_record.source_id " +
                " JOIN patient ON patient.patient_id = data_record.patient_id " +
                " JOIN clinic ON clinic.clinic_id = data_record.clinic_id " +
                " where sensor_source.source_id like '%"+ charSequence.toString()+"%' " +
                " OR patient.last_name like '%"+ charSequence.toString()+"%' " +
                " OR clinic.clinic_code like '%"+ charSequence.toString()+"%' ";

        OSADBHelper mDbHelper = new OSADBHelper(getApplication());
        SQLiteDatabase mDatabase = mDbHelper.getReadableDatabase();

        try {
            Cursor cursor = mDatabase.rawQuery(queryString, null);
            cursor.moveToFirst();

            sourceIDs = new String[cursor.getCount()];
            clinicIDs = new String[cursor.getCount()];
            patinetIDs = new String[cursor.getCount()];

            choiceList = new String[cursor.getCount()];

            int cnt = 0;
            while (!cursor.isAfterLast()) {
                sourceIDs[cnt] = cursor.getString(0);
                clinicIDs[cnt] = cursor.getString(3);
                patinetIDs[cnt] = cursor.getString(1);
                choiceList[cnt] = "SID: "+cursor.getString(0) +
                        " PID: " + cursor.getString(1) +
                        " CID: " + cursor.getString(3);
                cnt++;
                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        mDatabase.close();
        mDbHelper.close();

        if(choiceList != null){
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice,choiceList);
            tblSource.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            tblSource.setAdapter(adapter);

            tblSource.setOnItemClickListener(sourceListener);
        }

    }

    private class SourceClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String[] choiceList = null;
            sourceID = sourceIDs[i];
            patientID = patinetIDs[i];
            clinicID = clinicIDs[i];

            String queryString = "SELECT DISTINCT " +
                    " channel_id, channel_name, transducer_type, dimension " +
                    " from channel " +
                    " WHERE source_id = '"+ sourceIDs[i] +"'";

            OSADBHelper mDbHelper = new OSADBHelper(getApplication());
            SQLiteDatabase mDatabase = mDbHelper.getReadableDatabase();

            try {
                Cursor cursor = mDatabase.rawQuery(queryString, null);
                cursor.moveToFirst();

                choiceList = new String[cursor.getCount()];

                int cnt = 0;
                while (!cursor.isAfterLast()) {
                    choiceList[cnt] = "ID: "+cursor.getString(0) +" "+ cursor.getString(1) +" "+ cursor.getString(2) +" "+ cursor.getString(3);
                    cnt++;
                    cursor.moveToNext();
                }

                //close the cursor
                cursor.close();

            }catch (Exception e){
                System.out.println(e.getMessage());
            }

            mDatabase.close();
            mDbHelper.close();

            if(choiceList != null){
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(), android.R.layout.simple_list_item_checked, choiceList);
                tblChannel.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                tblChannel.setAdapter(adapter);
                btnExport.setEnabled(true);
            }
        }
    }


    private class ExportSourceToEDF extends Thread{
        private final String source_id;
        private final String patient_id;
        private final String clinic_id;
        private final String[] channelIDS;
        private final String fileName;
        private final int NR_OF_DR_BUFF = 50;
        private final Context context;
        private final int totalDatarecord;
        private RandomAccessFile raf;

        ExportSourceToEDF(String source_ID, String patient_ID, String clinic_ID, String[] channelIDS, String fileName){
            this.source_id = source_ID;
            this.patient_id = patient_ID;
            this.clinic_id = clinic_ID;
            this.channelIDS = channelIDS;
            this.fileName = fileName;
            this.context = getApplication();
            totalDatarecord = numberOfDataRecord();
        }

        @Override
        public void run(){
            try{
                raf = new RandomAccessFile(this.fileName, "rw");
                buildEDFheader();
                storeDataRecord();
                raf.close();
            }catch (Exception e){
                e.printStackTrace();
            }

            System.out.println("----> FINISHED WRITE TO FILE "+this.fileName);
        }

        private void buildEDFheader() throws IOException{
            EDFHeader edfHeader = new EDFHeader();

            SensorSourceAdapter sourceAdapter = new SensorSourceAdapter(this.context);
            SensorSource sensorSource = sourceAdapter.getSensorSourceById(this.source_id);
            sourceAdapter.close();

            ClinicAdapter clinicAdapter = new ClinicAdapter(this.context);
            Clinic clinic = clinicAdapter.getClinicById(this.clinic_id);
            clinicAdapter.close();

            PersonAdapter personAdapter = new PersonAdapter(this.context);
            Patient patient = personAdapter.getPatientById(this.patient_id);
            personAdapter.close();
            String localPatientID = (patient.getPatient_code_in_clinic().equals("") ? "X" : patient.getPatient_code_in_clinic()) + " "+
                    (patient.getGender().equals("") ? "X" : patient.getGender()) +" "+
                    (patient.getDateOfBirth().equals("") ? "X" : patient.getDateOfBirth()) +" "+
                    ((patient.getFirstName()+" "+patient.getLastName()).equals("") ? "X" : (patient.getFirstName()+" "+patient.getLastName()));
            String localrecordingID = "Startdate" + " X "+(clinic.getClinic_CODE().equals("") ? "X" : clinic.getClinic_CODE())+ " " +
                    (clinic.getTechnician_ID().equals("") ? "X":clinic.getClinic_CODE())+ " " +
                    (clinic.getUsed_equipment().equals("") ? "X" : clinic.getUsed_equipment());

            Channel[] channels = getChannelList();
            int numberOfChannels = nrOfChannels();

            edfHeader.setVersion("0");
            edfHeader.setPatientInfo(localPatientID);
            edfHeader.setClinicInfo(localrecordingID);
            edfHeader.setStartDate(sensorSource.getCreatedDate());
            edfHeader.setStartTime(sensorSource.getCreatedTime());
            edfHeader.setBytesInHeader(numberOfByteInHeader());
            edfHeader.setReservedFormat(new String(sensorSource.getReserved()));
            edfHeader.setNumberOfRecords(totalDatarecord);
            edfHeader.setDurationOfRecords(sensorSource.getData_record_duration());
            edfHeader.setNumberOfChannels(numberOfChannels);

            String[] channelLabels = new String[numberOfChannels];
            String[] transducerTypes = new String[numberOfChannels];
            String[] dimensions = new String[numberOfChannels];
            Double[] minInUnits = new Double[numberOfChannels];
            Double[] maxInUnits = new Double[numberOfChannels];
            Integer[] digitalMin = new Integer[numberOfChannels];
            Integer[] digitalMax = new Integer[numberOfChannels];
            String[] prefilterings = new String[numberOfChannels];
            Integer[] numberOfSamples = new Integer[numberOfChannels];
            byte[][] reserveds = new byte[numberOfChannels][];

            for (int i = 0; i < channels.length; i++) {
                channelLabels[i] = channels[i].getChannel_name();
                transducerTypes[i] = channels[i].getTransducer_type();
                dimensions[i] = channels[i].getPhysical_dimension();
                minInUnits[i] = channels[i].getPhysical_min();
                maxInUnits[i] = channels[i].getPhysical_max();
                digitalMin[i] = channels[i].getDigital_min();
                digitalMax[i] = channels[i].getDigital_max();
                prefilterings[i] = channels[i].getPrefiltering();
                numberOfSamples[i] = 100;//channels[i].getNumberSampleEDF();
                reserveds[i] = channels[i].getReserved();
                //System.out.println(channelLabels[i]+" "+ transducerTypes[i]
                // +" "+dimensions[i]+" "+minInUnits[i]+" "+maxInUnits[i]+" "+digitalMin[i]
                // +" "+digitalMax[i]+" "+prefilterings[i]);
            }

            edfHeader.setChannelLabels(channelLabels);
            edfHeader.setTransducerTypes(transducerTypes);
            edfHeader.setDimensions(dimensions);
            edfHeader.setMinInUnits(minInUnits);
            edfHeader.setMaxInUnits(maxInUnits);
            edfHeader.setDigitalMin(digitalMin);
            edfHeader.setDigitalMax(digitalMax);
            edfHeader.setPrefilterings(prefilterings);
            edfHeader.setNumberOfSamples(numberOfSamples);
            edfHeader.setReserveds(reserveds);

            EDFWriter.writeEDFHeaderToFile(raf,edfHeader);
        }

        private void storeDataRecord() throws IOException{
            Record[] records = null;
            int numberOfDRecord = totalDatarecord;
            int skip = 0;

            while(numberOfDRecord > 0){
                int arrayBuff = numberOfDRecord/NR_OF_DR_BUFF;
                if(arrayBuff == 0){
                    records = new Record[numberOfDRecord%NR_OF_DR_BUFF];
                }else{
                    records = new Record[NR_OF_DR_BUFF];
                }


                //initial data record list
                for(int i = 0; i < records.length; i++){
                    records[i] = new Record(0,this.source_id, this.patient_id, this.clinic_id,0);
                    records[i].initSampleSet(channelIDS);
                }

                //number of bytes for this record
                int nrofBytesRecord = 0;
                //get the data record for each channel
                for (int i = 0; i < channelIDS.length; i++) {
                    if(channelIDS[i] != null){
                        String query_s = "SELECT dr_id, sample_data " +
                                " FROM sampleset " +
                                " WHERE source_id = '"+this.source_id+"' AND " +
                                " channel_id = '"+this.channelIDS[i]+"' AND " +
                                " patient_id = '"+this.patient_id+"' AND " +
                                " clinic_id = '"+this.clinic_id+"' " +
                                " ORDER BY dr_id " +
                                " LIMIT "+skip+","+ records.length+"";

                        //Open readDB and read DATA RECORD for this channel
                        OSADBHelper mDbHelper = new OSADBHelper(getApplication());
                        SQLiteDatabase mDatabase = mDbHelper.getReadableDatabase();
                        try {
                            Cursor cursor = mDatabase.rawQuery(query_s, null);
                            //System.out.println(totalDatarecord+"----"+skip+"-----"+dataRecords.length+"-------> "+channelIDS[i]+ " " +DatabaseUtils.dumpCursorToString(cursor));
                            if (cursor.getCount() != 0) {
                                cursor.moveToFirst();
                                while (!cursor.isAfterLast()){
                                    int record_NR = cursor.getInt(0);
                                    byte[] sample_data = cursor.getBlob(1);
                                    nrofBytesRecord += sample_data.length;
                                    records[record_NR-skip].setData_record_ID(record_NR);
                                    records[record_NR-skip].addSampleSetsToList(channelIDS[i],sample_data);
                                    cursor.moveToNext();
                                }
                            }
                            cursor.close();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        mDatabase.close();
                        mDbHelper.close();
                    }
                }


                numberOfDRecord = numberOfDRecord-NR_OF_DR_BUFF;
                skip += records.length;
                EDFWriter.writeDatarecordsToEDF(raf, records, nrofBytesRecord);
            }
        }

        private int numberOfByteInHeader(){
            return  EDFElementSize.VERSION_SIZE+
                    EDFElementSize.PATIENT_INFO_SIZE+
                    EDFElementSize.CLINIC_INFO_SIZE+
                    EDFElementSize.START_DATE_SIZE+
                    EDFElementSize.START_TIME_SIZE+
                    EDFElementSize.HEADER_SIZE+
                    EDFElementSize.RESERVED_FORMAT_SIZE+
                    EDFElementSize.DURATION_DATA_RECORDS_SIZE+
                    EDFElementSize.NUMBER_OF_DATA_RECORDS_SIZE+
                    EDFElementSize.NUMBER_OF_CHANNELS_SIZE+
                    + nrOfChannels()*(EDFElementSize.LABEL_OF_CHANNEL_SIZE+
                                    EDFElementSize.TRANSDUCER_TYPE_SIZE+
                                    EDFElementSize.PHYSICAL_DIMENSION_OF_CHANNEL_SIZE+
                                    EDFElementSize.PHYSICAL_MIN_IN_UNITS_SIZE+
                                    EDFElementSize.PHYSICAL_MAX_IN_UNITS_SIZE+
                                    EDFElementSize.DIGITAL_MIN_SIZE+
                                    EDFElementSize.DIGITAL_MAX_SIZE+
                                    EDFElementSize.PREFILTERING_SIZE+
                                    EDFElementSize.NUMBER_OF_SAMPLES_SIZE+
                                    EDFElementSize.RESERVED_SIZE);
        }

        private int numberOfDataRecord(){
            String query_s = "SELECT COUNT(DISTINCT data_record.dr_id) as r_ID " +
                    " from data_record " +
                    " JOIN sensor_source ON sensor_source.source_id = data_record.source_id " +
                    " JOIN patient ON patient.patient_id = data_record.patient_id " +
                    " JOIN clinic ON clinic.clinic_id = data_record.clinic_id " +
                    " where sensor_source.source_id = '"+this.source_id+"' " +
                    " OR patient.last_name = '"+this.patient_id+"' " +
                    " OR clinic.clinic_code = '"+this.clinic_id+"' ";

            int nrR = 0;
            OSADBHelper mDbHelper = new OSADBHelper(getApplication());
            SQLiteDatabase mDatabase = mDbHelper.getReadableDatabase();
            try {
                Cursor cursor = mDatabase.rawQuery(query_s, null);
                //System.out.println(DatabaseUtils.dumpCursorToString(cursor));
                cursor.moveToFirst();
                nrR = cursor.getInt(0);
                cursor.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            mDatabase.close();
            mDbHelper.close();

            return nrR;
        }

        private int nrOfChannels(){
            int numberOfChannels = 0;
            for(String s : channelIDS) if(s != null) numberOfChannels++;
            return numberOfChannels;
        }

        private Channel[] getChannelList(){
            Channel[] channels = new Channel[nrOfChannels()];
            ChannelAdapter channelAdapter = new ChannelAdapter(this.context,false);
            int j = 0;
            for(int i = 0; i < channelIDS.length; i++){
                if(channelIDS[i] != null){
                    channels[j++] = channelAdapter.getChannelById(channelIDS[i],this.source_id);
                }
            }
            channelAdapter.close();

            return channels;
        }
    }

    */

}
