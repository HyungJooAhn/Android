package com.example.hyungjoo.viewclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;

public class MainActivity extends ActionBarActivity {
    private Thread thread;
    private String deviceid;
    private String regId;
    public ImageView imgView;
    public GLSurfaceView glCanvas;
    public CustomRenderer mainRen;
    public int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        count = 0;
        imgView = (ImageView) findViewById(R.id.CView);
        setContentView(R.layout.activity_main);
        Bitmap polaImage = BitmapFactory.decodeResource(this.getResources(), R.drawable.pof);

        glCanvas = new GLSurfaceView(this);

        mainRen = new CustomRenderer(glCanvas, this, count, polaImage);
        glCanvas.setBackgroundResource(R.drawable.corkboard);
        glCanvas.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glCanvas.setRenderer(mainRen);
        glCanvas.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        glCanvas.setZOrderOnTop(true);
        setContentView(glCanvas);


        ActReceiver broReceiver = new ActReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("msg.brreceiver");
        intentFilter.addAction("img.brreceiver");
        registerReceiver(broReceiver, intentFilter);

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceid = tm.getDeviceId();
        registerGcm();
        SendInfo board_insert = new SendInfo();
        thread = new Thread(board_insert);
        thread.start();
    }

    public void registerGcm() {

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);

        regId = GCMRegistrar.getRegistrationId(this);

        if (regId.equals("")) {
            GCMRegistrar.register(this, "789381039529");
        } else {
            Log.e("GCM-id", regId);
        }

    }

    public class SendInfo implements Runnable {

        @Override
        public void run() {

            try {

                HttpEntity result = null;
                HttpPost httpPost = new HttpPost("http://192.168.1.11:8080/View");

                MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                entity.addPart("gid", new StringBody("2", Charset.forName("UTF-8")));
                entity.addPart("type", new StringBody("B", Charset.forName("UTF-8")));
                entity.addPart("did", new StringBody(deviceid, Charset.forName("UTF-8")));
                entity.addPart("regid", new StringBody(regId, Charset.forName("UTF-8")));

                HttpParams params = httpPost.getParams();
                HttpConnectionParams.setConnectionTimeout(params, 5000);

                httpPost.setEntity(entity);


                HttpClient client = new DefaultHttpClient();

                HttpResponse response = client.execute(httpPost);
                HttpEntity entityResponse = response.getEntity();
                entityResponse.consumeContent();
                InputStream resultStream = entityResponse.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(resultStream, HTTP.UTF_8));

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connect Fail", Toast.LENGTH_LONG).show();
                    }
                });
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ActReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String name = intent.getAction();

            if (name.equals("msg.brreceiver")) {

                String msg = intent.getStringExtra("msg");
                DownloadImageTask loadTask = new DownloadImageTask(MainActivity.this);
                loadTask.execute("http://192.168.1.11:8080/upload/" + msg + ".jpg");

            }
            else if (name.equals("img.brreceiver")) {

                byte[] byteArray = intent.getByteArrayExtra("img");
                GLSurfaceView glView = new GLSurfaceView(MainActivity.this);
                if (byteArray != null) {
                    Bitmap loadImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    count++;

                    if (count < 9) {
                        mainRen.setCount(count);
                        mainRen.setImage(loadImage);

                        glCanvas.requestRender();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "Please retry", Toast.LENGTH_LONG).show();
                }

            }
        }
    }
}