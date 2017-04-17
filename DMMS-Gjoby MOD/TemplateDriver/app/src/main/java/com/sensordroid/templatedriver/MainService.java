package com.sensordroid.templatedriver;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.sensordroid.IMainServiceConnection;
import com.sensordroid.templatedriver.Handlers.CommunicationHandler;

public class MainService extends Service {
    public static final String START_ACTION = "com.sensordroid.START";
    public static final String STOP_ACTION = "com.sensordroid.STOP";

    /*
        TODO: Change the name to the name of your driver
            - This name is the name used in the data packets and when the driver is listed in the main application
     */
    public static final String name = R.string.app_name;

    private static int id;
    private static IMainServiceConnection binder;
    private MainServiceConnection serviceConnection;

    @Override
    public void onCreate(){
        this.serviceConnection = new MainServiceConnection();
        this.id = -1;
        this.binder = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
        Called when started by intent, as from StartReceiver and StopReceiver
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        String action = intent.getStringExtra("ACTION");
        switch (action) {
            case START_ACTION:
                this.id = intent.getIntExtra("DRIVER_ID", 0);
                start(intent.getStringExtra("SERVICE_ACTION"),
                        intent.getStringExtra("SERVICE_PACKAGE"),
                        intent.getStringExtra("SERVICE_NAME"));
            case STOP_ACTION:
                stop();
            default:
                Log.d("onStartCommand", "Illegal action string");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /*
        Start data acquisition by binding to the Collector application
            - binds to service_name, which is located in service_package and is listening for service_action
     */
    public void start(String service_action, String service_package, String service_name) {
        Log.d("bind service", getApplicationContext().toString());
        if(binder == null) {
            Intent service = new Intent(service_action);
            service.setComponent(new ComponentName(service_package, service_name));
            getApplicationContext().bindService(service, serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    /*
        Stop data acquisition, interrupt thread and unbind
     */
    public void stop() {
        if(binder != null) {
            try {
                serviceConnection.interruptThread();
                getApplicationContext().unbindService(serviceConnection);
            } catch (IllegalArgumentException iae){
                iae.printStackTrace();
            }
            stopSelf();
        }
    }

    private class MainServiceConnection implements ServiceConnection {
        private Thread connectionThread;

        public void interruptThread() {
            /* Interrupt thread*/
            if (connectionThread != null) {
                connectionThread.interrupt();
                connectionThread = null;
            }
            binder = null;
        }

        /*
            Called when the service is bound successfully
        */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = IMainServiceConnection.Stub.asInterface(iBinder);

            // Starts the thread for communication with the device
            connectionThread = new Thread(new CommunicationHandler(binder, name, id, getApplicationContext()));
            connectionThread.start();
        }

        /*
            Called if the service unbound unexpectedly
        */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            interruptThread();
        }
    }
}
