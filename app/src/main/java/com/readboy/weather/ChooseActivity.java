package com.readboy.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.readboy.weather.CityList.Area;
import com.readboy.weather.db.City;
import com.readboy.weather.db.Country;
import com.readboy.weather.db.Province;
import com.readboy.weather.gson.Weather;
import com.readboy.weather.util.HttpUtil;
import com.readboy.weather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseActivity extends AppCompatActivity {
    private static final int LEVEL_PROVINCE=0;
    private static final int LEVEL_CITY=1;
    private static final int LEVEL_COUNTRY=2;
    private ProgressDialog progressDialog;
    private TextView tv_title;
    private Button bt_back;
    private ListView lv_area_list;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
//        Log.d("ChooseActivity",currentLevel+"currentLevel");//初始为0
        tv_title=(TextView)findViewById(R.id.tv_title);
        bt_back=(Button)findViewById(R.id.bt_back);
        lv_area_list=(ListView)findViewById(R.id.lv_area_list);
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dataList);
        lv_area_list.setAdapter(adapter);
        lv_area_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCountries();
                }else if (currentLevel==LEVEL_COUNTRY){
                    String weatherId=countryList.get(position).getWeatherId();
//                    Intent intent=new Intent(ChooseActivity.this,MainActivity.class);
//                    intent.putExtra("weather_id",weatherId);
//                    startActivity(intent);
//                    finish();
                    Log.d("ChooseActivity",weatherId+"");
                    saveSelectedWeather(weatherId);
                }
            }
        });
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTRY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces(){
        tv_title.setText("中国");
        bt_back.setVisibility(View.GONE);//hint the back button
        provinceList= DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            Log.d("ChooseActivity","datalist："+dataList.size());
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            lv_area_list.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFormServer(address,"province");
        }
    }

    private void queryCities(){
        tv_title.setText(selectedProvince.getProvinceName());
        bt_back.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceId = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            lv_area_list.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFormServer(address,"city");
        }
    }

    private void queryCountries(){
        tv_title.setText(selectedCity.getCityName());
        bt_back.setVisibility(View.VISIBLE);
        countryList=DataSupport.where("cityId = ?",String.valueOf(selectedCity.getId())).find(Country.class);
        if (countryList.size()>0){
            dataList.clear();
            for (Country country:countryList){
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            lv_area_list.setSelection(0);
            currentLevel=LEVEL_COUNTRY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFormServer(address,"country");
        }
    }

    private void queryFormServer(final String address, final String type){
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(ChooseActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                Log.d("ChooseActivity",address+"的responseText："+responseText);
                //responseText
                //[{"id":205,"name":"广州"},{"id":206,"name":"韶关"},{"id":207,"name":"惠州"},{"id":208,"name":"梅州"},{"id":209,"name":"汕头"},{"id":210,"name":"深圳"},{"id":211,"name":"珠海"},{"id":212,"name":"顺德"},{"id":213,"name":"肇庆"},{"id":214,"name":"湛江"},{"id":215,"name":"江门"},{"id":216,"name":"河源"},{"id":217,"name":"清远"},{"id":218,"name":"云浮"},{"id":219,"name":"潮州"},{"id":220,"name":"东莞"},{"id":221,"name":"中山"},{"id":222,"name":"阳江"},{"id":223,"name":"揭阳"},{"id":224,"name":"茂名"},{"id":225,"name":"汕尾"},{"id":350,"name":"佛山"}]
                //[{"id":1615,"name":"中山","weather_id":"CN101281701"}]
                //CN101281701
                boolean result=false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("country".equals(type)){
                    result = Utility.handleCountriesResponse(responseText,selectedCity.getId());
                }
                if (result){
                    ChooseActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();;
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("country".equals(type)){
                                queryCountries();
                            }
                        }
                    });
                }
            }
        });
    }

    public void saveSelectedWeather(final String weatherId){
        List<Area> areas=DataSupport.findAll(Area.class);
        for (Area areaTmp:areas){
            if (areaTmp.getAreaId().equals(weatherId)){
                Toast.makeText(ChooseActivity.this,"该城市已在列表中",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (DataSupport.count(Area.class)>5){
            Toast.makeText(ChooseActivity.this,"城市列表超过5条，不允许再添加",Toast.LENGTH_SHORT).show();
            return;
        }
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChooseActivity.this,"onFailure获取天气信息失败",Toast.LENGTH_SHORT).show();
                }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
//                Log.d("ChooseActivity","saveSelectedWeather"+responseText);
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null&&"ok".equals(weather.status)){
                            String degree=weather.now.tmperature+"℃";
                            String info=weather.now.more.info;
                            Area area=new Area();
                            area.setLocal(false);
                            area.setAreaId(weatherId);
                            area.setCountry(weather.basic.cityName);
                            area.setProvince(selectedProvince.getProvinceName());
                            area.setWeatherData(info+","+degree);
                            area.save();
                            Toast.makeText(ChooseActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
//                            Log.d("ChooseActivity","save保存成功"+area.getAreaId()+" "+area.getCountry()+" "+area.getProvince()+" "+area.getWeatherData());
                        }else {
                            Toast.makeText(ChooseActivity.this,"onResponse获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
