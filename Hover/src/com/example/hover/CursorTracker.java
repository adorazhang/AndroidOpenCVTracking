package com.example.hover;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;

public class CursorTracker implements Runnable{

	static Context mContext;
	private static CursorTracker handle = null;
	private static ImageView cursor, imageview;
	private static Canvas canvas;
	private static int topy;
	private static int targetsize=20;
	private static int bottomy;
	private Activity parentActivity;
	float x = 0;
	float y = 0;
	private Paint hoverpaint, inactivepaint;
	private MediaPlayer mp;
	private static Paint whitepaint;
	static File file;
	static String startTime;

	public CursorTracker(Context context, ImageView icon, ImageView imagev, Activity activity) throws IOException {
		this.mContext = context;
		this.cursor = icon;
		this.imageview = imagev;
		this.parentActivity = activity;
		this.handle = this;

		Bitmap bitmap = Bitmap.createBitmap((int) activity.getWindowManager()
				.getDefaultDisplay().getWidth(), (int) activity.getWindowManager()
				.getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		imageview.setImageBitmap(bitmap);

		inactivepaint =  new Paint();
		inactivepaint.setColor(Color.GRAY);
		hoverpaint =  new Paint();
		hoverpaint.setColor(Color.MAGENTA);
		whitepaint =  new Paint();
		whitepaint.setColor(Color.WHITE);
		changeTargetSize();

		startTime = new SimpleDateFormat("MM-dd-hh-mm-ss").format(new Date());
		file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), startTime+".csv");
		file.createNewFile();
		
		mp = MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI);
	}

	public static void logEvent(){
		String str;
		Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		if(hovering()){
			str = "1," + cursor.getY() +", "+ ((float)topy/1920*900) +", "+ ((float)bottomy/1920*900);
			v.vibrate(50);
			canvas.drawRect(0, topy, canvas.getWidth(), bottomy, whitepaint);
			imageview.invalidate();
			changeTargetSize();
		}
		else {
			str = "0," + cursor.getY() +", "+ ((float)topy/1920*900) +", "+ ((float)bottomy/1920*900);
			v.vibrate(400);
		}
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

	public static void updateLoc(float x, float y){
		if (handle != null){
			handle.x = x;
			handle.y = y;
		}
	}

	private static int randInt(int min, int max) {
		Random rand = new Random();
		return rand.nextInt((max-min)+1)+min;
	}

	public static void changeTargetSize(){
		topy = randInt(300,1600);
		bottomy = topy+targetsize;
	}

	public static void redraw(Paint p){
		canvas.drawRect(0, topy, canvas.getWidth(), bottomy, p);
	}

	static boolean hovering() {
		// TODO Auto-generated method stub
		if(cursor.getX()<=0) return false;
		float pos = cursor.getY()/900*1920;
		if(pos>=topy && pos<= bottomy){
			return true;
		}
		return false;
	}

	@Override
	public void run() {	
		while (true){
			parentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					cursor.setX(x);
					cursor.setY(y);
					imageview.invalidate();
					if(hovering()) {
						mp.start();
						redraw(hoverpaint);
					}
					else redraw(inactivepaint);
				}
			});
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
