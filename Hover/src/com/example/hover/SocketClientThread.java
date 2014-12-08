package com.example.hover;

import java.net.*;
import java.io.*;

import android.graphics.*;
import android.util.Log;

public class SocketClientThread implements Runnable {
	Socket socket;  
	@Override
	public void run() {
		while(true) {
			try {  
				socket = new Socket("150.212.42.140", 15213);  
				Log.e("", "!!");  
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());   
				SerialBitmap bmp = MyActivity.getScreenshot();
				out.writeObject(bmp);
				Log.e("","Sent screenshot!!");
				out.close();  
				socket.close();  
			} catch (UnknownHostException e) {  
				e.printStackTrace(); 
				Log.e("", "!!"+e.toString());  
			} catch (IOException e) {  
				e.printStackTrace(); 
				Log.e("", "!!"+e.toString());  
			}  

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
