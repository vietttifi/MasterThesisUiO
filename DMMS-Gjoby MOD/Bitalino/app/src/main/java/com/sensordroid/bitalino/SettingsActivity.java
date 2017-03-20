package com.sensordroid.bitalino;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SettingsActivity extends Activity {
    // Shared Preferences keys
    public static final String sharedKey = "com.sensordroid.bitalino";
    public static final String channelKey = sharedKey + ".channels";
    public static final String macKey = sharedKey + ".mac";
    public static final String frequencyKey = sharedKey + ".frequency";
    public static final String[] descriptionKeys = new String[]{sharedKey + ".d1", sharedKey + ".d2",
        sharedKey + ".d3", sharedKey + ".d4", sharedKey + ".d5", sharedKey + ".d6"};


    public static final int[] freq = new int[]{1,10,100,1000};
    public static final int NUM_CHANNELS = 6;
    private static float selectedFreq = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // Keep keyboard from poping up at launch
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        final SharedPreferences preferences = getSharedPreferences(sharedKey, MODE_PRIVATE);
        final SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
        final EditText editMac = (EditText)findViewById(R.id.editMac);
        final TextView freqText = (TextView)findViewById(R.id.textSampling);

        List<String> types = new ArrayList<>();
        types.add("<OFF>");
        types.add("RAW");
        types.add("LUX");
        types.add("ACC");
        types.add("PZT");
        types.add("ECG");
        types.add("EEG");
        types.add("EDA");
        types.add("EMG");
        types.add("TMP");

        final List<Spinner> spinners = new ArrayList<>();
        spinners.add((Spinner) findViewById(R.id.spinner1));
        spinners.add((Spinner) findViewById(R.id.spinner2));
        spinners.add((Spinner) findViewById(R.id.spinner3));
        spinners.add((Spinner) findViewById(R.id.spinner4));
        spinners.add((Spinner) findViewById(R.id.spinner5));
        spinners.add((Spinner) findViewById(R.id.spinner6));

        final List<EditText> descriptions = new ArrayList<>();
        descriptions.add((EditText) findViewById(R.id.editDesc1));
        descriptions.add((EditText) findViewById(R.id.editDesc2));
        descriptions.add((EditText) findViewById(R.id.editDesc3));
        descriptions.add((EditText) findViewById(R.id.editDesc4));
        descriptions.add((EditText) findViewById(R.id.editDesc5));
        descriptions.add((EditText) findViewById(R.id.editDesc6));

        String sharedMac = preferences.getString(macKey, "98:D3:31:B2:BB:A5");
        int sharedProg = preferences.getInt(frequencyKey, 0);

        editMac.setText(sharedMac);
        seekBar.setProgress(sharedProg);
        setFreqText(sharedProg, freqText, freq);


        final int[] selectedList = getSharedList(preferences, channelKey);
        int counter = 0;
        for(Spinner spinner : spinners) {
            setSpinnerContent(types, spinner);
            spinner.setSelection(selectedList[counter++]);
        }

        final String[] descriptionList = getDescriptionStrings(preferences, descriptionKeys);
        counter = 0;
        for (EditText elem : descriptions){
            if (descriptionList[counter].compareTo(" ") == 0){
                // Do nothing
            } else {
                elem.setText(descriptionList[counter]);
            }
            counter++;
        }

        Button saveButton = (Button)findViewById(R.id.button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pattern pattern = Pattern.compile("([\\da-fA-F]{2}(?:\\:|$)){6}");
                Matcher matcher = pattern.matcher(editMac.getText().toString());
                if (matcher.find()) {
                    int i = 0;
                    for (Spinner spinner : spinners) {
                        selectedList[i++] = spinner.getSelectedItemPosition();
                    }
                    setSharedList(preferences, channelKey, selectedList);
                    setDescriptionString(preferences, descriptionKeys, descriptions);
                    //TODO: CHECK FOR VALID MAC-ADRESS
                    preferences.edit().putString(macKey, editMac.getText().toString().toUpperCase()).apply();
                    preferences.edit().putInt(frequencyKey, seekBar.getProgress()).apply();
                    Toast.makeText(getApplicationContext(), "Configuration saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid MAC-address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setFreqText(i, freqText, freq);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Auto-generated method
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Number chosen to give the user a feeling of a smoother seek bar.
                int i = seekBar.getProgress();
                if (i < 17) {
                    seekBar.setProgress(0);
                } else if (i < 50) {
                    seekBar.setProgress(33);
                } else if (i < 83) {
                    seekBar.setProgress(66);
                } else {
                    seekBar.setProgress(100);
                }
            }
        });
    }

    /*
        Set the content of the spinner to match the "types" list
     */
    public void setSpinnerContent(List<String> types, Spinner spinner) {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
    }

    /*
        Retrieve int[] list containing channel information from shared preferences
     */
    public int[] getSharedList(SharedPreferences pref, String key) {
        String savedString = pref.getString(key, "0,0,0,0,0,0");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        int[] list = new int[NUM_CHANNELS];
        for (int i = 0; i < NUM_CHANNELS; i++) {
            list[i] = Integer.parseInt(st.nextToken());
        }
        return list;

    }

    /*
        Save int[] containing channel information list to shared prefrences
     */
    public void setSharedList(SharedPreferences pref, String key, int[] list) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            str.append(list[i]).append(",");
        }
        pref.edit().putString(key, str.toString()).apply();
    }

    /*
        Fetch the description keys from shared preferences.
     */
    public String[] getDescriptionStrings(SharedPreferences pref, String[] keys) {
        String[] result = new String[NUM_CHANNELS];

        for (int i = 0; i < keys.length; i++){
            String tmp = pref.getString(keys[i], " ");
            result[i] = tmp;
        }
        return result;
    }

    /*
        Set the text in a description EditText-object.
     */
    public void setDescriptionString(SharedPreferences pref, String[] keys, List<EditText> editTextList) {
        int counter = 0;
        for (EditText editElem : editTextList) {
            pref.edit().putString(keys[counter++], editElem.getText().toString()).apply();
        }
    }

    /*
        Set the text to tell the user which frequency is choosen.
     */
    public void setFreqText(int i, TextView freqText, int[] freq){
        int pos;
        if (i < 17) {
            pos = 0;
        } else if (i < 50) {
            pos = 1;
        } else if (i < 83) {
            pos = 2;
        } else {
            pos = 3;
        }
        selectedFreq = freq[pos];
        freqText.setText("Selected sampling frequency: " + freq[pos]);
    }

    public static float getSelectedFreq() {
        return selectedFreq;
    }
}
