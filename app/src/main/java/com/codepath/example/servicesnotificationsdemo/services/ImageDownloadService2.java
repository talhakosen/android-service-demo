package com.codepath.example.servicesnotificationsdemo.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.codepath.example.servicesnotificationsdemo.R;
import com.codepath.example.servicesnotificationsdemo.activities.ImagePreviewActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloadService2 extends Service {
	public  int NOTIF_ID = 1000;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			// Extract additional values from the bundle
			String imageUrl = msg.obj.toString();
			// Download image
			Bitmap bitmap = downloadImage(imageUrl);
			// Create completion notification
			Log.d("image-service","ImageDownloadService2 -> image downloaded");
			createNotification(bitmap,NOTIF_ID++);
			stopSelf(msg.arg1);
		}
	}

	@Override
	public void onCreate() {
		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Thread.MAX_PRIORITY);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		Log.d("image-service","ImageDownloadService2 -> intent arrived");
		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj  = intent.getStringExtra("url");
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
	}


	protected Bitmap downloadImage(String address) {
		// Convert string to URL
		URL url = getUrlFromString(address);
		// Get input stream
		InputStream in = getInputStream(url);
		// Decode bitmap
		Bitmap bitmap = decodeBitmap(in);
		// Return bitmap result
		return bitmap;
	}

	// Construct compatible notification
	private void createNotification(Bitmap bmp,int id) {
		// Resize bitmap
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / 5, bmp.getHeight() / 5, false);
		// Construct pending intent to serve as action for notification item
		Intent intent = new Intent(this, ImagePreviewActivity.class);
		intent.putExtra("bitmap", resizedBitmap);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		// Create notification
		Notification noti = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Image Download Complete!").setContentText("Image download from IntentService has completed! Click to view!")
				.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bmp))
				.setContentIntent(pIntent).build();

		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(id, noti);
	}

	private URL getUrlFromString(String address) {
		URL url;
		try {
			url = new URL(address);
		} catch (MalformedURLException e1) {
			url = null;
		}
		return url;
	}

	private InputStream getInputStream(URL url) {
		InputStream in;
		// Open connection
		URLConnection conn;
		try {
			conn = url.openConnection();
			conn.connect();
			in = conn.getInputStream();
		} catch (IOException e) {
			in = null;
		}
		return in;
	}

	private Bitmap decodeBitmap(InputStream in) {
		Bitmap bitmap;
		try {
			// Turn response into Bitmap
			bitmap = BitmapFactory.decodeStream(in);
			// Close the input stream
			in.close();
		} catch (IOException e) {
			in = null;
			bitmap = null;
		}
		return bitmap;
	}
	
	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
