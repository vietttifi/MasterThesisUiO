package com.sensordroid.templatedriver.Handlers;

import android.os.RemoteException;

import com.sensordroid.IMainServiceConnection;
import com.sensordroid.templatedriver.util.JSONHelper;

import org.json.JSONObject;

/**
 * Created by sveinpg on 10.03.16.
 */
public class MetadataHandler implements Runnable {
    private static String driverName;
    private static int driverId;
    private static IMainServiceConnection binder;

    // Metadata variables
    private static int[] ids;
    private static String[] types;
    private static String[] metrics;
    private static String[] descriptions;

    public MetadataHandler(IMainServiceConnection binder, String name, int id){
        this.binder = binder;
        this.driverName = name;
        this.driverId = id;
    }

    /*
        Runned at the start of the acquisition to send metadata
     */
    @Override
    public void run() {
        /*
            TODO 13: Initialize metadata and save them in the metadata variables above.
                - For instance get them from sharedPreferences
         */

        JSONObject metadata = JSONHelper.metadata(driverName, driverId, ids,
                types, metrics, descriptions);

        try{
            binder.putJson(metadata.toString());
        } catch (RemoteException re){
            re.printStackTrace();
        }
    }
}