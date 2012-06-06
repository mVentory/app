package com.mageventory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mageventory.job.Job;
import com.mageventory.job.JobControlInterface;
import com.mageventory.job.JobID;
import com.mageventory.job.JobQueue;
import com.mageventory.job.JobQueueDBHelper;
import com.mageventory.job.JobService;
import com.mageventory.job.JobQueue.JobsSummary;
import com.mageventory.res.ResourceStateActivity;
import com.mageventory.settings.Settings;
import com.mageventory.speech.SpeechRecognition;
import com.mageventory.speech.SpeechRecognition.OnRecognitionFinishedListener;
import com.mageventory.util.DefaultOptionsMenuHelper;
import com.mageventory.util.Log;

public class MainActivity extends BaseActivity {
	protected MyApplication app;
	private Settings settings;
	public static final String PREFS_NAME = "pref.dat";
	ProgressDialog pDialog;
	private boolean isActivityAlive;
	
	private JobQueue.JobSummaryChangedListener jobSummaryListener;

	
	
	SpeechRecognition sr = null;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		isActivityAlive = true;
		
		/* TODO: Don't forget to delete this!!!! This is just for testing purposes!!! */
   		//this.deleteDatabase(JobQueueDBHelper.DB_NAME);
   		
   		JobService.wakeUp(this);

		app=(MyApplication) getApplication();
		settings=new Settings(getApplicationContext());
		
		String versionName;
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			versionName = versionName.substring(versionName.lastIndexOf("r"));
			
			this.setTitle("Mventory: Home " + versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			this.setTitle("Mventory: Home");
			Log.logCaughtException(e);
		}
		
		if (settings.hasSettings()) {
			TextView host_url = (TextView) findViewById(R.id.config_state);
			host_url.setText(settings.getUrl());
			Linkify.addLinks(host_url,Linkify.WEB_URLS);
			
		} else {
			Toast.makeText(getApplicationContext(), "Make Config", 1000);
		}
		
		Button settingsButton = (Button) findViewById(R.id.settingsButton);
		settingsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent newInt = new Intent(getApplicationContext(),ConfigServerActivity.class);
				startActivityForResult(newInt,0);				
			}
		});
		
		
		Button queueButton = (Button) findViewById(R.id.queueButton);
		queueButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, QueueActivity.class);
				MainActivity.this.startActivity(intent);
			}
		});
		
		final TextView new_prod_pending = (TextView) findViewById(R.id.new_prod_pending);
		final TextView photos_pending = (TextView) findViewById(R.id.photos_pending);
		
		final TextView new_prod_failed = (TextView) findViewById(R.id.new_prod_failed);
		final TextView photos_failed = (TextView) findViewById(R.id.photos_failed);
		
		jobSummaryListener = new JobQueue.JobSummaryChangedListener() {
			
			@Override
			public void OnJobSummaryChanged(final JobsSummary jobsSummary) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if (isActivityAlive)
						{
							new_prod_pending.setText("" + jobsSummary.pending.newProd);
							photos_pending.setText("" + jobsSummary.pending.photo);
							
							new_prod_failed.setText("" + jobsSummary.failed.newProd);
							photos_failed.setText("" + jobsSummary.failed.photo);
						}
					}
				});
			}
		};
		
		Button speechRecognition = (Button) findViewById(R.id.testSpeechRecognition);
		speechRecognition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 SpeechRecognition sr = new SpeechRecognition(MainActivity.this, new OnRecognitionFinishedListener() {
 					
 					@Override
 					public void onRecognitionFinished(String output) {
 					}
 				},"");
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		JobQueue.setOnJobSummaryChangedListener(jobSummaryListener);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		JobQueue.setOnJobSummaryChangedListener(null);
		
		if (sr != null)
		{
			sr.finishSpeechRecognition();
		}
	}
	
	@Override
	public void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    /* must be here, on onCreate app crashes*/
	    openOptionsMenu();
	}
	
	public void onResourceStateButtonClick(View v) {
		Intent i = new Intent(this, ResourceStateActivity.class);
		startActivity(i);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		isActivityAlive = false;
	}
}
