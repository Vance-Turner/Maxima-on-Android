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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class MaximaOnAndroidActivity extends Activity implements TextView.OnEditorActionListener
{
	String maximaURL="file:///android_asset/index.html";
	String oldmaximaURL="file:///android_asset/index.html";
	String newmaximaURL="file:///android_asset/maxima.html";
	Semaphore sem = new Semaphore(1);
	EditText _editText;
    WebView webview;
    CommandExec maximaProccess;
    File internalDir;
    File externalDir;
    MaximaVersion mvers=new MaximaVersion(5,28,0);

      @Override
    public boolean onCreateOptionsMenu(Menu menu) {
  	  super.onCreateOptionsMenu(menu);
  	  MenuInflater inflater = getMenuInflater();
  	  inflater.inflate(R.menu.menu, menu);
  	  return true;
    }
      @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
		  switch (item.getItemId()) {
		  case R.id.about:
			   showHTML("file:///android_asset/docs/aboutMOA.html");
			   return true;
		  case R.id.graph:
			  showGraph();
			  return true;
		  case R.id.quit:
			  exitMOA();
			  return true;
		  case R.id.manual_en:
			  showManual("file://"+internalDir+"/additions/en/maxima.html", ManualActivity.ENGLISH_VERSION);
			  return true;
		  case R.id.manual_jap:
		    showManual("file://"+internalDir+"/additions/ja/maxima.html", ManualActivity.JAPANESE_VERSION);
		    return true;
		  default:
			   return super.onOptionsItemSelected(item);
		  }
	 }
    
      @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d("My Test", "Clicked!1");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	internalDir = this.getFilesDir();
    	externalDir = this.getExternalFilesDir(null);

        webview = (WebView) findViewById(R.id.webView1);
        webview.getSettings().setJavaScriptEnabled(true); 
        webview.setWebViewClient(new WebViewClient() {}); 
        webview.getSettings().setBuiltInZoomControls(true);
        
        if (Build.VERSION.SDK_INT > 16) { // > JELLY_BEAN
        	maximaURL=newmaximaURL;
        } else {
        	maximaURL=oldmaximaURL;
        }
        webview.loadUrl(maximaURL);
        webview.addJavascriptInterface(this, "MOA");
        _editText=(EditText)findViewById(R.id.editText1);
        _editText.setOnEditorActionListener(this);

        MaximaVersion prevVers=new MaximaVersion();
        prevVers.loadVersFromSharedPrefs(this);
        long verNo = prevVers.versionInteger();
        long thisVerNo = mvers.versionInteger();
        
    	if ((thisVerNo > verNo) || 
    		!((new File(internalDir+"/maxima")).exists()) || 
    		!((new File(internalDir+"/additions")).exists()) ||
    		(!( new File( internalDir+"/maxima-"+mvers.versionString() ) ).exists() 
        	    && ! ( new File( externalDir+"/maxima-"+mvers.versionString() ) ).exists()))
    	{
              	Intent intent = new Intent(this,MOAInstallerActivity.class);
              	intent.setAction(Intent.ACTION_VIEW);
              	intent.putExtra("version", mvers.versionString());
              	this.startActivityForResult(intent,0);
       	} else {
       		// startMaxima();
       		new Thread(new Runnable() {
       			@Override
       			public void run() {
       				startMaxima();
       			}
       		}).start();
       	}   	
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if (resultCode==RESULT_OK) {
    		/* everything is installed properly. */
    		mvers.saveVersToSharedPrefs(this);
       		// startMaxima();
       		
       		new Thread(new Runnable() {
       			@Override
       			public void run() {
       				startMaxima();
       			}
       		}).start();
       		
    	} else {
    		new AlertDialog.Builder(this)
    		.setTitle("MaximaOnAndroid Installer")
    		.setMessage("The installation NOT completed. Please uninstall this apk and try to re-install again.")
    		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
            		finish();
            	}
            	})
    		.show();
    	}
    }
    
    private void startMaxima() {
    	try {
			sem.acquire();
		} catch (InterruptedException e1) {
			// TODO ©®¶¬³ê½ catch ubN
			e1.printStackTrace();
		}
    	if ( ! ( new File( internalDir+"/maxima-"+mvers.versionString() ) ).exists() &&
             	 ! ( new File( externalDir+"/maxima-"+mvers.versionString() ) ).exists()) {
             	this.finish();
      	} 
        List<String> list = new ArrayList<String>();
        list.add(internalDir+"/maxima");
        list.add("--init-lisp="+internalDir+"/init.lisp");
        maximaProccess = new CommandExec();
        Log.d("My Test", "Clicked!2");
        try {
            maximaProccess.execCommand(list);
        } catch (Exception e) {
            // áO
        }
        maximaProccess.clearStringBuilder();
        sem.release();
        Log.d("My Test", "Clicked!3");
                
    }
    public void scrollToEnd() {
    	Log.v("My Test", "scrollToEnd called");
    	Handler handler = new Handler();
    	Runnable task = new Runnable() {
    		
			@Override
			public void run() {
				Runnable viewtask = new Runnable() {
					@Override
					public void run() {
					  webview.pageDown(true);
						Log.v("My Test","scroll!");
					}
				};
				webview.post(viewtask);
			}
    	};
    	handler.postDelayed(task, 1000);
    }
    
   	public boolean onEditorAction(TextView testview, int id, KeyEvent keyEvent) {
   		try {
			sem.acquire();
		} catch (InterruptedException e1) {
			// TODO ©®¶¬³ê½ catch ubN
			e1.printStackTrace();
		}
   		sem.release();
   		String cmdstr="";
   		if ((keyEvent == null) || (keyEvent.getAction()==KeyEvent.ACTION_UP)) {
   			try {
   				cmdstr=_editText.getText().toString();
   				if (cmdstr.equals("reload;")) {webview.loadUrl(maximaURL);return true;}
   				if (cmdstr.equals("sc;")) {this.scrollToEnd();return true;}
   				if (cmdstr.equals("quit();")) exitMOA();
   				if (cmdstr.equals("aboutme;")) {
   					showHTML("file:///android_asset/docs/aboutMOA.html");
   					return true;
   				}
   				if (cmdstr.equals("man;")) {
   					showHTML("file://"+internalDir+"/additions/en/maxima.html");
   					return true;
   				}
   				if (cmdstr.equals("manj;")) {
   					showHTML("file://"+internalDir+"/additions/ja/maxima.html");
   					return true;
   				}
   				removeTmpFiles();
   				if (maxima_syntax_check(cmdstr)) {
   					maximaProccess.maximaCmd(cmdstr+"\n");
   				} else {
   					Toast.makeText(this, "Syntax error. Please correct.", Toast.LENGTH_LONG).show();
   					return false;
   				}
			} catch (IOException e) {
				// TODO ©®¶¬³ê½ catch ubN
				e.printStackTrace();
			} catch (Exception e) {
				// TODO ©®¶¬³ê½ catch ubN
				e.printStackTrace();
			}

   			webview.loadUrl("javascript:window.UpdateText('"+ escapeChars(cmdstr) +"<br>" +"')");
   			String[] resArray=maximaProccess.getProcessResult().split("\n");
   	        maximaProccess.clearStringBuilder();

  			for (int i = 0 ; i < resArray.length ; i++) {
   				if (resArray[i].indexOf("$$") != -1) {
   					String reststr=resArray[i];
   					while (reststr.indexOf("$$") != -1) {
   						int start=reststr.indexOf("$$");
   						reststr=reststr.substring(start+2);
   						int end=reststr.indexOf("$$");
   						webview.loadUrl("javascript:window.UpdateMath('"+ reststr.substring(0,end) +"')");
   						reststr=reststr.substring(end+2);
   					}
   					if (reststr.length()>0) {
   	  		   			String brstr="";
   	   		   			if (i<resArray.length-1) brstr="<br>";
   	   		   			webview.loadUrl("javascript:window.UpdateText('"+ reststr + brstr +"')");
   					}
   				} else {
   		   			String brstr="";
   		   			if (i<resArray.length-1) brstr="<br>";
   		   			webview.loadUrl("javascript:window.UpdateText('"+ resArray[i] + brstr +"')");
   				}
   			}
			if (isGraphFile()) {
		        List<String> list = new ArrayList<String>();
		        list.add(internalDir+"/additions/gnuplot/bin/gnuplot");
		        list.add(internalDir+"/maxout.gnuplot");
		        CommandExec gnuplotcom = new CommandExec();
		        try {
		        	gnuplotcom.execCommand(list);
		        } catch (Exception e) {
		            // áO
		        }
		        if ((new File("/data/data/jp.yhonda/files/maxout.html")).exists()) {
		        	showHTML("file:///data/data/jp.yhonda/files/maxout.html");
		        }
			}
			if (isQepcadFile()) {
		        List<String> list = new ArrayList<String>();
		        list.add("/data/data/jp.yhonda/files/additions/qepcad/qepcad.sh");
		        CommandExec qepcadcom = new CommandExec();
		        try {
		        	qepcadcom.execCommand(list);
		        } catch (Exception e) {
		            // áO
		        }
				
			}

   		}

   		return true;
   	}
   	
   	private boolean maxima_syntax_check(String cmd) {
   		// ÅãªZ~R é¢Í_[ÅIíÁÄ¢é©`FbN
   		if (!cmd.endsWith(";") && !cmd.endsWith("$")) return false;
   		if (cmd.endsWith(";;")) return false;
   		return true;
   	}
   	
   	private String escapeChars(String cmd) {
   		return substitute(cmd, "'", "\\'");
   	}
   	
   	static private String substitute(String input, String pattern, String replacement) {
   	    // u·ÎÛ¶ñª¶Ý·éêðæ¾
   	    int index = input.indexOf(pattern);

   	    // u·ÎÛ¶ñª¶ÝµÈ¯êÎI¹
   	    if(index == -1) {
   	        return input;
   	    }

   	    // ðs¤½ßÌ StringBuffer
   	    StringBuffer buffer = new StringBuffer();

   	    buffer.append(input.substring(0, index) + replacement);

   	    if(index + pattern.length() < input.length()) {
   	        // cèÌ¶ñðÄAIÉu·
   	        String rest = input.substring(index + pattern.length(), input.length());
   	        buffer.append(substitute(rest, pattern, replacement));
   	    }
   	    return buffer.toString();
   	}
   	
   	private void showHTML(String url) {
      	Intent intent = new Intent(this,HTMLActivity.class);
      	intent.setAction(Intent.ACTION_VIEW);
      	intent.putExtra("url", url);
      	this.startActivity(intent);
   	}

   	/**
   	 * 
   	 * @param url
   	 * @param languageFlag Should be one of 
   	 * {@link jp.yhonda.ManualActivity.ENGLISH_VERSION} or
   	 * {@link jp.yhonda.ManualActivity.JAPANESE_VERSION} to indicate
   	 * which manual language version should be displayed.
   	 */
   	private void showManual(String url, int languageFlag) {
      	Intent intent = new Intent(this,ManualActivity.class);
      	intent.setAction(Intent.ACTION_VIEW);
      	intent.putExtra("url", url);
      	intent.putExtra("dir", internalDir);
      	intent.putExtra(ManualActivity.LANGUAGE_EXTRA, languageFlag);
      	
      	// The design seems to be that ManualActivity doesn't know
      	// the locations of the manual files; so they are passed here
      	// for lanuage switching within ManualActivity.
      	intent.putExtra(ManualActivity.ENGLISH_LOCATION_EXTRA, "file://"+internalDir+"/additions/en/maxima.html");
      	intent.putExtra(ManualActivity.JAPANESE_LOCATION_EXTRA, "file://"+internalDir+"/additions/ja/maxima.html");
      	this.startActivity(intent);
   	}   	
   	private void showGraph() {
        if ((new File("/data/data/jp.yhonda/files/maxout.html")).exists()) {
        	showHTML("file:///data/data/jp.yhonda/files/maxout.html");
        } else {
			Toast.makeText(this, "No graph to show.", Toast.LENGTH_LONG).show();        	
        }
   	}

   	private void removeTmpFiles() {
   		File a=new File("/data/data/jp.yhonda/files/maxout.gnuplot");
   		if (a.exists()) {
   			a.delete();
   		}
   		a=new File("/data/data/jp.yhonda/files/maxout.html");
   		if (a.exists()) {
   			a.delete();
   		}
   		a=new File("/data/data/jp.yhonda/files/qepcad_input.txt");
   		if (a.exists()) {
   			a.delete();
   		}
   	}
   	
   	private Boolean isGraphFile() {
   		File a=new File("/data/data/jp.yhonda/files/maxout.gnuplot");
   		return(a.exists());   		
   	}
   	private Boolean isQepcadFile() {
   		File a=new File("/data/data/jp.yhonda/files/qepcad_input.txt");
   		return(a.exists());   		
   	}
   	private void exitMOA() {
   		try {
			maximaProccess.maximaCmd("quit();\n");
			finish();
		} catch (IOException e) {
			// TODO ©®¶¬³ê½ catch ubN
			e.printStackTrace();
		} catch (Exception e) {
			// TODO ©®¶¬³ê½ catch ubN
			e.printStackTrace();
		}
   	}
   	@Override
   	public boolean dispatchKeyEvent(KeyEvent event) {
   	    if (event.getAction()==KeyEvent.ACTION_DOWN) {
   	        switch (event.getKeyCode()) {
   	        case KeyEvent.KEYCODE_BACK:
   				Toast.makeText(this, "Use Quit in the menu.", Toast.LENGTH_LONG).show();        	
   	            return true;
   	        }
   	    }
   	    return super.dispatchKeyEvent(event);
   	}
}


