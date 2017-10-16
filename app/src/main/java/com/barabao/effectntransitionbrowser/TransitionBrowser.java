package com.barabao.effectntransitionbrowser;

import java.io.File;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class TransitionBrowser extends BrowserTarget {

	private static final String TAG = "TransitionBrowser";
	
	private File nextImage = null;
	private Frame mNextFrame = null;
	private boolean transitionStarting = false;
	private boolean isRunning = false;
	private float transitionStartingTime = -1f;
	
	private boolean needTextureReload = true;
	
	private static final float TRANSITION_TIME = 3000f;
	
	public TransitionBrowser(Context ctx){
		this.ctx = ctx.getApplicationContext();
		this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);    
		this.editor = pref.edit();
		
		this.resEntries = R.array.transitions_entries;
		this.entries = ctx.getResources().getStringArray(R.array.transitions_entries);
		this.values = ctx.getResources().getStringArray(R.array.transitions_values);
		this.entriesList = Arrays.asList(entries);
		this.valuesList = Arrays.asList(values);				
		
		this.mAdapter = ArrayAdapter.createFromResource(ctx,
	            resEntries,
	            android.R.layout.simple_spinner_dropdown_item);
		
		this.selectedItem = "selectedTransition";
		this.selectedItemClipTitle = "Selected transitions: ";
		this.favListPrefName = "transitionFavoriteList";
		initFavoriteList();
	}
	
	@Override
	protected void initFavoriteList() {
		String effFavList = pref.getString(favListPrefName, null);
		if (effFavList != null && effFavList.length() > 0){
			for (String s : Arrays.asList(effFavList.replaceAll(", ", ",").split(","))){
				favoriteList.add(s);
			}
		}
	}

	@Override
	public void onFling(File f, GLSurfaceView surface) {
		Log.d(TAG, "fling");
		transitionStarting = true;
		transitionStartingTime = SystemClock.uptimeMillis();
		nextImage = f;
		surface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);	
	}

	@Override
	public void setTargetButton(MenuItem item) {
		item.setIcon(R.drawable.ic_action_slideshow);
		item.setTitle(R.string.action_change_to_transition);
	}

	@Override
	public void onResume() {
		needTextureReload = true;
	}

	@Override
	synchronized public void draw(GLSurfaceView mSurface) {
		if (mFrame == null){						
			Log.d(TAG, "Creating Frames");
			// Draw background color
	        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

	        // Set the camera position (View matrix)
	        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

	        // Calculate the projection and view transformation
	        Matrix.multiplyMM(mMVPMatrix, 0, MyGLRenderer.mProjectionMatrix, 0, mViewMatrix, 0);
	        
	        this.mFrame = new Frame(ctx,0,0, mSurface);
			
		}	
		if (needTextureReload){
			loadTexture(textureHandles, 0, selectedImage, mSurface);
			needTextureReload = false;
		}				
		
		if (transitionStarting){
			transitionStarting = false;
			isRunning = true;	
			
			if (mNextFrame == null){
				loadTexture(textureHandles, 1, nextImage, mSurface);
				this.mNextFrame = new Frame(ctx,1,0,mSurface);			
			}
											
		}
		
		if (isRunning){
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			float delta = SystemClock.uptimeMillis() - transitionStartingTime;
			//applyOrigTransition(mSurface, delta);
			applyDestTransition(mSurface, delta);
		}
		else {
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			//change
			mFrame.resetBuffer();
			mFrame.draw(mMVPMatrix, GLES20.GL_TEXTURE0, textureHandles[0]);
		}
		
	}

    public void applyOrigTransition(GLSurfaceView mSurface, float delta){
    	
    	float timeRatio = delta / TRANSITION_TIME;    	
    	Log.d(TAG, "Orig transition, delta:" + delta + ", timeRatio:" + timeRatio);
    	
    	if (timeRatio >= 1){
    		isRunning = false;
    		mSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    		return;
    	}
    	    	
		mFrame.resetBuffer();
    	
    	float distance = -1 + 2 * timeRatio; 	
    	float[] translationMatrix = new float[16];
    	Matrix.translateM(translationMatrix, 0, mMVPMatrix, 0, distance, 0.0f, 0.0f);
    	mFrame.draw(translationMatrix, GLES20.GL_TEXTURE0, textureHandles[0]);
    }
    
	public void applyDestTransition(GLSurfaceView mSurface, float delta){
		
    	float timeRatio = delta / TRANSITION_TIME;    	
    	
    	if (timeRatio >= 1){
    		isRunning = false;
    		mSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    		mFrame.draw(mMVPMatrix, GLES20.GL_TEXTURE1, textureHandles[1]);
    		return;
    	}
    	
    	mNextFrame.resetBuffer();
    	
    	float distance = -3 + 2 * timeRatio;    	
    	float[] translationMatrix = new float[16];
    	Matrix.translateM(translationMatrix, 0, mMVPMatrix, 0, distance, 0.0f, 0.0f);
    	mNextFrame.draw(translationMatrix, GLES20.GL_TEXTURE1, textureHandles[1]);
	}
	
	@Override
	public void reinitFrames() {
		mFrame = null;
		mNextFrame = null;
		//GLES20.glDeleteTextures(2, textureHandles, 0);
		//GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	}

}
