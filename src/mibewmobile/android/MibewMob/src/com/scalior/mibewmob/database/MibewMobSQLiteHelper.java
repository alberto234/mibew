package com.scalior.mibewmob.database;

import com.scalior.mibewmob.model.ChatOperator;
import com.scalior.mibewmob.model.ChatServer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MibewMobSQLiteHelper extends SQLiteOpenHelper {
	// Database information
	private static final String DATABASE_NAME = "mibewmob.db";
	private final static int DATABASE_VERSION = 2;
	
	// Tables:
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

		    
	public static final String TABLE_CHATOPERATOR 			= "chatoperator";
	public static final String CHATOPERATOR_ID 				= "_id";
	public static final String CHATOPERATOR_USERNAME 		= "username";
	public static final String CHATOPERATOR_LOCALENAME		= "localename";
	public static final String CHATOPERATOR_COMMONNAME 		= "commonname";
	public static final String CHATOPERATOR_EMAIL 			= "email";
	public static final String CHATOPERATOR_PERMISSIONS 	= "permissions";
	private static final String TABLE_CHATOPERATOR_CREATE 	= "create table " + 
			TABLE_CHATOPERATOR + " (" +
			CHATOPERATOR_ID + " integer primary key autoincrement, " +
			CHATOPERATOR_USERNAME + " text not null, " +
			CHATOPERATOR_LOCALENAME + " text not null, " +
			CHATOPERATOR_COMMONNAME + " text not null, " +
			CHATOPERATOR_EMAIL + " text not null, " +
			CHATOPERATOR_PERMISSIONS + " integer);";

	public MibewMobSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(TABLE_CHATSERVER_CREATE);
		database.execSQL(TABLE_CHATOPERATOR_CREATE);
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
		if (oldversion <= 1) {
			database.execSQL(TABLE_CHATOPERATOR_CREATE);
		}
	}
	
	public boolean addNewChatServer(ChatServer p_server) {
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

			if (cursor.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(CHATSERVER_NAME, p_server.getName());
				values.put(CHATSERVER_URL, p_server.getURL());
				values.put(CHATSERVER_LOGO, p_server.getLogoURL());
				values.put(CHATSERVER_VERSION, p_server.getVersion());
				values.put(CHATSERVER_MIBEWMOBVERSION, p_server.getMibewMobVersion());
				values.put(CHATSERVER_WEBSERVICEURL, p_server.getWebServiceURL());
				long insertId = database.insert(TABLE_CHATSERVER, null, values);
				
				if (insertId != -1) {
					return true;
				}
			}
			
			cursor.close();
		}
		return false;
	}


	public boolean addNewOperator(ChatOperator p_operator) {
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

			if (cursor.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(CHATOPERATOR_USERNAME, p_operator.getUsername());
				values.put(CHATOPERATOR_LOCALENAME, p_operator.getLocaleName());
				values.put(CHATOPERATOR_COMMONNAME, p_operator.getCommonName());
				values.put(CHATOPERATOR_EMAIL, p_operator.getEmail());
				values.put(CHATOPERATOR_PERMISSIONS, p_operator.getPermissions());
				long insertId = database.insert(TABLE_CHATOPERATOR, null, values);
				
				if (insertId != -1) {
					return true;
				}
			}
			
			cursor.close();
		}
		return false;
	}
}
