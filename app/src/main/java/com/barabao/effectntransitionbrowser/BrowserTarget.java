package com.barabao.effectntransitionbrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.SpinnerAdapter;

public abstract class BrowserTarget {
	
	protected Context ctx;
	protected SharedPreferences pref;
	protected Editor editor;
	
	protected int resEntries;	
	protected String[] entries;
	protected String[] values;
	protected List<String> entriesList;
	protected List<String> valuesList;
	protected List<String> favoriteList = new ArrayList<String>();
	protected String selectedItem;
	protected String selectedItemClipTitle;
	protected String favListPrefName;
	protected SpinnerAdapter mAdapter;	
	
	public static File selectedImage = null;
	
	protected Frame mFrame;
	protected final float[] mViewMatrix = new float[16];
	
	protected final float[] mMVPMatrix = new float[16];
	protected static int[] textureHandles = new int[2];
	
	private static final String TAG = "EffectBrowser";
	
	protected abstract void initFavoriteList();

	public abstract void onFling(File f, GLSurfaceView surface);
	
	public abstract void onResume();

	public abstract void setTargetButton(MenuItem item);
	
	public abstract void draw(GLSurfaceView surface);
	
	public abstract void reinitFrames();

	public String favoriteListToString(){
		return favoriteList.toString().replace("[", "").replace("]", "");
	}

	public void loadTexture(int[] textureHandles, int position, File f, GLSurfaceView surface) {    	 
	     Bitmap bmp = null;
	     /*if (f != null){
	    	Log.d(TAG, "loading texture " + f.getAbsolutePath());
	     	bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
	     }
	     else {*/
	     	BitmapFactory.Options opt = new BitmapFactory.Options();
	     	opt.inJustDecodeBounds = true;
	     	BitmapFactory.decodeResource(ctx.getResources(), R.drawable.defaultimage, opt);	     	
	     	
	     	double sizeRatio = 1;
	     	if ((double)opt.outHeight / (double)surface.getHeight() > (double)opt.outWidth / (double)surface.getWidth()){
	     		sizeRatio =  Math.floor((double)opt.outHeight / (double)surface.getHeight()) ;
	     	}
	     	else {
	     		sizeRatio = Math.floor((double)opt.outWidth / (double)surface.getWidth());
	     	}
	     	opt.inJustDecodeBounds = false;
	     	opt.inSampleSize = (int) sizeRatio;
	     	Log.d("BrowserTarget", "sizeRatio : " + sizeRatio);
	     	bmp = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.defaultimage, opt);
	     	
	     	Bitmap finalBmp = null;
	     	if (bmp.getWidth() >= bmp.getHeight()){

	     		finalBmp = Bitmap.createBitmap(
	     				bmp, 
	     				bmp.getWidth()/2 - bmp.getHeight()/2,
	     		     0,
	     		    bmp.getHeight(), 
	     		    bmp.getHeight()
	     		     );

     		}else{

     			finalBmp = Bitmap.createBitmap(
     					bmp,
     		     0, 
     		    bmp.getHeight()/2 - bmp.getWidth()/2,
     		    bmp.getWidth(),
     		    bmp.getWidth() 
     		     );
     		}	
	     	bmp.recycle();
	     	
	     	//bmp = ThumbnailUtils.extractThumbnail(bmp, surface.getWidth(), surface.getHeight());
	     	Log.d("BrowserTarget", "width:" + surface.getWidth() + ", height:" + surface.getHeight());
	     //}        
	     
	     GlesUtils.loadTexture(finalBmp, textureHandles, position);
    }
	
	public void onSurfaceCreated(GLSurfaceView surface) {
		Log.d(TAG, "onSurfaceCreated");
		// Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);   
        
        loadTexture(textureHandles, 0, selectedImage, surface);                
	}		
	
	public int getResEntries() {
		return resEntries;
	}

	public String[] getEntries() {
		return entries;
	}

	public String[] getValues() {
		return values;
	}

	public List<String> getEntriesList() {
		return entriesList;
	}

	public List<String> getValuesList() {
		return valuesList;
	}

	public List<String> getFavoriteList() {
		return favoriteList;
	}

	public SpinnerAdapter getmAdapter() {
		return mAdapter;
	}

	public String getSelectedItem() {
		return selectedItem;
	}

	public String getSelectedItemClipTitle() {
		return selectedItemClipTitle;
	}

	public String getFavListPrefName() {
		return favListPrefName;
	}

	public static File getSelectedImage() {
		return selectedImage;
	}

	public Frame getmFrame() {
		return mFrame;
	}

	public float[] getmViewMatrix() {
		return mViewMatrix;
	}

	public float[] getmMVPMatrix() {
		return mMVPMatrix;
	}			
	
}
