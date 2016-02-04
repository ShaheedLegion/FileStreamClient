package inspirational.designs.filestreamclient.fragments;

import inspirational.designs.filestreamclient.PersonInfo;
import inspirational.designs.filestreamclient.PersonInfo.PersonDetails;
import inspirational.designs.filestreamclient.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PeopleListFragment extends ListFragment {
	int fragNum;
	private static PeopleListFragment instance = null;
	private static PeopleListAdapter adapterInstance = null;

	public static PeopleListAdapter getInstance(PeopleListFragment peopleListFragment, ListView listView,
			int simpleListItem1) {
		if (adapterInstance == null)
			adapterInstance = instance.new PeopleListAdapter(peopleListFragment, listView, simpleListItem1);
		
		return adapterInstance;
	}


	public static PeopleListFragment init(int val) {
		PeopleListFragment peopleList;
		
		if (instance == null) {
			peopleList = new PeopleListFragment();
		} else 
			peopleList = instance;

		// Supply val input as an argument. change
		Bundle args = new Bundle();
		args.putInt("val", val);
		peopleList.setArguments(args);

		if (instance == null)
			instance = peopleList;

		return peopleList;
	}

	public static void AddUser(PersonInfo.PersonDetails user) {
		if (instance == null)
			instance = new PeopleListFragment();
		
		instance.AddUserInternal(user);
	}
	
	public static void RemoveUser(PersonInfo.PersonDetails user) {
		if (instance == null)
			instance = new PeopleListFragment();
		
		instance.RemoveUserInternal(user);
	}
	
	/**
	 * Retrieving this instance's number from its arguments.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragNum = getArguments() != null ? getArguments().getInt("val") : 1;
	}

	/**
	 * The Fragment's UI is a simple text view showing its instance number and
	 * an associated list.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layoutView = inflater.inflate(R.layout.people_pager_list, container,
				false);
		View tv = layoutView.findViewById(R.id.people_text);
		((TextView) tv).setText("Current Users");
		return layoutView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setListAdapter(getInstance(this, getListView(), android.R.layout.simple_list_item_1));
	}

	public void AddUserInternal(PersonInfo.PersonDetails user) {
		new AddBackgroundTask(user).execute();
	}
	
	public void RemoveUserInternal(PersonInfo.PersonDetails user) {
		new RemoveBackgroundTask(user).execute();
	}
	
	private class BackgroundTask extends AsyncTask<String, Void, String> {
		PersonInfo.PersonDetails user;
		
		public BackgroundTask(PersonInfo.PersonDetails user) {
			super();
			this.user = user;
		}
		
	    protected void onPostExecute(String result) {
	    }

		@Override
		protected String doInBackground(String... params) {
			return null;
		}
	}
	
	private class AddBackgroundTask extends BackgroundTask {

		public AddBackgroundTask(PersonDetails user) {
			super(user);
		}
		
		protected void onPostExecute(String result) {
			((PeopleListAdapter) getListAdapter()).addUser(user);
		}
	}
	
	private class RemoveBackgroundTask extends BackgroundTask {

		public RemoveBackgroundTask(PersonDetails user) {
			super(user);
		}
		
		protected void onPostExecute(String result) {
			((PeopleListAdapter) getListAdapter()).removeUser(user);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	}
	
	public class PeopleListAdapter extends ArrayAdapter<String> {
		private final Context context;
		private ListView listView;

		public PeopleListAdapter(Context context) {
			super(context, -1);
			this.context = context;
			this.listView = null;
		}

		public PeopleListAdapter(PeopleListFragment peopleListFragment, ListView listView,
				int simpleListItem1) {
			super(peopleListFragment.getActivity().getBaseContext(), -1);
			this.context = peopleListFragment.getActivity().getBaseContext();
			this.listView = listView;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.chat_rowlayout, parent,
					false);
			TextView textView = (TextView) rowView.findViewById(R.id.label);
			PersonInfo.PersonDetails user = PersonInfo.getInstance().getUserList().get(position);
			if (user != null) {
				if (user.getPhoto() != null) {
					ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
					imageView.setImageBitmap(user.getPhoto());
				}
			}
			textView.setText(user.getName());
			return rowView;
		}

		@Override
		public int getCount() {
			return PersonInfo.getInstance().getUserList().size();
		}
		
		public void addUser(PersonInfo.PersonDetails user) {
			super.notifyDataSetChanged();
			
			if (this.listView != null) {
				this.listView.post(new Runnable() {
					public void run() {
						listView.setSelection(listView.getCount() - 1);
					}
				});
			}
		}
		
		public void removeUser(PersonInfo.PersonDetails user) {
			// Do nothing for now - we'll have some kind of icon that denotes that the user is online.
		}
	}

}
