package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.SampleAdapter;
import no.uio.ifi.viettt.mscosa.SensorsObjects.Sample;
import no.uio.ifi.viettt.mscosa.Services.Constants;
import no.uio.ifi.viettt.mscosa.Services.ForegroundDBService;

/**
 * Created by viettt on 07/02/2017.
 */

public class UpdateDBThread extends Thread{
    public ForegroundDBService foregroundDBService;
    public boolean isBinded = false, firsttime = false;

    private long usedTimeForSQL = 0, totalInserttion = 0;
    private Context context;
    private boolean stop = false;
    private final Object lock = new Object();
    private ArrayList<ArrayList<Sample>> bufferList = new ArrayList<>();
    String fileName;

    public UpdateDBThread(Context context){
        this.context = context;
        File path = new File(Environment.getExternalStorageDirectory().getPath()+"/Download/");
        if(!path.exists()) path.mkdir();
        path.setReadable(true);
        path.setWritable(true);
        fileName = path.getPath()+"/"+"UsageTime30Seconds.txt";

        Intent i = new Intent(context, ForegroundDBService.class);
        if(isBinded) return;
        i.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        context.startService(i);
        context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void run() {
        while(!stop || !bufferList.isEmpty()){
            ArrayList<Sample> aSamplesBuff = getAFragment();
            System.out.println("buffer waiting in list: "+bufferList.size()+", curr size: "+aSamplesBuff.size());
            if(aSamplesBuff != null){
                totalInserttion += aSamplesBuff.size();
                long begintrans = System.currentTimeMillis();
                //SampleAdapter sampleAdapter = new SampleAdapter(context);
                //sampleAdapter.saveSampleToDB(aSamplesBuff);
                //sampleAdapter.close();
                foregroundDBService.manageIsStoring(getId(),aSamplesBuff);
                usedTimeForSQL += (System.currentTimeMillis() - begintrans);
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

        try{
            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
            pw.println("total used time: "+usedTimeForSQL+" ms. Total insertion: "+totalInserttion);
            pw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        unBoundDBService();
    }

    public long getUsedTimeForSQL(){
        return usedTimeForSQL;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ForegroundDBService.DBService dbService = (ForegroundDBService.DBService)iBinder;
            foregroundDBService = dbService.getService();
            isBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBinded = false;
        }
    };

    public void unBoundDBService(){
        if(isBinded){
            //context.unbindService(mConnection);
            isBinded = false;
        }
    }
}
