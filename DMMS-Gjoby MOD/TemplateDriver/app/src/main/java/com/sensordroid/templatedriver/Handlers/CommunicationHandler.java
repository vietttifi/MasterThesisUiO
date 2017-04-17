package com.sensordroid.templatedriver.Handlers;

import android.content.Context;

import com.sensordroid.IMainServiceConnection;
import com.sensordroid.templatedriver.MainActivity;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sveinpg on 27.01.16.
 */
public class CommunicationHandler implements Runnable {
    private static IMainServiceConnection binder;
    private static int driverId;
    private static String driverName;
    private static boolean interrupted;
    private Context context;

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public CommunicationHandler(IMainServiceConnection binder, String name,  int id, Context context) {
        this.binder = binder;
        this.driverName = name;
        this.driverId = id;
        this.interrupted = false;
        this.context = context;
        /*
            TODO 3: initialize implemenatation spesific variables.
         */
    }
    @Override
    public void run() {
        // Send metadata
        executor.submit(new MetadataHandler(binder, driverName, driverId));

        // While loop to reconnect in case of disconnection
        while (!interrupted){
            // Check if interrupted
            if (Thread.currentThread().isInterrupted()){
                interrupted = true;
                break;
            }

            // TODO 4: Surround with appropriate try /catch
            connect();
            collectData();
            resetConnection();
        }
    }

    private void connect() {
        /*
            TODO 5: Code to connect to the data source.
         */

    }

    private void collectData() {
        /*
            TODO 6: Code to collect data,
                - Collect data until the thread is interrupted.
         */
        while(!interrupted){
            if (Thread.currentThread().isInterrupted()) {
                interrupted = true;
                break;
            }
            // TODO 7: Change type of collectedData to match your data format
            Object[] collectedData = new Object[]{};
            int[] channelsUsed = new int[]{};

            // Pass the collected data to the working threads for computations and sending.
            executor.submit(new DataHandler(binder, driverId, collectedData, channelsUsed));
        }
    }

    private void resetConnection() {
        /*
            TODO 8: Code to reset connection
                - For instance close sockets, files etc.
         */

    }
}
