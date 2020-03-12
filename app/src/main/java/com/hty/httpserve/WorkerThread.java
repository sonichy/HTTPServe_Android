//https://blog.csdn.net/jkeven/article/details/9271145
package com.hty.httpserve;

import android.util.Log;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;

public class WorkerThread extends Thread {
    private final HttpService httpservice;
    private final HttpServerConnection conn;

    public WorkerThread(final HttpService httpservice, final HttpServerConnection conn) {
        super();
        this.httpservice = httpservice;
        this.conn = conn;
    }

    @Override
    public void run() {
        Log.e(Thread.currentThread().getStackTrace()[2] + "", "New connection thread");
        HttpContext context = new BasicHttpContext(null);
        try {
            while (!Thread.interrupted() && this.conn.isOpen()) {
                this.httpservice.handleRequest(this.conn, context);
            }
        } catch (Exception e) {
            Log.e(Thread.currentThread().getStackTrace()[2] + "", e.toString());
        } finally {
            try {
                this.conn.shutdown();
            } catch (Exception e) {
                Log.e(Thread.currentThread().getStackTrace()[2] + "", e.toString());
            }
        }
    }
}