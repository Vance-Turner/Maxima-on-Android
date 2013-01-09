/*
    Copyright 2012, Yasuaki Honda (yasuaki.honda@gmail.com)
    This file is part of MaximaOnAndroid.

    MaximaOnAndroid is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    MaximaOnAndroid is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MaximaOnAndroid.  If not, see <http://www.gnu.org/licenses/>.
*/

package jp.yhonda;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ManualActivity extends HTMLActivity {
	Context cont=this;
	
	ActionBar actionBar;
	
	public static final String LANGUAGE_EXTRA = "MANUAL_LANGUAGE_EXTRA";
	public static final String ENGLISH_LOCATION_EXTRA = "ENGLISH_MANUAL_LOCATION_EXTRA";
	public static final String JAPANESE_LOCATION_EXTRA = "JAPENESE_MANUAL_LOCATION_EXTRA";
	
	public static final int ENGLISH_VERSION = 10;
	public static final int JAPANESE_VERSION = 11;
	
	public static final int LOADING_MANUAL_DIALOG_FLAG = 1993;
	
	private Bundle extras;
	
	private ProgressDialog loadingDialog;
	
	// Used to determine which version of the manual
	// is being shown.
	int languageFlag = ENGLISH_VERSION;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    // Create the drop-down navigation for different manual
    // languages. Needs to be called before super.onCreate to
	  // set extras!
    extras = getIntent().getExtras();
    languageFlag = extras.getInt(LANGUAGE_EXTRA);
    
	  super.onCreate(savedInstanceState);
	  interceptBackButton = false;
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
	  switch(id){
	    case LOADING_MANUAL_DIALOG_FLAG:
	      loadingDialog = new ProgressDialog(this);
	      loadingDialog.setCancelable(true);
	      loadingDialog.setTitle("Loading");
	      loadingDialog.setIndeterminate(true);
	      return loadingDialog;
	  }
	  return super.onCreateDialog(id);
	}
	
	@Override
	public void onPrepareDialog(int id, Dialog dialog) {
	  switch(id){
	    case LOADING_MANUAL_DIALOG_FLAG:
	      switch(languageFlag){
	        case ENGLISH_VERSION: loadingDialog.setMessage("Loading English version..."); break;
	        default: loadingDialog.setMessage("Loading Japanese version..."); break;
	      }
	  }
	}
	
	private class CustomWebView extends WebViewClient {
		//y[WÌÇÝÝ®¹
		@Override
		public void onPageFinished(WebView view, String url) {
			Log.v("man","onPageFinished");
			if(loadingDialog != null && loadingDialog.isShowing())
			  dismissDialog(LOADING_MANUAL_DIALOG_FLAG);
			SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(cont);
			String targetURL=pref.getString("url", "");
			if (!targetURL.equals(url)) return;
			int scx=pref.getInt("scrollX", -1);
			int scy=pref.getInt("scrollY", -1);
			if (scx!=-1 && scy!=-1) {
				webview.scrollTo(scx, scy);
				//webview.loadUrl("javascript:window.scrollTo("+String.valueOf(scx)+","+String.valueOf(scy)+")");
			}
			Editor ed=pref.edit();
			ed.remove("scrollX");
			ed.remove("scrollY");
			ed.remove("url");
			ed.remove("scale");
			ed.commit();
		}
	}
	
	@Override
	public void loadURLonCreate() {
		webview.setWebViewClient(new CustomWebView());
		SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
		String url=pref.getString("url", "");
		int sc=(int) (100*pref.getFloat("scale", 0.0f));
		if (url!="" && sc!=0) {
			webview.setInitialScale(sc);
			webview.loadUrl(url);
		} else {
		  switch(languageFlag) {
		    case ENGLISH_VERSION:
		      getIntent().putExtra("url", extras.getString(ENGLISH_LOCATION_EXTRA));
		      break;
		    default:
		      getIntent().putExtra("url", extras.getString(JAPANESE_LOCATION_EXTRA));
		      break;
		  }
		  showDialog(LOADING_MANUAL_DIALOG_FLAG);
		  super.loadURLonCreate();
		}
		
	}
		
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	  super.onCreateOptionsMenu(menu);
	  MenuInflater inflater = getMenuInflater();
	  inflater.inflate(R.menu.manmenu, menu);

	  return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	  switch (item.getItemId()) {
	    /* Removed. The ManualActivity now behaves like
	     * a traditional Android activity: the back button
	     * is used to navigate back up the stack.
	  case R.id.gomaxima:
		  float scale=webview.getScale();
		  String url=webview.getUrl();
		  webview.setInitialScale(100);
		  int scy=webview.getScrollY();
		  int scx=webview.getScrollX();
		  SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
		  Editor ed=pref.edit();
		  ed.putString("url", url);
		  ed.putInt("scrollY", scy);
		  ed.putInt("scrollX", scx);
		  ed.putFloat("scale", scale);
		  ed.commit();
		  
		  Intent intent = new Intent(this,MaximaOnAndroidActivity.class);
		  intent.setAction(Intent.ACTION_VIEW);
		  this.startActivity(intent);


		  return true;
		  */
	  case R.id.manual_en:
        if (languageFlag != ENGLISH_VERSION) {
          languageFlag = ENGLISH_VERSION;
          loadURLonCreate();
        }
	    return true;
	  case R.id.manual_jap:
        if (languageFlag != JAPANESE_VERSION) {
          languageFlag = JAPANESE_VERSION;
          loadURLonCreate();
        }
	    return true;
	  default:
		  return super.onOptionsItemSelected(item);
	  }
    }
}
