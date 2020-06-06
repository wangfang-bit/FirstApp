package com.swufe.firstapp;

import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends ListActivity implements Runnable{
    String TAG = "Search";
    Handler handler;
    EditText input;
    private String updateDate = "";
    private ArrayList<HashMap<String, String>> listItems;
    private SimpleAdapter listItemAdapter;//适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        final ListView listView = findViewById(R.id.result);
        input = findViewById(R.id.key);
        final String str = input.toString();

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what==7){
                    List<HashMap<String,String>> list2 = (List<HashMap<String,String>>)msg.obj;
                    listItemAdapter = new SimpleAdapter(SearchActivity.this, listItems,//listItems数据源
                            R.layout.list_item,//ListItem的XML的布局实现
                            new String[]{"ItemTitle", "ItemDetail"},
                            new int[]{R.id.itemTitle, R.id.itemDetail}
                    );
                    setListAdapter(listItemAdapter);

                    //保存更新的日期
                    SharedPreferences sharedPreferences = getSharedPreferences("myResult", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("update_date",todayStr);
                    editor.apply();

                    Toast.makeText(SearchActivity.this,"汇率已更新",Toast.LENGTH_SHORT).show();

                }
                super.handleMessage(msg);
            }
        };
    }
    android.content.SharedPreferences SharedPreferences = getSharedPreferences("myResult", Activity.MODE_PRIVATE);


    Date today = Calendar.getInstance().getTime();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
    final String todayStr = sdf.format(today);


    private void initListView() {
        listItems = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < 10; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("ItemTitle", "Rate:" + i);//标题文字
            map.put("ItemDetail", "Detail:" + i);//详情描述
            listItems.add(map);
        }
        //生成适配器的Item和动态数组对应的元素
        listItemAdapter = new SimpleAdapter(this, listItems,//listItems数据源
                R.layout.list_item,//ListItem的XML的布局实现
                new String[]{"ItemTitle", "ItemDetail"},
                new int[]{R.id.itemTitle, R.id.itemDetail}
        );
    }

    @Override
    public void run() {
        //获取网络数据，带回到主线程中
        List<HashMap<String, String>> retList = new ArrayList<>();
        Document doc = null;
        try {
            Thread.sleep(3000);
            doc = Jsoup.connect("http://www.swufe.edu.cn/index/tzgg.htm").get();
            Log.i(TAG,"run:" + doc.title());
            Elements spans = doc.getElementsByTag("span");
            for(int i=7;i<45;i+=2){
                Element titles = spans.get(i);
                Log.i(TAG,"run:" + titles.text());

                String txt = titles.text();


            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Message msg = handler.obtainMessage(7);
        msg.obj = retList;
        handler.sendMessage(msg);

    }
}

