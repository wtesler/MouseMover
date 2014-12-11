package tesler.will.mousemover;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;
import com.firebase.simplelogin.User;

public class Login extends Activity {

	final String TAG = "Login";

	TextView tv_title;
	EditText et_email, et_password;
	Button bt_create, bt_login;

	final int[] colors = { Color.RED, 0xFFFF7F00, Color.YELLOW, Color.GREEN,
			Color.BLUE, 0xFF4B0082, 0xFF8F00FF };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		new Global(this);

	}

	@Override
	protected void onResume() {

		Firebase ref = new Firebase("https://luminous-fire-2276.firebaseio.com");
		final SimpleLogin authClient = new SimpleLogin(ref,
				getApplicationContext());

		authClient.checkAuthStatus(new SimpleLoginAuthenticatedHandler() {
			@Override
			public void authenticated(
					com.firebase.simplelogin.enums.Error error, User user) {
				if (error != null) {
					Log.w(TAG, "Error: " + error.name());
					Toast t = Toast.makeText(Login.this, error.name(),
							Toast.LENGTH_SHORT);
					t.setGravity(Gravity.CENTER, 0, 0);
					t.show();
					setContentView(R.layout.activity_login);
					init_layout(authClient);
				} else if (user == null) {
					setContentView(R.layout.activity_login);
					Log.w(TAG, "User was null when logging in.");
					init_layout(authClient);
				} else {
					Log.i(TAG, "User " + user.getUserId() + " is logged in.");
					start_next_activity();
				}
			}
		});

		Firebase authRef = ref.getRoot().child(".info/authenticated");
		authRef.addValueEventListener(new ValueEventListener() {
			public void onDataChange(DataSnapshot snap) {
				boolean isAuthenticated = snap.getValue(Boolean.class);
				if (isAuthenticated) {
					Log.i(TAG, "User is authenticated.");
					Global.toast("User is authenticated.", Toast.LENGTH_SHORT);
				} else {
					Log.w(TAG, "User is not authenticated.");
				}
			}

			@Override
			public void onCancelled(FirebaseError error) {
				Log.w(TAG, error.getMessage());
			}
		});

		super.onResume();
	}

	private void init_layout(final SimpleLogin authClient) {

		tv_title = (TextView) findViewById(R.id.tv_roygbiv);

		Shader textShader = new LinearGradient(0, 0, 0, 100, colors,
				new float[] { 0, 0.2f, 0.4f, 0.6f, 0.8f, 0.9f, 1 },
				TileMode.CLAMP);
		tv_title.getPaint().setShader(textShader);
		tv_title.setTextSize(40);
		tv_title.setBackgroundColor(Color.BLACK);

		et_email = (EditText) findViewById(R.id.et_email);
		et_password = (EditText) findViewById(R.id.et_password);
		bt_create = (Button) findViewById(R.id.bt_create);
		bt_create.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bt_create.setClickable(false);
				bt_login.setClickable(false);
				String email = et_email.getText().toString();
				String pass = et_password.getText().toString();
				if (!email.isEmpty() && !pass.isEmpty()) {
					createAccount(authClient, email, pass);
				}
			}
		});
		bt_login = (Button) findViewById(R.id.bt_login);
		bt_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bt_login.setClickable(false);
				bt_create.setClickable(false);
				String email = et_email.getText().toString();
				String pass = et_password.getText().toString();
				if (!email.isEmpty() && !pass.isEmpty()) {
					login(authClient, email, pass);
				} else {
					Toast t = Toast
							.makeText(Login.this,
									"You must fill in both fields.",
									Toast.LENGTH_SHORT);
					t.setGravity(Gravity.CENTER, 0, 0);
					t.show();
					bt_login.setClickable(true);
					bt_create.setClickable(true);
				}
			}
		});

		String prefEmail = Global.load("email");
		et_email.setText(prefEmail);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void createAccount(final SimpleLogin authClient,
			final String email, final String password) {

		authClient.createUser(email, password,
				new SimpleLoginAuthenticatedHandler() {
					@Override
					public void authenticated(
							com.firebase.simplelogin.enums.Error error,
							User user) {
						if (error != null) {
							Log.w(TAG, "Error: " + error.name());
							Toast t = Toast.makeText(Login.this, error.name(),
									Toast.LENGTH_SHORT);
							t.setGravity(Gravity.CENTER, 0, 0);
							t.show();
						} else {
							Log.i(TAG, "User " + user.getUserId()
									+ " has created an account.");
							Global.toast("Successfully Created Account.",
									Toast.LENGTH_SHORT);
							login(authClient, email, password);
						}
						bt_create.setClickable(true);
						bt_login.setClickable(true);
					}
				});
	}

	private void login(SimpleLogin authClient, String email, String password) {
		authClient.loginWithEmail(email, password,
				new SimpleLoginAuthenticatedHandler() {
					@Override
					public void authenticated(
							com.firebase.simplelogin.enums.Error error,
							User user) {
						if (error != null) {
							Log.w(TAG, "Error: " + error.name());
							Toast t = Toast.makeText(Login.this, error.name(),
									Toast.LENGTH_SHORT);
							t.setGravity(Gravity.CENTER, 0, 0);
							t.show();
						} else {
							Log.i(TAG, "User " + user.getUserId()
									+ " has logged in.");
							Global.save("email", user.getEmail());
							Global.save("user", user.getUid());
							Global.toast("Successfully Logged In.",
									Toast.LENGTH_SHORT);

							start_next_activity();
						}

						bt_login.setClickable(true);
						bt_create.setClickable(true);
					}
				});
	}

	protected void start_next_activity() {

		if (et_password != null)
			et_password.setText("");

		startActivity(new Intent(Login.this, MouseMover.class));

		finish();
	}
}
