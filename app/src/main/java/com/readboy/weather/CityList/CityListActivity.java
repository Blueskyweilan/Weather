package com.readboy.weather.CityList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.readboy.weather.MainActivity;
import com.readboy.weather.R;
import com.readboy.weather.ChooseActivity;

import java.util.ArrayList;
import java.util.List;

public class CityListActivity extends AppCompatActivity {
    private ImageView iv_back;
    private ImageView iv_add_city;
    private ListView lv_cities;
    private List<Area> areaList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
        iv_back=(ImageView)findViewById(R.id.iv_back);
        iv_add_city=(ImageView)findViewById(R.id.iv_add_city);
        lv_cities=(ListView)findViewById(R.id.lv_cities);
        initArea();
        AreaAdapter areaAdapter=new AreaAdapter(CityListActivity.this,R.layout.area_item,areaList);
        lv_cities.setAdapter(areaAdapter);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CityListActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        iv_add_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CityListActivity.this, ChooseActivity.class);
                startActivity(intent);
            }
        });

    }

    private void initArea(){

    }
}
