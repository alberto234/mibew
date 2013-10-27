package com.scalior.mibewmob.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.scalior.mibewmob.model.ChatMessage;
import com.scalior.mibewmob.model.ChatOperator;
import com.scalior.mibewmob.model.ChatServer;
import com.scalior.mibewmob.model.ChatThread;
import com.scalior.mibewmob.model.MonitoredSite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MibewMobSQLiteHelper extends SQLiteOpenHelper {
	// Database information
	private static final String DATABASE_NAME = "mibewmob.db";
	private final static int DATABASE_VERSION = 6;
	
	// Tables:
	//		ChatServer
	public static final String TABLE_CHATSERVER 			= "chatserver";
	public static final String CHATSERVER_ID 				= "_id";
	public static final String CHATSERVER_NAME 				= "name";
	public static final String CHATSERVER_URL 				= "url";
	public static final String CHATSERVER_VERSION 			= "version";
	public static final String CHATSERVER_LOGO 				= "logo";
	public static final String CHATSERVER_MIBEWMOBVERSION 	= "mibewmobversion";
	public static final String CHATSERVER_WEBSERVICEURL 	= "webserviceurl";
	private static final String TABLE_CHATSERVER_CREATE 	= "create table " + 
			TABLE_CHATSERVER + " (" +
			CHATSERVER_ID + " integer primary key autoincrement, " +
			CHATSERVER_NAME + " text not null, " +
			CHATSERVER_URL + " text not null, " +
			CHATSERVER_VERSION + " text not null, " +
			CHATSERVER_LOGO + " text not null, " +
			CHATSERVER_MIBEWMOBVERSION + " text not null, " +
			CHATSERVER_WEBSERVICEURL + " text not null);";

		    
	//		ChatOperator
	public static final String TABLE_CHATOPERATOR 			= "chatoperator";
	public static final String CHATOPERATOR_ID 				= "_id";
	public static final String CHATOPERATOR_USERNAME 		= "username";
	public static final String CHATOPERATOR_TOKEN 			= "token";
	public static final String CHATOPERATOR_LOCALENAME		= "localename";
	public static final String CHATOPERATOR_COMMONNAME 		= "commonname";
	public static final String CHATOPERATOR_EMAIL 			= "email";
	public static final String CHATOPERATOR_PERMISSIONS 	= "permissions";
	public static final String CHATOPERATOR_SERVER_ID	 	= "server_id";
	private static final String TABLE_CHATOPERATOR_CREATE 	= "create table " + 
			TABLE_CHATOPERATOR + " (" +
			CHATOPERATOR_ID + " integer primary key autoincrement, " +
			CHATOPERATOR_USERNAME + " text not null, " +
			CHATOPERATOR_TOKEN + " text not null, " +
			CHATOPERATOR_LOCALENAME + " text not null, " +
			CHATOPERATOR_COMMONNAME + " text not null, " +
			CHATOPERATOR_EMAIL + " text, " +
			CHATOPERATOR_PERMISSIONS + " integer, " +
			CHATOPERATOR_SERVER_ID + " integer not null);";

	
	//		ChatThread
	public static final String TABLE_CHATTHREAD 			= "chatthread";
	public static final String CHATTHREAD_ID 				= "_id";
	public static final String CHATTHREAD_SERVERID 			= "serverid";
	public static final String CHATTHREAD_THREADID 			= "threadid";
	public static final String CHATTHREAD_STATE				= "state";
	public static final String CHATTHREAD_CANOPEN 			= "canopen";
	public static final String CHATTHREAD_CANVIEW 			= "canview";
	public static final String CHATTHREAD_CANBAN 			= "canban";
	public static final String CHATTHREAD_GUESTTYPING 		= "guesttyping";
	public static final String CHATTHREAD_GUESTNAME 		= "guestname";
	public static final String CHATTHREAD_AGENTNAME 		= "agentname";
	public static final String CHATTHREAD_USERAGENT 		= "useragent";
	public static final String CHATTHREAD_INITIALMESSAGE	= "initialmessage";
	private static final String TABLE_CHATTHREAD_CREATE 	= "create table " + 
			TABLE_CHATTHREAD + " (" +
			CHATTHREAD_ID + " integer primary key autoincrement, " +
			CHATTHREAD_SERVERID + " integer not null, " +
			CHATTHREAD_THREADID + " integer not null, " +
			CHATTHREAD_STATE + " integer not null, " +
			CHATTHREAD_CANOPEN + " boolean, " +
			CHATTHREAD_CANVIEW + " boolean, " +
			CHATTHREAD_CANBAN + " boolean, " +
			CHATTHREAD_GUESTTYPING + " boolean, " +
			CHATTHREAD_GUESTNAME + " text, " +
			CHATTHREAD_AGENTNAME + " text not null, " +
			CHATTHREAD_USERAGENT + " text, " +
			CHATTHREAD_INITIALMESSAGE + " text);";

	
	//		ChatMessage
	public static final String TABLE_CHATMESSAGE 			= "chatmessage";
	public static final String CHATMESSAGE_ID 				= "_id";
	public static final String CHATMESSAGE_THREADID 		= "threadid";
	public static final String CHATMESSAGE_TYPE 			= "type";
	public static final String CHATMESSAGE_OPERATORID 		= "operatorid";
	public static final String CHATMESSAGE_OPERATORNAME 	= "operatorname";
	public static final String CHATMESSAGE_MESSAGE 			= "message";
	public static final String CHATMESSAGE_TIMECREATED 		= "timecreated";
	private static final String TABLE_CHATMESSAGE_CREATE 	= "create table " + 
			TABLE_CHATMESSAGE + " (" +
			CHATMESSAGE_ID + " integer primary key autoincrement, " +
			CHATMESSAGE_THREADID + " integer not null, " +
			CHATMESSAGE_TYPE + " integer not null, " +
			CHATMESSAGE_OPERATORID + " integer, " +
			CHATMESSAGE_OPERATORNAME + " text, " +
			CHATMESSAGE_MESSAGE + " text not null, " +
			CHATMESSAGE_TIMECREATED + " datetime not null);";

	// Constructor
	public MibewMobSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(TABLE_CHATSERVER_CREATE);
		database.execSQL(TABLE_CHATOPERATOR_CREATE);
		database.execSQL(TABLE_CHATTHREAD_CREATE);
		database.execSQL(TABLE_CHATMESSAGE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldversion, int newversion) {
		// Here, we are going to decide how we are upgrading. We can only grow the
		// tables without dropping the contents by using ALTER TABLE
		// The version numbers are going to be used to determine how many changes need
		// to be done. e.g say we are at version 4
		// 1 - 2 added column a, b
		// 2 - 3 added c
		// 3 - 4 added d
		// If someone is upgrading from 1 - 4, we have to add all columns.
		//if (oldversion <= 1) {
			//database.execSQL(TABLE_CHATOPERATOR_CREATE);
		//}
		
		// Let's temporarily drop everything and recreate the database
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHATSERVER);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHATOPERATOR);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHATTHREAD);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHATMESSAGE);
		onCreate(database);
	}
	
	/**
	 * Description:
	 * 		Add a new chatserver to the database
	 * @param p_server
	 * @return long - the database id if successful, -1 otherwise
	 * 
	 */
	public long addNewChatServer(ChatServer p_server) {
		long retVal = -1;
		
		if (p_server != null) {
			SQLiteDatabase database = getWritableDatabase();

			// First make sure that this is not a duplicate.
			// Ignore duplicates for now
			String[] serverColumns = {CHATSERVER_ID};
			StringBuilder selection = new StringBuilder(CHATSERVER_URL);
			selection.append(" = \"").append(p_server.getURL()).append("\"");
			
			Cursor cursor  = database.query(TABLE_CHATSERVER,
											serverColumns, 
											selection.toString(),
											null, null, null, null);

			if (cursor.getCount() == 1) {
				// Duplicate
				cursor.moveToFirst();
				retVal = cursor.getLong(0);
			}
			else if (cursor.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(CHATSERVER_NAME, p_server.getName());
				values.put(CHATSERVER_URL, p_server.getURL());
				values.put(CHATSERVER_LOGO, p_server.getLogoURL());
				values.put(CHATSERVER_VERSION, p_server.getVersion());
				values.put(CHATSERVER_MIBEWMOBVERSION, p_server.getMibewMobVersion());
				values.put(CHATSERVER_WEBSERVICEURL, p_server.getWebServiceURL());
				retVal = database.insert(TABLE_CHATSERVER, null, values);
				p_server.setID(retVal);
			}

			cursor.close();
		}
		return retVal;
	}


	/**
	 * Description:
	 * 		Add a new chat operator to the database
	 * @param p_operator
	 * @return long - the database id if successful, -1 otherwise
	 * 
	 */
	public long addNewOperator(ChatOperator p_operator) {
		long retVal = -1;
		
		if (p_operator != null) {
			SQLiteDatabase database = getWritableDatabase();

			// First make sure that this is not a duplicate.
			// Ignore duplicates for now
			String[] serverColumns = {CHATOPERATOR_ID};
			StringBuilder selection = new StringBuilder(CHATOPERATOR_USERNAME);
			selection.append(" = \"").append(p_operator.getUsername()).append("\"");
			
			Cursor cursor  = database.query(TABLE_CHATOPERATOR,
											serverColumns, 
											selection.toString(),
											null, null, null, null);

			if (cursor.getCount() == 1) {
				cursor.moveToFirst();
				retVal = cursor.getLong(0);
			}
			else if (cursor.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(CHATOPERATOR_USERNAME, p_operator.getUsername());
				values.put(CHATOPERATOR_TOKEN, p_operator.getToken());
				values.put(CHATOPERATOR_LOCALENAME, p_operator.getLocaleName());
				values.put(CHATOPERATOR_COMMONNAME, p_operator.getCommonName());
				values.put(CHATOPERATOR_EMAIL, p_operator.getEmail());
				values.put(CHATOPERATOR_PERMISSIONS, p_operator.getPermissions());
				values.put(CHATOPERATOR_SERVER_ID, p_operator.getServerID());
				retVal = database.insert(TABLE_CHATOPERATOR, null, values);
			}
			
			cursor.close();
		}
		return retVal;
	}
	
	
	/**
	 * Description:
	 * 		Add or update a chat thread.
	 * @param p_thread
	 * @return long - the database id if successful, -1 otherwise
	 * 
	 * NOTE: Will have to convert this into a content provider to facilitate updates
	 */
	public long addOrUpdateThread(ChatThread p_thread) {
		long retVal = -1;
		
		if (p_thread != null) {
			SQLiteDatabase database = getWritableDatabase();

			// First, check if this is a duplicate.
			String[] serverColumns = {CHATTHREAD_ID};
			StringBuilder selection = new StringBuilder(CHATTHREAD_THREADID);
			selection.append(" = ").append(p_thread.getThreadID());
			
			Cursor cursor  = database.query(TABLE_CHATTHREAD,
											serverColumns, 
											selection.toString(),
											null, null, null, null);

			if (cursor.getCount() > 1) {
				// Major error
			}
			if (cursor.getCount() == 1) {
				cursor.moveToFirst();
				retVal = cursor.getLong(0);

				selection = new StringBuilder(CHATTHREAD_ID);
				selection.append(" = ").append(retVal);
				
				// TODO: Update the entry here
				ContentValues values = new ContentValues();
				values.put(CHATTHREAD_STATE, p_thread.getState());
				values.put(CHATTHREAD_GUESTTYPING, p_thread.isTyping());
				values.put(CHATTHREAD_GUESTNAME, p_thread.getGuestName());
				values.put(CHATTHREAD_USERAGENT, p_thread.getUserAgent());
				values.put(CHATTHREAD_INITIALMESSAGE, p_thread.getInitialMessage());
				database.update(TABLE_CHATTHREAD, values, selection.toString(), null);
			}
			else if (cursor.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(CHATTHREAD_SERVERID, p_thread.getServerID());
				values.put(CHATTHREAD_THREADID, p_thread.getThreadID());
				values.put(CHATTHREAD_STATE, p_thread.getState());
				values.put(CHATTHREAD_CANOPEN, p_thread.isCanOpen());
				values.put(CHATTHREAD_CANVIEW, p_thread.isCanView());
				values.put(CHATTHREAD_CANBAN, p_thread.isCanBan());
				values.put(CHATTHREAD_GUESTTYPING, p_thread.isTyping());
				values.put(CHATTHREAD_GUESTNAME, p_thread.getGuestName());
				values.put(CHATTHREAD_USERAGENT, p_thread.getUserAgent());
				values.put(CHATTHREAD_INITIALMESSAGE, p_thread.getInitialMessage());
				retVal = database.insert(TABLE_CHATTHREAD, null, values);
			}
			
			cursor.close();
		}
		return retVal;
	}

	/**
	 * Description:
	 * 		Add a new chat message to the database
	 * @param p_message
	 * @return long - the database id if successful, -1 otherwise
	 * 
	 */
	public long addNewChatMessage(ChatMessage p_message) {
		long retVal = -1;
		
		if (p_message != null) {
			SQLiteDatabase database = getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(CHATMESSAGE_THREADID, p_message.getThreadID());
			values.put(CHATMESSAGE_MESSAGE, p_message.getMessage());
			values.put(CHATMESSAGE_TYPE, p_message.getType());
			values.put(CHATMESSAGE_OPERATORID, p_message.getOperatorGuid());
			values.put(CHATMESSAGE_OPERATORNAME, p_message.getOperatorName());
			values.put(CHATMESSAGE_TIMECREATED, p_message.getTimeCreated().toString());
			retVal = database.insert(TABLE_CHATMESSAGE, null, values);
		}
		return retVal;
	}

	
	/**
	 * Description:
	 * 		Method to refresh the list of monitored sites.
	 * @param p_monitoredSites. This must already be created by the caller
	 * @return true if the list changed, false if there was an error
	 */
	public boolean refreshMonitoredSites(List<MonitoredSite> p_monitoredSites) {
		// Clear the list if one exists.
		if (p_monitoredSites == null) {
			return false;
		}
		
		p_monitoredSites.clear();
		
		String rawSQL = "SELECT " + CHATSERVER_NAME + ", " +
						  CHATSERVER_URL + ", " + CHATSERVER_LOGO + ", " + 
						  CHATSERVER_WEBSERVICEURL + ", " + CHATOPERATOR_TOKEN + ", " +
						  CHATOPERATOR_LOCALENAME + ", " + CHATOPERATOR_EMAIL + ", " +
						  CHATOPERATOR_PERMISSIONS + ", " + TABLE_CHATSERVER + "." + CHATSERVER_ID +
						  " FROM " + TABLE_CHATSERVER + " INNER JOIN " + 
						  TABLE_CHATOPERATOR + " ON " + TABLE_CHATSERVER + "." + CHATSERVER_ID + 
						  " = " + TABLE_CHATOPERATOR + "." + CHATOPERATOR_SERVER_ID;
		
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor  = database.rawQuery(rawSQL, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {
				ChatServer chatServer = new ChatServer(cursor.getString(0), 
						cursor.getString(1),
						null,
						cursor.getString(2),
						null, 
						cursor.getString(3));
				chatServer.setID(cursor.getLong(8));

				ChatOperator chatOperator = new ChatOperator(cursor.getString(4),
						null,
						cursor.getString(5),
						null,
						cursor.getString(6),
						cursor.getInt(7));

				p_monitoredSites.add(new MonitoredSite(chatServer, chatOperator));
				cursor.moveToNext();
			}
		}
		cursor.close();
		return true;
	}
	
	/**
	 * Description:
	 * 		Method to load the list of visitors.
	 * @param p_monitoredSites. This must already be created by the caller
	 * @return true if the list changed, false if there was an error
	 * 
	 * Remarks:
	 * 		Ideally this should only hold the list of visitors that the operator
	 *		has initiated a conversation with
	 */
	public boolean loadArchivedVisitors(List<ChatThread> p_visitorList) {
		// Clear the list if one exists.
		if (p_visitorList == null) {
			return false;
		}
		
		p_visitorList.clear();
		
		String rawSQL = "SELECT " + TABLE_CHATTHREAD + "." + CHATTHREAD_ID + ", " +
						  CHATTHREAD_SERVERID + ", " + CHATTHREAD_THREADID + ", " + 
						  CHATTHREAD_STATE + ", " + CHATTHREAD_CANOPEN + ", " +
						  CHATTHREAD_CANVIEW + ", " + CHATTHREAD_CANBAN + ", " +
						  CHATTHREAD_GUESTTYPING + ", " + CHATTHREAD_GUESTNAME + ", " +
						  CHATTHREAD_AGENTNAME + ", " + CHATTHREAD_USERAGENT + ", " +
						  CHATTHREAD_INITIALMESSAGE + ", " + CHATSERVER_LOGO +
						  " FROM " + TABLE_CHATTHREAD + " INNER JOIN " + 
						  TABLE_CHATSERVER + " ON " + TABLE_CHATTHREAD + "." + CHATTHREAD_SERVERID + 
						  " = " + TABLE_CHATSERVER + "." + CHATSERVER_ID;
		
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor  = database.rawQuery(rawSQL, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {
				ChatThread tempThread = new ChatThread(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
						cursor.getInt(4) > 0, cursor.getInt(5) > 0, cursor.getInt(6) > 0, cursor.getInt(7) > 0,
						cursor.getString(8), cursor.getString(9), cursor.getString(10), cursor.getString(11));
				tempThread.setServerLogoURL(cursor.getString(12));
				
				p_visitorList.add(tempThread);
				cursor.moveToNext();
			}
		}
		cursor.close();
		return true;
	}

	public List<ChatMessage> getMessages(long p_threadID) {
		List<ChatMessage> messages = null;
		
		String rawSQL = "SELECT " + CHATMESSAGE_MESSAGE + ", " +
				CHATMESSAGE_TYPE + ", " + CHATMESSAGE_OPERATORID + ", " + 
				CHATMESSAGE_OPERATORNAME + ", " + CHATMESSAGE_TIMECREATED + 
				  " FROM " + TABLE_CHATMESSAGE + 
				  " WHERE " + CHATMESSAGE_THREADID + " = " + p_threadID;

		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor  = database.rawQuery(rawSQL, null);

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			messages = new ArrayList<ChatMessage>();
			
			while (!cursor.isAfterLast()) {
				messages.add(new ChatMessage(p_threadID, cursor.getString(0),
						cursor.getInt(1), cursor.getInt(2), cursor.getString(3),
						new Date(cursor.getInt(4))));
				
				cursor.moveToNext();
			}
		}
		
		return messages;
	}
}
