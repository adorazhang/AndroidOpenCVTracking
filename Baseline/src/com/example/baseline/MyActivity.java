package com.example.baseline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MyActivity extends Activity {

	static ImageView imageview;
	private static int topy,bottomy,targetsize=20;
	private static Canvas canvas;
	private static Paint inactivepaint;
	private String startTime;
	private static File file;
	private MediaPlayer mp;
	private float mDownX;
	private float mDownY;
	private boolean isOnClick;
	private float SCROLL_THRESHOLD=10;
	private static Paint whitepaint;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.main);
		imageview = (ImageView) findViewById(R.id.imageview);
		
		Bitmap bitmap = Bitmap.createBitmap((int) getWindowManager()
				.getDefaultDisplay().getWidth(), (int) getWindowManager()
				.getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		imageview.setImageBitmap(bitmap);
		
		whitepaint =  new Paint();
		whitepaint.setColor(Color.WHITE);

		inactivepaint =  new Paint();
		inactivepaint.setColor(Color.GRAY);
		changeTargetSize();
		canvas.drawRect(0, topy, canvas.getWidth(), bottomy, inactivepaint);
		
		startTime = new SimpleDateFormat("MM-dd-hh-mm-ss").format(new Date());
		file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), startTime+".csv");
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mp = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    switch (ev.getAction() & MotionEvent.ACTION_MASK) {
	        case MotionEvent.ACTION_DOWN:
	            mDownX = ev.getX();
	            mDownY = ev.getY();
	            isOnClick = true;
	            break;
	        case MotionEvent.ACTION_CANCEL:
	        case MotionEvent.ACTION_UP:
	            if (isOnClick) {
	            	if(mDownY>900) return false;
					Log.e("","!!!"+mDownY+" "+((float)topy/1920*900) +" "+((float)bottomy/1920*900));
					if(isHit()) {
						canvas.drawRect(0, topy, canvas.getWidth(), bottomy, whitepaint);
						logEvent("1,"+mDownY+","+((float)topy/1920*900)+","+((float)bottomy/1920*900));
						changeTargetSize();
						imageview.invalidate();
						canvas.drawRect(0, topy, canvas.getWidth(), bottomy, inactivepaint);
						v.vibrate(50);
						mp.start();
					}
					else {
						v.vibrate(400);
						logEvent("0,"+mDownY+","+((float)topy/1920*900)+","+((float)bottomy/1920*900));
					}
	            }
	            break;
	        case MotionEvent.ACTION_MOVE:
	            if (isOnClick && (Math.abs(mDownX - ev.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - ev.getY()) > SCROLL_THRESHOLD)) {
	                isOnClick = false;
	            }
	            break;
	        default:
	            break;
	    }
	    return true;
	}
	
	private boolean isHit() {
		float pos = mDownY/900*1920;
		if(pos>=topy && pos<= bottomy){
			return true;
		}
		return false;
	}

	private static int randInt(int min, int max) {
		Random rand = new Random();
		return rand.nextInt((max-min)+1)+min;
	}
	
	public static void changeTargetSize(){
		topy = randInt(300,1600);
		bottomy = topy+targetsize;
	}
	
	public static void logEvent(String str){
		try {
			if(file.exists()){
				BufferedWriter buf = new BufferedWriter(new FileWriter(file, true)); 
				buf.append(str);
				buf.newLine();
				buf.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
