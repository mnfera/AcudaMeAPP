package com.mgtech.acudame.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mgtech.acudame.R;
import com.mgtech.acudame.model.CostumItem;

import java.util.ArrayList;

public class AdapterSpinner extends ArrayAdapter {

    public AdapterSpinner(@NonNull Context context, @Deprecated ArrayList<CostumItem> costumItems) {
        super(context, 0, costumItems);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if( convertView == null ){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_spinner_layout, parent, false);
        }

        CostumItem item = (CostumItem) getItem(position);
        ImageView spinnerIV2=convertView.findViewById(R.id.imageLayout);
        TextView spinnerTV2=convertView.findViewById(R.id.textLayout);

        if(item !=  null){
            spinnerIV2.setImageResource(item.getSpinnerItemImage());
            spinnerTV2.setText(item.getSpinnerItemName());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if( convertView == null ){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.costum, parent, false);
        }

        CostumItem item = (CostumItem) getItem(position);
        ImageView spinnerIV1=convertView.findViewById(R.id.image);
        TextView spinnerTV1=convertView.findViewById(R.id.text);

        if(item !=  null){
            spinnerIV1.setImageResource(item.getSpinnerItemImage());
            spinnerTV1.setText(item.getSpinnerItemName());
        }

        return convertView;
    }
}
