package com.sensordroid.bitalino.Handlers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;

import com.sensordroid.bitalino.SettingsActivity;
import com.sensordroid.bitalino.util.BitalinoTransfer;
import com.sensordroid.IMainServiceConnection;
import com.sensordroid.bitalino.util.JSONHelper;

/**
 * Created by sveinpg on 26.02.16.
 */
public class MetadataHandler implements Runnable {
    private static IMainServiceConnection binder;
    private static String name;
    private static int id;
    private static int[] channel_ids;
    private static String[] data_types;
    private static String[] metrics;
    private static String[] descriptions;
    private static float[] frequency;

    public MetadataHandler(IMainServiceConnection binder, String name, int id, Context context, int[] types) {
        this.binder = binder;
        this.id = id;
        this.name = name;

        this.data_types = new String[types.length];
        this.metrics = new String[types.length];
        this.descriptions = new String[types.length];
        this.frequency = new float[types.length];

        // Fetching values from shared preferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsActivity.sharedKey, Context.MODE_PRIVATE);
        int counter = 0;
        for(int i = 0; i < types.length; i++) {
            if (types[i] == BitalinoTransfer.TYPE_OFF){
                continue;
            }
            this.data_types[counter] = BitalinoTransfer.getType(types[i]);
            this.metrics[counter] = BitalinoTransfer.getMetric(types[i]);
            this.descriptions[counter] = sharedPreferences.getString(SettingsActivity.descriptionKeys[i], " ");
            this.frequency[counter] = SettingsActivity.getSelectedFreq();
            Log.d("Metadata", "Descriptions " + counter + " value " + descriptions[counter]);
            counter++;
        }

        channel_ids = new int[counter];
        for (int i = 0; i < channel_ids.length; i++){
            channel_ids[i] = i;
        }
    }

    @Override
    public void run() {
        try {
            String sendString = JSONHelper.metadata(name, id, channel_ids, data_types,
                metrics, frequency, descriptions).toString();
            binder.putJson(sendString);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
