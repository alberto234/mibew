package com.scalior.mibewmob.fragments;

import java.util.ArrayList;
import java.util.List;

import com.scalior.mibewmob.ChatServerListAdapter;
import com.scalior.mibewmob.ChatServerUtils;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.database.MibewMobSQLiteHelper;
import com.scalior.mibewmob.model.ChatServer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ChatServerListFragment extends ListFragment {

	private boolean m_bReloadList;
	private View m_rootView;
	private List<ChatServer> m_serverList;
	
	
	public ChatServerListFragment() {
		m_bReloadList = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_chatserverlist,
				container, false);
		
		/* TODO:
		 * Query if we have a list of chat servers that we are monitoring
		 * If so, populate the list view
		 * else, disable the list view and show the text view.
		 */
		
		if (m_bReloadList) {
			reloadList();
		}
		
		ChatServerListAdapter serverListAdapter = 
				new ChatServerListAdapter(getActivity(),
										  R.layout.serverlistitem,
										  m_serverList);
		
		setListAdapter(serverListAdapter);
		
		return m_rootView;
	}

	
	private void reloadList() {
		/* TODO: 
		 * Get a list of chat servers
		 * If none is returned, show textview
		 */
		
		// Clear the server list before the starting the query
		if (m_serverList == null) {
			m_serverList = new ArrayList<ChatServer>();
		}
		else {
			m_serverList.clear();
		}
		
		MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(getActivity());
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String[] serverColumns = {MibewMobSQLiteHelper.CHATSERVER_NAME,
							      MibewMobSQLiteHelper.CHATSERVER_URL,
							      MibewMobSQLiteHelper.CHATSERVER_LOGO};

		Cursor cursor  = database.query(MibewMobSQLiteHelper.TABLE_CHATSERVER,
							serverColumns, null, null, null, null, null);

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				m_serverList.add(new ChatServer(cursor.getString(0), 
												cursor.getString(1),
												null,
												cursor.getString(2),
												null, null));
				cursor.moveToNext();
			}
		}
		else {
			// Disable the listview and show the textview
			m_rootView.findViewById(android.R.id.list).setVisibility(View.GONE);
			m_rootView.findViewById(R.id.no_servers).setVisibility(View.VISIBLE);
		}
		
		cursor.close();
		m_bReloadList = false;
		ChatServerUtils.getInstance(getActivity()).setRefreshServerList(false);
	}
	
	@Override
	public void onResume () {
		if (m_bReloadList || 
				ChatServerUtils.getInstance(getActivity()).getRefreshServerList()) {
			reloadList();
		}
		super.onResume();
	}
}
