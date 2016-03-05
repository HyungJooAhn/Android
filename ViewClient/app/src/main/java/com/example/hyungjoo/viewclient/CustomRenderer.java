package com.example.hyungjoo.viewclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by HyungJoo on 2015-08-03.
 */
public class CustomRenderer implements GLSurfaceView.Renderer {

    private PointF containPoint[];
    private Rectangle recA[];
    private Rectangle polaA[];
    private Bitmap polaro;
    private Bitmap rImage[];
    private int count;
    private Context context;
    private GLSurfaceView glview;
    private float rnumx;
    private float rnumy;
    private float rangle;
    private float way;
    private int minusCount;
    private int plusCount;
    private int p;
    private boolean check;

    public CustomRenderer(GLSurfaceView glview, Context context, int count, Bitmap pola) {
        this.recA = new Rectangle[6];
        this.polaA = new Rectangle[6];
        this.rImage = new Bitmap[6];
        this.containPoint = new PointF[6];
        this.polaro = pola;
        this.count = count;
        this.glview = glview;
        this.context = context;
        this.rnumx = 0.0f;
        this.rnumy = 0.0f;
        this.rangle = 0.0f;
        this.way = 0.0f;
        this.plusCount = 0;
        this.minusCount = 0;
        this.check = false;
        this.p = 0;
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, -6.0f);
        gl.glDisable(GL10.GL_DEPTH_TEST);

        if (count != 0) {
            if(containPoint[0] == null){
                containPoint[0] = new PointF();
                rnumx = (float)((Math.random() * 7.5) - 4.5);
                rnumy = (float)((Math.random() * 2.5) - 0.5);
                containPoint[0].set(rnumx, rnumy);
            }
            else{
                while(check != true){
                    rnumx = (float)((Math.random() * 7.5) - 4.5);
                    rnumy = (float)((Math.random() * 2.8) - 0.5);
                    checkPoint(rnumx, rnumy);
                }
            }
            rangle = (float)((Math.random() * 9.0));
            p = (int)((Math.random() * 10.0) + 1.0);

            if(p < 6){
                way = 1.0f;
                plusCount++;
            }
            else{
                way = -1.0f;
                minusCount++;
            }
            if(plusCount > 3){way = -1.0f;}
            else if(minusCount > 3){way = 1.0f;}

            for (int k = 0; k < count; k++) {
                if (recA[k] == null) {
                    recA[k] = new Rectangle(new PointF(rnumx, rnumy - 1.26f), new PointF(rnumx, rnumy), new PointF(rnumx + 1.64f, rnumy - 1.26f), new PointF(rnumx + 1.64f, rnumy), (rangle * 0.01f), way);
                    polaA[k] = new Rectangle(new PointF(rnumx - 0.18f, rnumy - 1.72f), new PointF(rnumx - 0.18f, rnumy + 0.19f), new PointF(rnumx + 1.82f, rnumy - 1.72f), new PointF(rnumx + 1.82f, rnumy + 0.19f), rangle, way);
                }
                    polaA[k].setImage(polaro);
                    polaA[k].loadGLTexture(gl);
                    polaA[k].draw(gl);
                    recA[k].setImage(rImage[k]);
                    recA[k].loadGLTexture(gl);
                    recA[k].draw(gl);
            }
            check = false;
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float) width / height, 1.0f, 30.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glShadeModel(GL10.GL_DEPTH_TEST);
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setImage(Bitmap image) {
        this.rImage[count - 1] = image;
    }

    public void checkPoint(float x, float y){
        int unableCount = 0;
        int checkCount = 0;
        for(int k = 0; k<count-1; k++){
            if((x - 0.18f + 2.0f > containPoint[k].x - 0.18 + 0.1) && (x - 0.18f < (containPoint[k].x - 0.18 + 2.0f - 0.1))){
                unableCount ++;
            }
            if((y - 1.26f - 0.46f < containPoint[k].y + 0.18f - 0.1f) && (y + 0.18 > (containPoint[k].y - 1.26f - 0.46f + 0.1f))){
                unableCount ++;
            }
            if(unableCount == 2){
                checkCount ++;
                break;
            }
            unableCount = 0;
        }
        if(checkCount == 0) {
            containPoint[count-1] = new PointF();
            this.check = true;
            containPoint[count-1].set(x, y);
        }
    }
}
