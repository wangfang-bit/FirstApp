package com.swufe.firstapp;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MyList2Activity extends ListActivity implements Runnable{

    Handler handler;
    private ArrayList<HashMap<String, String>> listItems;//存放文字、图片信息
    private SimpleAdapter listItemAdapter;//适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListView();
        Thread t = new Thread(this);
        t.start();

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what==7){
                    List<HashMap<String,String>> list2 = (List<HashMap<String,String>>)msg.obj;
                    listItemAdapter = new SimpleAdapter(MyList2Activity.this, listItems,//listItems数据源
                            R.layout.list_item,//ListItem的XML的布局实现
                            new String[]{"ItemTitle", "ItemDetail"},
                            new int[]{R.id.itemTitle, R.id.itemDetail}
                    );
                   setListAdapter(listItemAdapter);
                }
                super.handleMessage(msg);
            }
        };
    }
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
            Elements tables = doc.getElementsByTag("table");

            Element table2 = tables.get(1);
            //获取TD中的数值
            Elements tds = table2.getElementsByTag("td");

            for(int i=0;i<tds.size();i+=8){
                Element td1 = tds.get(i);
                Element td2 = tds.get(i+5);

                String str1 = td1.text();
                String val = td2.text();

                Log.i(TAG,"run:"+str1+"==>"+val);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ItemTitle",str1);
                map.put("ItemDetail",val);
                retList.add(map);
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
