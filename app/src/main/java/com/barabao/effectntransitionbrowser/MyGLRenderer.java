package com.barabao.effectntransitionbrowser;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class MyGLRenderer implements GLSurfaceView.Renderer {

	private static final String TAG = "MyGLRenderer";	
	
	private Context ctx;
	private GLSurfaceView mSurface;
	
	private SharedPreferences pref;
	
	public static float[] mProjectionMatrix = new float[16];
	
	public MyGLRenderer(Context ctx, GLSurfaceView surface) {
		super();
		this.ctx = ctx.getApplicationContext();		
		this.mSurface = surface;
	}

	@Override
	synchronized public void onDrawFrame(GL10 arg0) {
		Log.d(TAG, "onDrawFrame");
		MainActivity.currentBrowser.draw(mSurface);               
	}

	@Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
		Log.d(TAG, "onSurfaceChanged");
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		MainActivity.currentBrowser.onSurfaceCreated(mSurface);
	}	                       

}
