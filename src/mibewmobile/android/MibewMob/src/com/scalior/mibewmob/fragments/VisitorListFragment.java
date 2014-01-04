package com.scalior.mibewmob.fragments;

import java.util.ArrayList;
import java.util.List;

import com.scalior.mibewmob.ChatUtils;
import com.scalior.mibewmob.MibewMobException;
import com.scalior.mibewmob.MibewMobLogger;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.VisitorListAdapter;
import com.scalior.mibewmob.activities.ChattingActivity;
import com.scalior.mibewmob.interfaces.VisitorListListener;
import com.scalior.mibewmob.model.ChatThread;
import com.scalior.mibewmob.services.PollingService;
import com.scalior.mibewmob.services.PollingService.PollingServiceBinder;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
// import android.widget.Toast;

public class VisitorListFragment extends ListFragment
								 implements VisitorListListener {

	private View m_rootView;
	private ChatUtils m_serverUtils;
	private List<ChatThread> m_visitorList;
	private VisitorListAdapter m_visitorListAdapter;
	private Object m_visitorListLock = new Object();
	
	// Service connection and binding variables
	protected PollingServiceBinder m_pollingServiceBinder; 
	private boolean m_isBound;
	private ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			m_pollingServiceBinder = (PollingServiceBinder)service;
			m_pollingServiceBinder.subscribeToVisitorList(VisitorListFragment.this);
			// Toast.makeText(getActivity(), "Service connected", Toast.LENGTH_SHORT).show();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			// Toast.makeText(getActivity(), "Service disconnected", Toast.LENGTH_SHORT).show();
			m_pollingServiceBinder = null;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_serverUtils = ChatUtils.getInstance(getActivity().getApplicationContext());
		m_visitorList = new ArrayList<ChatThread>();
		
		updateVisitors();
		
		doBindService();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_visitorlist, container, false);
		
		m_visitorListAdapter = new VisitorListAdapter(getActivity(),
													  R.layout.visitorlistitem,
													  m_visitorList);
		setListAdapter(m_visitorListAdapter);

		return m_rootView;
	}

	
	private void updateVisitors() {
		Thread updateThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					m_serverUtils.checkForNewVisitors(true, false);
				} catch (MibewMobException e) {
					// TODO This is not fatal, just log it.
					e.printStackTrace();
				}
				onListUpdated();
			}
		});
		
		updateThread.start();
	}

	private void showList() {
		if (m_visitorList.size() == 0) {

			// Disable the listview and show the textview
			m_rootView.findViewById(android.R.id.list).setVisibility(View.GONE);
			m_rootView.findViewById(R.id.no_visitors).setVisibility(View.VISIBLE);
		}
		else {
			// Disable the textview and show the listview
			m_rootView.findViewById(android.R.id.list).setVisibility(View.VISIBLE);
			m_rootView.findViewById(R.id.no_visitors).setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onResume () {
		super.onResume();
		showList();
		if (m_pollingServiceBinder != null) {
			m_pollingServiceBinder.subscribeToVisitorList(this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (m_pollingServiceBinder != null) {
			m_pollingServiceBinder.unsubscribeToVisitorList(this);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		// Note: 
		// With the synchronization below, it is still possible that another 
		// thread could have acquired the lock and is changing the list below us
		// while we are waiting...
		// The ideal solution is to give this method priority, and not "commit" or
		// "rollback" changes that are being done in the mergeWithDb method.
		// Now that I think about it, the merge can use a separate list to do all
		// the computations, then at the very end we synchronize only the act of
		// copying the temp list to the m_visitorList
		synchronized(m_visitorListLock) {
			Intent showChat = new Intent(getActivity(), ChattingActivity.class);
			int chatKey = m_serverUtils.setThreadToExpand(m_visitorList.get(position));
			showChat.putExtra(ChatUtils.CHAT_KEY, chatKey);
			MibewMobLogger.Log("Chat key for thread " + m_visitorList.get(position).getThreadID() + 
								" is: " + chatKey);
			startActivity(showChat);
		}
	}
	

	/*private List<ChatThread> mergeWithDb(List<ChatThread> p_updatedList) {
		// This method updates the internal m_visitorList as well as returns it.
		synchronized(m_visitorListLock) {

			m_visitorList.clear();
			List<ChatThread> dbList = m_serverUtils.getVisitorList();
			int dbListIdx = 0;
			int updatedListIdx = 0;
			MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(getActivity().getApplicationContext());

			// Loop through all the elements in both lists
			for (int i = 0; i < dbList.size() + p_updatedList.size(); i++) {
				if (updatedListIdx >= p_updatedList.size() &&
						dbListIdx < dbList.size()) {
					m_visitorList.add(dbList.get(dbListIdx++));
				} else if (updatedListIdx < p_updatedList.size()) {
					if (dbListIdx >= dbList.size() || 
							p_updatedList.get(updatedListIdx).getThreadID() > 
								dbList.get(dbListIdx).getThreadID()) {
						dbHelper.addOrUpdateThread(p_updatedList.get(updatedListIdx));
						m_visitorList.add(p_updatedList.get(updatedListIdx++));
					} else {
						m_visitorList.add(dbList.get(dbListIdx++));
					}
				}
			}
			return m_visitorList;
		}
	}*/
	
	
	
/*	@Override
	public void onUpdateList(final List<ChatThread> p_activeVisitors) {
		if (p_activeVisitors != null && p_activeVisitors.size() > 0) {
			synchronized(m_visitorListLock) {
				m_visitorList.clear();
				m_visitorList.addAll(m_serverUtils.getVisitorList());
			}

			getActivity().runOnUiThread(new Runnable() {
	
				@Override
				public void run() {
					m_visitorListAdapter.notifyDataSetChanged();
					showList();
				}
			});
		}
	}*/
	
	@Override
	public void onListUpdated() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized(m_visitorListLock) {
					m_visitorList.clear();
					m_visitorList.addAll(m_serverUtils.getVisitorList());
				}

				m_visitorListAdapter.notifyDataSetChanged();
				showList();
			}
		});
	}

	private void doBindService() {
		getActivity().getApplicationContext().bindService(
				new Intent(getActivity().getApplicationContext(), PollingService.class),
				m_connection, Context.BIND_AUTO_CREATE);
		// Toast.makeText(getActivity(), "Service bound", Toast.LENGTH_SHORT).show();

		m_isBound = true;
	}
	
	private void doUnbindService() {
		if (m_isBound) {
			getActivity().getApplicationContext().unbindService(m_connection);
			// Toast.makeText(getActivity(), "Service unbound", Toast.LENGTH_SHORT).show();
			m_isBound = false;
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	public void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}
}
