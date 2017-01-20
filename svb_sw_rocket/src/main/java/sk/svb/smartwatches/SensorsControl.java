/*
 Copyright (c) 2011, Sony Ericsson Mobile Communications AB
 Copyright (c) 2011-2013, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
 of its contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package sk.svb.smartwatches;

import java.util.ArrayList;
import java.util.List;

import sk.svb.smartwatches.logic.Blocks;
import sk.svb.smartwatches.logic.Stars;
import sk.svb.smartwatches.logic.Stars.Star;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.aef.registration.Registration.SensorTypeValue;
import com.sonyericsson.extras.liveware.aef.sensor.Sensor;
import com.sonyericsson.extras.liveware.aef.sensor.Sensor.SensorAccuracy;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensor;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorEvent;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorEventListener;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorException;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensorManager;

/**
 * This demonstrates how to collect and display data from two different sensors,
 * accelerometer and light.
 */
class SensorsControl extends ControlExtension {

	private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;

	private int mWidth = 220;

	private int mHeight = 176;

	AccessorySensor mySensor;
	
	private final AccessorySensorEventListener mListener = new AccessorySensorEventListener() {

		@Override
		public void onSensorEvent(AccessorySensorEvent sensorEvent) {
			Log.d(SensorsExtensionService.LOG_TAG,
					"Listener: OnSensorEvent");
			updateCurrentDisplay(sensorEvent);
		}
	};

	/**
	 * Creates a control extension.
	 *
	 * @param hostAppPackageName
	 *            Package name of host application.
	 * @param context
	 *            The context.
	 */
	SensorsControl(final String hostAppPackageName, final Context context) {
		super(context, hostAppPackageName);

		AccessorySensorManager manager = new AccessorySensorManager(context,
				hostAppPackageName);

		mySensor = manager.getSensor(SensorTypeValue.ACCELEROMETER);

		// Determine host application screen size.
		determineSize(context, hostAppPackageName);
	}

	@Override
	public void onResume() {
		myInit();

		Log.d(SensorsExtensionService.LOG_TAG, "Starting control");

		// Note: Setting the screen to be always on will drain the accessory
		// battery. It is done here solely for demonstration purposes.
		setScreenState(Control.Intents.SCREEN_STATE_ON);

		// Start listening for sensor updates.
		register();

	}

	@Override
	public void onPause() {
		// Stop sensor.
		unregister();
	}

	@Override
	public void onDestroy() {
		// Stop sensor.
		unregisterAndDestroy();
		unregisterCanvasThread();
	}
	

	/**
	 * Checks if the control extension supports the given width.
	 *
	 * @param context
	 *            The context.
	 * @param width The width.
	 * @return True if the control extension supports the given width.
	 */
	public static boolean isWidthSupported(Context context, int width) {
		return width == context.getResources().getDimensionPixelSize(
				R.dimen.smart_watch_2_control_width)
				|| width == context.getResources().getDimensionPixelSize(
						R.dimen.smart_watch_control_width);
	}

	/**
	 * Checks if the control extension supports the given height.
	 *
	 * @param context
	 *            The context.
	 * @param height The height.
	 * @return True if the control extension supports the given height.
	 */
	public static boolean isHeightSupported(Context context, int height) {
		return height == context.getResources().getDimensionPixelSize(
				R.dimen.smart_watch_2_control_height)
				|| height == context.getResources().getDimensionPixelSize(
						R.dimen.smart_watch_control_height);
	}

	@Override
	public void onTouch(ControlTouchEvent event) {
		super.onTouch(event);
		if (event.getAction() == Control.Intents.TOUCH_ACTION_RELEASE) {
			// do something
		}
	}

	/**
	 * Determines the width and height in pixels of a given host application.
	 *
	 * @param context
	 *            The context.
	 * @param hostAppPackageName
	 *            The host application.
	 */
	private void determineSize(Context context, String hostAppPackageName) {
		Log.d(SensorsExtensionService.LOG_TAG,
				"Now determine screen size.");

		boolean smartWatch2Supported = DeviceInfoHelper
				.isSmartWatch2ApiAndScreenDetected(context, hostAppPackageName);
		if (smartWatch2Supported) {
			mWidth = context.getResources().getDimensionPixelSize(
					R.dimen.smart_watch_2_control_width);
			mHeight = context.getResources().getDimensionPixelSize(
					R.dimen.smart_watch_2_control_height);
		} else {
			mWidth = context.getResources().getDimensionPixelSize(
					R.dimen.smart_watch_control_width);
			mHeight = context.getResources().getDimensionPixelSize(
					R.dimen.smart_watch_control_height);
		}
	}

	/**
	 * Checks if the sensor currently being used supports interrupt mode and
	 * registers an interrupt listener if it does. If not, a fixed rate listener
	 * will be registered instead.
	 */
	private void register() {
		Log.d(SensorsExtensionService.LOG_TAG, "Register listener");

		if (mySensor != null) {
			try {
				if (mySensor.isInterruptModeSupported()) {
					mySensor.registerInterruptListener(mListener);
				} else {
					mySensor.registerFixedRateListener(mListener,
							Sensor.SensorRates.SENSOR_DELAY_UI);
				}
			} catch (AccessorySensorException e) {
				Log.d(SensorsExtensionService.LOG_TAG,
						"Failed to register listener", e);
			}
		}
	}

	/**
	 * Unregisters any sensor event listeners connected to the sensor currently
	 * being used.
	 */
	private void unregister() {
		if (mySensor != null) {
			mySensor.unregisterListener();
		}
	}

	/**
	 * Unregisters any sensor event listeners and unsets the sensor currently
	 * being used.
	 */
	private void unregisterAndDestroy() {
		unregister();		
		mySensor = null;
	}

	/**
	 * Determines what sensor is currently being used and updates the display
	 * with new data.
	 *
	 * @param sensorEvent
	 */
	private void updateCurrentDisplay(AccessorySensorEvent sensorEvent) {
		
		if (mySensor.getType().getName()
				.equals(Registration.SensorTypeValue.ACCELEROMETER)){
				
			myUpdateGenericSensorDisplay(sensorEvent, mySensor.getType()
					.getName());
		} 
	}

		
	private void myUpdateGenericSensorDisplay(AccessorySensorEvent sensorEvent,
			String sensorType) {

		// Update the values.
		if (sensorEvent != null) {
			float[] values = sensorEvent.getSensorValues();

			if (values != null && values.length == 3) {
				// values[0,1] are in range <-9,9>

				// old version
				// x = (int) values[0];
				// y = (int) values[1];

				// difference between moves must be larger than 1
				if (Math.abs(Math.abs(x) - Math.abs(values[0])) > 1) {
					x = (int) values[0];
				}
				if (Math.abs(Math.abs(y) - Math.abs(values[1])) > 1) {
					y = (int) values[1];
				}
			}
		}
	}

	/*
	 * GAME
	 */
	private Stars stars;
	private Blocks block;
	int x = 0;
	int y = 0;
	Paint c_red, c_bl, c_wh, c_wht;

	// rocket size
	public static final int R_SIZE_W = 6;
	public static final int R_SIZE_H = 6;
	public static final int INIT_POSITION = -1000;

	public static final int PADDING_GAME_LEFT = R_SIZE_W;
	public static final int PADDING_GAME_RIGHT = R_SIZE_W;
	public static final int PADDING_GAME_TOP = R_SIZE_H;
	public static final int PADDING_GAME_BOTTOM = R_SIZE_H + 1;

	public static final int PADDING_GAME_OVER_LEFT = 37;
	public static final int PADDING_GAME_OVER_RIGHT = 35;
	public static final int PADDING_GAME_OVER_TOP = 11;
	public static final int PADDING_GAME_OVER_BOTTOM = 13;

	private static final boolean DEBUG = false;

	public int points = 0;
	int rx = INIT_POSITION, ry = INIT_POSITION;
	int countDown = 20;
	boolean endGame = false;
	private CanvasThread canvasThread;
	

	private void myInit() {
		stars = new Stars(mWidth, mHeight);
		block = new Blocks(mWidth, mHeight);

		c_wh = new Paint();
		c_wh.setStrokeWidth(2);
		c_wh.setColor(Color.WHITE);

		c_wht = new Paint();
		c_wht.setColor(Color.WHITE);
				
		this.canvasThread = new CanvasThread();
		canvasThread.setRunning(true);
		canvasThread.start();
	}

	private void drawRocket(Canvas canvas, int rx, int ry) {
		canvas.drawLine(rx, ry - R_SIZE_H, rx + R_SIZE_W, ry + R_SIZE_H, c_wh);
		canvas.drawLine(rx + R_SIZE_W, ry + R_SIZE_H, rx - R_SIZE_W, ry
				+ R_SIZE_H, c_wh);
		canvas.drawLine(rx - R_SIZE_W, ry + R_SIZE_H, rx, ry - R_SIZE_H, c_wh);
	}

	private Canvas myDraw(Canvas canvas) {

		screenLimitationsAndMovement(canvas);

		// draw black bg
		canvas.drawARGB(255, 0, 0, 0);

		if (!endGame){
			// draw start
			drawStars(canvas);
	
			// // draw rocket
			drawRocket(canvas, rx, ry);
			
			// countdown at startup
			if (countDown <= 0){
			// draw gates
				drawGates(canvas, rx, ry);
			}else{
				countDown --;
			}
			if (DEBUG){
				 canvas.drawText("x: " + x, 10, 10, c_wh);
				 canvas.drawText("y: " + y, 10, 20, c_wh);
			}else{
				canvas.drawText("score: " + points, 10, 10, c_wh);
				canvas.drawText("speed: " + Math.round(block.speed), 10, 20, c_wh);
			}

		}else{
			canvas.drawText("GAME OVER", rx-PADDING_GAME_OVER_RIGHT, ry, c_wh);
			canvas.drawText("score: " + points, rx-PADDING_GAME_OVER_RIGHT+10, ry+10, c_wh);
		}
		return canvas;
	}

	private void screenLimitationsAndMovement(Canvas canvas){

		// do a smooth moving not used
		// int rx = this.rx - (int)Math.round(x * Math.abs(x) * 2);
		// int ry = this.ry + (int)Math.round(y * Math.abs(y) * 2);

		// old version of moving - jump to position
		int xx = (int) ((double) canvas.getWidth() / 18 * x);
		int yy = (int) ((double) canvas.getHeight() / 18 * y);
		// new position
		int rx = canvas.getWidth() / 2 - xx;
		int ry = canvas.getHeight() / 2 + yy;

		// do some smooth filtering for a movement, use previous position
		rx = (int)Math.round(this.rx*0.5 + rx*0.5);
		ry = (int)Math.round(this.ry*0.5 + ry*0.5);

		// init position
		if (this.rx == INIT_POSITION && this.ry == INIT_POSITION){
			if (!endGame) {
				rx = canvas.getWidth() / 2;
				ry = canvas.getHeight() / 2;
			}else{
				rx = canvas.getWidth() / 2 - PADDING_GAME_OVER_RIGHT;
				ry = canvas.getHeight() / 2 - PADDING_GAME_OVER_BOTTOM;
			}
		}

		// screen limitations
		if (!endGame) {
			if (rx - PADDING_GAME_LEFT < 0) rx = PADDING_GAME_LEFT;
			if (rx + PADDING_GAME_RIGHT > canvas.getWidth()) rx = canvas.getWidth() - PADDING_GAME_RIGHT;
			if (ry - PADDING_GAME_TOP < 0) ry = PADDING_GAME_TOP ;
			if (ry + PADDING_GAME_BOTTOM > canvas.getHeight()) ry = canvas.getHeight() - PADDING_GAME_BOTTOM;
		}else{
			if (rx - PADDING_GAME_OVER_LEFT < 0) rx = PADDING_GAME_OVER_LEFT;
			if (rx + PADDING_GAME_OVER_RIGHT > canvas.getWidth()) rx = canvas.getWidth() - PADDING_GAME_OVER_RIGHT;
			if (ry - PADDING_GAME_OVER_TOP < 0) ry = PADDING_GAME_OVER_TOP;
			if (ry + PADDING_GAME_OVER_BOTTOM > canvas.getHeight()) ry = canvas.getHeight() - PADDING_GAME_OVER_BOTTOM;
		}

		// save actual coordinates
		this.rx = rx;
		this.ry = ry;
	}

	private void drawGates(Canvas canvas, int rx, int ry){
		canvas.drawRect(block.getRect1(), c_wht);
		canvas.drawRect(block.getRect2(), c_wht);
		block.update(points, 0);
		if (block.throughGate(rx, ry)) {
			points++;
			startVibrator(20, 0, 0);
		}
		if (block.detectCollision(rx - R_SIZE_W, ry - R_SIZE_H)
				|| block.detectCollision(rx + R_SIZE_W, ry + R_SIZE_H)){
			
			endGame();		
		}
	}
	
	public void endGame(){
		endGame = true;
		rx = INIT_POSITION;
		ry = INIT_POSITION;
	}
	
	private void drawStars(Canvas canvas) {
		// draw stars
		for (Star s : stars.getStarList()) {
			canvas.drawCircle(s.x, s.y, s.size, c_wht);
		}
		stars.updateStars();
	}
	
	private void unregisterCanvasThread() {
		boolean retry = true;
		if (canvasThread != null){
			canvasThread.setRunning(false);
			while (retry) {
				try {
					canvasThread.join();
					retry = false;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private class CanvasThread extends Thread {
//		private SurfaceHolder surfaceHolder;
		private boolean isRun = false;

		public CanvasThread() {
//			this.surfaceHolder = holder;
		}

		public void setRunning(boolean run) {
			this.isRun = run;
		}

		public boolean isRunning() {
			return this.isRun;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Canvas c;

			while (isRun) {
				c = null;

				Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, BITMAP_CONFIG);
				
				Canvas canvas = new Canvas(bitmap);
				canvas = myDraw(canvas);
				showBitmap(bitmap);


				try {
					sleep(50); // 20 redraw times a second
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
