package com.sensordroid.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.sensordroid.R;

public class ConfigurationActivity extends Activity {
    public static final String sharedKey = "com.sensordroid.collector";
    public static final String ipKey = sharedKey + ".ip";
    public static final String portKey = sharedKey + ".port";
    public static final String usefileKey = sharedKey + ".usefile";
    public static final String fileNameKey = sharedKey + ".filename";
    public static final String updateCountKey = sharedKey + ".update";


    private static EditText mEditIP;
    private static EditText mEditPort;
    private static EditText mEditFileName;
    private static ToggleButton mToggleFile;
    private static CheckBox mCheckUpdate;
    private static Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        final SharedPreferences sharedPreferences = getSharedPreferences(
                sharedKey, Context.MODE_PRIVATE);

        // Fetch values from shared preferences
        String sharedIP = sharedPreferences.getString(ipKey, "vor.ifi.uio.no");
        String sharedName = sharedPreferences.getString(fileNameKey, "datavalues.txt");
        int sharedPort = sharedPreferences.getInt(portKey, 12345);
        boolean sharedUseFile = sharedPreferences.getBoolean(usefileKey, false);
        boolean sharedPackCount = sharedPreferences.getBoolean(updateCountKey, false);

        // Fetch views
        mEditIP = (EditText)findViewById(R.id.editIP);
        mEditPort = (EditText)findViewById(R.id.editPort);
        mEditFileName = (EditText)findViewById(R.id.editName);
        mToggleFile = (ToggleButton)findViewById(R.id.toggleFile);
        mCheckUpdate = (CheckBox)findViewById(R.id.checkCount);
        mButton = (Button)findViewById(R.id.buttonNetwork);

        // Set values to views
        mEditIP.setText(sharedIP);
        mEditPort.setText("" + sharedPort);
        mEditFileName.setText(sharedName);
        mToggleFile.setChecked(sharedUseFile);
        mCheckUpdate.setChecked(sharedPackCount);

        /*
            Saves the values from the layout to shared preferences when the button is clicked.
         */
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString(ipKey, mEditIP.getText().toString()).apply();
                sharedPreferences.edit().putString(fileNameKey, mEditFileName.getText().toString()).apply();
                sharedPreferences.edit().putInt(portKey, Integer.parseInt(mEditPort.getText().toString())).apply();
                sharedPreferences.edit().putBoolean(usefileKey, mToggleFile.isChecked()).apply();
                sharedPreferences.edit().putBoolean(updateCountKey, mCheckUpdate.isChecked()).apply();
                Intent intent = new Intent(ConfigurationActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });
    }
}
