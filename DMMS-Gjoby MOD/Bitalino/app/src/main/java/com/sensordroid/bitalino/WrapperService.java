package com.sensordroid.bitalino;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sensordroid.bitalino.Handlers.CommunicationHandler;
import com.sensordroid.IMainServiceConnection;

/**
 * Created by SveinPetter on 04/11/2015.
 */
public class WrapperService extends Service {
    private static final String TAG = "TestService";
    public static final String START_ACTION = "com.sensordroid.START";
    public static final String STOP_ACTION = "com.sensordroid.STOP";
    public static final String name = "BITalino";

    public static int driverId;
    public static IntentFilter filter;

    private MainServiceConnection serviceConnection;
    private static IMainServiceConnection binder;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate called");
        serviceConnection = new MainServiceConnection();
        binder = null;
        driverId = -1;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
        Called when startService is called from the broadcast receivers.
            * The intent contains information about the action to execute
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null) {
            String action = intent.getStringExtra("ACTION");
            Log.d("onStartCommand", "action: " + action + " binder: " + (binder == null));

            if (action.compareTo(START_ACTION) == 0) {
                driverId = intent.getIntExtra("DRIVER_ID", -1);
                start(intent.getStringExtra("SERVICE_ACTION"),
                        intent.getStringExtra("SERVICE_PACKAGE"),
                        intent.getStringExtra("SERVICE_NAME"));
            } else if(action.compareTo(STOP_ACTION) == 0) {
                stop();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
        Starts the data acquisition
            * Set the process to a foreground process
            * Bind to remote service
     */
    public void start(String action, String pack, String name) {
        Log.d("bind service", getApplicationContext().toString());
        if(binder == null) {
            toForeground();
            Intent service = new Intent(action);
            service.setComponent(new ComponentName(pack, name));
            getApplicationContext().bindService(service, serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    /**
        Stops the data acquisition,
            * Interrupts working thread
            * Unbinds from the remote service
            * Change the service from foreground
     */
    public void stop() {
        if(binder != null) {
            try {
                serviceConnection.interruptThread();
                getApplicationContext().unbindService(serviceConnection);
                binder = null;
                stopForeground(true);
            } catch (IllegalArgumentException iae){
                iae.printStackTrace();
            }
        }
    }

    /**
        Sets the current service to the foreground
     */
    public void toForeground() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.stat_notify_chat);
        builder.setContentTitle(WrapperService.name);
        builder.setTicker("Starting data collecting");
        builder.setContentText("Collecting data");

        Intent i = new Intent(this, WrapperService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        builder.setContentIntent(pi);

        final Notification note = builder.build();

        startForeground(android.os.Process.myPid(), note);
    }

    private class MainServiceConnection implements ServiceConnection {
        private Thread connectionThread;

        PowerManager powerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, name + "WakeLock");

        /*
            Called when the service is connected,
                * Starts the working thread and acquires the wakelock
         */
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = IMainServiceConnection.Stub.asInterface(iBinder);
            connectionThread = new Thread(new CommunicationHandler(binder, name, driverId, getApplicationContext()));
            connectionThread.start();

            if(!wakeLock.isHeld()){
                Log.d("WakeLock", "Acquire");
                wakeLock.acquire();
            }
        }

        /*
            Called if the service is unexpectedly disconnected
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("onServiceDisconnected", "called");
            interruptThread();
        }

        /*
            Releases wakelock and interrupts the working thread
         */
        public void interruptThread() {
            if(wakeLock.isHeld()){
                Log.d("WakeLock", "Release");
                wakeLock.release();
            }
            if (connectionThread != null) {
                connectionThread.interrupt();
                connectionThread = null;
            }
        }
    }
}
