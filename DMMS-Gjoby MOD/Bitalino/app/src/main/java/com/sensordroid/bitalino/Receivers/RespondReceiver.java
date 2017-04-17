package com.sensordroid.bitalino.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sensordroid.bitalino.WrapperService;

public class RespondReceiver extends BroadcastReceiver {
    private static final String TAG = "RespondReceiver";
    private static final String REGISTER_ACTION = "com.sensordroid.REGISTER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got Intent, responding...");

        Intent respons = new Intent(REGISTER_ACTION);
        respons.putExtra("NAME", WrapperService.name);
        respons.putExtra("ID", context.getPackageName());
        context.sendBroadcast(respons);
    }
}
