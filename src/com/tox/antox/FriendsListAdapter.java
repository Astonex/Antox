package com.tox.antox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendsListAdapter extends ArrayAdapter<FriendsList> implements Filterable {
	Context context;
	int layoutResourceId;
	List<FriendsList> data = null;

	private final Object lock = new Object();
	private ArrayList<FriendsList> originalData;
	private FriendsFilter filter;

	public FriendsListAdapter(Context context, int layoutResourceId, FriendsList[] data)
	{
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = Arrays.asList(data);
	}
	
	public FriendsListAdapter(Context context, int layoutResourceId, List<FriendsList> data)
	{
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}
	
	@Override
	public int getCount() {
		return data.size();
	}
	
	@Override
	public FriendsList getItem(int position) {
		return data.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		FriendsListHolder holder = null;
		
		if(row == null)
		{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new FriendsListHolder();
			holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
			holder.friendName = (TextView)row.findViewById(R.id.friend_name);
			holder.friendStatus = (TextView)row.findViewById(R.id.friend_status);
			row.setTag(holder);
		}
		else
		{
			holder = (FriendsListHolder)row.getTag();
		}
		
		FriendsList friendsList = data.get(position);
		holder.friendName.setText(friendsList.friendName);
		holder.imgIcon.setImageResource(friendsList.icon);
		holder.friendStatus.setText(friendsList.friendStatus);
		
		return row;
	}
	
	static class FriendsListHolder
	{
		ImageView imgIcon;
		TextView friendName;
		TextView friendStatus;
	}

	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new FriendsFilter();
		}
		return filter;
	}

	class FriendsFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();

			if (originalData == null) {
				synchronized (lock) {
					originalData = new ArrayList<FriendsList>(data);
				}
			}

			// filter is empty string, result is original data of friends list
			if (constraint == null || constraint.length() == 0) {
				ArrayList<FriendsList> list;
				synchronized (lock) {
					list = new ArrayList<FriendsList>(originalData);
				}
				results.values = list;
				results.count = list.size();
			} else {
				String prefixString = constraint.toString().toLowerCase();

				ArrayList<FriendsList> values;
				synchronized (lock) {
					values = new ArrayList<FriendsList>(originalData);
				}

				final int count = values.size();
				final ArrayList<FriendsList> newValues = new ArrayList<FriendsList>();

				for (int i = 0; i < count; i++) {
					final FriendsList value = values.get(i);
					final String valueText = value.toString().toLowerCase();

					// First match against the whole, non-splitted value
					if (valueText.startsWith(prefixString)) {
						newValues.add(value);
					} else if (findByWords(valueText, prefixString)) {
						newValues.add(value);
					} else if (valueText.contains(prefixString)) {
						newValues.add(value);
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		private boolean findByWords(String haystack, String needle) {
			final String[] words = haystack.split(" ");
			final int wordCount = words.length;

			// Start at index 0, in case valueText starts with space(s)
			for (int k = 0; k < wordCount; k++) {
				if (words[k].startsWith(needle)) {
					return true;
				}
			}

			return false;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			data = (List<FriendsList>) results.values;
			notifyDataSetChanged();
		}
	}
}
