package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;

import no.uio.ifi.viettt.mscosa.MainFragments.PlotViewFragment;

/**
 * Created by viettt on 07/02/2017.
 */

public class UpdatePlotThread implements Runnable{

    private PlotViewFragment plotViewFragment;
    private boolean stop = false;
    private ABITalinoData abiTalinoData;
    private final Object lock = new Object();

    public UpdatePlotThread(PlotViewFragment plotViewFragment){
        this.plotViewFragment = plotViewFragment;
    }

    @Override
    public void run() {
        int cnt = 0;
        do{
            synchronized (lock) {
                while (abiTalinoData == null) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (String channel_id : abiTalinoData.getData().keySet()) {
                    if (!stop)
                        plotViewFragment.addNewSample(channel_id,
                                Float.parseFloat(abiTalinoData.getData().get(channel_id)), abiTalinoData.getTime(), cnt);
                }
                cnt++;
                abiTalinoData = null;
            }
        }while(!stop);
        plotViewFragment = null;
        abiTalinoData = null;
    }

    public void updateSamples(ABITalinoData abiTalinoData){
        synchronized (lock){
            this.abiTalinoData = abiTalinoData;
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
