package no.uio.ifi.viettt.mscosa.interfacesAndHelpClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import no.uio.ifi.viettt.mscosa.R;
import no.uio.ifi.viettt.mscosa.SensorsObjects.SensorSource;

/**
 * Created by viettt on 21/12/2016.
 */

public class ConnectedListAdapter extends BaseAdapter {

    static class ViewElem{
        ImageView imageView_logo;
        TextView lbl_sensor_source_ID;
        TextView lbl_source_name;
        TextView lbl_SenStatus;
    }

    private List<SensorSource> listData;
    private LayoutInflater layoutInflater;

    public ConnectedListAdapter(Context aContext,  List<SensorSource> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    public void add(SensorSource s){
        listData.add(s);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int i) {
        if(i<0 || i >= listData.size()) return null;
        return listData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewElem holder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_items_connected_layout, null);
            holder = new ViewElem();
            holder.imageView_logo = (ImageView) view.findViewById(R.id.imageView_logo);
            holder.lbl_sensor_source_ID = (TextView) view.findViewById(R.id.lbl_source_ID);
            holder.lbl_source_name = (TextView) view.findViewById(R.id.lbl_source_name);
            holder.lbl_SenStatus = (TextView) view.findViewById(R.id.lblSenStatus);
            view.setTag(holder);
        } else {
            holder = (ViewElem) view.getTag();
        }

        SensorSource source_s = this.listData.get(i);
        holder.lbl_sensor_source_ID.setText(source_s.getSource_id());
        holder.lbl_source_name.setText(source_s.getSource_name());
        holder.lbl_SenStatus.setText(source_s.source_status);
        holder.imageView_logo.setImageResource(source_s.logo_in_drawable);

        return view;
    }
}
