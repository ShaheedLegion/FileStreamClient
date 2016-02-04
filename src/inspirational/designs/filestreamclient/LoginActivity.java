package inspirational.designs.filestreamclient;

import java.util.ArrayList;
import inspirational.designs.filestreamclient.network.ClientSocketConnection.ServerPerson;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class LoginActivity extends Activity {
	public final static String ALIAS_NAME = "inspirational.designs.filestreamclient.ALIAS_NAME";
	public final static String ALIAS_PHOTO = "inspirational.designs.filestreamclient.ALIAS_PHOTO";
	public final static String ALIAS_EMAIL = "inspirational.designs.filestreamclient.ALIAS_EMAIL";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		onPeopleLoaded();
	}

	public void onPeopleLoaded() {
		{
			// First populate our current users list.
			ArrayList<ServerPerson> people = PersonInfo.getInstance().getServerPeople();
			for (ServerPerson person : people) {
				PersonInfo.getInstance().addUser(person.name, person.image, "no-email", null);
			}
		}
		ListView lv = (ListView)findViewById(R.id.listView8);
		if (lv != null) {
			lv.setAdapter(new PeopleAdapter(getBaseContext(), R.layout.chat_rowlayout , PersonInfo.getInstance().getServerPeople()));
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					personChosen(arg2);
				}});
		}
		TextView tv = (TextView)findViewById(R.id.fullscreen_content2);
		if (tv != null)
			tv.setText("Choose your alias...");
	}

	
	public void personChosen(int index) {
		Log.d("FileStreamClient", "User chose an alias!");
		ServerPerson user = PersonInfo.getInstance().getServerPeople().get(index);

        // Else we can get the alias as a string, then switch views.
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ALIAS_NAME, user.name);
		intent.putExtra(ALIAS_PHOTO, user.image);
		intent.putExtra(ALIAS_EMAIL, "no-email");
		startActivity(intent);
	}
	
	class PeopleAdapter extends ArrayAdapter<ServerPerson> {
		private final ArrayList<ServerPerson> users;
		private final Context context;

		public PeopleAdapter(Context context, int resource,
				ArrayList<ServerPerson> objects) {
			super(context, resource, objects);
			this.users = objects;
			this.context = context;
		}
		
		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.chat_rowlayout, parent,
					false);
			TextView textView = (TextView) rowView.findViewById(R.id.label);
			ServerPerson user = this.users.get(position);
			textView.setText(user.name);
			return rowView;
		}

		@Override
		public int getCount() {
			return this.users.size();
		}
		
	}
	
}
