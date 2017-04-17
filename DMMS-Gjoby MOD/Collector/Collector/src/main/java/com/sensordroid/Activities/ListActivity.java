package com.sensordroid.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sensordroid.Wrapper;
import com.sensordroid.R;
import com.sensordroid.RegisterReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ListActivity extends Activity {
    private int wrapperCount;
    WrapperAdapter wrapperAdapter = null;
    ArrayList<Wrapper> wrappers;

    /*
        Comparator for Wrapper-objects
     */
    public class WrapperComparator implements Comparator<Wrapper>{
        @Override
        public int compare(Wrapper d1, Wrapper d2){
            return d1.getName().toLowerCase().compareTo(d2.getName().toLowerCase());
        }
    }

    /*
    BroadcastReceiver for REGISTER-commands sent by Sensor wrapper applications
     */
    BroadcastReceiver wrapperReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Wrapper wrapper = new Wrapper(intent.getStringExtra("NAME"), intent.getStringExtra("ID"), false);

            if (wrappers.contains(wrapper)){
                return;
            }
            wrappers.add(wrapper);

            // Sorts wrappers by name
            Collections.sort(wrappers, new WrapperComparator());
            //create an ArrayAdaptar from the String Array
            wrapperAdapter = new WrapperAdapter(ListActivity.this,
                    R.layout.wrapper_info, wrappers);
            ListView listView = (ListView) findViewById(R.id.listView1);
            // Assign adapter to ListView
            listView.setAdapter(wrapperAdapter);
            checkButtonClick();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        wrappers = new ArrayList<>();
        sendBroadcast(new Intent("com.sensordroid.HELLO"));
        registerReceiver((wrapperReceiver),
                new IntentFilter(RegisterReceiver.REGISTER_ACTION)
        );

        Button networkButton = (Button)findViewById(R.id.buttonConfigure);
        networkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent configure = new Intent(ListActivity.this, ConfigurationActivity.class);
                startActivity(configure);
            }
        });
        checkButtonClick();
    }



    public class WrapperAdapter extends ArrayAdapter<Wrapper>{
        private ArrayList<Wrapper> wrapperList;

        public WrapperAdapter(Context context, int textViewResourceId,
                             ArrayList<Wrapper> wrapperList) {
            super(context, textViewResourceId, wrapperList);
            this.wrapperList = new ArrayList<Wrapper>();
            this.wrapperList.addAll(wrapperList);
        }

        private class WrapperView {
            TextView code;
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WrapperView wrapperView = null;

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.wrapper_info, null);

                wrapperView = new WrapperView();
                wrapperView.code = (TextView) convertView.findViewById(R.id.code);
                wrapperView.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(wrapperView);

                wrapperView.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        // Checks checkbox
                        CheckBox cb = (CheckBox) v ;
                        Wrapper wrapper = (Wrapper) cb.getTag();
                        wrapper.setSelected(cb.isChecked());
                    }
                });
            }
            else {
                wrapperView = (WrapperView) convertView.getTag();
            }
            Wrapper wrapper = wrapperList.get(position);
            wrapperView.code.setText(" ("+ wrapper.getId() + ") ");
            wrapperView.name.setText(wrapper.getName());
            wrapperView.name.setChecked(wrapper.isSelected());
            wrapperView.name.setTag(wrapper);

            return convertView;
        }

    }

    /*
    Invoked when "select wrappers" button is clicked.
        - Passes the selected wrapper to the MainActivity.
     */
    private void checkButtonClick() {
        Button myButton = (Button) findViewById(R.id.findSelected);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                StringBuffer message = new StringBuffer();
                message.append("The following were selected...\n");
                ArrayList<String> wrapperArrayList = new ArrayList<>();

                // Check for selected wrappers
                ArrayList<Wrapper> wrapperList = wrapperAdapter.wrapperList;
                for(int i=0;i< wrapperList.size();i++){
                    Wrapper wrapper = wrapperList.get(i);
                    if(wrapper.isSelected()){
                        wrapperArrayList.add(wrapper.getId());
                        message.append("\n" + wrapper.getName());
                    }
                }

                Toast.makeText(getApplicationContext(),
                        message, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                intent.putStringArrayListExtra("WRAPPERS", wrapperArrayList);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(wrapperReceiver);
    }
}
