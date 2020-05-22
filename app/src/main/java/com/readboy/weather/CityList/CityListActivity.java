package com.readboy.weather.CityList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.readboy.weather.MainActivity;
import com.readboy.weather.R;
import com.readboy.weather.ChooseActivity;

import org.litepal.crud.DataSupport;

//import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CityListActivity extends AppCompatActivity {
    private ImageView iv_back;
    private ImageView iv_add_city;
    private ListView lv_cities;
    private List<Area> areas;
    private String selectedWeatherId;
    private int selectedId;
    List<Map<String, Object>> mlistItems;
    private AreaAdapter areaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
        iv_back=(ImageView)findViewById(R.id.iv_back);
        iv_add_city=(ImageView)findViewById(R.id.iv_add_city);
        lv_cities=(ListView)findViewById(R.id.lv_cities);

        this.registerForContextMenu(lv_cities);
//        registerForContextMenu(lv_cities);

        areas= DataSupport.findAll(Area.class);
        areaAdapter=new AreaAdapter(CityListActivity.this,R.layout.area_item,areas);
        lv_cities.setAdapter(areaAdapter);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent=new Intent(CityListActivity.this, MainActivity.class);
//                startActivity(intent);
//                finish();
            }
        });
        iv_add_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CityListActivity.this, ChooseActivity.class);
                startActivity(intent);
            }
        });
        lv_cities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedWeatherId=areas.get(position).getAreaId();
                for(int i=0;i<areas.size();i++){
                    if (i==position){
                        areas.get(i).setLocal(true);
//                        Log.d("CityListActivity","修改"+areas.get(i).isLocal());
                    }else {
                        areas.get(i).setLocal(false);
                    }
                    areas.get(i).save();
                }
//                Log.d("CityListActivity","修改position"+areas.get(position).isLocal());
                Intent intent=new Intent(CityListActivity.this,MainActivity.class);
                intent.putExtra("weather_id",selectedWeatherId);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("选择操作");
        menu.add(0, 1, Menu.NONE, "删除");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case 1:
                //删除
                int pos = (int) lv_cities.getAdapter().getItemId(menuInfo.position);
                DataSupport.deleteAll(Area.class,"areaId = ?",areas.get(pos).getAreaId());
//                if (areas.remove(pos) != null) {
//
//                    System.out.println("success");
//
//                } else {
//                    System.out.println("failed");
//                }
                areas.remove(pos);
                areaAdapter.notifyDataSetChanged();
                Toast.makeText(getBaseContext(), "删除此项", Toast.LENGTH_SHORT).show();
                break;
            default:
                //标记
                return super.onContextItemSelected(item);
        }
        return true;
    }



}
