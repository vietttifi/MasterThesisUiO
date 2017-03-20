package com.sensordroid;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.sensordroid.Activities.ConfigurationActivity;
import com.sensordroid.Handlers.DispatchFileHandler;
import com.sensordroid.Handlers.DispatchTCPHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteService extends Service {
    private static final String TAG = "RemoteService";
    public static final String PROVIDER_RESULT = "com.sensordroid.UPDATE_COUNT";

    //private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LocalBroadcastManager broadcaster;

    // Update count variables
    private static boolean update = true;
    private static int count;

    // TCP variables
    public static String SERVER_IP;
    public static int SERVER_PORT;
    public static boolean tcp = true;
    private static Socket socket;
    private static OutputStream output;
    private static PrintWriter printWriter;

    // File variables
    public static boolean toFile = true;
    private static FileWriter fileOut;
    private static String filePath = "datasamples.txt";


    /*
        Implementation of the interface defined in MainServiceConnection.aidl
     */
    private final IMainServiceConnection.Stub binder = new IMainServiceConnection.Stub() {
        /*
            Receives string from a remote process and passes it a sender-thread
         */

        /**
         * Writes <tt>json</tt> to file/socket
         * @param json
         *              String to be written
         */
        @Override
        public void putJson(String json) {
            if (toFile) {
                executor.submit(new DispatchFileHandler(json, fileOut));
            } else {
                executor.submit(new DispatchTCPHandler(json, output, printWriter));
            }

            if(update) {
                count++;
                updateCount();
            }
        }
    };

    /**
        Updates TextField in MainActivity
            - The count is appended to make sure the count is correct even
              if the user change foreground activity
     */
    public void updateCount() {
        Intent intent = new Intent(PROVIDER_RESULT);
        intent.putExtra("COUNT", count);
        intent.setPackage(getPackageName());
        broadcaster.sendBroadcast(intent);
    }


    /*
        Return binder object to expose the implemented interface to remote processes.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Got connection from some dude.");
        return binder;
    }

    /*
        Initialize variables
     */
    public void onCreate(){
        super.onCreate();

        // Initialize variables
        count = 0;
        broadcaster = LocalBroadcastManager.getInstance(this);

        // Set the Service to the foreground to decrease chance of getting killed
        toForeground();

        // Collect value from shared preferences and set up TCP connection if tcp is selected
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                ConfigurationActivity.sharedKey, Context.MODE_PRIVATE);

        toFile      = sharedPreferences.getBoolean(ConfigurationActivity.usefileKey, false);
        filePath    = sharedPreferences.getString(ConfigurationActivity.fileNameKey, "datasamples.txt");
        SERVER_IP   = sharedPreferences.getString(ConfigurationActivity.ipKey, "vor.ifi.uio.no");
        SERVER_PORT = sharedPreferences.getInt(ConfigurationActivity.portKey, 12345);
        update      = sharedPreferences.getBoolean(ConfigurationActivity.updateCountKey, true);

        if (toFile){
            openFile(filePath);
        }else {
            new ConnectTCPTask().execute();
        }
    }


    /**
     * Opens the file specified by the argument
     * @param filepath
     *                  file to open
     */
    public void openFile(String filepath){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            try {
                File outfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filepath);

                fileOut = new FileWriter(outfile, true);
                Intent intent =
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(outfile));
                sendBroadcast(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Connects to the specified IP/Port using TCP
     */
    class ConnectTCPTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            Log.d("TCP-setup", "connecting to "+ SERVER_IP);
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = socket.getOutputStream();
                printWriter = new PrintWriter(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
        Sets the current service to the foreground
     */
    public void toForeground() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.stat_notify_chat);
        builder.setContentTitle("Collector");
        builder.setTicker("Forwarding");
        builder.setContentText("Forwarding data");

        Intent i = new Intent(this, RemoteService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        builder.setContentIntent(pi);

        final Notification note = builder.build();

        startForeground(android.os.Process.myPid(), note);
    }

    public void onDestroy(){
        Log.d("ON DESTROY", "Service destroyed");

        // Close tcp-connection
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (toFile && fileOut != null){
            try {
                fileOut.flush();
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Move the service back from the foreground
        stopForeground(true);
        super.onCreate();
    }
}
