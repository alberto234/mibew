package com.scalior.mibewmob.fragments;

import com.scalior.mibewmob.ChatUtils;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.model.ChatServer;
import com.scalior.mibewmob.model.MonitoredSite;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ServerDetailsFragment extends Fragment 
			implements OnClickListener {
	
	// UI elements
	private View m_rootView;
	private Button m_confirmBtn;

	// Other data elements
	private ChatServer m_chatServer;
	private OnConfirmListener m_listener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_confirmdetails,
				container, false);
	
		// TODO: This should be a logout button
		m_confirmBtn = (Button) m_rootView.findViewById(R.id.confirm_btn);
		m_confirmBtn.setOnClickListener(this);
		
		// Populate the view with server and operator details
		if (getArguments().containsKey("serverID")) {
			MonitoredSite siteDetails = 
					ChatUtils.getInstance(getActivity().getApplicationContext())
						.getSiteWithID(getArguments().getLong("serverID"));
			if (siteDetails != null) {
				TextView serverNametv = (TextView) m_rootView.findViewById(R.id.server_name);
				TextView urltv = (TextView) m_rootView.findViewById(R.id.server_url);
				//ImageView logoiv = (ImageView) m_rootView.findViewById(R.id.server_logo);
				
				TextView operatorNametv = (TextView) m_rootView.findViewById(R.id.operator_name);
				TextView emailtv = (TextView) m_rootView.findViewById(R.id.operator_email);
				//ImageView logoiv = (ImageView) m_rootView.findViewById(R.id.server_logo);
	
				serverNametv.setText(siteDetails.getServer().getName());
				urltv.setText(siteDetails.getServer().getURL());
				operatorNametv.setText(siteDetails.getOperator().getCommonName());
				emailtv.setText(siteDetails.getOperator().getEmail());
			}
		}

		// TODO: If there is an error, hide all views and show a blank error view
		return m_rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			m_listener = (OnConfirmListener) activity;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnConfirmListener");
		}
	}
	
	
	private void confirmServer() {
		if (m_chatServer != null) {
			
			m_listener.onConfirm();
		}
	}

	// The activity needs to implement this interface to act on the confirm button
	public interface OnConfirmListener {
		public void onConfirm();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.confirm_btn:
			confirmServer();
			break;
		default:
			break;
		}
	}
}
