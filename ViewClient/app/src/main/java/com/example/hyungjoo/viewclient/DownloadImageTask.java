package com.example.hyungjoo.viewclient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    public Context context;

    public DownloadImageTask(Context context) {
        this.context = context;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        try {
            InputStream in = new java.net.URL(url).openStream();
            Bitmap loadImg = BitmapFactory.decodeStream(in);
            return loadImg;
        } catch (Exception e) {
            Log.e("Image Connect Error", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    protected void onPostExecute(Bitmap returnImage) {
        Intent imgIt = new Intent("img.brreceiver");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] byteArray = null;
        if (returnImage != null) {
            returnImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byteArray = stream.toByteArray();
            returnImage.recycle();
        }
        imgIt.putExtra("img", byteArray);
        context.sendBroadcast(imgIt);
    }
}

