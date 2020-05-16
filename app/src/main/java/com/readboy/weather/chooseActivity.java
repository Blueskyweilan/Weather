package com.readboy.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.readboy.weather.db.City;
import com.readboy.weather.db.Country;
import com.readboy.weather.db.Province;
import com.readboy.weather.util.HttpUtil;
import com.readboy.weather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class chooseActivity extends AppCompatActivity {
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

    private void queryFormServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                closeProgressDialog();
                Toast.makeText(chooseActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("country".equals(type)){
                    result = Utility.handleCountriesResponse(responseText,selectedCity.getId());
                }
                if (result){
                    chooseActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
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

    private void showProgressDialog(){

    }

    private void closeProgressDialog(){

    }
}