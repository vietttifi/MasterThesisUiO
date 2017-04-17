package com.sensordroid.bitalino.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sensordroid.bitalino.WrapperService;

public class StartReceiver extends BroadcastReceiver {
    private static final String TAG = "StartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got intent. Connecting to service...");

        int driverId = -1;
        Bundle b = intent.getExtras();

        if(b!=null)
        {
            int counter = 0;
            for (String elem : b.getStringArrayList("WRAPPERS")){
                if (elem.equals(context.getPackageName())){
                    Log.d(TAG, " id found");
                    driverId = counter;
                    break;
                }
                counter++;
            }
        }

        if(driverId != -1) {
            String serv_action = b.getString("SERVICE_ACTION");
            String serv_name = b.getString("SERVICE_NAME");
            String serv_pack = b.getString("SERVICE_PACKAGE");

            Intent service = new Intent(context, WrapperService.class);
            service.putExtra("ACTION", WrapperService.START_ACTION);
            service.putExtra("DRIVER_ID", driverId);
            service.putExtra("SERVICE_ACTION", serv_action);
            service.putExtra("SERVICE_NAME", serv_name);
            service.putExtra("SERVICE_PACKAGE", serv_pack);
            context.startService(service);
        }
    }
}
