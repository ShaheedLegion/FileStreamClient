package inspirational.designs.filestreamclient.fragments;

import java.util.ArrayList;

import inspirational.designs.filestreamclient.ChatActivity;
import inspirational.designs.filestreamclient.PersonInfo;
import inspirational.designs.filestreamclient.PersonInfo.PersonDetails;
import inspirational.designs.filestreamclient.R;
import inspirational.designs.filestreamclient.network.ClientSocketConnection;
import inspirational.designs.filestreamclient.network.ClientSocketConnection.PacketType;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ChatListFragment extends ListFragment {
	int fragNum;

	private static ChatListFragment instance = null;
	private static ChatListAdapter adapterInstance = null;
	
	public static ChatListAdapter getInstance(ChatListFragment chatListFragment, ListView list, int simpleListItem1) {
		if (adapterInstance == null)
			adapterInstance = instance.new  ChatListAdapter(chatListFragment, list, simpleListItem1);
		
		return adapterInstance;
	}
	
	public static ChatListFragment init(int val) {
		if (instance == null) {
			instance = new ChatListFragment();
		}
		
		// Supply val input as an argument.
		Bundle args = new Bundle();
		args.putInt("val", val);
		instance.setArguments(args);
		
		return instance;
	}
	
	public static void AddMessage(PersonInfo.PersonDetails details, String message) {
		if (instance == null)
			instance = new ChatListFragment();
		
		instance.addMessage(details, message);
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
		final View layoutView = inflater.inflate(R.layout.chat_pager_list, container,
				false);

		// Get the text input and the button here.
		Button sendBtn = (Button) layoutView.findViewById(R.id.send_message_button);
		sendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText edit = (EditText)layoutView.findViewById(R.id.chat_message_text);
				Editable ed = edit.getText();
				
				if (ed.length() != 0) {
					// Now try to send this off to the server ... 
					PersonInfo info = PersonInfo.getInstance();
					PersonInfo.PersonDetails user = info.getCurrentUser();
					String message = ed.toString();
					ClientSocketConnection.getInstance().send(PacketType.MSG, 0,
							user.getName() + ChatActivity.TOKEN + user.getPhotoUrl() +
							ChatActivity.TOKEN + user.getEmail() + ChatActivity.TOKEN + message);
					edit.setText(null);
				}
			}
		});
		return layoutView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView list = this.getListView();
		setListAdapter(getInstance(this, list, android.R.layout.simple_list_item_1));
	}
	
	public void addMessage(PersonInfo.PersonDetails details, String message) {
		new BackgroundTask(details, message).execute();
	}
	
	private class BackgroundTask extends AsyncTask<String, Void, String> {
		PersonInfo.PersonDetails details;
		String message;
		
		public BackgroundTask(PersonInfo.PersonDetails details, String message) {
			super();
			this.details = details;
			this.message = message;
		}
		
	    protected void onPostExecute(String result) {
	    	((ChatListAdapter) getListAdapter()).addMessage(details, message);
	    }

		@Override
		protected String doInBackground(String... params) {
			return null;
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	}

	public class ChatListAdapter extends ArrayAdapter<String> {
		class MessageContent {
			PersonInfo.PersonDetails user;
			String message;
			
			public MessageContent(PersonDetails currentUser, String string) {
				this.user = currentUser;
				this.message = string;
			}
		}
		
		private final Context context;
		private final ArrayList<MessageContent> messages;
		private ListView listView;

		public ChatListAdapter(Context context) {
			super(context, -1);
			this.context = context;
			this.messages = new ArrayList<MessageContent>();
			this.listView = null;
			
			PersonInfo info = PersonInfo.getInstance();
			PersonInfo.PersonDetails currentUser = info.getCurrentUser();
			messages.add(new MessageContent(currentUser, "Welcome to FileStreamClient - by Shaheed Abdol."));
		}

		public ChatListAdapter(ChatListFragment chatListFragment, ListView listView,
				int simpleListItem1) {
			super(chatListFragment.getActivity().getBaseContext(), -1);

			this.context = chatListFragment.getActivity().getBaseContext();
			this.messages = new ArrayList<MessageContent>();
			this.listView = listView;
			
			PersonInfo info = PersonInfo.getInstance();
			PersonInfo.PersonDetails currentUser = info.getCurrentUser();
			messages.add(new MessageContent(currentUser, "Welcome to FileStreamClient - by Shaheed Abdol."));
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.chat_rowlayout, parent,
					false);
			TextView textView = (TextView) rowView.findViewById(R.id.label);
			
			MessageContent content = this.messages.get(position);
			
			if (content.user != null) {
				PersonInfo info = PersonInfo.getInstance();
				PersonInfo.PersonDetails updated = info.findUser(content.user.getName(), content.user.getPhotoUrl(), content.user.getEmail());

				if (updated.getPhoto() != null) {
					ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
					imageView.setImageBitmap(updated.getPhoto());
				}
			}
			textView.setText(content.message);

			return rowView;
		}

		@Override
		public int getCount() {
			return this.messages.size();
		}
		
		public void addMessage(PersonInfo.PersonDetails user, String message) {
			// First check if it's an ack from the server.
			if (message.length() > 0) {
				this.messages.add(new MessageContent(user, message));
				super.notifyDataSetChanged();
				
				if (this.listView != null) {
					this.listView.post(new Runnable() {
						public void run() {
							listView.setSelection(listView.getCount() - 1);
						}
					});
				}
			}
		}
	}
}
