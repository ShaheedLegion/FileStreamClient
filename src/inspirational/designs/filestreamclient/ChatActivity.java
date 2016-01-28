package inspirational.designs.filestreamclient;

import java.util.regex.Pattern;

import inspirational.designs.filestreamclient.fragments.ChatListFragment;
import inspirational.designs.filestreamclient.fragments.MenuListFragment;
import inspirational.designs.filestreamclient.fragments.PeopleListFragment;
import inspirational.designs.filestreamclient.network.ClientSocketConnection;
import inspirational.designs.filestreamclient.network.ClientSocketConnection.PacketType;
import inspirational.designs.filestreamclient.network.DataListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;

public class ChatActivity extends FragmentActivity implements PersonImageLoaded, DataListener {
	public final static String ALIAS_MESSAGE = "inspirational.designs.filestreamclient.ALIAS";
	private MyAdapter mAdapter;
	private ViewPager mPager;
	private static ChatActivity instance = null;
	public static String TOKEN = "|_+_|"; 

	public static ChatActivity getInstance() {
		return instance;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up the global access for other classes.
		instance = this;
		
		setContentView(R.layout.activity_chat);
		
		mAdapter = new MyAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		
		// Get the info for this user.
		Intent intent = getIntent();
		String personName = intent.getStringExtra(LoginActivity.ALIAS_NAME);
		String personPhoto = intent.getStringExtra(LoginActivity.ALIAS_PHOTO);
		String personEmail = intent.getStringExtra(LoginActivity.ALIAS_EMAIL);
		
		// Add it to our global list of person things.
		PersonInfo info = PersonInfo.getInstance();
		info.setCurrentUser(personName,  personPhoto, personEmail, this);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActionBar().show();
		ClientSocketConnection.getInstance().setDataListener(this);
	}

	public static class MyAdapter extends FragmentPagerAdapter {
		public MyAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0: // Fragment # 0 - This will show image
				return ChatListFragment.init(position);
			case 1: // Fragment # 1 - This will show image
				return PeopleListFragment.init(position);
			case 2:// Fragment # 2-9 - Will show list
				return MenuListFragment.init(position);
			}
			return null;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	@Override
	public void onImageLoaded(String name, String photo, String email) {
		// Some image was just loaded, if it's the primary user, then we can modify our list to show this image.
		PersonInfo info = PersonInfo.getInstance();
		PersonInfo.PersonDetails currentUser = info.getCurrentUser();
		
		if (currentUser.getName() == name && currentUser.getPhotoUrl() == photo) {
			String outboundMessage = name + TOKEN + photo + TOKEN + email;
			ClientSocketConnection.getInstance().send(ClientSocketConnection.PacketType.ALIAS, 0, outboundMessage);
		}
	}

	@Override
	public void onDataReceived(PacketType type, String data) {
		new BackgroundTask(type, data).execute();
	}
	
	private class BackgroundTask extends AsyncTask<String, Void, String> {
		PacketType type;
		String data;
		
		public BackgroundTask(PacketType type, String data) {
			super();
			this.type = type;
			this.data = data;
		}
		
	    protected void onPostExecute(String result) {
	    	onDataReceivedInternal(type, data);
	    }

		@Override
		protected String doInBackground(String... params) {
			return null;
		}
	}
	
	private void onDataReceivedInternal(PacketType type, String data) {
		// Based on the type of data we received, we can deal with something here.
		// Determine what to do with the packet based on the type.
		if (type == PacketType.ALIAS_ACK) {
			// Do nothing - someone joined. There's no data with this response.
		} else if (type == PacketType.JOIN) {
			// This is a join response from the server.
			// Add it to the user list.
			String[] values = data.split(Pattern.quote(TOKEN));
			
			PersonInfo info = PersonInfo.getInstance();
			info.addUser(values[0],  values[1], values[2], this);
			
			PeopleListFragment.AddUser(info.findUser(values[0], values[1], values[2]));
		} else if (type == PacketType.MSG) {
			String[] values = data.split(Pattern.quote(TOKEN));

			PersonInfo info = PersonInfo.getInstance();
			PersonInfo.PersonDetails details = info.findUser(values[0],  values[1], values[2]);
			ChatListFragment.AddMessage(details, values[3]);
		} else if (type == PacketType.LEAVE) {
			String[] values = data.split(Pattern.quote(TOKEN));
			
			PersonInfo info = PersonInfo.getInstance();
			info.removeUser(values[0],  values[1], values[2], this);
			
			PeopleListFragment.RemoveUser(info.findUser(values[0], values[1], values[2]));
		}
	}
}
