package com.example.hyungjoo.viewclient;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by HyungJoo on 2015-08-03.
 */
public class Rectangle{
    private Bitmap receiveImage;
    private PointF gp;
    private int[] textures = new int[1];
    private float vertices[] = new float[12];
    private float angle;
    private float way;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private float texture[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };


    Rectangle(PointF bottomLeft, PointF topLeft, PointF bottomRight, PointF topRight, float angle, float way) {
        this.angle = angle;
        this.way = way;
        gp = new PointF();
        setVecArray(bottomLeft, topLeft, bottomRight, topRight);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);
    }

    void draw(GL10 gl) {

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glFrontFace(GL10.GL_CW);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glRotatef(angle, 0.0f, 0.0f, way);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    public void loadGLTexture(GL10 gl) {
        Bitmap bitmap = Bitmap.createScaledBitmap(receiveImage, 256, 256, true);

        gl.glGenTextures(1, textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    public void setImage(Bitmap image){
        this.receiveImage = image;
    }

    public void setVecArray(PointF bottomLeft, PointF topLeft, PointF bottomRight, PointF topRight){
        vertices[0] = bottomLeft.x;
        vertices[1] = bottomLeft.y;
        vertices[2] = 0.0f;
        vertices[3] = topLeft.x;
        vertices[4] = topLeft.y;
        vertices[5] = 0.0f;
        vertices[6] = bottomRight.x;
        vertices[7] = bottomRight.y;
        vertices[8] = 0.0f;
        vertices[9] = topRight.x;
        vertices[10] = topRight.y;
        vertices[11] = 0.0f;
        /*float gx1 = (topLeft.x + bottomLeft.x + bottomRight.x) / 3.0f;
        float gy1 = (topLeft.y + bottomLeft.y + bottomRight.y) / 3.0f;
        float gx2 = (topLeft.x + topRight.x + bottomRight.x) / 3.0f;
        float gy2 = (topLeft.y + topRight.y + bottomRight.y) / 3.0f;
        float gx3 = (topRight.x + topLeft.x + bottomLeft.x) / 3.0f;
        float gy3 = (topRight.y + topLeft.y + bottomLeft.y) / 3.0f;
        float gx4 = (topRight.x + bottomRight.x + bottomLeft.x) / 3.0f;
        float gy4 = (topRight.y + bottomRight.y + bottomLeft.y) / 3.0f;
        float under = (((gx1 - gx2)*(gy3 - gy4)) - ((gy1 - gy2)*(gx3 - gx4)));
        float mx = ((((gx1*gy2) - (gy1*gx2))*(gx3 - gx4)) - ((gx1 - gx2)*((gx3*gy4) - (gy3*gx4)))) / under;
        float my = ((((gx1*gy2) - (gy1*gx2))*(gy3 - gy4)) - ((gy1 - gy2)*((gx3*gy4) - (gy3*gx4)))) / under;
        gp.set(mx,my);*/
    }
}

