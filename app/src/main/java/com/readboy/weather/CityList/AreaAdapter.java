package com.readboy.weather.CityList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.readboy.weather.R;

import java.util.List;

public class AreaAdapter extends ArrayAdapter<Area> {
    private int resourceId;
    private ImageView iv_local;
    private TextView tv_country;
    private TextView tv_province;
    private TextView tv_area_data;
    public AreaAdapter(Context context, int textViewResourceId, List<Area> objects){
        super(context,textViewResourceId,objects);
        this.resourceId=textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Area area=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        iv_local=(ImageView)view.findViewById(R.id.iv_local);
        tv_country=(TextView)view.findViewById(R.id.tv_country);
        tv_province=(TextView)view.findViewById(R.id.tv_province);
        tv_area_data=(TextView)view.findViewById(R.id.tv_area_data);
        if (area.isLocal()){
            iv_local.setVisibility(View.VISIBLE);
        }else {
            iv_local.setVisibility(View.GONE);
        }
        tv_country.setText(area.getCountry());
        tv_province.setText(area.getProvince()+",中国");
        tv_area_data.setText(area.getWeatherData());

        return view;
    }
}
