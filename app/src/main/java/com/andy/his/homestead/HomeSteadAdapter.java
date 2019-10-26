package com.andy.his.homestead;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.andy.his.R;

import java.util.ArrayList;

public class HomeSteadAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<HomeSteadTableDetail> homeSteadTableDetails;

    public HomeSteadAdapter() {}

    public HomeSteadAdapter(ArrayList<HomeSteadTableDetail> homeSteadTableDetails, Context context) {
        this.homeSteadTableDetails = homeSteadTableDetails;
        this.context = context;
    }
    @Override
    public int getCount() {
        return homeSteadTableDetails.size();
    }

    @Override
    public Object getItem(int position) {
        return homeSteadTableDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        HomeSteadAdapter.HomeSteadViewHolder holder = null;
        if(convertView == null)
        {
            holder = new HomeSteadAdapter.HomeSteadViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.homestead_item_list, parent, false);

            final int pos = position;
            holder.moduleID = (TextView) convertView.findViewById(R.id.moduleID);
            holder.homeSteadID = (TextView) convertView.findViewById(R.id.homeSteadID);
            holder.moduleInfo = (TextView) convertView.findViewById(R.id.moduleInfo);
            holder.homeSteadNumber = (TextView) convertView.findViewById(R.id.homeSteadNumber);
            holder.houseHolder = (TextView) convertView.findViewById(R.id.houseHolder);
            holder.cbxStatus = (CheckBox) convertView.findViewById(R.id.cbxStatus);

            holder.cbxStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((HomeSteadActivity)context).putCheckboxStatus(homeSteadTableDetails.get(pos), ((CheckBox)v).isChecked());
                }
            });

            convertView.setTag(holder);
        }
        else {
            holder = (HomeSteadAdapter.HomeSteadViewHolder) convertView.getTag();
        }

        holder.moduleID.setText(String.valueOf(homeSteadTableDetails.get(position).getModuleID()));
        holder.homeSteadID.setText(String.valueOf(homeSteadTableDetails.get(position).getHomeSteadID()));
        holder.moduleInfo.setText(homeSteadTableDetails.get(position).getModuleInfo());
        holder.homeSteadNumber.setText(homeSteadTableDetails.get(position).getHomeSteadNumber());
        holder.houseHolder.setText(homeSteadTableDetails.get(position).getHouseHolder());
        holder.cbxStatus.setChecked(false);

        return convertView;
    }

    private class HomeSteadViewHolder {

        private TextView moduleID;

        private TextView homeSteadID;

        private TextView moduleInfo;

        private TextView homeSteadNumber;

        private TextView houseHolder;

        private CheckBox cbxStatus;
    }
}
