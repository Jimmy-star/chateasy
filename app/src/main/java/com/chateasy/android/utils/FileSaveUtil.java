package com.chateasy.android.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2022/1/21.
 */

public class FileSaveUtil {
    public static final String SD_CARD_PATH = Environment.getExternalStorageState() + "/MAXI";
    public static final String saveFn = SD_CARD_PATH + "/user_chat_data/";
    public static final String saveListFn = SD_CARD_PATH + "/user_chat_data/chatList";
    public static final String savechannelFn = SD_CARD_PATH + "/user_chat_data/channel_id";
    public static final String saveUnReadFn = SD_CARD_PATH + "/user_chat_data/UnRead/";
    public static final String voice_dir = SD_CARD_PATH;
    private boolean hasSD = false;
    private String FILEPATH;

    public static boolean isFileExists(File file){
        if(!file.exists()){
            return false;
        }
        return true;
    }

    /**
     * 获取文件夹下所有的文件名
     */
    public static List<String> getFileName(String fileName){
        List<String> fileList = new ArrayList<String>();
        String path = fileName;
        File f = new File(path);
        if(!f.exists()){
            System.out.println(path + "not exists");
            return null;
        }
        File fa[] = f.listFiles();
        for(int i = 0;i < fa.length;i++){
            File fs = fa[i];
            if(!fs.isDirectory()){
                fileList.add(fs.getName());
            }
        }
        return fileList;
    }
    /**
     * 在SD卡上创建文件
     */
    public static File createSDFile(String fileName) throws IOException{
        File file = new File(fileName);
        if(!isFileExists(file)){
            if(file.isDirectory()){
                file.mkdirs();
            }else{
                file.createNewFile();
            }
        }
        return file;
    }

    /**
     * 在SD卡上创建文件夹
     */
    public static File createSDDirectory(String fileName) throws IOException{
        File file = new File(fileName);
        if(!isFileExists(file)){
            file.mkdirs();
        }
        return file;
    }

    /**
     * 写入内容
     */
    public synchronized static void writeString(String content, String file, boolean isAppend){
        try{
            createSDDirectory(saveFn);
            createSDDirectory(saveListFn);
            createSDDirectory(savechannelFn);
            byte[] data = content.getBytes("utf-8");
            writeBytes(file, data, isAppend);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public synchronized static boolean writeBytes(String filePath, byte[] data, boolean isAppend){
        try{
            FileOutputStream fos;
            if(isAppend){
                fos = new FileOutputStream(filePath, true);
            }else{
                fos = new FileOutputStream(filePath);
            }
            fos.write(data);
            fos.close();
            return true;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    /**
     * 读取SD卡中文本文件
     */
    public synchronized static String readSDFile(String fileName){
        StringBuffer sb = new StringBuffer();
        File f1 = new File(fileName);
        String str = null;
        try{
            InputStream is = new FileInputStream(f1);
            InputStreamReader input = new InputStreamReader(is, "UTF-8");
            @SuppressWarnings("resource")
            BufferedReader reader = new BufferedReader(input);
            while((str = reader.readLine()) != null){
                sb.append(str);
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    public String getFILEPATH(){
        return FILEPATH;
    }

    public boolean hasSD(){
        return hasSD;
    }

    /**
     * 删除单个文件
     */
    public static boolean deletefile(String filePath){
        File file = new File(filePath);
        if(file.isFile() && file.exists()){
            return file.delete();
        }
        return false;
    }

    public static boolean deleteDirectory(String filePath){
        boolean flag = false;
        if(!filePath.endsWith(File.separator)){
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if(!dirFile.exists() || !dirFile.isDirectory()){
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        for(int i = 0;i < files.length;i++){
            if(files[i].isFile()){
                flag = deletefile(files[i].getAbsolutePath());
                if(!flag){
                    break;
                }else{
                    flag = deleteDirectory(files[i].getAbsolutePath());
                    if(!flag){
                        break;
                    }
                }
            }
        }
        if(!flag){
            return false;
        }
        return dirFile.delete();
    }

    public static boolean saveBitMap(Bitmap bm, String picName){
        try{
            File f = new File(picName);
            if(f.exists()){
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return true;
        }catch(FileNotFoundException e){
            e.printStackTrace();
            return false;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public static String encodeBase64File(String path) throws Exception{
        byte[] videoBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        @SuppressWarnings("resource")
        FileInputStream fis = new FileInputStream(new File(path));
        byte[] buf= new byte[1024];
        int n;
        while(-1 !=(n = fis.read(buf))){
            baos.write(buf, 0, n);
        }
        videoBytes = baos.toByteArray();
        return Base64.encodeToString(videoBytes, Base64.NO_WRAP);
    }

    /**
     * 根据相册媒体库路径转换成SD卡路径
     */
    public static String getPath(final Context context, final Uri uri){
        final boolean isOverKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if(isOverKitKat && DocumentsContract.isDocumentUri(context, uri)){
            if(isExternalStorageDocument(uri)){
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if("primary".equalsIgnoreCase(type)){
                    return Environment.getExternalStorageState() + "/" + split[1];
                }
            }else if(isDownloadsDocument(uri)){
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }else if(isMediaDocument(uri)){
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if("image".equals(type)){
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }else if("video".equals(contentUri)){
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }else if("audio".equals(contentUri)){
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        else if("content".equalsIgnoreCase(uri.getScheme())){
            if(isGooglePhotosUri(uri)){
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        }
        else if("file".equalsIgnoreCase(uri.getScheme())){
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs){
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try{
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if(cursor != null && cursor.moveToFirst()){
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return null;
    }

    public static boolean isGooglePhotosUri(Uri uri){
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri){
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri){
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isExternalStorageDocument(Uri uri){
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
}
