// https://blog.csdn.net/jkeven/article/details/9271145
package com.hty.httpserve;

import android.util.Log;

import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestListenerThread extends Thread {
    private final ServerSocket serversocket;
    private final HttpParams params;
    private final HttpService httpService;
    public boolean isStop = false;

    public RequestListenerThread(int port) throws IOException {
        this.serversocket = new ServerSocket(port);

        // Set up the HTTP protocol processor
        HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                        new ResponseDate(), new ResponseServer(),
                        new ResponseContent(), new ResponseConnControl() });

        this.params = new BasicHttpParams();
        this.params
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

        // Set up request handlers
        HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
        reqistry.register("*", new WebServiceHandler());  //WebServiceHandler用来处理webservice请求

        this.httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());
        httpService.setParams(this.params);
        httpService.setHandlerResolver(reqistry);		//为http服务设置注册好的请求处理器
    }

    @Override
    public void run() {
        Log.e(Thread.currentThread().getStackTrace()[2] + "", "Listening on port " + this.serversocket.getLocalPort());
        Log.e(Thread.currentThread().getStackTrace()[2] + "", "Thread.interrupted = " + Thread.interrupted());
        while (!isStop) {
            try {
                // Set up HTTP connection
                Socket socket = this.serversocket.accept();
                DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                Log.e(Thread.currentThread().getStackTrace()[2] + "", "Incoming connection from " + socket.getInetAddress());
                conn.bind(socket, this.params);

                // Start worker thread
                Thread t = new WorkerThread(this.httpService, conn);
                t.setDaemon(true);
                t.start();
            } catch (InterruptedIOException ex) {
                break;
            } catch (IOException e) {
                Log.e(Thread.currentThread().getStackTrace()[2] + "", "I/O error initialising connection thread: " + e.getMessage());
                break;
            }
        }
    }

}