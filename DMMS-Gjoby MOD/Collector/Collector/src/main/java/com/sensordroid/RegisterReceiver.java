package com.sensordroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class RegisterReceiver extends BroadcastReceiver {
    static final public String REGISTER_ACTION = "com.sensordroid.ADD_DRIVER";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(REGISTER_ACTION);
        String name = intent.getStringExtra("NAME");
        String id = intent.getStringExtra("ID");

        if (id == null || name == null) return;

        i.putExtra("NAME", name);
        i.putExtra("ID", id);
        Log.d("RegisterReceiver", intent.getStringExtra("ID"));
        context.sendBroadcast(i);
    }
}
