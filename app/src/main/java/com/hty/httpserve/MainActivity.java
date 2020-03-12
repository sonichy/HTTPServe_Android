package com.hty.httpserve;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
//import android.text.TextUtils;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private EditText editText_ip, editText_port;
    private TextView textView_info, textView_log;
    private Button button_start, button_clear;
    boolean isStop = true;
    RequestListenerThread thread;
    Timer timer;
    TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        editText_ip = (EditText) findViewById(R.id.editText_ip);
        editText_port = (EditText) findViewById(R.id.editText_port);
        button_start = (Button) findViewById(R.id.button_start);
        button_start.setOnClickListener(new ClickListener());
        button_clear = (Button) findViewById(R.id.button_clear);
        button_clear.setOnClickListener(new ClickListener());
        textView_info = (TextView) findViewById(R.id.textView_info);
        textView_log = (TextView) findViewById(R.id.textView_log);
        textView_log.setMovementMethod(ScrollingMovementMethod.getInstance());

        String ip = getIp();
        if (ip.equals("")) {
            textView_info.setText("获取不到IP，请连接网络");
        }else{
            editText_ip.setText(ip);
        }

        Utils.writeFile("log.txt", "开始", Context.MODE_PRIVATE);
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    //Log.e(Thread.currentThread().getStackTrace()[2] + "", "timerTask");
                    Message message = new Message();
                    message.what = 0;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    Log.e(Thread.currentThread().getStackTrace()[2] + "", e.toString());
                }
            }
        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread.isStop = true;
        timer.cancel();
    }

    public String getIp(){
        //https://blog.csdn.net/u011068702/article/details/77870152
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(Thread.currentThread().getStackTrace()[2] + " get IP Address fail", e.toString());
            Utils.writeFile("log.txt", e.toString(), Context.MODE_APPEND);
            return "";
        }
        return "";
    }

    class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.button_start:
                    if (isStop) {
                        String ip = getIp();
                        //if (TextUtils.isEmpty(ip)) {
                        if (ip.equals("")) {
                            textView_info.setText("获取不到IP，请连接网络");
                        } else {
                            editText_ip.setText(ip);
                            try {
                                thread = new RequestListenerThread(Integer.parseInt(editText_port.getText().toString()));
                                thread.setDaemon(false);
                                thread.start();
                            } catch (Exception e) {
                                textView_info.setText(e.toString());
                            }
                            isStop = false;
                            editText_port.setEnabled(false);
                            button_start.setText("停止服务");
                            String str = "在浏览器上输入网址访问HTTP服务\nhttp://" + editText_ip.getText().toString() + ":" + editText_port.getText().toString();
                            textView_info.setText(str);
                            timer.schedule(timerTask, 0, 1000);
                        }
                    } else {
                        thread.isStop = true;
                        isStop = true;
                        editText_port.setEnabled(true);
                        button_start.setText("开始服务");
                        textView_info.setText("");
                        timer.cancel();
                    }
                    break;
                case R.id.button_clear:
                    Utils.writeFile("log.txt", "清空", Context.MODE_PRIVATE);
                    break;
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case 0:
                    textView_log.setText(Utils.readFile("log.txt"));
                    break;
            }
        }
    };

}