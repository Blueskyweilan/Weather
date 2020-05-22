package com.readboy.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.readboy.weather.CityList.Area;
import com.readboy.weather.CityList.CityListActivity;
import com.readboy.weather.db.City;
import com.readboy.weather.db.Country;
import com.readboy.weather.db.Province;
import com.readboy.weather.gson.Forecast;
import com.readboy.weather.gson.Weather;
import com.readboy.weather.util.HttpUtil;
import com.readboy.weather.util.Utility;

import org.json.*;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
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
    private String TAG="MainActivity";
    private String country;
    private String locality;
    public LocationClient mLoctionClient;
    private static final int LEVEL_CITY=1;
    private static final int LEVEL_DISTRICT=2;
    private int currentLevel;
    private String currentProvince;
    private String currentCity;
    private String currentDistrict;
    private int currentProvinceId;
    private int currentCityId;
    private String currentWeatherId;
    private String currentArea;
    private boolean flag=true;
    private boolean exit=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String weather_id = bundle.getString("weather_id");
            if (weather_id != null ) {
                flag=false;
                Toast.makeText(MainActivity.this, "weather_id:" + weather_id , Toast.LENGTH_SHORT).show();
                requestWeather(weather_id);
            }
        }
//        Log.d(TAG,"first:"+flag);
        if (flag){
            init();
        }
    }

    private void init(){

//        LitePal.getDatabase();

//        List<Area> areaList= DataSupport.findAll(Area.class);
//        for (Area area:areaList){
//            Log.d("MainActivity","isLocal:"+area.isLocal());
//        }

        mLoctionClient=new LocationClient(getApplicationContext());
        mLoctionClient.registerLocationListener(new MyLocationListener());

        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] pemissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,pemissions,1);
        }else {
            requsetLocation();
        }

        LocationClientOption option=new LocationClientOption();
        option.setIsNeedAddress(true);
        mLoctionClient.setLocOption(option);

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
        List<Area> areaList=DataSupport.findAll(Area.class);
        for (Area area1:areaList){
            if (area1.getAreaId().equals(weatherId)){
                area1.setLocal(true);
                exit=true;
            }else {
                area1.setLocal(false);
            }
            area1.save();
        }

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
//                Log.d("MainActivity","responseText"+responseText);
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null&&"ok".equals(weather.status)){
                            if (!exit){
                                String degree=weather.now.tmperature+"℃";
                                String info=weather.now.more.info;
                                Area area=new Area();
                                area.setLocal(true);
                                area.setAreaId(weatherId);
                                area.setCountry(weather.basic.cityName);
                                area.setProvince(currentProvince);
                                area.setWeatherData(info+","+degree);
                                area.save();
                                List<Area> areaList=DataSupport.findAll(Area.class);
                                for (Area area1:areaList){
                                    if (area1.getAreaId().equals(weatherId)){
                                        area1.setLocal(true);
                                    }else {
                                        area1.setLocal(false);
                                    }
                                }
                            }

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

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition=new StringBuilder();
                    //location.getCityCode() 187
                    //location.getLocationID() _aWo6r78qaSj_765saH04rXt--Wcv--Zx-Gew5bhlOD4q93U1f2tpdaB3o_Q1fP3zp6cvYael5nWmozXx52Rwe67vra867itseWq5bak5OOn_fykq__GzQ0CBf..
                    //location.getAdCode() 442000
                    //location.getAddrStr() 中国广东省中山市五桂山街道长逸路38
                    currentProvince=location.getProvince().substring(0,location.getProvince().indexOf("省"));
//                    Log.d(TAG,currentProvince);
                    currentCity=location.getCity().substring(0,location.getCity().indexOf("市"));
//                    Log.d(TAG,currentCity);
                    if (location.getDistrict().equals("")){
                        currentLevel=LEVEL_CITY;
                        currentArea=currentCity;
                        currentPosition.append(location.getCity());
                    }else {
                        currentLevel=LEVEL_DISTRICT;
                        currentDistrict=location.getDistrict();
                        currentArea=currentDistrict;
                        currentPosition.append(location.getDistrict());
                    }
//                    Log.d(TAG,currentPosition+"");
//                    tv_area.setText(currentPosition);
                    if (location.getLocType()==BDLocation.TypeGpsLocation){
                        Log.d(TAG,"GPS");
                    }else if (location.getLocType()==BDLocation.TypeNetWorkLocation){
                        Log.d(TAG,"NETWORK");
                    }
                }
            });
            String address="http://guolin.tech/api/china";
            queryProvince(address);

        }

    }

    public void requsetLocation(){
        mLoctionClient.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result:grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(MainActivity.this,"需要同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requsetLocation();
                }else {
                    Toast.makeText(MainActivity.this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public void queryProvince(final String address){
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
//                Log.d(TAG,address+"的responseText："+responseText);
                if (!TextUtils.isEmpty(responseText)){
                    try {
                        JSONArray allProvinces = new JSONArray(responseText);
                        for (int i=0;i<allProvinces.length();i++){
                            JSONObject provinceObject = allProvinces.getJSONObject(i);
                            if (provinceObject.getString("name").equals(currentProvince)){
                                currentProvinceId=provinceObject.getInt("id");
                                String address1="http://guolin.tech/api/china/"+currentProvinceId;
                                queryCity(address1);
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void queryCity(final  String address){
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
//                Log.d(TAG,address+"的responseText："+responseText);
                if (!TextUtils.isEmpty(responseText)){
                    try {
                        JSONArray allCities = new JSONArray(responseText);
                        for (int i=0;i<allCities.length();i++){
                            JSONObject cityObject = allCities.getJSONObject(i);
                            if (cityObject.getString("name").equals(currentCity)){
                                currentCityId=cityObject.getInt("id");
                                String address2="http://guolin.tech/api/china/"+currentProvinceId+"/"+currentCityId;
                                queryCountry(address2);
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void queryCountry(final String address){

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
//                Log.d(TAG,address+"的responseText："+responseText);
                if (!TextUtils.isEmpty(responseText)){
                    try {
                        JSONArray allCountries = new JSONArray(responseText);
                        for (int i=0;i<allCountries.length();i++){
                            JSONObject countryObject = allCountries.getJSONObject(i);
                            if (countryObject.getString("name").equals(currentArea)){
                                currentWeatherId=countryObject.getString("weather_id");
//                                Log.d(TAG,"queryCountry:"+currentWeatherId);
                                if (flag){
                                    requestWeather(currentWeatherId);
                                }
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
