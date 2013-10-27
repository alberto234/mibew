package com.scalior.mibewmob.fragments;

import java.util.List;

import com.scalior.mibewmob.MonitoredSitesListAdapter;
import com.scalior.mibewmob.ChatUtils;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.model.MonitoredSite;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MonitoredSitesListFragment extends ListFragment {

	private View m_rootView;
	private ChatUtils m_serverUtils;
	private List<MonitoredSite> m_monitoredSites;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_chatserverlist,
				container, false);
		m_serverUtils = ChatUtils.getInstance(getActivity().getApplicationContext());
		m_monitoredSites = m_serverUtils.reloadMonitoredSitesList();
		
		MonitoredSitesListAdapter serverListAdapter = 
				new MonitoredSitesListAdapter(getActivity(),
										  R.layout.serverlistitem,
										  m_monitoredSites);
		
		setListAdapter(serverListAdapter);
		
		return m_rootView;
	}

	
	private void showList() {
		if (m_monitoredSites == null ||
			m_monitoredSites.size() == 0) {

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
	}
}
