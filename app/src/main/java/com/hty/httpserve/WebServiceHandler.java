// https://blog.csdn.net/jkeven/article/details/9271145
package com.hty.httpserve;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class WebServiceHandler implements HttpRequestHandler {

    SimpleDateFormat SDF =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public WebServiceHandler() {
        super();
    }

    public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws  IOException {
        String target = URLDecoder.decode(request.getRequestLine().getUri(),"UTF-8");   //解决中文名无法访问的问题
        Log.e(Thread.currentThread().getStackTrace()[2] + "", target);
        Utils.writeFile("log.txt", target, Context.MODE_APPEND);
        response.setStatusCode(HttpStatus.SC_OK);
        final File file = new File(target);
        if (file.isDirectory()) {
            response.setHeader("Content-Type", "text/html; charset=UTF-8");
            String text = "<html>\n<head>\n<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n<title>文件服务</title>\n<style>\na { text-decoration:none; }\ntd { padding:0 20px; }\ntd:nth-child(2) { text-align:right; }\n</style>\n</head>\n<body>\n<h1>[" + target + "]</h1>\n<table>\n<tr><th>名称</th><th>大小</th><th>创建时间</th></tr>\n";
            File[] files = file.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    long diff = f2.lastModified() - f1.lastModified();
                    if (diff > 0)
                        return 1;
                    else if (diff == 0)
                        return 0;
                    else
                        return -1;
                }
            });
            for (int i = 0; i < files.length; i++) {
                String target1;
                if (target.equals("/")) {
                    target1 = target;
                } else {
                    target1 = target + "/";
                }
                long ltime = files[i].lastModified();
                Date date = new Date(ltime);
                if (files[i].isDirectory()) {
                    text += "<tr><td><a href='" + target1 + files[i].getName() + "'>[" + files[i].getName() + "]</a></td><td></td><td>" + SDF.format(date) + "</td></tr>\n";
                } else {
                    text += "<tr><td><a href='" + target1 + files[i].getName() + "'>" + files[i].getName() + "</a></td><td>" + files[i].length() / 1000 + " KB</td><td>" + SDF.format(date) + "</td></tr>\n";
                }
            }
            text += "</table>\n</body>\n</html>";
            StringEntity entity = new StringEntity(text, "UTF-8");
            response.setEntity(entity);
        } else { //file.isFile()
            String mime = URLConnection.getFileNameMap().getContentTypeFor(file.getCanonicalPath());
            Log.e(Thread.currentThread().getStackTrace()[2] + " " + mime, file.getCanonicalPath());
            response.setHeader("Content-Type", mime);
            HttpEntity entity = new EntityTemplate(new ContentProducer() {
                @Override
                public void writeTo(OutputStream outStream) throws IOException {
                    write(file, outStream);
                }
            });
            response.setEntity(entity);
        }
    }

    void write(File inputFile, OutputStream outStream) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        try {
            int count;
            byte[] buffer = new byte[1024*4];
            while ((count = fis.read(buffer)) != -1) {
                outStream.write(buffer, 0, count);
            }
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            fis.close();
            outStream.close();
        }
    }


}