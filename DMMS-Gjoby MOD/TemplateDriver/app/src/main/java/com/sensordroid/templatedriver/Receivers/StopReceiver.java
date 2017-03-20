package com.sensordroid.templatedriver.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sensordroid.templatedriver.MainService;

public class StopReceiver extends BroadcastReceiver {
    private static final String TAG = "StopReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got intent. Checking list...");
        int driverId = -1;
        Bundle bundle = intent.getExtras();

        // Checking if driver is suppose to stop
        if(bundle!=null)
        {
            int counter = 0;
            for (String elem : bundle.getStringArrayList("DRIVERS")){
                if (elem.equals(MainService.name)){
                    Log.d(TAG, " id found");
                    driverId = counter;
                    break;
                }
            }
        }

        if(driverId != -1) {
            // Sending stop-intent to Service
            Intent service = new Intent(context, MainService.class);
            service.putExtra("ACTION", MainService.STOP_ACTION);
            context.startService(service);
        }
    }
}
