package com.scalior.mibewmob.activities;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.scalior.mibewmob.ChatUtils;
import com.scalior.mibewmob.MessageListAdapter;
import com.scalior.mibewmob.MibewMobException;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.interfaces.ChatThreadListener;
import com.scalior.mibewmob.model.ChatMessage;
import com.scalior.mibewmob.model.ChatThread;
import com.scalior.mibewmob.services.PollingService;
import com.scalior.mibewmob.services.PollingService.PollingServiceBinder;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;

public class ChattingActivity extends ListActivity 
								implements ChatThreadListener {
	private ChatUtils m_serverUtils;
	private ChatThread m_chatThread;
	private List<ChatMessage> m_messageList;
	private MessageHandler m_handler;
	private MessageListAdapter m_listAdapter;
	
	// Views to keep track off
	private MenuItem m_initiateChatMI;
	private MenuItem m_closeChatMI;
	private ImageButton m_sendBtn;
	private EditText m_messageBox;
	private ListView m_vMessageList;
	
	// Some constants used by the handler for this activity
	private static final int COMMAND_VIEWTHREAD 	= 1;
	private static final int COMMAND_STARTCHAT 		= 2;
	private static final int COMMAND_NEWMESSAGES	= 3;
	private static final int COMMAND_SENDMESSAGE	= 4;
	private static final int COMMAND_CLOSECHAT		= 5;
	
	// Service connection and binding variables
	protected PollingServiceBinder m_pollingServiceBinder; 
	private boolean m_isBound;
	private ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			m_pollingServiceBinder = (PollingServiceBinder)service;
		}
		
		public void onServiceDisconnected(ComponentName className) {
			m_pollingServiceBinder = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatting);
		// Show the Up button in the action bar.
		setupActionBar();
		
		m_serverUtils = ChatUtils.getInstance(getApplicationContext());
		m_handler = new MessageHandler(this);

		// Get views we are interested in
		m_messageBox = (EditText)findViewById(R.id.messageBox);
		m_sendBtn = (ImageButton)findViewById(R.id.sendButton);
		m_vMessageList = this.getListView();
		m_vMessageList.setStackFromBottom(true);
		m_vMessageList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		
		m_sendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
		
		m_messageBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// Do nothing
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// Do nothing;
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() > 0) {
					m_sendBtn.setVisibility(View.VISIBLE);
				} else {
					m_sendBtn.setVisibility(View.INVISIBLE);
				}
			}
			
		});
			
		// Extract the thread of interest
		m_chatThread = m_serverUtils.getThreadToExpand();
		m_serverUtils.setThreadToExpand(null);

		m_messageList = m_serverUtils.getMessages(m_chatThread.getID());
		if (m_messageList == null) {
			m_messageList = new ArrayList<ChatMessage>();
		}
 
		m_listAdapter = new MessageListAdapter(this, 
				R.layout.chatbubbleitem,
				m_messageList);
		setListAdapter(m_listAdapter);

		doBindService();
		
		if (!m_chatThread.isChattingWithGuest()) {
			m_messageBox.setFocusable(false);
			m_messageBox.setFocusableInTouchMode(false);
			
			if (m_chatThread.getState() == ChatThread.STATE_CLOSED) {
				m_messageBox.setHint("Closed session. View messages only");
			} else {
				m_messageBox.setHint("Tap the start chat icon");
			}
		}

		if (!m_chatThread.isViewed()) {
			viewThread();
		} else {
			getNewMessages();
		}
	}



	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chatting, menu);
		m_initiateChatMI = menu.findItem(R.id.action_initiate_chat);
		m_closeChatMI = menu.findItem(R.id.action_close_chat);
		
		if (m_chatThread.isChattingWithGuest()) {
			m_initiateChatMI.setVisible(false);
			m_closeChatMI.setVisible(true);
		}
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
			
		case R.id.action_settings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			return true;

		case R.id.action_initiate_chat:
			// TODO: Change the background to a rotating icon and disable any clicking
			// Run a background thread that starts the chat.
			// On success, enable the chat box
			
			m_initiateChatMI.setEnabled(false);
			Thread thrInitiate = new Thread(new Runnable() {

				@Override
				public void run() {
					Message msg;
					try {
						m_serverUtils.startChatWithGuest(m_chatThread);
						msg = m_handler.obtainMessage(COMMAND_STARTCHAT, ChatUtils.SERVER_ERROR_SUCCESS, 0);
					} catch (MibewMobException e) {
						// TODO: Add entry to log file
						msg = m_handler.obtainMessage(COMMAND_STARTCHAT, ChatUtils.SERVER_ERROR_UNKNOWN, 0);
					}
					m_handler.sendMessage(msg);
				}
			});
			
			thrInitiate.start();
			return true;
			
		case R.id.action_close_chat:
			// TODO: Change the background to a rotating icon and disable any clicking
			
			m_closeChatMI.setEnabled(false);
			Thread thrClose = new Thread(new Runnable() {

				@Override
				public void run() {
					Message msg;
					try {
						m_serverUtils.closeThread(m_chatThread);
						msg = m_handler.obtainMessage(COMMAND_CLOSECHAT, ChatUtils.SERVER_ERROR_SUCCESS, 0);
					} catch (MibewMobException e) {
						// TODO: Add entry to log file
						msg = m_handler.obtainMessage(COMMAND_CLOSECHAT, ChatUtils.SERVER_ERROR_UNKNOWN, 0);
					}
					m_handler.sendMessage(msg);
				}
			});
			
			thrClose.start();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void viewThread() {

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg;
				try {
					m_serverUtils.viewThread(m_chatThread);
					msg = m_handler.obtainMessage(COMMAND_VIEWTHREAD, ChatUtils.SERVER_ERROR_SUCCESS, 0);
				} catch (MibewMobException e) {
					// TODO: Add entry to log file
					msg = m_handler.obtainMessage(COMMAND_VIEWTHREAD, ChatUtils.SERVER_ERROR_UNKNOWN, 0);
				}
				m_handler.sendMessage(msg);
			}
		});
		
		thread.start();
	}
	
	private void getNewMessages() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg;
				try {
					List<ChatMessage> newMessages = m_serverUtils.getNewMessagesFromServer(m_chatThread);
					int count = 0;
					if (newMessages != null) {
						count = newMessages.size();
						// Add these new messages to the existing list
						m_messageList.addAll(newMessages);
					}
					
					msg = m_handler.obtainMessage(COMMAND_NEWMESSAGES, 
							ChatUtils.SERVER_ERROR_SUCCESS, count);
				} catch (MibewMobException e) {
					// TODO: Add entry to log file
					msg = m_handler.obtainMessage(COMMAND_NEWMESSAGES, 
							ChatUtils.SERVER_ERROR_UNKNOWN, 0);
				}
				m_handler.sendMessage(msg);
				
				// Now that we queried the initial set of messages, subscribe to the new messages event
				m_pollingServiceBinder.subscribeToNewMessages(m_chatThread, ChattingActivity.this);
			}
		});
		
		thread.start();
	}

	protected void sendMessage() {
		// Add the message to the message list first
		final ChatMessage chatMessage = new ChatMessage(m_chatThread.getID(),
				m_messageBox.getText().toString(),
				0, 0, null, null);
		m_messageList.add(chatMessage);
		m_messageBox.setText("");
		m_messageBox.requestFocus();
		updateMessageList();
		
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg;
				try {
					m_serverUtils.sendMessage(m_chatThread, chatMessage);
					msg = m_handler.obtainMessage(COMMAND_SENDMESSAGE, ChatUtils.SERVER_ERROR_SUCCESS, 0);
				} catch (MibewMobException e) {
					// TODO: Add entry to log file
					msg = m_handler.obtainMessage(COMMAND_SENDMESSAGE, ChatUtils.SERVER_ERROR_UNKNOWN, 0);
				}
				m_handler.sendMessage(msg);
			}
		});
		
		thread.start();
	}

	private void updateMessageList() {
		m_listAdapter.notifyDataSetChanged();
	}
	
	// This callback is invoked when we receive new messages for this thread
	@Override
	public void onNewMessages(List<ChatMessage> p_newMessages) {
		Message msg;
		int count = p_newMessages.size();
		// Add these new messages to the existing list
		m_messageList.addAll(p_newMessages);

		msg = m_handler.obtainMessage(COMMAND_NEWMESSAGES, 
				ChatUtils.SERVER_ERROR_SUCCESS, count);
		m_handler.sendMessage(msg);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}
	
	private void doBindService() {
		getApplicationContext().bindService(
				new Intent(getApplicationContext(), PollingService.class),
				m_connection, Context.BIND_AUTO_CREATE);

		m_isBound = true;
	}
	
	private void doUnbindService() {
		if (m_isBound) {
			m_pollingServiceBinder.unsubscribeFromNewMessages(m_chatThread, this);
			getApplicationContext().unbindService(m_connection);
			m_isBound = false;
		}
	}
	
	private static class MessageHandler extends Handler {
		private WeakReference<ChattingActivity> m_activity;

		public MessageHandler(ChattingActivity activity) {
			super();
			m_activity = new WeakReference<ChattingActivity>(activity);
		}
		
		// For messages sent to this handler,
		// what is the command
		// arg1 is the error code
		// arg2 is determined by the command
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == COMMAND_STARTCHAT) {
				if (msg.arg1 == ChatUtils.SERVER_ERROR_SUCCESS) {
					// Disable the start chat now icon and enable the message box
					m_activity.get().m_initiateChatMI.setVisible(false);
					m_activity.get().m_closeChatMI.setVisible(true);
					m_activity.get().m_messageBox.setFocusable(true);
					m_activity.get().m_messageBox.setFocusableInTouchMode(true);
					m_activity.get().m_messageBox.setHint(null);
				}
				else {
					Toast.makeText(m_activity.get(), "Failed to start the chat. Try again", Toast.LENGTH_SHORT).show();
					// Switch from the animation to the start chat icon
					m_activity.get().m_initiateChatMI.setEnabled(true);
				}
			}
			else if (msg.what == COMMAND_VIEWTHREAD) {
				if (msg.arg1 == ChatUtils.SERVER_ERROR_SUCCESS) {
					m_activity.get().getNewMessages();
				}
			}
			else if (msg.what == COMMAND_NEWMESSAGES) {
				// arg2 is the number of new messages added
				if (msg.arg1 == ChatUtils.SERVER_ERROR_SUCCESS) {
					if (msg.arg2 > 0) {
						m_activity.get().updateMessageList();
					}
				}
			}
			else if (msg.what == COMMAND_SENDMESSAGE) {
				if (msg.arg1 == ChatUtils.SERVER_ERROR_SUCCESS) {
					// Nothing to do for now
				}
				else {
					Toast.makeText(m_activity.get(), "Failed to send the message. Will attempt to send it again", Toast.LENGTH_SHORT).show();
				}
			}
			else if (msg.what == COMMAND_CLOSECHAT) {
				if (msg.arg1 == ChatUtils.SERVER_ERROR_SUCCESS) {
					m_activity.get().m_initiateChatMI.setVisible(false);
				}
				else {
					Toast.makeText(m_activity.get(), "Failed to close the chat. Try again later", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
}
