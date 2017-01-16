package com.example.administrator.flushcsdn;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Button start;
    Button stop;
    Button flush;
    Button stop_flush;
    Messenger rMessenger;
    Messenger mMessenger;
    TextView tv;
    String sysout = "";
    flushServicce fs;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            l.i("客户端收到了" + msg.what);
            if (sysout.length() >= 200) {
                sysout = "";
            }
            switch (msg.what) {
                case 0:
                    l.i("服务端告诉说网络请求失败");
                    tv.setText(sysout += ("请求失败" + System.currentTimeMillis() + "\n"));

                    break;
                case 1:
                    l.i("客户端第一次收到服务端的信息");
                    break;
                case 2:
                    l.i("请求成功");
                    tv.setText(sysout += ("请求成功！" + ((Response) msg.obj).isSuccessful() + "\n"));
                    break;

            }
        }

    };
    ServiceConnection fc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            l.i("链接建立了");
            rMessenger = new Messenger(service);
            mMessenger = new Messenger(handler);

            Message msg = new Message();
            msg.replyTo = mMessenger;
            msg.what = 1;
            try {
                rMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            l.e("断开与服务的链接");
            rMessenger = null;

        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        flush = (Button) findViewById(R.id.flush);
        stop_flush = (Button) findViewById(R.id.stop_flush);
        tv = (TextView) findViewById(R.id.tv);
        tv.setText(sysout);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, flushServicce.class);
                bindService(intent, fc, BIND_AUTO_CREATE);

            }
        });
        flush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rMessenger != null) {
                    Message msg = new Message();
                    msg.what = 2;
                    try {
                        rMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        stop_flush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = 0;
                try {
                    rMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, flushServicce.class);
                stopService(intent);
                unbindService(fc);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(fc);
    }

}
