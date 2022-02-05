package com.chateasy.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2022/1/24.
 */

public class PictureUtil {
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if(height > reqHeight || width > reqWidth){
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 根据路径获得图片并压缩返回bitmap用于显示
     */
    public static Bitmap getSmallBitmap(String filePath){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, 320, 480);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 压缩大图片
     */
    public static Bitmap compressSizeImage(String srcPath){
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在大多数手机分辨率为480*800
        float hh =800f;
        float ww = 480f;
        int be = 1;
        if(w > h && w > ww){
            be = (int) (newOpts.outWidth / ww);
        }else if(w < h && h > hh){
            be = (int) (newOpts.outHeight / hh);
        }
        if(be < 0){
            be = 1;
        }
        newOpts.inSampleSize = be;
        //重新读入图片
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);
    }

    public static Bitmap compressImage(Bitmap image){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while(baos.toByteArray().length / 1024 > 100){
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return  bitmap;
    }

    /**
     * 获取图片文件的信息，是否旋转了90度，如果是则反转
     */
    public static Bitmap reviewPicRotate(Bitmap bitmap, String path){
        int degree = getPicRotate(path);
        if(degree != 0){
            Matrix m = new Matrix();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            m.setRotate(degree);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
        }
        return bitmap;
    }

    /**
     * 读取图片文件选择的角度
     */
    public static int getPicRotate(String path){
        int degree = 0;
        try{
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return degree;
    }
}
