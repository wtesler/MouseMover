package tesler.will.mousemover;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MouseMover2 extends Activity implements SensorEventListener,
		OnClickListener, OnSeekBarChangeListener, PeerListListener {

	SensorManager sm;

	Button click, calibrate;

	SeekBar sb;

	int clickCounter = 0;
	int calibrateCounter = 0;

	int sensitivity = 0;

	WifiP2pManager mManager;
	Channel mChannel;
	BroadcastReceiver mReceiver;
	MouseMover cont;
	IntentFilter mIntentFilter;
	PeerListListener myPeerListListener;
	Client client;

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

		init_wifi_direct();

	}

	private void init_wifi_direct() {

		mManager = (WifiP2pManager) cont
				.getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(cont, cont.getMainLooper(), null);

		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, cont,
				myPeerListListener);

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
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

	}

	void postCalibrate() {
		// SEND A CALIBRATE SIGNAL TO THE PC
		Map<String, Object> updates = new HashMap<String, Object>();
		updates.put("CALIBRATE", calibrateCounter++);
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

	void register() {
		cont.registerReceiver(mReceiver, mIntentFilter);
	}

	void unregister() {
		cont.unregisterReceiver(mReceiver);
	}

	void discoverPeers() {
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				//
			}

			@Override
			public void onFailure(int reasonCode) {
				//
			}
		});
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		// obtain a peer from the WifiP2pDeviceList
		if (peers.getDeviceList().size() > 0) {
			WifiP2pDevice device = (WifiP2pDevice) peers.getDeviceList()
					.toArray()[0];
			final WifiP2pConfig config = new WifiP2pConfig();
			config.deviceAddress = device.deviceAddress;
			mManager.connect(mChannel, config, new ActionListener() {
				@Override
				public void onSuccess() {
					client = new Client(getApplicationContext(),
							config.deviceAddress, 8888);
					client.start();
				}

				@Override
				public void onFailure(int reason) {
				}
			});
		}
	}
}
