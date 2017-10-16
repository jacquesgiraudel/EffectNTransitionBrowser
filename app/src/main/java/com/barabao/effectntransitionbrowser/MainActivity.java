package com.barabao.effectntransitionbrowser;

import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private MyGLSurfaceView mGLView;
		
	private LinkedList<File> images = new LinkedList<File>();
	private ListIterator<File> imageIterator;
	private Menu menu;	
	
	public static BrowserTarget currentBrowser;
	private EffectBrowser effectBrowser;
	private TransitionBrowser transitionBrowser;
	
	private SharedPreferences pref;
	private Editor editor;
	
	private OnNavigationListener onl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
		
		final ActionBar bar = getSupportActionBar();
		bar.setDisplayShowTitleEnabled(false);
		bar.setDisplayUseLogoEnabled(false);
		bar.setDisplayShowHomeEnabled(false);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);    
		editor = pref.edit();				
		
		effectBrowser = new EffectBrowser(this);
		//transitionBrowser = new TransitionBrowser(this);
		
		//if (pref.getString("mode", "effects").compareTo("effects") == 0){
			currentBrowser = effectBrowser;
		/*}
		else {
			currentBrowser = transitionBrowser;
		}*/		
			
		/* Effects */
				
		onl = new OnNavigationListener(){		
			
			@Override
			public boolean onNavigationItemSelected(int position, long id) {
				editor.putString(currentBrowser.getSelectedItem(), currentBrowser.getValues()[position]);
				editor.commit();
	    		mGLView.requestRender();
	    		
	    		if (currentBrowser.getFavoriteList().contains(currentBrowser.getEntries()[position])){	 	    			
	        		menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_action_important);
	        	}
	        	else {            
	        		menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_action_not_important);
	        	}
				
				return true;
			}};
		
		bar.setListNavigationCallbacks(currentBrowser.getmAdapter(), onl);
		bar.setSelectedNavigationItem(currentBrowser.getValuesList().indexOf(pref.getString(currentBrowser.getSelectedItem(), "null")));
				
		/* Image */
		//fillImageList();
		mGLView = new MyGLSurfaceView(this);
		setContentView(mGLView);		
				
		SimpleOnGestureListener mListener =  new SimpleOnGestureListener(){
			
			private static final int SWIPE_MIN_DISTANCE = 50;
			private static final int SWIPE_THRESHOLD_VELOCITY = 80;

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				float dX = e2.getX() - e1.getX();

				if (Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY &&
					Math.abs(dX) >= SWIPE_MIN_DISTANCE ) {

					//if (dX <= 0 && imageIterator.hasNext()) {
					if (dX <= 0 && bar.getSelectedNavigationIndex() < bar.getNavigationItemCount() - 1) {
						//currentBrowser.onFling(imageIterator.next(), mGLView);
						bar.setSelectedNavigationItem(bar.getSelectedNavigationIndex() + 1);
						
						return true;											
					} 
					//else if (dX > 0 && imageIterator.hasPrevious()) {
					else if (dX > 0 && bar.getSelectedNavigationIndex() > 0) {
						//currentBrowser.onFling(imageIterator.previous(), mGLView);
						bar.setSelectedNavigationItem(bar.getSelectedNavigationIndex() - 1);
						
						return true;
					} 
				} 
				
				return false;
			}	
			
		};
    	
		final GestureDetector mGestureDetector = new GestureDetector(getApplicationContext(), mListener);
		mGLView.setOnTouchListener(new OnTouchListener() {		 

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mGestureDetector.onTouchEvent(event);
				return true;
			}

		});
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.actions, menu);
	    
	    updateFavoriteIcon();
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_favorite:
	        	if (currentBrowser.getFavoriteList().contains(currentBrowser.getEntries()[currentBrowser.getValuesList().indexOf(pref.getString(currentBrowser.getSelectedItem(), "null"))])){
	        		currentBrowser.getFavoriteList().remove(currentBrowser.getEntries()[currentBrowser.getValuesList().indexOf(pref.getString(currentBrowser.getSelectedItem(), "null"))]);
	        		item.setIcon(R.drawable.ic_action_not_important);
	        	}
	        	else {
	        		currentBrowser.getFavoriteList().add(currentBrowser.getEntries()[currentBrowser.getValuesList().indexOf(pref.getString(currentBrowser.getSelectedItem(), "null"))]);	            
		            item.setIcon(R.drawable.ic_action_important);
	        	}
	            
	            return true;
	        case R.id.action_favorites:
	        	if (currentBrowser.getFavoriteList().size() == 0){
	    			Toast.makeText(this, this.getResources().getText(R.string.favorites_toast_none), Toast.LENGTH_SHORT).show();
	    			return false;
	    		}
	        	createFavoritesDialog().show();
	        	return true;
	        	
	        case R.id.action_add_to_clipboard: 	
	        	ClipboardManager clipboard = (ClipboardManager)
	        	        getSystemService(Context.CLIPBOARD_SERVICE);
	        	StringBuffer clipTitle = new StringBuffer(currentBrowser.getSelectedItemClipTitle());
	        	for (String s:currentBrowser.getFavoriteList()){
	        		clipTitle.append("\n\t" + s);
	        	}
	        	clipboard.setText(clipTitle.toString());
	        	return true;
	       /* case R.id.action_change_mode:
	        	currentBrowser.setTargetButton(item);
	        	currentBrowser.reinitFrames();
	        	if (currentBrowser instanceof EffectBrowser){	        		
	        		currentBrowser = transitionBrowser;
	        	}
	        	else {
	        		currentBrowser = effectBrowser;
	        	}
	        	getSupportActionBar().setListNavigationCallbacks(currentBrowser.getmAdapter(), onl);
	        	
	        	
	        	updateFavoriteIcon();
	        	
	        	return true;*/
	    }
		return false;
	}	
	
	public Dialog createFavoritesDialog() {						
		
		String[] items = new String[currentBrowser.getFavoriteList().size()];
		items = currentBrowser.getFavoriteList().toArray(items);
		
		final String[] fItems = items;
		
		boolean[] checks = new boolean[currentBrowser.getFavoriteList().size()];
		for (int i=0;i<checks.length;i++){
			checks[i] = true;
		}
		
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    // Set the dialog title
	    builder.setTitle(R.string.favorites_title)
	    // Specify the list array, the items to be selected by default (null for none),
	    // and the listener through which to receive callbacks when items are selected
	           .setMultiChoiceItems(items, checks,
	                      new DialogInterface.OnMultiChoiceClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int which,
	                       boolean isChecked) {
	                   if (isChecked) {
	                       // If the user checked the item, add it to the selected items
	                	   if (!currentBrowser.getFavoriteList().contains(fItems[which])){
	                		   currentBrowser.getFavoriteList().add(fItems[which]);
	                		   if (pref.getString(currentBrowser.getSelectedItem(), "null").compareTo(currentBrowser.getValues()[currentBrowser.getEntriesList().indexOf(fItems[which])]) == 0){
		                		   menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_action_important);
		                	   }
	                	   }
	                   } else if (currentBrowser.getFavoriteList().contains(fItems[which])) {
	                       // Else, if the item is already in the array, remove it 
	                	   currentBrowser.getFavoriteList().remove(fItems[which]);
	                	   if (pref.getString(currentBrowser.getSelectedItem(), "null").compareTo(currentBrowser.getValues()[currentBrowser.getEntriesList().indexOf(fItems[which])]) == 0){
	                		   menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_action_not_important);
	                	   }
	                   }
	               }
	           })
	    // Set the action buttons
	           .setPositiveButton(R.string.favorites_ok, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   
	               }
	           });	           

	    return builder.create();
	}
	
	/*private void fillImageList(){
		File DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (DCIM == null || !DCIM.exists()){
			return;
		}
		File[] files = DCIM.listFiles();
		Set<File> subfolder = new TreeSet<File>();
		for (File file:files){
			if (file.isDirectory()){
				subfolder.add(file);
			}
			else if (isImage(file)){				
				images.add(file);
			}
		}
		for (File folder:subfolder){
			for (File file:folder.listFiles()){
				if (file.isFile() && isImage(file)){
					images.add(file);
				}
			}
		}
		imageIterator = images.listIterator();
		if (imageIterator.hasNext()){
			BrowserTarget.selectedImage = imageIterator.next();
		}		
		
	}*/
		
	private void updateFavoriteIcon(){
		if (currentBrowser.getFavoriteList().contains(currentBrowser.getEntries()[currentBrowser.getValuesList().indexOf(pref.getString(currentBrowser.getSelectedItem(), "null"))])){	 	    			
    		menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_action_important);
    	}
    	else {            
    		menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_action_not_important);
    	}
	}
	
	/*public static boolean isImage(File file) {
	    if (file == null || !file.exists()) {
	        return false;
	    }
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file.getPath(), options);
	    return options.outWidth != -1 && options.outHeight != -1;
	}*/
	
	@Override
	protected void onSaveInstanceState(Bundle b){		
		editor.putString(effectBrowser.getFavListPrefName(), effectBrowser.favoriteListToString());
		//editor.putString(transitionBrowser.getFavListPrefName(), transitionBrowser.favoriteListToString());
		editor.commit();
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        currentBrowser.onResume();
        mGLView.onResume();
    }
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();    	
    	editor.putString(effectBrowser.getFavListPrefName(), effectBrowser.favoriteListToString());
//		editor.putString(transitionBrowser.getFavListPrefName(), transitionBrowser.favoriteListToString());
		editor.commit();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}   