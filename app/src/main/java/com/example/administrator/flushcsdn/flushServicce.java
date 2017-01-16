package com.example.administrator.flushcsdn;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.spdy.FrameReader;

import java.io.IOException;

/**
 * Created by Administrator on 2016/12/21.
 */

public class flushServicce extends Service {
    final Request request = new Request.Builder().url("http://blog.csdn.net/gediseer/article/details/53641296 ").build();
    private static Messenger mMessenger;
    public static boolean flag = true;
    int flushCount = 0;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            l.i("服务收到了客户端发来的信息");
            switch (msg.what) {
                case 0:
                    l.i("服务收到0信号 停止刷新");
                    flag = false;
                    break;
                case 1:
                    l.i("服务收到1信号 拿到client的messenger");
                    mMessenger = msg.replyTo;
                    Message m = new Message();
                    m.what = 1;
                    try {
                        mMessenger.send(m);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    l.i("服务收到2信号 开始刷");
                    startFlush();
                    break;
                default:
                    l.i("服务收到其他信号");

            }
        }
    };
    Messenger messenger = new Messenger(handler);


    public void flushCSDN() throws IOException {
        while (flag) {

        }


    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    public void startFlush() {
        l.i("开始刷新 第" + flushCount++ + "次 flag=" + flag);

        new Thread() {
            @Override
            public void run() {
                while (flag) {
                    Call call = new OkHttpClient().newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            l.e("网络请求失败");
                            if (mMessenger != null) {
                                Message msg = new Message();
                                msg.what = 0;
                                try {
                                    mMessenger.send(msg);
                                } catch (RemoteException e1) {
                                    l.e("连接到远程client异常");
                                    e1.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onResponse(Response response) throws IOException {
                            l.i("网络请求成功");
                            Message msg = new Message();
                            msg.what = 2;
                            msg.obj = response;
                            try {
                                if (mMessenger != null) {
                                    l.i("开始向client发送成功消息");
                                    mMessenger.send(msg);
                                }
                            } catch (RemoteException e) {
                                l.e("连接到远程client异常");
                                e.printStackTrace();
                            }
                        }
                    });
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        l.e("刷新线程停止 flag=" + flag);
    }

}
