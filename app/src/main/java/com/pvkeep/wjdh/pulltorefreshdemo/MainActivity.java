package com.pvkeep.wjdh.pulltorefreshdemo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements RefreshableView.RefreshListener {

    private RefreshableView rootViewLl;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == 0){
                //完成加载
                rootViewLl.finishRefresh();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootViewLl = (RefreshableView) findViewById(R.id.root_view);
        rootViewLl.setRefreshListener(this);
    }

    @Override
    public void onRefresh(RefreshableView view) {
        Log.i("luke", "开始刷新");

        //模拟加载网络
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    Message message = new Message();
                    message.what = 0;
                    handler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
