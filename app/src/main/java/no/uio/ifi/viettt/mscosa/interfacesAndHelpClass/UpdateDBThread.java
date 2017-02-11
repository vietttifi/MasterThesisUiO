package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.DataRecordAdapter;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleSetAdapter;
import no.uio.ifi.viettt.mscosa.SensorsObjects.DataRecord;

/**
 * Created by viettt on 07/02/2017.
 */

public class UpdateDBThread implements Runnable{

    private DataRecordAdapter dataRecordAdapter;
    private DataRecord dataRecord;
    private Context context;
    private boolean stop = false;
    private final Object lock = new Object();

    public UpdateDBThread(Context context){
        this.context = context;
    }

    @Override
    public void run() {
        do{
            synchronized (lock){
                while (dataRecord == null) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                storeDataRecord();
                storeSampleSet();
                dataRecord = null;
            }
        }while(!stop);

    }

    private void storeDataRecord(){
        dataRecordAdapter = new DataRecordAdapter(this.context);
        dataRecordAdapter.saveRecordToDB(dataRecord.getData_record_ID(),dataRecord.getSource_ID(),
                dataRecord.getPatient_ID(),dataRecord.getClinic_ID(),dataRecord.getCreated_date(),
                dataRecord.getExperiments(),dataRecord.getDescriptions(),dataRecord.getMax_sample());
        dataRecordAdapter.close();
    }

    private void storeSampleSet(){
        SampleSetAdapter sampleSetAdapter = new SampleSetAdapter(this.context);
        //Transaction will be used here
        sampleSetAdapter.saveSampleToDB(dataRecord.getSampleSetList());

        sampleSetAdapter.close();
    }

    public void updateDataRecord(DataRecord dataRecord){
        synchronized (lock){
            this.dataRecord = dataRecord;
            lock.notify();
        }
    }

    public void stopThread(){
        this.stop = true;
        synchronized (lock) {
            lock.notify();
        }
    }
}
