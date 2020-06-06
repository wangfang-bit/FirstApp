package com.swufe.firstapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class  QueryActivity extends AppCompatActivity implements Runnable{
    private final String TAG="key";
    private String but="";
    private String updateDate = "";

    EditText key;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        key = (EditText) findViewById(R.id.key);

        //获取sp里保存的数据
        SharedPreferences SharedPreferences = getSharedPreferences("myrate", Activity.MODE_PRIVATE);
        but = SharedPreferences.getString("Btu","");

        //获得当前系统时间
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        final String todayStr = sdf.format(today);
        Log.i(TAG,"onCreate: sp but=" + but);
        Log.i(TAG,"onCreate: sp updateDate=" + updateDate);
        Log.i(TAG,"onCreate: todayStr=" + todayStr);
        //判断时间
        if(!todayStr.equals(updateDate+6)){
            Log.i(TAG,"onCreate:需要更新");
            //开启子线程
            Thread t = new Thread(this);
            t.start();
        }else{
            Log.i(TAG,"onCreate:不需要更新");
        }
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what==5){
                    Bundle bdl = (Bundle) msg.obj;
                    but = bdl.getString("But");

                    Log.i(TAG,"handleMessage: but:"+but);
                    //保存更新的日期
                    SharedPreferences sharedPreferences = getSharedPreferences("myrate", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("But",but);
                    editor.putString("update_date",todayStr);
                    editor.apply();

                    Toast.makeText(QueryActivity.this,"信息已更新",Toast.LENGTH_SHORT).show();
                }
                super.handleMessage(msg);
            }
        };

    }
    public void onClick(View btn) {

        //获取用户输入内容
        Log.i(TAG, "onClick");
        String str = key.getText().toString();
        String s = "";
        if (str.length() > 0) {
            s = str;
        } else {
            //提示用户输入内容
            Toast.makeText(this, "请输入关键词", Toast.LENGTH_LONG).show();
            return;
        }
        Log.i(TAG,"onClick: s="+s);
    }

    public void openOne(View btn){
        //打开一个页面Activity
        openConfig();
    }
    private void openConfig() {
        Intent config = new Intent(this, ConfigActivity.class);
        config.putExtra("But_key", but);

        Log.i(TAG, "openOne:dollarRate=" + but);

        //startActivity(config);
        startActivityForResult(config, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rate,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.btn_query){
            //打开列表窗口
            Intent list = new Intent(this, MyList2Activity.class);
            startActivity(list);
        }else {
            openConfig();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1 && resultCode==2){

            Bundle bundle = data.getExtras();
            but = bundle.getString("key_but","");

            Log.i(TAG,"onActivityResult: but"+but);

            //将新设置的汇率写到sp里
            SharedPreferences sharedPreferences = getSharedPreferences("myquery", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("But",but);
            editor.commit();

            Log.i(TAG,"onActivityResult:数据已保存到sharedPreference");

        }

        super.onActivityResult(requestCode,resultCode,data);
    }
    @Override
    public void run() {
        Log.i(TAG,"run:run()....");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //用于保存获取的汇率
        Bundle bundle;

        bundle = getFromBOC();

        //bundle中保存所获取的汇率

        //获取msg对象，用于返回主线程
        Message msg = handler.obtainMessage(5);
        //msg.what = 5;
        //msg.obj = "Hello from run()";
        msg.obj = bundle;
        handler.sendMessage(msg);
    }

    private Bundle getFromBOC() {
        Bundle bundle = new Bundle();
        Document doc = null;
        try {
            doc = Jsoup.connect("http://www.swufe.edu.cn/index/tzgg.htm").get();
            //Log.i(TAG,"run:" + doc.title());
            Elements tables = doc.getElementsByTag("li id");

            Element table1 = tables.get(0);
            //获取TD中的数值
            Elements tds = table1.getElementsByTag("title");

            for(int i=0;i<tds.size();i++){
                Element td1 = tds.get(i);
                //Element td2 = tds.get(i+5);

                String str1 = td1.text();
                //String val = td2.text();

                Log.i(TAG,"run:"+str1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bundle;
    }

    private String inputStream2String(InputStream inputStream) throws IOException{
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream,"gb2312");
        for(; ;){
            int rsz = in.read(buffer,0,buffer.length);
            if(rsz<0)
                break;
            out.append(buffer,0,rsz);
        }
        return out.toString();
    }

}
