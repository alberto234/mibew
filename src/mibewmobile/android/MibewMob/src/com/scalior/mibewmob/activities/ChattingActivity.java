package com.scalior.mibewmob.activities;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.scalior.mibewmob.ChatUtils;
import com.scalior.mibewmob.MessageListAdapter;
import com.scalior.mibewmob.MibewMobException;
import com.scalior.mibewmob.R;
import com.scalior.mibewmob.model.ChatMessage;
import com.scalior.mibewmob.model.ChatThread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ListActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class ChattingActivity extends ListActivity {
	private ChatUtils m_serverUtils;
	private ChatThread m_chatThread;
	private List<ChatMessage> m_messageList;
	private MessageHandler m_handler;
	private MessageListAdapter m_listAdapter;
	
	// Views to keep track off
	private MenuItem m_initiateChatMI;
	private ImageButton m_sendBtn;
	private EditText m_messageBox;
	
	// Some constants used by the handler for this activity
	private static final int COMMAND_VIEWTHREAD 	= 1;
	private static final int COMMAND_STARTCHAT 		= 2;
	private static final int COMMAND_NEWMESSAGES	= 3;
	private static final int COMMAND_SENDMESSAGE	= 4;
	
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

		m_sendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
			
		// Extract the thread of interest
		if (getIntent().getExtras().containsKey("position")) {
			int position = getIntent().getExtras().getInt("position");
			int threadID = getIntent().getExtras().getInt("threadid");
		
			 m_chatThread = m_serverUtils.getVisitorList().get(position);
			 m_messageList = m_serverUtils.getMessages(m_chatThread.getID());
			 if (m_messageList == null) {
				 m_messageList = new ArrayList<ChatMessage>();
			 }
			 
			// For sanity, confirm that we retrieved the right thread
			if (threadID == m_chatThread.getThreadID()) {
				// Load messages pertaining to this thread.
				m_listAdapter = new MessageListAdapter(this, 
						R.layout.chatbubbleitem,
						m_messageList);
				setListAdapter(m_listAdapter);
			}
			else {
				// TODO: Notify user of error and prompt to go back
			}
			
			if (!m_chatThread.isViewed()) {
				viewThread();
			}
			else {
				getNewMessages();
			}
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
			
		case R.id.action_initiate_chat:
			// Change the background to a rotating icon and disable any clicking
			// Log run a background thread that starts the chat.
			// On success, enable the chat box
			
			m_initiateChatMI.setEnabled(false);
			Thread thread = new Thread(new Runnable() {

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
			
			thread.start();
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
					int count = newMessages.size();
					// Add these new messages to the existing list
					// TODO: We need to mark messages that have been received as sync'ed.
					//		 For now, each request for new messages will send the entire list
					m_messageList.clear();
					m_messageList.addAll(newMessages);

					msg = m_handler.obtainMessage(COMMAND_NEWMESSAGES, 
							ChatUtils.SERVER_ERROR_SUCCESS, count);
				} catch (MibewMobException e) {
					// TODO: Add entry to log file
					msg = m_handler.obtainMessage(COMMAND_NEWMESSAGES, 
							ChatUtils.SERVER_ERROR_UNKNOWN, 0);
				}
				m_handler.sendMessage(msg);
			}
		});
		
		thread.start();
	}

	protected void sendMessage() {
		// Add the message to the message list first
		final ChatMessage chatMessage = new ChatMessage(m_chatThread.getThreadID(),
				m_messageBox.getText().toString(),
				0, 45, null, null);
		m_messageList.add(chatMessage);
		m_listAdapter.notifyDataSetChanged();
		
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
					// Disable the start chat now icon
					m_activity.get().m_initiateChatMI.setVisible(false);
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
						m_activity.get().m_listAdapter.notifyDataSetChanged();
					}
				}
			}
			else if (msg.what == COMMAND_SENDMESSAGE) {
				if (msg.arg1 == ChatUtils.SERVER_ERROR_SUCCESS) {
					m_activity.get().m_listAdapter.notifyDataSetChanged();
				}
				else {
					Toast.makeText(m_activity.get(), "Failed to send the message. Will attempt to send it again", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
}
