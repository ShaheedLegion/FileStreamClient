package inspirational.designs.filestreamclient;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


public class LoginActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<LoadPeopleResult> {
	public final static String ALIAS_NAME = "inspirational.designs.filestreamclient.ALIAS_NAME";
	public final static String ALIAS_PHOTO = "inspirational.designs.filestreamclient.ALIAS_PHOTO";
	public final static String ALIAS_EMAIL = "inspirational.designs.filestreamclient.ALIAS_EMAIL";
	
	private final static int PROFILE_PIC_SIZE = 400;
	private final static int RC_SIGN_IN = 0;
	private GoogleApiClient mGoogleApiClient;
	private boolean mIntentInProgress;
	private boolean mShouldResolve;
	private ConnectionResult connectionResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		
		mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_LOGIN)
        .build();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	@Override
	public void onConnected(Bundle arg0) {
		mShouldResolve = false;
		Log.d("FileStreamClient", "OnConnected");
	    try {
	    	Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
	        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
	            Person person = Plus.PeopleApi
	                    .getCurrentPerson(mGoogleApiClient);
	            String personName = person.getDisplayName();
	            String personPhotoUrl = person.getImage().getUrl();
	            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

	            // Add this to class variables so that we can load it into the bundle
	            // and pass it along to the next activity.
	            personPhotoUrl = personPhotoUrl.substring(0,
	                    personPhotoUrl.length() - 2)
	                    + PROFILE_PIC_SIZE;

	            // Else we can get the alias as a string, then switch views.
	    		Intent intent = new Intent(this, ChatActivity.class);
	    		intent.putExtra(ALIAS_NAME, personName);
	    		intent.putExtra(ALIAS_PHOTO, personPhotoUrl);
	    		intent.putExtra(ALIAS_EMAIL, email);
	    		startActivity(intent);
	            
	            Toast.makeText(getApplicationContext(),
	                    "You are Logged In " + personName, Toast.LENGTH_LONG).show();
	        } else {
	            Toast.makeText(getApplicationContext(),
	                    "Couldnt Get the Person Info", Toast.LENGTH_SHORT).show();
	            Log.d("FileStreamClient", "current person result is null.");
	            mIntentInProgress = true;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        Log.d("FileStreamClient", e.getMessage());
	    }
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
		Log.d("FileStreamClient", "onConnectionSuspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d("FileStreamClient", "onConnectionFailed");
		if (!result.hasResolution()) {
	        GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
	        return;
	    }

	    if (!mIntentInProgress) {
	        connectionResult = result;
	        if (mShouldResolve) {
	            resolveSignInError();
	        }
	    }
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		Log.d("FileStreamClient", "OnActivityResult " + requestCode + " " + responseCode);
	    if (requestCode == RC_SIGN_IN) {
	        if (responseCode != RESULT_OK) {
	            mShouldResolve = false;
	        } else {
	        	Log.d("FileStreamClient", "response code is already ok, seems to be logged in.");
	        }

	        mIntentInProgress = false;

	        if (!mGoogleApiClient.isConnecting()) {
	            mGoogleApiClient.connect();
	        }
	    }
	}
	
	protected void onStart() {
	    super.onStart();
	    mGoogleApiClient.connect();
	}

	protected void onStop() {
	    super.onStop();
	    if (mGoogleApiClient.isConnected()) {
	        mGoogleApiClient.disconnect();
	    }
	}
	
	private void resolveSignInError() {
	    if (connectionResult != null && connectionResult.hasResolution()) {
	        try {
	            mIntentInProgress = true;
	            connectionResult.startResolutionForResult(this, RC_SIGN_IN);
	        } catch (SendIntentException e) {
	            mIntentInProgress = false;
	            mGoogleApiClient.connect();
	        }
	    }
	}

	@Override
	public void onResult(LoadPeopleResult arg0) {
	}
}
