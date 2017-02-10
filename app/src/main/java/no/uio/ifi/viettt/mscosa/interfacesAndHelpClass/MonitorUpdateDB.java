package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import no.uio.ifi.viettt.mscosa.SensorsObjects.DataRecord;

/**
 * Created by viettt on 07/02/2017.
 */

public class MonitorUpdateDB {

    private DataRecord dataRecord;
    private boolean stopUpdateThread;

    public synchronized void addDateRecord(DataRecord dataRecord){
        while(this.dataRecord != null && !stopUpdateThread){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.dataRecord = dataRecord;
        notify();
    }

    public synchronized DataRecord getDataRecord(){
        while (dataRecord == null && !stopUpdateThread){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        DataRecord tmp = dataRecord;
        dataRecord = null;
        notify();
        return tmp;
    }

    public synchronized void setStopUpdateThread(boolean status){
        this.stopUpdateThread = status;
    }
}
