package no.uio.ifi.viettt.mscosa;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADBHelper;


public class RawQueryActivity extends AppCompatActivity {
    private static final int LIMIT = 100;
    EditText txtSearch;
    TableLayout tableResult;
    Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_raw_query);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        Intent intent = getIntent();
        String value = intent.getStringExtra("key"); //if it's a string you stored.
        System.out.println(value);

        txtSearch = (EditText) findViewById(R.id.txtSearch);
        tableResult = (TableLayout) findViewById(R.id.tblRawData);
        btnSearch = (Button) findViewById(R.id.btnSearchRaw);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String queryString = txtSearch.getText().toString();
                if(queryString.equals("")) return;

                //clear result table
                tableResult.removeAllViews();

                OSADBHelper mDbHelper = new OSADBHelper(getApplication());
                SQLiteDatabase mDatabase = mDbHelper.getWritableDatabase();

                try {
                    Cursor cursor = mDatabase.rawQuery(queryString, null);
                    cursor.moveToFirst();
                    Toast.makeText(getApplication(),"Total row: "+cursor.getCount(),Toast.LENGTH_SHORT).show();
                    int cnt = 0;

                    int columnCount = cursor.getColumnCount();
                    TableRow tr = new TableRow(getApplication());
                    TableRow trSep = new TableRow(getApplication());
                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    trSep.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    //add columns
                    for (int i=0; i<columnCount; i++) {
                        TextView column = new TextView(getApplication());
                        TextView separator = new TextView(getApplication());
                        column.setGravity(Gravity.CENTER);
                        separator.setGravity(Gravity.CENTER);
                        separator.setText("---");
                        column.setText(String.valueOf(cursor.getColumnName(i)));
                        column.setPadding(5, 5, 5, 0);
                        tr.addView(column);
                        trSep.addView(separator);
                    }

                    tableResult.addView(tr, new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));
                    tableResult.addView(trSep, new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));

                    while (!cursor.isAfterLast() && cnt++ < LIMIT) {
                        tr = new TableRow(getApplication());
                        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                        for (int i=0; i<columnCount; i++) {
                            TextView column = new TextView(getApplication());
                            column.setGravity(Gravity.CENTER);
                            if(cursor.getType(i) == Cursor.FIELD_TYPE_BLOB){
                                column.setText("UNPRINTABLE");
                            }else if(cursor.getType(i) == Cursor.FIELD_TYPE_NULL) {
                                column.setText("NULL");
                            }else if(cursor.getType(i) == Cursor.FIELD_TYPE_FLOAT) {
                                column.setText(String.valueOf(cursor.getFloat(i)));
                            }else if(cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER) {
                                column.setText(String.valueOf(cursor.getLong(i)));
                            }else {
                                column.setText(cursor.getString(i));
                            }

                            column.setPadding(5, 5, 5, 0);
                            tr.addView(column);
                        }

                        tableResult.addView(tr, new TableLayout.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT));

                        cursor.moveToNext();
                    }

                    //close the cursor
                    cursor.close();


                }catch (Exception e){
                    //clear result table
                    tableResult.removeAllViews();
                    TableRow tr = new TableRow(getApplication());
                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    TextView column = new TextView(getApplication());
                    column.setGravity(Gravity.CENTER);
                    column.setText("Query syntax fail.\nWe have tables:\n" +
                            "SOURCE\n" +
                            "CHANNEL \n " +
                            "RECORD\n " +
                            "FRAGMENT\n " +
                            "PERSON \n " +
                            "PHYSICIAN \n " +
                            "PATIENT\n" +
                            "CLINIC\n" +
                            "SAMPLE\n" +
                            "And fail is: "+e.getMessage());
                    tr.addView(column);
                    tableResult.addView(tr, new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));
                }

                mDatabase.close();
                mDbHelper.close();
            }
        });

    }

}
