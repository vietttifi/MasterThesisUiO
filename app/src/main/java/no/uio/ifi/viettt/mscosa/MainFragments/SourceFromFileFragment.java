package no.uio.ifi.viettt.mscosa.MainFragments;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import no.uio.ifi.viettt.mscosa.MainActivity;
import no.uio.ifi.viettt.mscosa.OpenSourceFileChooserFromDelaroy.FileUtils;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.File_Sensor_Source;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.LogReadFile;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.ProgressBarHandler;
import no.uio.ifi.viettt.mscosa.interfacesAndHelpClass.EDFFileReader;

import static android.app.Activity.RESULT_OK;

/**
 * Created by viettt on 18/12/2016.
 */

public class SourceFromFileFragment extends Fragment {



    MainActivity mMainActivity;

    //For progressBar management
    static public final int ADD_FILE_SOURCE = 0xCAFE1, FILE_IS_LOADING = 0xCAFE2, FILE_IS_LOADED = 0xCAFE3, START_STOP_LOADING = 0xCAFE4;
    static public final long REFRESH_INTERVAL = 100;
    public boolean isScrolling = false ;
    public long lastUpdate = 0;
    public LinkedHashMap<Integer, File_Sensor_Source> loading_File_data;
    LinkedHashMap<Integer, File_Sensor_Source> in_coming_File_data = new LinkedHashMap<>();
    public List<Integer> done_list = new ArrayList<>();

    private static final int REQUEST_CODE = 6384;
    public final Object lock = new Object();
    private int file_ID = 0;

    //For GUI
    View v;

    ListView listview;
    Button btn_addNewFile, btn_chooseFile;
    TextView lblFilePath;

    SourceListAdapter adapter;
    LayoutInflater m_inflater;

    //helper object
    Handler progressHandler;
    List<LogReadFile> logReadFiles = new ArrayList<>();

    public SourceFromFileFragment(){
        loading_File_data  = new LinkedHashMap<>();
        progressHandler = new ProgressBarHandler(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.source_from_file_fragment_layout, container, false);

        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);//PERMISSION_REQUEST_CODE
        }

        listview = (ListView) v.findViewById(R.id.listFile_sources) ;
        adapter = new SourceListAdapter();
        listview.setAdapter(adapter);
        listview.setOnScrollListener( new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        isScrolling = false;
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        isScrolling = true;
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        isScrolling = true;
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        }) ;


        btn_addNewFile  = (Button)v.findViewById(R.id.btn_addNewFile);
        btn_chooseFile = (Button)v.findViewById(R.id.btn_chooseFile);
        lblFilePath = (TextView) v.findViewById(R.id.lblFilePath);
        btn_chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the GET_CONTENT intent from the utility class
                Intent target = FileUtils.createGetContentIntent();
                // Create the chooser Intent
                Intent intent = Intent.createChooser(
                        target, getString(R.string.chooser_title));
                try {
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    // The reason for the existence of aFileChooser
                }
            }
        });

        btn_addNewFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filePath = lblFilePath.getText().toString();
                btn_addNewFile.setEnabled(false);
                File_Sensor_Source file_info = new File_Sensor_Source();
                file_info.setTitle(filePath.substring(filePath.lastIndexOf('/')+1,filePath.lastIndexOf('.')));
                file_info.setProgress(0);
                file_info.setId(file_ID);
                file_info.setIndex(file_ID);
                file_info.setFilePath(filePath);
                synchronized (lock) {
                    in_coming_File_data.put(file_ID,file_info);
                    file_ID++;
                }
                sendMessageToHandler(ADD_FILE_SOURCE,file_info.getId());
            }
        });

        m_inflater = LayoutInflater.from(getContext());
        return v;
    }

    public void setPointerToCurUI(MainActivity mMainActivity){
        this.mMainActivity = mMainActivity;
    }

    void sendMessageToHandler(int what,int arg1){
        Message message = progressHandler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        message.sendToTarget();
    }

    public void updateListView(){
        synchronized (lock) {
            //Transfer to current_connected_list
            //Then just remove from the
            for (Integer done_id : done_list){
                File_Sensor_Source s = loading_File_data.remove(done_id);
                // FINISH WITH READ EDF SOURCE
                // ...........
                final String title = s.getTitle();
                if(getActivity() != null){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),title+" has successful saved to database",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            //load those who coming to the current loading
            loading_File_data.putAll(in_coming_File_data);
            //and let it go :)
            for (File_Sensor_Source file_info : in_coming_File_data.values()){
                new EDFFileReader("FILE#"+file_info.getId(),file_info,logReadFiles,mMainActivity,progressHandler).start();
            }

            //empty those who have done
            //and those who have just come
            done_list.clear();
            in_coming_File_data.clear();
        }
        adapter.notifyDataSetChanged();
    }


    //-------------------- HELP CLASS FOR GUI -----------------------------

    private class ViewHolder{
        TextView text;
        ProgressBar bar;
        Button button;
        ItemClickListener listener;

        ViewHolder(){
            listener = new ItemClickListener();
        }

    }

    private class ItemClickListener implements View.OnClickListener {
        private int id;

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            sendMessageToHandler(START_STOP_LOADING,id);
        }

    }

    private class SourceListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return loading_File_data.keySet().size();
        }

        @Override
        public Object getItem(int position) {
            Object[] d = loading_File_data.keySet().toArray();
            int c = (Integer)d[position];
            return loading_File_data.get(c);
        }

        @Override
        public long getItemId(int position) {
            return ((File_Sensor_Source)this.getItem(position)).getId() ;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ViewHolder holder;
            if (v==null) {
                v = m_inflater.inflate(R.layout.source_from_file_listview_item, null);
                holder = new ViewHolder();

                holder.text = (TextView) v.findViewById(R.id.lbl_newFile_filename);
                holder.bar = (ProgressBar) v.findViewById(R.id.progressBar_loading);
                holder.button = (Button) v.findViewById(R.id.btn_start_stop);

                v.setTag(holder);
            }else{
                holder = (ViewHolder) v.getTag();
            }
            final File_Sensor_Source data = (File_Sensor_Source)getItem(position);

            holder.listener.setId(data.getIndex());
            holder.button.setOnClickListener(holder.listener);

            holder.text.setText(data.getTitle());
            holder.bar.setProgress(data.getProgress());

            if (data.isRunning()) {
                holder.button.setText("STOP");
            }else{
                holder.button.setText("START");
            }

            return v;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(getContext(), uri);
                            lblFilePath.setText(path);
                            btn_addNewFile.setEnabled(true);
                            Toast.makeText(getActivity(),
                                    "File Selected: " + path, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e("FileS", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
