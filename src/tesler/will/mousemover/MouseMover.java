package tesler.will.mousemover;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.firebase.client.Firebase;

public class MouseMover extends Activity implements SensorEventListener,
		OnClickListener, OnSeekBarChangeListener {

	SensorManager sm;

	Button click, calibrate;

	SeekBar sb;

	int clickCounter = 0;
	int calibrateCounter = 0;

	int sensitivity = 0;

	Firebase firePointer = new Firebase(
			"https://luminous-fire-2276.firebaseio.com/pointer");

	Firebase fireClicker = new Firebase(
			"https://luminous-fire-2276.firebaseio.com/clicker");

	Firebase fireCalibrate = new Firebase(
			"https://luminous-fire-2276.firebaseio.com/calibrate");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// DISPLAY LAYOUT
		setContentView(R.layout.activity_mouse);

		// SETUP MOUSE CLICK SOUND
		final MediaPlayer mp = MediaPlayer.create(this, R.raw.click);

		// SETUP MOUSE CLICK BUTTON
		click = (Button) findViewById(R.id.click);
		click.setOnClickListener(this);
		click.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					postClick();
				} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
					mp.start();
					clickCounter = -2;
					postClick();
				}
				return false;
			}
		});

		// SETUP UI CALIBRATE BUTTON
		calibrate = (Button) findViewById(R.id.calibrate);
		calibrate.setOnClickListener(this);

		// SETUP UI SEEKBAR
		sb = (SeekBar) findViewById(R.id.seekBar1);
		sb.setOnSeekBarChangeListener(this);
		sb.setProgress(25);

		// SENSOR MANAGER
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// REGISTER SENSOR LISTENER
		List<Sensor> typedSensors = sm.getSensorList(Sensor.TYPE_GYROSCOPE);
		if (typedSensors == null || typedSensors.size() <= 0) {
		} else {
			sm.registerListener(this, typedSensors.get(0),
					SensorManager.SENSOR_DELAY_FASTEST);
		}

	}

	// SEND A CLICK SIGNAL TO THE PC
	private void postClick() {

		Map<String, Object> updates = new HashMap<String, Object>();
		updates.put("CLICK", clickCounter++);
		fireClicker.updateChildren(updates);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Nothing
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		// SEND A MOUSE POSITION TO THE PC
		Map<String, Object> updates = new HashMap<String, Object>();
		updates.put("MOUSE-X", (int) (event.values[2] * (sensitivity)));
		updates.put("MOUSE-Y", (int) (event.values[0] * (sensitivity)));
		firePointer.updateChildren(updates);

	}

	void postCalibrate() {
		// SEND A CALIBRATE SIGNAL TO THE PC
		Map<String, Object> updates = new HashMap<String, Object>();
		updates.put("CALIBRATE", calibrateCounter++);
		fireCalibrate.updateChildren(updates);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.click)
			// CLICK!
			postClick();
		else if (v.getId() == R.id.calibrate)
			// CALIBRATE!
			postCalibrate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mouse, menu);
		return true;
	}

	@Override
	protected void onPause() {
		sm.unregisterListener(this);
		super.onPause();
	}

	@Override
	protected void onStop() {
		sm.unregisterListener(this);
		super.onStop();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		sensitivity = progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Nothing
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Nothing
	}
}
