package com.codepath.example.servicesnotificationsdemo.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.example.servicesnotificationsdemo.R;
import com.codepath.example.servicesnotificationsdemo.receiver.MyAlarmReceiver;
import com.codepath.example.servicesnotificationsdemo.receiver.MySimpleReceiver;
import com.codepath.example.servicesnotificationsdemo.services.ImageDownloadService;
import com.codepath.example.servicesnotificationsdemo.services.ImageDownloadService2;
import com.codepath.example.servicesnotificationsdemo.services.MySimpleService;
import com.codepath.example.servicesnotificationsdemo.services.SimpleBindService;

public class MainActivity extends Activity {
	public MySimpleReceiver receiverForSimple;
	SimpleBindService mBoundService;
	boolean mServiceBound = false;
	TextView timestampText;
	String[] logos = {"http://www.designrazor.net/wp-content/uploads/2015/01/restaurant-logo-design-examples-3.jpg",
			"http://coolhomepages.com/thumbs/all_files/2011/09/09/itorae.jpg",
			"http://www.designrazor.net/wp-content/uploads/2015/01/restaurant-logo-design-examples-12.png",
			"https://s-media-cache-ak0.pinimg.com/736x/c0/e3/87/c0e3871b3374c245ce26ed783de0fb97.jpg",
			"http://logos.bitra.com/images/banzara.jpg"};

	private PendingIntent alarmPendingIntent;
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mServiceBound = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			SimpleBindService.MyBinder myBinder = (SimpleBindService.MyBinder) service;
			mBoundService = myBinder.getService();
			mServiceBound = true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		timestampText = (TextView) findViewById(R.id.timestamp_text);
		setupServiceReceiver();
		checkForMessage();


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onSimpleService(View v) {
		// Construct our Intent specifying the Service
		Intent i = new Intent(this, MySimpleService.class);
		// Add extras to the bundle
		i.putExtra("foo", "bar");
		i.putExtra("receiver", receiverForSimple);
		// Start the service
		startService(i);

		stopService(i);
	}

	public void onImageDownloadService(View v) {
		startIntentService(logos[0]);
		startIntentService(logos[1]);
		startIntentService(logos[2]);
		startIntentService(logos[3]);
		startIntentService(logos[4]);

	}

	private void startIntentService(String logo) {
		// Construct our Intent specifying the Service
		Intent i = new Intent(this, ImageDownloadService.class);
		// Add extras to bundle
		i.putExtra("url", logo);
		// Start the service
		startService(i);
	}

	private void startService(String logo) {
		// Construct our Intent specifying the Service
		Intent i = new Intent(this, ImageDownloadService2.class);
		// Add extras to bundle
		i.putExtra("url", logo);
		// Start the service
		startService(i);
	}

	public void onImageDownloadService2(View v) {
		startService(logos[0]);
		startService(logos[1]);
		startService(logos[2]);
		startService(logos[3]);
		startService(logos[4]);
	}

	public void onStartAlarm(View v) {
		// Construct an intent that will execute the AlarmReceiver
		Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
		intent.putExtra("receiver", receiverForSimple);
		// Create a PendingIntent to be triggered when the alarm goes off
		alarmPendingIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// Setup periodic alarm every 5 seconds
		long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
		int intervalMillis = 5000; // 5 seconds
		AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, alarmPendingIntent);
	}

	public void onStopAlarm(View v) {
		AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		if (alarmPendingIntent != null) {
			alarm.cancel(alarmPendingIntent);
		}
	}

	public void onPrintTimeStamp(View v) {
		if (mServiceBound) {
			timestampText.setText(mBoundService.getTimestamp());
		}
	}

	public void onStopService(View v) {
		if (mServiceBound) {
			unbindService(mServiceConnection);
			mServiceBound = false;
		}
		Intent intent = new Intent(MainActivity.this, SimpleBindService.class);
		stopService(intent);

	}

	// Setup the callback for when data is received from the service
	public void setupServiceReceiver() {
		receiverForSimple = new MySimpleReceiver(new Handler());
		// This is where we specify what happens when data is received from the
		// service
		receiverForSimple.setReceiver(new MySimpleReceiver.Receiver() {
			@Override
			public void onReceiveResult(int resultCode, Bundle resultData) {
				if (resultCode == RESULT_OK) {
					String resultValue = resultData.getString("resultValue");
					Toast.makeText(MainActivity.this, resultValue, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	// Checks to see if service passed in a message
	private void checkForMessage() {
		String message = getIntent().getStringExtra("message");
		if (message != null) {
			Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, SimpleBindService.class);
		startService(intent);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mServiceBound) {
			unbindService(mServiceConnection);
			mServiceBound = false;
		}
	}

}
