package com.readboy.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_area;
    private ImageView iv_search;
    private ImageView iv_city_list;
    private TextView tv_date;
    private TextView tv_date1;
    private TextView tv_date2;
    private TextView tv_date_information;
    private TextView tv_date1_information;
    private TextView tv_date2_information;
    private TextView tv_china_weather;
    private TextView tv_refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    private void init(){
        tv_area=(TextView)findViewById(R.id.tv_area);
        iv_city_list=(ImageView)findViewById(R.id.iv_city_list);
        iv_search=(ImageView)findViewById(R.id.iv_search);
        tv_date=(TextView)findViewById(R.id.tv_date);
        tv_date1=(TextView)findViewById(R.id.tv_date1);
        tv_date2=(TextView)findViewById(R.id.tv_date2);
        tv_date_information=(TextView)findViewById(R.id.tv_date_information);
        tv_date1_information=(TextView)findViewById(R.id.tv_date1_information);
        tv_date2_information=(TextView)findViewById(R.id.tv_date2_information);
        tv_china_weather=(TextView)findViewById(R.id.tv_china_weather);
        tv_refresh=(TextView)findViewById(R.id.tv_refresh);

        tv_area.setOnClickListener(this);
        iv_city_list.setOnClickListener(this);
        iv_search.setOnClickListener(this);
        tv_date.setOnClickListener(this);
        tv_date1.setOnClickListener(this);
        tv_date2.setOnClickListener(this);
        tv_date_information.setOnClickListener(this);
        tv_date1_information.setOnClickListener(this);
        tv_date2_information.setOnClickListener(this);
        tv_china_weather.setOnClickListener(this);
        tv_refresh.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.iv_city_list:
                //
                break;
            case R.id.iv_search:
//                Intent intent=new Intent(MainActivity.this,)
                break;
            case R.id.tv_china_weather:
                //
                break;
            case R.id.tv_refresh:
                //refresh
                break;
        }
    }
}
