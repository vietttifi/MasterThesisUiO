package no.uio.ifi.viettt.mscosa.MainFragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADBHelper;
import no.uio.ifi.viettt.mscosa.R;


/**
 * Created by viettt on 18/12/2016.
 */

public class MiningFragment extends Fragment {
    View v;

    final int LIMIT = 1000;
    TableLayout tableResult;
    EditText txtSearch;
    Button btnSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.mining_fragment_layout, container, false);

        txtSearch = (EditText) v.findViewById(R.id.txtSearch);
        tableResult = (TableLayout) v.findViewById(R.id.tblRawData);
        btnSearch = (Button) v.findViewById(R.id.btnSearchRaw);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OSADBHelper mDbHelper = new OSADBHelper(getContext());
                SQLiteDatabase mDatabase = mDbHelper.getWritableDatabase();

                String queryString = txtSearch.getText().toString();

                if(queryString.equals("")) return;

                //clear result table
                tableResult.removeAllViews();

                try {
                    Cursor cursor = mDatabase.rawQuery(queryString, null);
                    cursor.moveToFirst();
                    int cnt = 0;

                    int columnCount = cursor.getColumnCount();
                    /*--------- create table header --------*/
                    TableRow tr = new TableRow(getContext());
                    TableRow trSep = new TableRow(getContext());
                    tr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    trSep.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    //add columns
                    for (int i=0; i<columnCount; i++) {
                        TextView column = new TextView(getContext());
                        TextView separator = new TextView(getContext());
                        separator.setText("---");
                        column.setText(String.valueOf(cursor.getColumnName(i)));
                        column.setPadding(5, 5, 5, 0);
                        tr.addView(column);
                        trSep.addView(separator);
                    }

                    tableResult.addView(tr, new TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    tableResult.addView(trSep, new TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    while (!cursor.isAfterLast() && cnt++ < LIMIT) {
                        /*--------- add data to table ----------*/
                        tr = new TableRow(getContext());
                        tr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        for (int i=0; i<columnCount; i++) {
                            TextView column = new TextView(getContext());
                            if(cursor.getType(i) == Cursor.FIELD_TYPE_BLOB){
                                column.setText("UNPRINTABLE");
                            }else if(cursor.getType(i) == Cursor.FIELD_TYPE_NULL) {
                                column.setText("NULL");
                            }else if(cursor.getType(i) == Cursor.FIELD_TYPE_FLOAT) {
                                column.setText(String.valueOf(cursor.getFloat(i)));
                            }else if(cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER) {
                                column.setText(String.valueOf(cursor.getInt(i)));
                            }else {
                                column.setText(cursor.getString(i));
                            }

                            column.setPadding(5, 5, 5, 0);
                            tr.addView(column);
                        }

                        tableResult.addView(tr, new TableLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

                        cursor.moveToNext();
                    }

                        //close the cursor
                        cursor.close();

                    mDatabase.close();
                    mDbHelper.close();
                }catch (Exception e){
                    //clear result table
                    tableResult.removeAllViews();
                    TableRow tr = new TableRow(getContext());
                    tr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    TextView column = new TextView(getContext());
                    column.setText("Query syntax fail.\nWe have tables:\n" +
                            "sensor_source\n" +
                            "channel \n " +
                            "data_record\n " +
                            "sample \n " +
                            "patient \n " +
                            "clinic\n" +
                            "And fail is: "+e.getMessage());
                    tr.addView(column);
                    tableResult.addView(tr, new TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
        });

        return v;
    }


}
