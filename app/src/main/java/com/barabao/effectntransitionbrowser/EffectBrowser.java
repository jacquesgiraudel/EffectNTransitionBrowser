package com.barabao.effectntransitionbrowser;

import java.io.File;
import java.util.Arrays;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class EffectBrowser extends BrowserTarget {

	private static final String TAG = "EffectBrowser";
	
	private boolean needTextureReload = true;	
	
	public EffectBrowser(Context ctx){
		this.ctx = ctx.getApplicationContext();
		this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);    
		this.editor = pref.edit();
		
		this.resEntries = R.array.effects_entries;
		this.entries = ctx.getResources().getStringArray(R.array.effects_entries);
		this.values = ctx.getResources().getStringArray(R.array.effects_values);
		this.entriesList = Arrays.asList(entries);
		this.valuesList = Arrays.asList(values);				
		
		this.mAdapter = ArrayAdapter.createFromResource(ctx,
	            resEntries,
	            android.R.layout.simple_spinner_dropdown_item);
		
		this.selectedItem = "selectedEffect";
		this.selectedItemClipTitle = "Selected effects: ";
		this.favListPrefName = "effectFavoriteList";
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
		surface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		selectedImage = f;
		needTextureReload = true;
		surface.requestRender();
	}

	@Override
	public void setTargetButton(MenuItem item) {
		item.setIcon(R.drawable.ic_action_picture);
		item.setTitle(R.string.action_change_to_effect);
	}

	@Override
	public void onResume() {
		needTextureReload = true;
	}

	@Override
	synchronized public void draw(GLSurfaceView mSurface) {		
		try {
			if (mSurface.getRenderMode() != GLSurfaceView.RENDERMODE_WHEN_DIRTY){
				mSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
			}
			//if (mFrame == null){
				Log.d(TAG, "Creating Frame");
				// Draw background color
		        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	
		        // Set the camera position (View matrix)
		        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
	
		        // Calculate the projection and view transformation
		        Matrix.multiplyMM(mMVPMatrix, 0, MyGLRenderer.mProjectionMatrix, 0, mViewMatrix, 0);
				mFrame = new Frame(ctx, 0, 0, mSurface);			
			/*}	
			else {
				Log.d(TAG, "Reseting Frame");
				GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
				mFrame.resetBuffer();
				mFrame.resetFragmentShader();
			}
			if (needTextureReload){
				loadTexture(textureHandles, 0, selectedImage, mSurface);
				needTextureReload = false;
			}*/
			
			mFrame.draw(mMVPMatrix, GLES20.GL_TEXTURE0, textureHandles[0]);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void reinitFrames() {
		Log.d(TAG, "reinitFrames");
		mFrame = null;
		//GLES20.glDeleteTextures(1, textureHandles, 0);
	}

}
