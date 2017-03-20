package com.sensordroid.bitalino.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sensordroid.bitalino.WrapperService;

public class StopReceiver extends BroadcastReceiver {
    private static final String TAG = "StopReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got stop intent");
        int driverId = -1;
        Bundle b = intent.getExtras();

        if(b!=null)
        {
            for (String elem : b.getStringArrayList("WRAPPERS")){
                if (elem.equals(context.getPackageName())){
                    Log.d(TAG, " id found");
                    break;
                }
            }
        }

        if(driverId != -1) {
            Intent service = new Intent(context, WrapperService.class);
            service.putExtra("ACTION", WrapperService.STOP_ACTION);
            context.startService(service);
        }
    }
}
