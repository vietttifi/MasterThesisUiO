package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;
import java.util.ArrayList;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleAdapter;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;

/**
 * Created by viettt on 07/02/2017.
 */

public class UpdateDBThread extends Thread{

    private Context context;
    private boolean stop = false;
    private final Object lock = new Object();
    private ArrayList<ArrayList<Sample>> bufferList = new ArrayList<>();

    public UpdateDBThread(Context context){
        this.context = context;
    }

    @Override
    public void run() {
        while(!stop || !bufferList.isEmpty()){
            ArrayList<Sample> aSamplesBuff = getAFragment();
            System.out.println("buffer waiting in list: "+bufferList.size());
            if(aSamplesBuff != null){
                SampleAdapter sampleAdapter = new SampleAdapter(context);
                sampleAdapter.saveSampleToDB(aSamplesBuff);
                sampleAdapter.close();
            }
        }
    }

    public void requestDataBaseSaving(ArrayList<Sample> samplesBuff){
        synchronized (lock){
            bufferList.add(samplesBuff);
            lock.notify();
        }
    }

    private ArrayList<Sample> getAFragment(){
        ArrayList<Sample> ret = null;
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
