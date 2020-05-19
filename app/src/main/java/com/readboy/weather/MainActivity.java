package com.readboy.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.readboy.weather.CityList.CityListActivity;
import com.readboy.weather.gson.Forecast;
import com.readboy.weather.gson.Weather;
import com.readboy.weather.util.HttpUtil;
import com.readboy.weather.util.Utility;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


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
    private TextView tv_aqi_index;
    private TextView tv_pm25_index;
//    public final static String WEATHER_DATA_URL = "http://weather.dream.cn/index.php?intent=index&action=weather&area=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
//        String url = Constant.WEATHER_DATA_URL + areaId;
//        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
//        String weatherString=prefs.getString("weather",null);
//        if (weatherString!=null){
//            //compile the weather data while having cache
//            Weather weather= Utility.handleWeatherResponse(weatherString);
//            showWeatherInfo(weather);
//        }else {
//            String weatherId=getIntent().getStringExtra("weather_id");
////            String weatherId="CN101280503";
//            Log.d("MainActivity","weatherId");
//            requestWeather(weatherId);
//        }

        String weatherId=getIntent().getStringExtra("weather_id");
//            String weatherId="CN101280503";中山CN101281701
        if (!("".equals(weatherId))){
            Log.d("MainActivity",weatherId+"");
            requestWeather(weatherId);
        }else {}

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
        tv_aqi_index=(TextView)findViewById(R.id.tv_aqi_index);
        tv_pm25_index=(TextView)findViewById(R.id.tv_pm25_index);

        iv_city_list.setOnClickListener(this);
        iv_search.setOnClickListener(this);
        tv_china_weather.setOnClickListener(this);
        tv_refresh.setOnClickListener(this);

    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.iv_city_list:
                Intent cityListIntent=new Intent(MainActivity.this, CityListActivity.class);
                startActivity(cityListIntent);
                break;
            case R.id.iv_search:
                Intent intent=new Intent(MainActivity.this, ChooseActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_china_weather:
                Uri uri = Uri.parse("https://m.weather.com.cn/d/town/index?lat=22.488641&lon=113.418838&areaid=101281701");
                Intent chinaWeatherIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(chinaWeatherIntent);
                break;
            case R.id.tv_refresh:
                //refresh
                break;
        }
    }

    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"onFailure获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.d("MainActivity","responseText"+responseText);
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null&&"ok".equals(weather.status)){
//                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
//                            editor.putString("weather",responseText);
//                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(MainActivity.this,"onResponse获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather){
        String cityName=weather.basic.cityName;
//        String date=weather.basic.update.updateTime.split(" ")[1];
        String date=weather.basic.update.updateTime.split(" ")[0];
        String degree=weather.now.tmperature+"℃";
        String info=weather.now.more.info;
        tv_area.setText(cityName);
        tv_date.setText(date);
        if (weather.aqi!=null){
            tv_aqi_index.setText(weather.aqi.city.aqi);
            tv_pm25_index.setText(weather.aqi.city.pm25);
        }
        tv_date_information.setText(info+","+degree);
        Forecast forecast1=weather.forecastList.get(0);
        tv_date1.setText(forecast1.date);
        tv_date1_information.setText(forecast1.more.info+"\n"+"最高温："+forecast1.temperature.max+"℃"+"\n最低温："+forecast1.temperature.min+"℃");
        Forecast forecast2=weather.forecastList.get(1);
        tv_date2.setText(forecast2.date+"\n");
        tv_date2_information.setText(forecast1.more.info+"\n"+"最高温："+forecast2.temperature.max+"℃"+"\n最低温："+forecast2.temperature.min+"℃");
    }

    // 获取地址信息
    private List<Address> getAddress(Location location) {
        List<Address> result = null;
        try {
            if (location != null) {
                Geocoder gc = new Geocoder(this, Locale.getDefault());
                result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
