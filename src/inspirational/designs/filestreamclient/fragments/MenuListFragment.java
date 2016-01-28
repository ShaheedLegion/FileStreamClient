package inspirational.designs.filestreamclient.fragments;

import inspirational.designs.filestreamclient.R;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MenuListFragment extends ListFragment {
	int fragNum;
	String arr[] = { "Attach File", "Change Alias", "Settings" };

	public static MenuListFragment init(int val) {
		MenuListFragment menuList = new MenuListFragment();

		// Supply val input as an argument.
		Bundle args = new Bundle();
		args.putInt("val", val);
		menuList.setArguments(args);

		return menuList;
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
		View layoutView = inflater.inflate(R.layout.menu_pager_list, container,
				false);
		View tv = layoutView.findViewById(R.id.menu_text);
		((TextView) tv).setText("Menu");
		return layoutView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, arr));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("FileStreamClient", "Item clicked: " + id + " - " + arr[(int) id]);
	}
}
