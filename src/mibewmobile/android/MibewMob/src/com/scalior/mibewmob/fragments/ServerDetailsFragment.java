package com.scalior.mibewmob.fragments;

import org.json.JSONException;
import org.json.JSONObject;

import com.scalior.mibewmob.ChatServerUtils;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.database.MibewMobSQLiteHelper;
import com.scalior.mibewmob.model.ChatOperator;
import com.scalior.mibewmob.model.ChatServer;

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
	private ChatOperator m_chatOperator;
	private OnConfirmListener m_listener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_rootView = inflater.inflate(R.layout.fragment_confirmdetails,
				container, false);
	
		m_confirmBtn = (Button) m_rootView.findViewById(R.id.confirm_btn);
		m_confirmBtn.setOnClickListener(this);
		
		// Populate the view with server details
		try {
			JSONObject jServerDetails = 
					new JSONObject(getArguments().getString("serverDetails"));
			JSONObject jOperatorDetails = 
					new JSONObject(getArguments().getString("operatorDetails"));
			m_chatServer = new ChatServer(jServerDetails);
			m_chatOperator = new ChatOperator(jOperatorDetails);
			
			TextView serverNametv = (TextView) m_rootView.findViewById(R.id.server_name);
			TextView urltv = (TextView) m_rootView.findViewById(R.id.server_url);
			//ImageView logoiv = (ImageView) m_rootView.findViewById(R.id.server_logo);
			
			TextView operatorNametv = (TextView) m_rootView.findViewById(R.id.operator_name);
			TextView emailtv = (TextView) m_rootView.findViewById(R.id.operator_email);
			//ImageView logoiv = (ImageView) m_rootView.findViewById(R.id.server_logo);

//			nametv.setText(getArguments().getString("serverDetails"));
			serverNametv.setText(jServerDetails.getString("name"));
			urltv.setText(jServerDetails.getString("chatURL"));
			operatorNametv.setText(jOperatorDetails.getString("commonname"));
			emailtv.setText(jOperatorDetails.getString("email"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			MibewMobSQLiteHelper dbHelper = new MibewMobSQLiteHelper(getActivity());
			boolean bServerAdded = dbHelper.addNewChatServer(m_chatServer);
			boolean bOperatorAdded = dbHelper.addNewOperator(m_chatOperator);
			ChatServerUtils.getInstance(getActivity()).
				setRefreshServerList(bServerAdded || bOperatorAdded);
			
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
