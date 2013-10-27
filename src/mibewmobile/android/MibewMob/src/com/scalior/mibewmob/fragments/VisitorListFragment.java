package com.scalior.mibewmob.fragments;

import com.scalior.mibewmob.ChatUtils;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.VisitorListAdapter;
import com.scalior.mibewmob.activities.ChattingActivity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class VisitorListFragment extends ListFragment {

	private View m_rootView;
	private ChatUtils m_serverUtils;
	private boolean m_bUpdateList;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_visitorlist,
				container, false);
		m_serverUtils = ChatUtils.getInstance(getActivity().getApplicationContext());
		m_bUpdateList = true;
		
		VisitorListAdapter visitorListAdapter = 
				new VisitorListAdapter(getActivity(),
										  R.layout.visitorlistitem,
										  m_serverUtils.getVisitorList());
		
		setListAdapter(visitorListAdapter);
		
		return m_rootView;
	}

	
	private void showList() {
		if (m_serverUtils.getVisitorList().size() == 0) {

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

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		m_bUpdateList = false;	// prevent another thread from updating the
								// visitor list while we transition
		
		Intent showChat = new Intent(getActivity(), ChattingActivity.class);
		showChat.putExtra("position", position);
		showChat.putExtra("threadid", m_serverUtils.getVisitorList().get(position).getThreadID());
		startActivity(showChat);
		
		m_bUpdateList = true;
	}
}
