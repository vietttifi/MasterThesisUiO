package com.sensordroid.bitalino.Handlers;

import android.os.RemoteException;
import android.util.Log;

import com.bitalino.comm.BITalinoFrame;
import com.bitalino.util.SensorDataConverter;
import com.sensordroid.IMainServiceConnection;
import com.sensordroid.bitalino.util.BitalinoTransfer;
import com.sensordroid.bitalino.util.JSONHelper;

public class DataHandler implements Runnable {
    private final IMainServiceConnection binder;
    private final BITalinoFrame frame;
    private final int id;
    private final int[] typeList;
    private final int[] channelList;

    public DataHandler(final IMainServiceConnection binder, final BITalinoFrame frame,
                       final int id, final int[] typeList, final int[] channelList){
        this.id = id;
        this.binder = binder;
        this.frame = frame;
        this.typeList = typeList;
        this.channelList = channelList;
    }

    @Override
    public void run() {
        try {
            Double[] data = new Double[channelList.length];
            int index = 0;
            for (int type : typeList){
                if (index >= channelList.length)
                    break;

                // Two last channels got 6 bit resolution

                switch (type) {
                    case BitalinoTransfer.TYPE_RAW:
                        data[index] = Double.valueOf(frame.getAnalog(channelList[index++]));
                        break;
                    case BitalinoTransfer.TYPE_LUX:
                        data[index] = SensorDataConverter.scaleLuminosity(channelList[index], frame.getAnalog(channelList[index++]));
                        break;
                    case BitalinoTransfer.TYPE_ACC:
                        data[index] = SensorDataConverter.scaleAccelerometer(channelList[index], frame.getAnalog(channelList[index++]));
                        break;
                    case BitalinoTransfer.TYPE_PZT:
                        data[index] = SensorDataConverter.scalePZT(channelList[index], frame.getAnalog(channelList[index++]));
                        break;
                    case BitalinoTransfer.TYPE_ECG:
                        data[index] = SensorDataConverter.scaleECG(channelList[index], frame.getAnalog(channelList[index++]));
                        break;
                    case BitalinoTransfer.TYPE_EEG:
                        data[index] = SensorDataConverter.scaleEEG(channelList[index], frame.getAnalog(channelList[index++]));
                        break;
                    case BitalinoTransfer.TYPE_EDA:
                        data[index] = SensorDataConverter.scaleEDA(channelList[index], frame.getAnalog(channelList[index++]));
                        break;
                    case BitalinoTransfer.TYPE_EMG:
                        data[index] = SensorDataConverter.scaleEMG(channelList[index], frame.getAnalog(channelList[index++]));
                        break;
                    case BitalinoTransfer.TYPE_TMP:
                        data[index] = SensorDataConverter.scaleTMP(channelList[index], frame.getAnalog(channelList[index++]), true);
                        break;
                }
            }

            // Construct and send JSON-string
            String sendString = JSONHelper.construct(id, channelList, data).toString();
            binder.putJson(sendString);
        } catch (RemoteException re) {
            re.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
