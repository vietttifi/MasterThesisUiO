package no.uio.ifi.viettt.mscosa.MainFragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import no.uio.ifi.viettt.mscosa.DatabaseManagement.OSADBHelper;
import no.uio.ifi.viettt.mscosa.DatabaseVisualisationActivity;
import no.uio.ifi.viettt.mscosa.EDFExportActivity;
import no.uio.ifi.viettt.mscosa.MainActivity;
import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.RawQueryActivity;


/**
 * Created by viettt on 18/12/2016.
 */

public class MiningFragment extends Fragment {
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.mining_fragment_layout, container, false);


        ImageButton ibtn_visualise = (ImageButton) v.findViewById(R.id.btnVisualise);
        ImageButton ibtn_raw_query = (ImageButton) v.findViewById(R.id.btnRawQuery);
        ImageButton ibtn_exportEDF = (ImageButton) v.findViewById(R.id.btnExportEDF);

        ibtn_raw_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), RawQueryActivity.class);
                myIntent.putExtra("key", "TEST"); //Optional parameters
                getActivity().startActivity(myIntent);
            }
        });

        ibtn_visualise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), DatabaseVisualisationActivity.class);
                myIntent.putExtra("key", "TEST"); //Optional parameters
                getActivity().startActivity(myIntent);
            }
        });

        ibtn_exportEDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), EDFExportActivity.class);
                myIntent.putExtra("key", "TEST"); //Optional parameters
                getActivity().startActivity(myIntent);
            }
        });



        return v;
    }


}
