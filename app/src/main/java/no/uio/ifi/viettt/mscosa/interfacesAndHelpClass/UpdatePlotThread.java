package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;

import no.uio.ifi.viettt.mscosa.MainFragments.PlotViewFragment;

/**
 * Created by viettt on 07/02/2017.
 */

public class UpdatePlotThread implements Runnable{

    private MonitorUpdatePlot monitorUpdatePlot;
    private Context context;
    private PlotViewFragment plotViewFragment;
    private boolean stop = false;

    ABITalinoData abiTalinoData;

    public UpdatePlotThread(PlotViewFragment plotViewFragment, MonitorUpdatePlot monitorUpdatePlot, Context context){
        this.monitorUpdatePlot = monitorUpdatePlot;
        this.context = context;
        this.plotViewFragment = plotViewFragment;
    }

    @Override
    public void run() {
        int cnt = 0;
        do{
            abiTalinoData = monitorUpdatePlot.getSample();
            if(abiTalinoData == null) break;

            for(String channel_id : abiTalinoData.getData().keySet()){
                if(!stop)
                plotViewFragment.addNewSample(channel_id,
                        Float.parseFloat(abiTalinoData.getData().get(channel_id)),abiTalinoData.getTime(), cnt);
            }
            cnt++;
        }while(!stop);

        monitorUpdatePlot.setStopUpdateThread(true);
    }

    public void stopThread(){
        this.stop = true;
    }
}
