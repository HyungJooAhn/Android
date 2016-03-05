package com.example.hyungjoo.client;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.androidquery.AQuery;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Vector;

public class MainActivity extends ActionBarActivity implements OnCheckedChangeListener {
    final static int RESULT_IMAGE = 1;

    private AQuery aq = new AQuery(this);
    private Thread thread;
    private File file;
    private String deviceid;
    private char type;
    private int groupid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupid = 0;
        setContentView(R.layout.activity_main);
        Button selectButton = (Button) findViewById(R.id.selectButton);
        Button sendButton = (Button) findViewById(R.id.sendButton);

        TelephonyManager tm =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceid = tm.getDeviceId();

        System.out.println(deviceid);
        /* select 버튼 눌렀을 경우 */
        selectButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // 앨범에서 선택
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_IMAGE);
            }
        });

        RadioGroup rg = (RadioGroup) findViewById(R.id.RG);
        rg.setOnCheckedChangeListener(this);

        /* send 버튼 눌렀을 경우 */
            sendButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    if(groupid != 0) {
                        // 새로운 객체 생성 후 쓰레드 실행
                        SendImage instSendImage = new SendImage();
                        thread = new Thread(instSendImage);
                        thread.start();
                        Toast.makeText(MainActivity.this, "Send Success", Toast.LENGTH_LONG).show();
                    }
                    else if(groupid == 0){
                        Toast.makeText(MainActivity.this, "You must select Group", Toast.LENGTH_LONG).show();
                    }
                }
            });


    }
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch(checkedId){
            case R.id.rbtnKNOWCK:
                groupid = 1;
                break;
            case R.id.rbtnHUFS:
                groupid = 2;
                break;
            case R.id.rbtnDDP:
                groupid = 3;
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        Bitmap image_bitmap = null;

        try {
            image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            // 이미지를 비트맵 객체에 저장
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap resizeImage = Bitmap.createScaledBitmap(image_bitmap, 300, 400, true);
        ImageView imageV = (ImageView)findViewById(R.id.selectImage);
        // activity_main의 이미지 뷰와 연결
        imageV.setImageBitmap(resizeImage);
        // 이미지 뷰에 세팅

        Uri image = null;
        String filePath = null;


        image = data.getData();
        // 선택된 사진 이미지를 받아온다.
        Cursor c = getContentResolver().query(Uri.parse(image.toString()), null, null, null, null);
        // 이미지를 문자열로 변환 후 쿼리 실행해 결과를 반환 받는다.
        // Uri : 데이터 집합
        // Cursor: 컨텐츠를 읽어 들이기 위해, 레코드 사이를 이동하게하는 오브젝트

        c.moveToNext(); // 다음 레코드로 이동

        filePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
        file = new File(filePath);


        System.out.println("isFile: " + file.isFile());
        System.out.println("isAbsolute: " + file.isAbsolute());


        if (file != null && file.isFile()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            File outputDir = this.getCacheDir();
            Bitmap sendResizeImage = Bitmap.createScaledBitmap(bitmap, 300, 400, true);
            OutputStream out = null;
            try {
                File outputFile = File.createTempFile("sened", "complete", outputDir);
                // 전송을 위해 임시 파일 생성
                outputFile.createNewFile();
                out = new FileOutputStream(outputFile);

                sendResizeImage.compress(Bitmap.CompressFormat.JPEG, 70, out);
                // 전송을 위해 이미지를 압축한다.
                file = outputFile;

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }



    public class SendImage implements Runnable {

        @Override
        public void run() {

            try {

                HttpEntity result = null;

//                HttpPost httpPost = new HttpPost("http://192.168.1.11:8080/helloWorld" + deviceid);
                HttpPost httpPost = new HttpPost("http://192.168.1.11:8080/ServerFile");
                // URL 지정
                MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                Vector<BasicNameValuePair> nameValue = new Vector<BasicNameValuePair>();
                nameValue.add(new BasicNameValuePair("gid", String.valueOf(groupid)));
                nameValue.add(new BasicNameValuePair("type", "A"));
                nameValue.add(new BasicNameValuePair("did", deviceid));

                entity.addPart("gid", new StringBody(String.valueOf(groupid), Charset.forName("UTF-8")));
                entity.addPart("type", new StringBody("A", Charset.forName("UTF-8")));
                entity.addPart("did", new StringBody(deviceid, Charset.forName("UTF-8")));
                FileBody bin = new FileBody(file);

                String filename = "image";
                entity.addPart(filename, bin);


                HttpParams params = httpPost.getParams();
                HttpConnectionParams.setConnectionTimeout(params, 5000);


                httpPost.setEntity(entity);


                HttpClient client = new DefaultHttpClient();

                HttpResponse response = client.execute(httpPost);
                HttpEntity entityResponse = response.getEntity();
                InputStream resultStream = entityResponse.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(resultStream, HTTP.UTF_8));

                StringBuffer page = new StringBuffer();
                String line = reader.readLine();
                while (line != null) {
                    line = reader.readLine();
                    page.append(line);
                }

                System.out.println(page);

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
