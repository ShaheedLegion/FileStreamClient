package inspirational.designs.filestreamclient;

import inspirational.designs.filestreamclient.network.ClientSocketConnection;
import inspirational.designs.filestreamclient.util.SystemUiHider;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
	private SystemUiHider mSystemUiHider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		final View contentView = findViewById(R.id.fullscreen_content);

		mSystemUiHider = SystemUiHider.getInstance(this, contentView, SystemUiHider.FLAG_HIDE_NAVIGATION);
		mSystemUiHider.setup();

		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSystemUiHider.toggle();
			}
		});
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		delayedHide(1000);
		
		// The next step is to connect and check the status of the server.
		new CountDownTimer(2000, 1000) {
			public void onTick(long millisUntilFinished) {}

		    public void onFinish() {
		    	new connectionTask().execute();
		    }
		}.start();
	}

	public void onConnected() {
		Log.d("FileStreamClient", "Connected - showing login activity.");
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}
	
	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	private class connectionTask extends AsyncTask<Void, Void, Void> {

		public connectionTask() {
			Log.d("FileStreamClient", "Creating connection object.");
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.d("FileStreamClient", "Calling the connect function.");
			ClientSocketConnection.getInstance().connect(PersonInfo.getInstance().getServerPeople());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.d("FileStreamClient", "The connection seems complete - executing callback.");
			onConnected();
		}
	}
}
