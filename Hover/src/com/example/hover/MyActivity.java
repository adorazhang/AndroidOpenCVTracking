package com.example.hover;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class MyActivity extends Activity implements  CvCameraViewListener2, OnTouchListener{

	private boolean              mIsColorSelected = false;
	private Mat                  mRgba;
	private Scalar               mBlobColorRgba;
	private Scalar               mBlobColorHsv;
	private ColorBlobDetector    mDetector;

	static MyActivity handle;
	private CameraBridgeViewBase mOpenCvCameraView;
	private static ImageView imageview;
	private Mat mRgbaT;
	private Mat mRgbaF;

	static Point current;

	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(MyActivity.this);
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main);

		CursorTracker tracker;
		try {
			tracker = new CursorTracker(this, (ImageView) findViewById(R.id.cursor), (ImageView) findViewById(R.id.imageview), this);
			new Thread(tracker).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraview);
		mOpenCvCameraView.setCameraIndex(1);
		mOpenCvCameraView.setCvCameraViewListener(this);


//		Thread socketClientThread;
//		socketClientThread = new Thread(new SocketClientThread());
//		socketClientThread.start();
	}

	public static SerialBitmap getScreenshot() {
		Bitmap bitmap;
		View v1 = imageview.getRootView();
		v1.setDrawingCacheEnabled(true);
		bitmap = Bitmap.createBitmap(v1.getDrawingCache());
		v1.setDrawingCacheEnabled(false);
		SerialBitmap srl = new SerialBitmap(bitmap);
		return srl;
	}	

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mRgbaF = new Mat(height, width, CvType.CV_8UC4);
		mRgbaT = new Mat(width, width, CvType.CV_8UC4);  // NOTE width,width is NOT a typo
		mDetector = new ColorBlobDetector();
		mBlobColorRgba = new Scalar(255);
		mBlobColorHsv = new Scalar(255);
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)||(keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
			CursorTracker.logEvent();
		}
		return true;
	}

	private void getCenter(List<MatOfPoint> contours) {
		List<Moments> mu = new ArrayList<Moments>(contours.size());
		for (int i = 0; i < contours.size(); i++) {
			mu.add(i, Imgproc.moments(contours.get(i), false));
			Moments p = mu.get(i);
			int x = (int) (p.get_m10() / p.get_m00());
			int y = (int) (p.get_m01() / p.get_m00());
			current = new Point(x,y);
		}
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		// Rotate mRgba 90 degrees
		Core.transpose(mRgba, mRgbaT);
		Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
		Core.flip(mRgbaF, mRgba, -1);

		Size originalSize = new Size(mRgba.size().width, mRgba.size().height);

		if (mIsColorSelected) {
			mDetector.process(mRgba);
			List<MatOfPoint> contours = mDetector.getContours();
			getCenter(contours);
			Core.circle(mRgba, current, 4, new Scalar(255,49,0,255));
			Mat colorLabel = mRgba.submat(4, 68, 4, 68);
			colorLabel.setTo(mBlobColorRgba);
			CursorTracker.updateLoc((float) current.x, (float)current.y);
		}

		Imgproc.resize(mRgba, mRgba, originalSize);
		return mRgba;
	}

	public boolean onTouch(View v, MotionEvent event) {
		int cols = mRgba.cols();
		int rows = mRgba.rows();

		int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
		int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

		int x = (int)event.getX() - xOffset;
		int y = (int)event.getY() - yOffset;

		if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

		Rect touchedRect = new Rect();

		touchedRect.x = (x>4) ? x-4 : 0;
		touchedRect.y = (y>4) ? y-4 : 0;

		touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
		touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

		Mat touchedRegionRgba = mRgba.submat(touchedRect);

		Mat touchedRegionHsv = new Mat();
		Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

		// Calculate average color of touched region
		mBlobColorHsv = Core.sumElems(touchedRegionHsv);
		int pointCount = touchedRect.width*touchedRect.height;
		for (int i = 0; i < mBlobColorHsv.val.length; i++)
			mBlobColorHsv.val[i] /= pointCount;

		mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
		mDetector.setHsvColor(mBlobColorHsv);

		mIsColorSelected = true;

		touchedRegionRgba.release();
		touchedRegionHsv.release();

		return false; // don't need subsequent touch events
	}


	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

		return new Scalar(pointMatRgba.get(0, 0));
	}
}