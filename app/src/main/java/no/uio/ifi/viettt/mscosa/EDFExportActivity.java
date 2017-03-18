package no.uio.ifi.viettt.mscosa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.ChannelAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADBHelper;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADataBaseManager;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.PersonAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.RecordAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleAdapter;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFElementSize;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFHeader;
import no.uio.ifi.viettt.mscosa.EDFManagement.EDFWriter;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Channel;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Patient;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Physician;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Record;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;


public class EDFExportActivity extends AppCompatActivity{

    RadioButton rdSource, rdPatient;
    EditText searchText;
    Button btnExport;
    ListView tblRecord;

    long[] chosenRecord;
    String[] chosenPatients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edf_export_fullscreen);

        searchText = (EditText)findViewById(R.id.txtSearch);
        tblRecord = (ListView)findViewById(R.id.tblRecord);
        btnExport = (Button)findViewById(R.id.btnExport);
        rdSource = (RadioButton)findViewById(R.id.rdBtnSource);
        rdPatient = (RadioButton)findViewById(R.id.rdBtnPatient);

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

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length()<2) return;
                manageKeySearch(charSequence,i,i1,i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnExport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                exportBtn();
            }
        });
    }

    private void manageRdSource(View view){
        manageKeySearch(searchText.getText(),0,0,0);
    }

    private void manageRdPatient(View view){
        manageKeySearch(searchText.getText(),0,0,0);
    }

    private void exportBtn(){

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplication());
        edittext.setText(String.valueOf(System.currentTimeMillis()/1000));
        edittext.setSelection(edittext.getText().length());
        alert.setTitle("Enter file name");
        alert.setView(edittext);
        alert.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                SparseBooleanArray checked = tblRecord.getCheckedItemPositions();
                ArrayList<String> exportedRecords = new ArrayList<String>();
                String firstPID = null;
                for (int i = 0; i < tblRecord.getAdapter().getCount(); i++) {
                    if (checked.get(i)) {
                        exportedRecords.add(String.valueOf(chosenRecord[i]));
                        if(firstPID == null) firstPID = chosenPatients[i];
                        else if(!firstPID.equals(chosenPatients[i])){
                            Toast.makeText(getApplication(),"PLEASE CHOOSE RECORD FOR THE SAME PATIENT!",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                if(exportedRecords.isEmpty()){
                    Toast.makeText(getApplication(),"PLEASE CHOOSE AT LEAST ONE RECORD!",Toast.LENGTH_SHORT).show();
                    return;
                }
                btnExport.setEnabled(false);
                tblRecord.setAdapter(null);
                
                File path = new File(Environment.getExternalStorageDirectory().getPath()+"/Download/");
                if(!path.exists()) path.mkdir();
                // fix
                path.setExecutable(true);
                path.setReadable(true);
                path.setWritable(true);

// initiate media scan and put the new things into the path array to
// make the scanner aware of the location and the files you want to see
                MediaScannerConnection.scanFile(getApplication(), new String[] {path.toString()}, null, null);
                String fileName = path.getPath()+"/"+edittext.getText().toString() + ".edf";
                Toast.makeText(getApplication(),"File save to: "+fileName,Toast.LENGTH_SHORT).show();
                //Start thread that will export data to EDF file.
                (new ExportSourceToEDF(exportedRecords,fileName, firstPID)).start();
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
        tblRecord.setAdapter(null);

        String queryString = "SELECT "
                + " R.r_id, R.p_owner as Patient, R.s_id as Source, C.ch_nr as C_NR, C.ch_name as C_Name, R.p_collect as Physician, R.timestamp "
                + " FROM RECORD R NATURAL JOIN CHANNEL C ";

        if(rdSource.isChecked()){
            queryString += " WHERE s_id like '%"+ charSequence.toString()+"%' ";
        } else {
            queryString += " WHERE p_owner like '%"+ charSequence.toString()+"%' ";
        }

        queryString += " ORDER BY R.p_owner, R.s_id, C.ch_nr, R.timestamp";

        OSADataBaseManager.initializeInstance(new OSADBHelper(getApplication()));
        OSADataBaseManager osaDataBaseManager = null;

        try {
            osaDataBaseManager = OSADataBaseManager.getInstance();

            SQLiteDatabase mDatabase = osaDataBaseManager.openDatabase();
            Cursor cursor = mDatabase.rawQuery(queryString, null);
            cursor.moveToFirst();

            choiceList = new String[cursor.getCount()];
            chosenRecord = new long[cursor.getCount()];
            chosenPatients = new String[cursor.getCount()];

            int cnt = 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z"); // the format of your date
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // give a timezone reference for formating (see comment at the bottom
            while (!cursor.isAfterLast()) {
                choiceList[cnt] = "R_ID: "+cursor.getLong(0) +"\nP_ID: "+ cursor.getString(1) +"\nSID: "+ cursor.getString(2)
                        +"\nCH_NR: "+ cursor.getInt(3)+"\nCH_NAME: "+cursor.getString(4) + "\nP_PHY: "+cursor.getString(5)+"\nDATE: "+sdf.format(cursor.getLong(6));
                chosenRecord[cnt] = cursor.getLong(0);
                chosenPatients[cnt] = cursor.getString(1);
                cnt++;
                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        if(osaDataBaseManager != null) osaDataBaseManager.closeDatabase();

        if(choiceList != null){
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplication(), R.layout.simple_list_checked, choiceList);
            tblRecord.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            tblRecord.setAdapter(adapter);
            btnExport.setEnabled(true);
        }
    }

    private class ExportSourceToEDF extends Thread{
        //5s, is 1000Hz source = 5000 samples = 10000bytes, it is not over the limit including the size limit of 61440
        private final int DURATION = 5;
        private final Record[] records;
        private final String fileName, patientID;
        private final Context context;
        private RandomAccessFile raf;
        private boolean isAnnotation = false;
        private EDFHeader edfHeader;

        ExportSourceToEDF(ArrayList<String> recordIDs, String fileName, String patientID){
            this.context = getApplication();
            this.patientID = patientID;
            this.fileName = fileName;
            RecordAdapter recordAdapter = new RecordAdapter(context);
            ArrayList<Record> recordList = recordAdapter.getListRecordByListIds(recordIDs);
            recordAdapter.close();
            records = new Record[recordList.size()];
            recordList.toArray(records);
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
                    + records.length*(EDFElementSize.LABEL_OF_CHANNEL_SIZE+
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

        private void buildEDFheader() throws IOException {
            edfHeader = new EDFHeader();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy"); // the format of your date
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // give a timezone reference for formating (see comment at the bottom

            PersonAdapter personAdapter = new PersonAdapter(this.context);
            Patient patient = personAdapter.getPatientByIds(this.patientID);
            personAdapter.close();

            String localPatientID = (patient.getPatient_id_in_clinic().equals("") ? "X" : patient.getPatient_id_in_clinic()) + " "+
                    ((patient.getGender() == null || patient.getGender().equals("")) ? "X" : patient.getGender()) +" "+
                    ((patient.getDayOfBirth() == null || patient.getDayOfBirth().equals("")) ? "X" : patient.getDayOfBirth()) +" "+
                    ((patient.getName() == null || patient.getName().equals("")) ? "X" : (patient.getName()));
            String localRecordingID = "Startdate" + " X "+(records[0].getTimestamp() == 0 ? "X" : sdf.format(records[0].getTimestamp()))+ " " +
                    ((records[0].getDescriptions() == null || records[0].getDescriptions().equals("")) ? "X": records[0].getDescriptions())+ " " +
                    ((records[0].getUsed_equip() == null || records[0].getUsed_equip().equals("")) ? "X" : records[0].getUsed_equip());

            List<String[]> channelsInfo = new ArrayList<>();
            long minTimeStamp = records[0].getTimestamp();
            for(int i = 0; i < records.length; i++){
                channelsInfo.add(new String[]{String.valueOf(records[i].getCh_nr()),records[i].getS_id()});
                if(minTimeStamp>records[i].getTimestamp()) minTimeStamp = records[i].getTimestamp();
            }
            ChannelAdapter channelAdapter = new ChannelAdapter(context);
            Channel channels[] = channelAdapter.getChannelsByListIds(channelsInfo);
            channelAdapter.close();

            SimpleDateFormat sdfStartDate = new SimpleDateFormat("dd.MM.yy"); // the format of your date
            sdfStartDate.setTimeZone(TimeZone.getTimeZone("UTC")); // give a timezone reference for formating (see comment at the bottom
            SimpleDateFormat sdfStartTime = new SimpleDateFormat("hh.mm.ss"); // the format of your date
            sdfStartTime.setTimeZone(TimeZone.getTimeZone("UTC")); // give a timezone reference for formating (see comment at the bottom

            int numberOfChannels = records.length;
            edfHeader.setVersion("0");
            edfHeader.setPatientInfo(localPatientID);
            edfHeader.setClinicInfo(localRecordingID);
            edfHeader.setStartDate(sdfStartDate.format(minTimeStamp));
            edfHeader.setStartTime(sdfStartTime.format(minTimeStamp));
            edfHeader.setBytesInHeader(numberOfByteInHeader());
            edfHeader.setReservedFormat(isAnnotation ? "EDF+C" : "");
            edfHeader.setNumberOfRecords(-1);
            edfHeader.setDurationOfRecords(DURATION);
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
                System.out.println(channels[i].getCh_nr()+" <----- ");
                channelLabels[i] = channels[i].getCh_name();
                transducerTypes[i] = channels[i].getTransducer();
                dimensions[i] = channels[i].getDimension();
                minInUnits[i] = channels[i].getPhy_min();
                maxInUnits[i] = channels[i].getPhy_max();
                digitalMin[i] = channels[i].getDig_min();
                digitalMax[i] = channels[i].getDig_max();
                prefilterings[i] = channels[i].getPrefiltering();
                numberOfSamples[i] = (int)(records[i].getFrequency()*DURATION);
                reserveds[i] = channels[i].getEdf_reserved();
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
            raf.seek(edfHeader.getBytesInHeader());

        }

        private void storeDataRecord() throws IOException{

            int dataRecordSize = 0;
            for(int i = 0; i<records.length; i++){
                dataRecordSize += edfHeader.getNumberOfSamples()[i];
            }

            System.out.println(dataRecordSize);
            SampleAdapter sampleAdapter = new SampleAdapter(getApplication());
            int dataRecordCnt = 1;
            boolean stop = false;
            while(!stop){
                ByteBuffer byteBuffer = ByteBuffer.allocate(dataRecordSize*2);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

                for(int i = 0; i<records.length; i++){
                    //dataRecordSize += edfHeader.getNumberOfSamples()[i];
                    short[] valuesRecord = sampleAdapter.getShortValues(records[i].getR_id(),(dataRecordCnt - 1)*edfHeader.getNumberOfSamples()[i],dataRecordCnt*edfHeader.getNumberOfSamples()[i]);
                    if(valuesRecord != null) {
                        //System.out.println("BUFF LENGTH "+byteBuffer.limit()+ ", values length"+valuesRecord.length);
                        for(int j = 0 ; j<valuesRecord.length; j++){
                            //System.out.println(valuesRecord[j]);
                            byteBuffer.putShort(valuesRecord[j]);
                        }
                    } else {
                        stop = true;
                    }
                }
                if(!stop){
                    raf.seek(edfHeader.getBytesInHeader()+(dataRecordCnt-1)*dataRecordSize*2);
                    System.out.println("A data record "+dataRecordCnt + " at "+(edfHeader.getBytesInHeader()+(dataRecordCnt-1)*dataRecordSize*2));
                    raf.write(byteBuffer.array());
                    dataRecordCnt++;
                }

            }
            sampleAdapter.close();
            System.out.println("NR OF DATA RECORD "+(dataRecordCnt-1));
            edfHeader.setNumberOfRecords(dataRecordCnt-1);
            //UPDATE TOTAL RECORD
            edfHeader.printHeader();
            raf.seek(0);
            EDFWriter.writeEDFHeaderToFile(raf,edfHeader);

        }

    }

}
