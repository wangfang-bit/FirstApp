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
import android.widget.TextView;
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

public class RateActivity extends AppCompatActivity implements Runnable{
    private final String TAG="Rate";
    private float dollarRate=0.0f;
    private float euroRate=0.0f;
    private float wonRate=0.0f;
    private String updateDate = "";

    EditText rmb;
    TextView show;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);
        rmb = (EditText) findViewById(R.id.rmb);
        show = (TextView) findViewById(R.id.showOut);

        //获取sp里保存的数据
        SharedPreferences SharedPreferences = getSharedPreferences("myrate", Activity.MODE_PRIVATE);
        /*SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);*/
        dollarRate = SharedPreferences.getFloat("dollar_rate",0.0f);
        euroRate = SharedPreferences.getFloat("euro_rate",0.0f);
        wonRate = SharedPreferences.getFloat("won_rate",0.0f);
        updateDate = SharedPreferences.getString("update_date","");

        //获得当前系统时间
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        final String todayStr = sdf.format(today);

        Log.i(TAG,"onCreate: sp dollarRate=" + dollarRate);
        Log.i(TAG,"onCreate: sp euroRate=" + euroRate);
        Log.i(TAG,"onCreate: sp wonRate=" + wonRate);
        Log.i(TAG,"onCreate: sp updateDate=" + updateDate);
        Log.i(TAG,"onCreate: todayStr=" + todayStr);
        //判断时间
        if(!todayStr.equals(updateDate)){
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
                    dollarRate = bdl.getFloat("dollar_rate");
                    euroRate = bdl.getFloat("euro_rate");
                    wonRate = bdl.getFloat("won_rate");

                    Log.i(TAG,"handleMessage: dollarRate:"+dollarRate);
                    Log.i(TAG,"handleMessage: euroRate:"+euroRate);
                    Log.i(TAG,"handleMessage: wonRate:"+wonRate);

                    //保存更新的日期
                    SharedPreferences sharedPreferences = getSharedPreferences("myrate", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("dollar_rate",dollarRate);
                    editor.putFloat("euro_rate",euroRate);
                    editor.putFloat("won_rate",wonRate);
                    editor.putString("update_date",todayStr);
                    editor.apply();

                    Toast.makeText(RateActivity.this,"汇率已更新",Toast.LENGTH_SHORT).show();
                }
                super.handleMessage(msg);
            }
        };
    }

    public void onClick(View btn){

        //获取用户输入内容
        Log.i(TAG,"onClick");
        String str=rmb.getText().toString();
        float r=0;
        if(str.length()>0) {
            r = Float.parseFloat(str);
        }
        else{
            //提示用户输入内容
            Toast.makeText(this,"请输入金额",Toast.LENGTH_LONG).show();
            return;
        }

        Log.i(TAG,"onClick: r="+r);
        //计算
        if(btn.getId()==R.id.btn_dollar){
            show.setText(String.format("%.2f",r*dollarRate));
        }
        else if(btn.getId()==R.id.btn_euro){
            show.setText(String.format("%.2f",r*euroRate));
        }
        else if(btn.getId()==R.id.btn_won){
            show.setText(String.format("%.2f",r*wonRate));
        }
    }

    public void openOne(View btn){
        //打开一个页面Activity
        openConfig();
    }

    private void openConfig() {
        Intent config = new Intent(this, ConfigActivity.class);
        config.putExtra("dollar_rate_key", dollarRate);
        config.putExtra("euro_rate_key", euroRate);
        config.putExtra("won_rate_key", wonRate);

        Log.i(TAG, "openOne:dollarRate=" + dollarRate);
        Log.i(TAG, "openOne:euroRate=" + euroRate);
        Log.i(TAG, "openOne:wonRate=" + wonRate);

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
        if(item.getItemId()==R.id.menu_set){
            openConfig();
        }else if(item.getItemId()==R.id.open_list){
            //打开列表窗口
            Intent list = new Intent(this, MyList2Activity.class);
            startActivity(list);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if(requestCode==1 && resultCode==2){

           Bundle bundle = data.getExtras();
           dollarRate = bundle.getFloat("key_Dollar",0.1f);
           euroRate = bundle.getFloat("key_Euro",0.1f);
           wonRate = bundle.getFloat("key_Won",0.1f);
           Log.i(TAG,"onActivityResult: dollarRate"+dollarRate);
           Log.i(TAG,"onActivityResult: euroRate"+euroRate);
           Log.i(TAG,"onActivityResult: wonRate"+wonRate);

           //将新设置的汇率写到sp里
           SharedPreferences sharedPreferences = getSharedPreferences("myrate", Activity.MODE_PRIVATE);
           SharedPreferences.Editor editor = sharedPreferences.edit();
           editor.putFloat("dollar_rate",dollarRate);
           editor.putFloat("euro_rate",euroRate);
           editor.putFloat("won_rate",wonRate);
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


        //获取网络数据
        /*URL url = null;
        try {
            url = new URL("www.usd-cny.com/bankofchina.htm");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            InputStream in = http.getInputStream();

            String html = inputStream2String(in);
            Log.i(TAG,"run:html=" +html);
            Document doc = Jsoup.parse(html);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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
            doc = Jsoup.connect("https://www.boc.cn/sourcedb/whpj/").get();
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

                if("美元".equals(str1)) {
                    bundle.putFloat("dollar_rate",100f/Float.parseFloat(val));
                }
                else if("欧元".equals(str1)) {
                    bundle.putFloat("euro_rate",100f/Float.parseFloat(val));
                }
                else if("韩国元".equals(str1)) {
                    bundle.putFloat("won_rate",100f/Float.parseFloat(val));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bundle;
    }

    private Bundle getFromUsdCyn() {
        Bundle bundle = new Bundle();
        Document doc = null;
        try {
            doc = Jsoup.connect("http://www.usd-cny.com/bankofchina.htm").get();
            Log.i(TAG,"run:" + doc.title());
            Elements tables = doc.getElementsByTag("table");

            Element table1 = tables.get(0);
            //Log.i(TAG,"run: table1=" + table1);
            //获取TD中的数值
            Elements tds = table1.getElementsByTag("td");

            for(int i=0;i<tds.size();i+=6){
                Element td1 = tds.get(i);
                Element td2 = tds.get(i+5);

                Log.i(TAG,"run:text="+td1.text());
                Log.i(TAG,"run:val="+td2.text());
                Log.i(TAG,"run: "+td1.text()+"==>"+td2.text());
                String str1 = td1.text();
                String val = td2.text();

                if("美元".equals(str1)) {
                    bundle.putFloat("dollar_rate",100f/Float.parseFloat(val));
                }
                else if("欧元".equals(str1)) {
                    bundle.putFloat("euro_rate",100f/Float.parseFloat(val));
                }
                else if("韩国元".equals(str1)) {
                    bundle.putFloat("won_rate",100f/Float.parseFloat(val));
                }
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

