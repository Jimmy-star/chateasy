package com.chateasy.android.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Administrator on 2022/1/23.
 */

public class ImageCheckoutUtil {
    @SuppressLint("NewApi")
    public static int getImageSize(Bitmap data){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1){
            return data.getRowBytes() * data.getHeight();
        }else{
            return data.getByteCount();
        }
    }

    public static Bitmap getLocalBitmap(String url){
        try{
            ByteArrayOutputStream out;
            FileInputStream fis = new FileInputStream(url);
            BufferedInputStream bis = new BufferedInputStream(fis);
            out = new ByteArrayOutputStream();
            @SuppressWarnings("unused")
            int hasRead = 0;
            byte[] buffer = new byte[1024 * 2];
            while((hasRead = bis.read(buffer)) > 0){
                out.write(buffer);
                out.flush();
            }
            out.close();
            fis.close();
            bis.close();
            byte[] data = out.toByteArray();
            //长宽减半
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 3;
            return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
