package no.uio.ifi.viettt.mscosa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADBHelper;
import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADataBaseManager;

public class DatabaseVisualisationActivity extends AppCompatActivity implements View.OnClickListener{

    RadioButton rdSource, rdPatient;
    EditText keySearch, annotations;
    TextView lblSelectedSource, selectChannelRP;
    Button btnApply;
    ImageButton ibtnPlay, ibtnStop, ibtnAddAnns, ibtnSaveAnns;
    TableLayout tblLayout;

    //GUI
    AlertDialog.Builder alertdialogbuilder;
    String[] alertDialogItems;
    boolean[] selectedChannels;
    final int NR_ENTRIES_WINDOW = 300;

    String source_ID, patient_ID;

    //Thread query and update
    //ConsumerDataRecord updateGUI;
    //ProducerDataRecord queryDataForGUI;
    final int BUFF_SIZE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_database_visualisation);


        rdSource = (RadioButton)findViewById(R.id.rdBtnSource);
        rdPatient = (RadioButton)findViewById(R.id.rdBtnPatient);

        keySearch = (EditText)findViewById(R.id.txtSearchRP);
        lblSelectedSource = (TextView)findViewById(R.id.lblSelectedSource);
        annotations = (EditText)findViewById(R.id.txtAnnRP);
        btnApply = (Button)findViewById(R.id.btnApply);
        tblLayout = (TableLayout)findViewById(R.id.tblSources);
        ibtnPlay = (ImageButton)findViewById(R.id.btnPlayRP);
        ibtnStop = (ImageButton)findViewById(R.id.btnStopRP);
        ibtnAddAnns = (ImageButton)findViewById(R.id.btnAddAnnRP);
        ibtnSaveAnns = (ImageButton)findViewById(R.id.btnSaveAnnRP);
        selectChannelRP = (TextView)findViewById(R.id.selectChannelRP);

        selectChannelRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogItems = new String[]{"TEST1", "TEST 2"};
                selectedChannels = new boolean[]{true,true};
                popUpSelectSensors();
            }
        });

        rdSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageRdSource(view);
            }
        });

        rdPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageRdPatient(view);
            }
        });

        keySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                manageKeySearch(charSequence,i,i1,i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageBtnApply(view);
            }
        });

        ibtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageIbtnPlay(view);
            }
        });

        ibtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageIbtnStop(view);
            }
        });

        ibtnAddAnns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageIbtnAddAnns(view);
            }
        });

        ibtnSaveAnns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageIbtnSaveAnns(view);
            }
        });


    }

    /*=======================  FUNCTIONS  ==========================*/


    private void manageRdSource(View view){
        manageKeySearch(keySearch.getText(),0,0,0);
    }

    private void manageRdPatient(View view){
        manageKeySearch(keySearch.getText(),0,0,0);
    }


    private void manageKeySearch(CharSequence charSequence, int i, int i1, int i2){
        //Toast.makeText(getApplication(),charSequence.toString(),Toast.LENGTH_SHORT).show();
        if(charSequence.toString().length() < 3) return;

        String queryString = "SELECT DISTINCT "
                + " p_owner as Patient, s_id as Source "
                + " FROM RECORD ";

        if(rdSource.isChecked()){
            queryString += " WHERE s_id like '%"+ charSequence.toString()+"%'";
        } else {
            queryString += " WHERE p_owner like '%"+ charSequence.toString()+"%'";
        }

        OSADataBaseManager.initializeInstance(new OSADBHelper(getApplication()));
        OSADataBaseManager osaDataBaseManager = null;
        tblLayout.removeAllViews();

        try {
            osaDataBaseManager = OSADataBaseManager.getInstance();
            SQLiteDatabase mDatabase = osaDataBaseManager.openDatabase();
            Cursor cursor = mDatabase.rawQuery(queryString, null);
            cursor.moveToFirst();

            int columnCount = cursor.getColumnCount();
            TableRow tr = new TableRow(getApplication());
            TableRow trSep = new TableRow(getApplication());
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            trSep.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            //add columns
            for (int k =0; k<columnCount; k++) {
                TextView column = new TextView(getApplication());
                TextView separator = new TextView(getApplication());
                column.setGravity(Gravity.CENTER);
                separator.setGravity(Gravity.CENTER);
                separator.setText("---");
                column.setText(String.valueOf(cursor.getColumnName(k)));
                column.setPadding(5, 5, 5, 0);
                tr.addView(column);
                trSep.addView(separator);
            }

            tblLayout.addView(tr, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            tblLayout.addView(trSep, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            while (!cursor.isAfterLast()) {
                tr = new TableRow(getApplication());
                tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                for (int k=0; k<columnCount; k++) {
                    TextView column = new TextView(getApplication());
                    column.setGravity(Gravity.CENTER);
                    if(cursor.getType(k) == Cursor.FIELD_TYPE_BLOB){
                        column.setText("UNPRINTABLE");
                    }else if(cursor.getType(k) == Cursor.FIELD_TYPE_NULL) {
                        column.setText("NULL");
                    }else if(cursor.getType(k) == Cursor.FIELD_TYPE_FLOAT) {
                        column.setText(String.valueOf(cursor.getFloat(k)));
                    }else if(cursor.getType(k) == Cursor.FIELD_TYPE_INTEGER) {
                        column.setText(String.valueOf(cursor.getInt(k)));
                    }else {
                        column.setText(cursor.getString(k));
                    }

                    column.setPadding(5, 5, 5, 0);
                    tr.addView(column);
                }

                tr.setOnClickListener(this);

                tblLayout.addView(tr, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                cursor.moveToNext();
            }

            //close the cursor
            cursor.close();

        }catch (Exception e){
            //clear result table
            tblLayout.removeAllViews();
            TableRow tr = new TableRow(getApplication());
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            TextView column = new TextView(getApplication());
            column.setGravity(Gravity.CENTER);
            column.setText("Query syntax fail. " + "And fail is: "+e.getMessage());
            tr.addView(column);
            tblLayout.addView(tr, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            System.out.println(e.getMessage());
        }

        if(osaDataBaseManager != null) osaDataBaseManager.closeDatabase();
    }

    private void manageBtnApply(View view){
        Toast.makeText(getApplication(),"APPLY "+source_ID+" "+patient_ID,Toast.LENGTH_SHORT).show();
        btnApply.setEnabled(false);
    }

    private void manageIbtnPlay(View view){
        Toast.makeText(getApplication(),"Play click",Toast.LENGTH_SHORT).show();
    }

    private void manageIbtnStop(View view){
        Toast.makeText(getApplication(),"Stop click",Toast.LENGTH_SHORT).show();
    }

    private void manageIbtnAddAnns(View view){
        Toast.makeText(getApplication(),"Annotation added click",Toast.LENGTH_SHORT).show();
    }

    private void manageIbtnSaveAnns(View view){
        Toast.makeText(getApplication(),"All annotations have saved click",Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view) {
        if(view instanceof TableRow) {
            TableRow tr = (TableRow)view;
            patient_ID = ((TextView) tr.getChildAt(0)).getText().toString();
            source_ID = ((TextView) tr.getChildAt(1)).getText().toString();
            lblSelectedSource.setText(source_ID);
            btnApply.setEnabled(true);
        }
    }

    private void popUpSelectSensors(){
        alertdialogbuilder = new AlertDialog.Builder(DatabaseVisualisationActivity.this);

        String nameSensors[] = new String[alertDialogItems.length];

        for(int i = 0; i < alertDialogItems.length; i ++) {
            nameSensors[i] = alertDialogItems[i];
        }

        alertdialogbuilder.setMultiChoiceItems(nameSensors, selectedChannels, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            }
        });

        alertdialogbuilder.setCancelable(false);

        alertdialogbuilder.setTitle("Select sensors");

        alertdialogbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertdialogbuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = alertdialogbuilder.create();
        dialog.show();
    }

}
