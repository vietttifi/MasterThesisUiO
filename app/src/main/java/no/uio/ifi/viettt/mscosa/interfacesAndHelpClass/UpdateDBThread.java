package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.DataRecordAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleSetAdapter;
import no.uio.ifi.viettt.mscosa.SensorsObjects.DataRecord;

/**
 * Created by viettt on 07/02/2017.
 */

public class UpdateDBThread implements Runnable{

    private MonitorUpdateDB monitorUpdateDB;
    private DataRecord dataRecord;
    private Context context;
    private boolean stop = false;

    public UpdateDBThread(MonitorUpdateDB monitorUpdateDB, Context context){
        this.monitorUpdateDB = monitorUpdateDB;
        this.context = context;
    }

    @Override
    public void run() {
        do{


            //monitor will wait until it has data record
            dataRecord = monitorUpdateDB.getDataRecord();
            if(dataRecord == null) break;

            storeDataRecord();
            storeSampleSet();

        }while(!stop);

        monitorUpdateDB.setStopUpdateThread(true);

    }

    private void storeDataRecord(){
        DataRecordAdapter dataRecordAdapter = new DataRecordAdapter(this.context);
        dataRecordAdapter.saveRecordToDB(dataRecord.getData_record_ID(),dataRecord.getSource_ID(),
                dataRecord.getPatient_ID(),dataRecord.getClinic_ID(),dataRecord.getCreated_date(),
                dataRecord.getExperiments(),dataRecord.getDescriptions(),dataRecord.getMax_sample());
        dataRecordAdapter.close();
    }

    private void storeSampleSet(){
        SampleSetAdapter sampleSetAdapter = new SampleSetAdapter(this.context);
        System.out.println("---> ADD sample to database");
        //Transaction will be used here
        sampleSetAdapter.saveSampleToDB(dataRecord.getSampleSetList());

        sampleSetAdapter.close();
    }

    public void stopThread(){
        this.stop = true;
    }
}
