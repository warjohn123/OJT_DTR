package com.example.gztrackz;

import java.util.StringTokenizer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.tabs.HomeFragment.MyInterface;
import com.example.tabsadapter.TabsPagerAdapter;


@SuppressLint("NewApi")
public class TabsManager extends FragmentActivity implements ActionBar.TabListener,MyInterface  {

	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;
	// Tab titles
	private String[] tabs = { "Home", "History", "StandUps" };
	private String PREFERENCE_NAME = "com.example.gztrackz",FNAME = "com.example.gztrackz.firstname",LNAME = "com.example.gztrackz.lastname",EMAIL="com.example.gztrackz.email";
	private String email;
	private DB_USER_TIME_LOG timeLogDB;
	private Context context;
	private SharedPreferences prefs ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabs_manager);
		timeLogDB = new DB_USER_TIME_LOG(this);
		timeLogDB.open();
		// Initialization
		 prefs = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
		context  = this;
		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setTitle(prefs.getString(FNAME, null) + " " + prefs.getString(LNAME,null));
		//actionBar.setDisplayShowTitleEnabled(false);	
		email = getIntent().getStringExtra("email");
		
		
		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}
					
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void buttonClicked(boolean timeIn) {
	//	Toast.makeText(getApplicationContext(), email, Toast.LENGTH_LONG).show();	
		new TimeLog(context, email,timeIn).execute();
		
	}		
	
	private class TimeLog extends AsyncTask<String, Void,Boolean> {	        
	    	String email,password;
	    	Context context;
	    	ProgressDialog progressD;
	    	String date, time;
	    	boolean timeIn;
	    	
	    	public TimeLog(Context context,String email,boolean timeIn){
	    		this.context = context;
	    		this.email = email;
	    		this.password = password;	    		
	    		this.timeIn = timeIn;
	    	}
	    	
	    	@Override
	        protected void onPreExecute() {
	    		progressD = new ProgressDialog(context);	    		
	    		if(timeIn)
	    			progressD.setMessage("TimeOut in progress...");
	    		else
	    			progressD.setMessage("TimeIn in progress...");
	    		progressD.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    		progressD.show();
	    		Log.d("PreCheck",Boolean.toString(timeIn));
	    			    			    
	        }
	    	
	    	@Override
	        protected void onPostExecute(Boolean result) {        	
	        	if(progressD.isShowing()){
	        		progressD.dismiss();
	        	}
	        	if(result){	    
	        		if(!timeIn){
	        			Toast.makeText(context,"Successfulled timed in at "+ time + ".", Toast.LENGTH_LONG).show();
	        		}else{
	        			Toast.makeText(context,"Successfulled timed out at "+ time + ".", Toast.LENGTH_LONG).show();
	        		}
	        			 	
	        		if(!timeIn)
	        			new StandupCheck(context,email).execute();
	        	}
	        	else
	        		Toast.makeText(context,"Unable to execute time in. Please check internet connection!", Toast.LENGTH_LONG).show();	        		        	
	    	}
	    	
	    	@Override
	        protected Boolean doInBackground(String... params) {	    		
	    		boolean flag = true;	
	            
	            try {	            	
	            	String urlTopTracks ;
					HttpClient client = new DefaultHttpClient();
					ResponseHandler<String> handler = new BasicResponseHandler();
					
					HttpPost request ;
					
					String httpResponseTopTracks;				
					
					StringTokenizer token;
					String retrieveResult;
					
					JSONObject result;
					String emailResult;	            		            
					Log.d("PreCheck",Boolean.toString(timeIn));
	            	if(timeIn){
	            		urlTopTracks = "http://gz123.site90.net/timeout/?email=" + email ;										
						request = new HttpPost(urlTopTracks);					
						httpResponseTopTracks = client.execute(request, handler);				
						
						 token = new StringTokenizer(httpResponseTopTracks,"<");
						retrieveResult = token.nextToken();
						
						result = new JSONObject(retrieveResult);
						emailResult = result.getString("email");
						if(emailResult.length()==0){					
							flag = false;
						}else{
							date = result.getString("date");
							time = result.getString("time");
							flag = true;
						}
						Log.d("Time out",Boolean.toString(timeIn));
	            	}
	            	else{
		            	urlTopTracks = "http://gz123.site90.net/timein/?email=" + email ;										
						request = new HttpPost(urlTopTracks);					
						httpResponseTopTracks = client.execute(request, handler);				
						
						 token = new StringTokenizer(httpResponseTopTracks,"<");
						retrieveResult = token.nextToken();
						
						result = new JSONObject(retrieveResult);
						emailResult = result.getString("email");
						if(emailResult.length()==0){					
							flag = false;
						}else{
							date = result.getString("date");
							time = result.getString("time");
							flag = true;
						}
						Log.d("Time In",Boolean.toString(timeIn));
					}
				} catch (Exception e) {
					flag = false;
					e.printStackTrace();
				}
	            
	            return flag;
	        }	             
	    }
	

	private class StandupCheck extends AsyncTask<String, Void,Boolean> {	        
    	String email,password;
    	Context context;
    	ProgressDialog progressD;
    	String date, time;
    	boolean standupAvailable;
    	
    	public StandupCheck(Context context,String email){
    		this.context = context;
    		this.email = email;
    		this.password = password;
    	}
    	
    	@Override
        protected void onPreExecute() {
    		progressD = new ProgressDialog(context);
    		progressD.setMessage("Checking standup status...");
    		progressD.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		progressD.setCancelable(false);
    		progressD.setCanceledOnTouchOutside(false);
    		progressD.show();
        }
    	
    	@Override
        protected void onPostExecute(Boolean result) {        	
        	if(progressD.isShowing()){
        		progressD.dismiss();
        	}	        		
        	
        	if(standupAvailable){
        		Intent i =new Intent(context,StandUpsDialog.class);
        		startActivityForResult(i,1);			        			
        	}
        }
    	
    	@Override
        protected Boolean doInBackground(String... params) {
            boolean flag = true;	            
            try {
            	String urlTopTracks = "http://gz123.site90.net/standups_status/?email=" + email ;
				HttpClient client = new DefaultHttpClient();
				ResponseHandler<String> handler = new BasicResponseHandler();
				
				HttpPost request = new HttpPost(urlTopTracks);
				
				String httpResponseTopTracks = client.execute(request, handler);				
				
				StringTokenizer token = new StringTokenizer(httpResponseTopTracks,"<");
				String retrieveResult = token.nextToken();
				
				
				if(retrieveResult.contains("empty")){
					standupAvailable= true;
				}else{
					standupAvailable = false;
				}
				Log.d("Standup Status",retrieveResult);
				
			} catch (Exception e) {			
				flag = false;
				e.printStackTrace();
			}
            
            return flag;
        }	             
    }
		
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.time_manager, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_logout:					
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				        	SharedPreferences.Editor editor = prefs.edit();
				        	setResult(RESULT_OK);
				        	editor.putString(LNAME,null);
							editor.putString(FNAME, null);
							editor.putString(EMAIL,null);
				            editor.commit();
				        	finish();
				            break;
				        case DialogInterface.BUTTON_NEGATIVE:
				            break;
				        }
				    }
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
				    .setNegativeButton("No", dialogClickListener).show();					        	
	        	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
	    }
		
	}
	
	
	
	 
	
}
