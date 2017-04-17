package com.sensordroid.bitalino.Handlers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;
import com.sensordroid.bitalino.SettingsActivity;
import com.sensordroid.IMainServiceConnection;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CommunicationHandler implements Runnable {
    private static final String TAG = CommunicationHandler.class.toString();

    //private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final IMainServiceConnection binder;
    private Context context;

    // Get these values from shared preferences
    private static int[] typeList;
    private static int[] channelList;
    private static final int FRAMES_TO_READ = 1;
    private static int SAMPLING_FREQ = 1;
    private static int driverId;
    private static String driverName;
    private static boolean interrupted;

    private BluetoothSocket mSocket;
    private BITalinoDevice bitalino;

    public CommunicationHandler(final IMainServiceConnection binder, String name, int id, Context context) {
        this.driverId = id;
        this.driverName = name;
        this.binder = binder;
        this.context = context;

        this.bitalino = null;
        this.mSocket = null;
        this.interrupted = false;

        // Get the sampling frequency from the shared preferences.
        int tmpFreq = context.getSharedPreferences(SettingsActivity.sharedKey,
                Context.MODE_PRIVATE).getInt(SettingsActivity.frequencyKey, 0);
        if (tmpFreq < 17) {
            SAMPLING_FREQ = 1;
        } else if (tmpFreq < 50) {
            SAMPLING_FREQ = 10;
        } else if (tmpFreq< 83) {
            SAMPLING_FREQ = 100;
        } else {
            SAMPLING_FREQ = 1000;
        }
    }


    @Override
    public void run() {
        sendMetadata();

        int sleepTime = 1000;
        while (!interrupted) {
            if (Thread.currentThread().isInterrupted()) {
                interrupted = true;
                break;
            }
            if (connect()) {
                Log.d("Run()", "connection successfull");
                sleepTime = 1000;
                // Start acquisition of predefined channels
                collectData();
            }
            resetConnection();
            if (!interrupted) {
                try {
                    Thread.sleep(sleepTime);
                    if (sleepTime < 30000)
                        sleepTime = sleepTime * 2;
                } catch (InterruptedException ie) {
                    interrupted = true;
                    ie.printStackTrace();
                    return;
                }
            }
        }
    }

    public void sendMetadata() {
        // Get types of channels from shared preferences
        String savedString = context.getSharedPreferences(SettingsActivity.sharedKey,
                Context.MODE_PRIVATE).getString(SettingsActivity.channelKey, "0,0,0,0,0,0");
        StringTokenizer st = new StringTokenizer(savedString, ",");

        // Create list of the data types
        int active_channels = 0;
        typeList = new int[SettingsActivity.NUM_CHANNELS];
        for (int i = 0; i < typeList.length; i++) {
            typeList[i] = Integer.parseInt(st.nextToken());
            if (typeList[i] != 0) {
                active_channels++;
            }
        }

        // Create a list of the active channels
        channelList = new int[active_channels];
        int index = 0;
        for (int i = 0; i < typeList.length; i++) {
            if (typeList[i] != 0) {
                channelList[index++] = i;
            }
        }
        executor.submit(new MetadataHandler(binder, driverName, driverId, context, typeList));
    }

    /*
        Collects data from the bitalino until the thread is interrupted.
     */
    private void collectData(){
        try {
            bitalino.start();

            while (!interrupted) {
                if (Thread.currentThread().isInterrupted()) {
                    interrupted = true;
                    return;
                }
                final BITalinoFrame[] frames;
                frames = bitalino.read(FRAMES_TO_READ);

                for (final BITalinoFrame frame : frames) {
                    // Pass the BITalinoFrame to a worker thread
                    executor.submit(new DataHandler(binder, frame, driverId, typeList, channelList));
                }
            }
        } catch (BITalinoException be) {
            if (Thread.currentThread().isInterrupted()){
                interrupted = true;
            }
            be.printStackTrace();
            return;
        }
    }

    /*
        Connects to the bitalino via bluetooth
    */
    public boolean connect(){
        // Connect to the bluetooth device
        String remoteDevice = context.getSharedPreferences(SettingsActivity.sharedKey,
                Context.MODE_PRIVATE).getString(SettingsActivity.macKey, "98:D3:31:B2:BB:A5");

        final BluetoothAdapter blueAdapt = BluetoothAdapter.getDefaultAdapter();
        final BluetoothDevice dev = blueAdapt.getRemoteDevice(remoteDevice);


        if (!blueAdapt.isEnabled()){
            blueAdapt.enable();
            Handler h = new Handler(context.getMainLooper());

            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Turning on bluetooth", Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }
        ParcelUuid[] uuidParcel = dev.getUuids();

        boolean connected = false;
        for (ParcelUuid uuid : uuidParcel) {
            BluetoothSocket tmp;
            try {
                tmp = dev.createInsecureRfcommSocketToServiceRecord(uuid.getUuid());
            } catch (IOException ioe){
                ioe.printStackTrace();
                continue;
            }
            mSocket = tmp;

            blueAdapt.cancelDiscovery();
            try {
                mSocket.connect();
                connected = true;
                break;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            if (Thread.currentThread().isInterrupted()){
                interrupted = true;
                return false;
            }
        }

        if(!connected){
            // Could not connect to the bluetooth device.
            return false;
        }

        Log.d(TAG, "Connecting to BITalino");

        // Creating a new bitalino device.
        try {
            bitalino = new BITalinoDevice(SAMPLING_FREQ, channelList);
            bitalino.open(mSocket.getInputStream(), mSocket.getOutputStream());
        } catch (BITalinoException be) {
            be.printStackTrace();
            return false;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
        return true;
    }

    /**
        Close the bitalino and the bluetooth connection
     */
    private void resetConnection(){
        if(bitalino != null){
            try {
                bitalino.stop();
                bitalino = null;
                Log.d(TAG, "Bitalino is stopped");
            } catch (BITalinoException e) {
                e.printStackTrace();
            }
        }

        if (mSocket != null){
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
