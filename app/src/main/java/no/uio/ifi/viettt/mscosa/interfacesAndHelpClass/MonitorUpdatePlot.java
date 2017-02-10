package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

/**
 * Created by viettt on 07/02/2017.
 */

public class MonitorUpdatePlot {

    private ABITalinoData abiTalinoData;
    private boolean stopUpdateThread = false;

    public synchronized void addSample(ABITalinoData abiTalinoData){
        if (stopUpdateThread) {
            notify();
            return;
        }
        while(this.abiTalinoData != null && !stopUpdateThread){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.abiTalinoData = abiTalinoData;
        notify();

    }

    public synchronized ABITalinoData getSample(){
        if (stopUpdateThread) {
            notify();
            return null;
        }
        while (this.abiTalinoData == null && !stopUpdateThread){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ABITalinoData tmp = this.abiTalinoData;
        abiTalinoData = null;
        notify();

        return tmp;
    }

    public synchronized void setStopUpdateThread(boolean status){
        this.stopUpdateThread = status;
    }
}
