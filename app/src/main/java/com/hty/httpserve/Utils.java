package com.hty.httpserve;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    static void writeFile(String filename, String text, int mode) {
        try {
            if(!text.equals("/favicon.ico")) {
                Date date = new Date();
                SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                text = SDF.format(date) + " " + text + "\n";
                FileOutputStream FOS = MainApplication.getContext().openFileOutput(filename, mode);
                FOS.write(text.getBytes());
                FOS.close();
            }
        } catch (Exception e) {
            Log.e(Thread.currentThread().getStackTrace()[2] + "", e.toString());
        }
    }

    static String readFile(String filename) {
        String s = "";
        try {
            FileInputStream FIS = MainApplication.getContext().openFileInput(filename);
            ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = FIS.read(buffer)) > 0) {
                BAOS.write(buffer, 0, len);
            }
            s = new String(BAOS.toByteArray());
            FIS.close();
            BAOS.close();
        } catch (Exception e) {
            Log.e(Thread.currentThread().getStackTrace()[2] + "", e.toString());
        }
        return s;
    }

}