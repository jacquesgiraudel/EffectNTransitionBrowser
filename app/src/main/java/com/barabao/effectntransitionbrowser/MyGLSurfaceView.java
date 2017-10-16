package com.barabao.effectntransitionbrowser;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;


public class MyGLSurfaceView extends android.opengl.GLSurfaceView {

	private MyGLRenderer mRenderer;
	
	public MyGLSurfaceView(Context context) {
		super(context);
		 // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        
        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context, this);
        setRenderer(mRenderer);
        
        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}	

    @Override
    public boolean onTouchEvent(MotionEvent e) {
       
        return true;
    }

	public MyGLRenderer getRenderer() {
		return mRenderer;
	}
	
	@Override
	public void onPause(){
		super.onPause();	
		MainActivity.currentBrowser.reinitFrames();
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}

}
