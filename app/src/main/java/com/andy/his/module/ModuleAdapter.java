package com.andy.his.module;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.andy.his.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ModuleAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ModuleDetail> moduleDetails;

    public ModuleAdapter() {}

    public ModuleAdapter(ArrayList<ModuleDetail> moduleDetails, Context context) {
        this.moduleDetails = moduleDetails;
        this.context = context;
    }

    @Override
    public int getCount() {
        return moduleDetails.size();
    }

    @Override
    public Object getItem(int position) {
        return moduleDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ModuleViewHolder holder = null;
        if(convertView == null)
        {
            holder = new ModuleViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.module_item_list, parent, false);

            final int pos = position;
            holder.moduleID = (TextView) convertView.findViewById(R.id.moduleID);
            holder.moduleCounty = (TextView) convertView.findViewById(R.id.moduleCounty);
            holder.moduleTown = (TextView) convertView.findViewById(R.id.moduleTown);
            holder.moduleVillage = (TextView) convertView.findViewById(R.id.moduleVillage);
            holder.moduleGroup = (TextView) convertView.findViewById(R.id.moduleGroup);
            holder.cbxStatus = (CheckBox) convertView.findViewById(R.id.cbxStatus);

            holder.cbxStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((ModuleActivity)context).putCheckboxStatus(moduleDetails.get(pos), ((CheckBox)v).isChecked());
                }
            });

            convertView.setTag(holder);
        }
        else {
            holder = (ModuleViewHolder) convertView.getTag();
        }

        holder.moduleID.setText(String.valueOf(moduleDetails.get(position).getModuleID()));
        holder.moduleCounty.setText(moduleDetails.get(position).getModuleCounty());
        holder.moduleTown.setText(moduleDetails.get(position).getModuleTown());
        holder.moduleVillage.setText(moduleDetails.get(position).getModuleVillage());
        holder.moduleGroup.setText(moduleDetails.get(position).getModuleGroup());
        holder.cbxStatus.setChecked(false);

        return convertView;
    }

    public ModuleDetail getModuleDetailByID(int moduleID){

        for(ModuleDetail detail : moduleDetails)
        {
            if (detail.getModuleID() == moduleID)
            {
                return detail;
            }
        }

        return null;
    }

    private class ModuleViewHolder {

        private TextView moduleID;

        private TextView moduleCounty;

        private TextView moduleTown;

        private TextView moduleVillage;

        private TextView moduleGroup;

        private CheckBox cbxStatus;
    }
    
}