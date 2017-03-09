package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;
import java.util.ArrayList;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleAdapter;
import no.uio.ifi.viettt.mscosa.SensorsObjects.RecordFragment;

/**
 * Created by viettt on 07/02/2017.
 */

public class UpdateDBThread extends Thread{

    private Context context;
    private boolean stop = false;
    private final Object lock = new Object();
    private ArrayList<RecordFragment> bufferList = new ArrayList<>();

    public UpdateDBThread(Context context){
        this.context = context;
    }

    @Override
    public void run() {
        while(!stop || !bufferList.isEmpty()){
            RecordFragment recordFragment = getAFragment();
            if(recordFragment != null){
                SampleAdapter sampleAdapter = new SampleAdapter(context);
                sampleAdapter.saveSampleToDB(recordFragment.getSamples_In_The_Same_Fragment());
                sampleAdapter.close();
            }
        }
    }

    public void requestDataBaseSaving(RecordFragment recordFragment){
        synchronized (lock){
            bufferList.add(recordFragment);
            lock.notify();
        }
    }

    private RecordFragment getAFragment(){
        RecordFragment ret = null;
        synchronized (lock){
            while(bufferList.isEmpty() && !stop){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(!bufferList.isEmpty()) ret = bufferList.remove(0);
        }
        return ret;
    }


    public void setStop(boolean stop) {
        synchronized (lock){
            this.stop = stop;
            lock.notifyAll();
        }
    }
}
