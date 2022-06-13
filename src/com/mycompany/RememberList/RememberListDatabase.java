package com.mycompany.RememberList;
import android.app.*;
import android.database.sqlite.*;
import java.util.*;
import android.provider.*;
import android.database.*;
import android.text.*;
import android.content.*;
import android.util.*;
import android.widget.*;

public class RememberListDatabase
{

    private static final String TAG = "RememberListDatabase";

    //The columns we'll include
    public static final String KEY_TEXT = "Text";//SearchManager.SUGGEST_COLUMN_TEXT_1;
	
    private static final String FTS_VIRTUAL_TABLE = "FTSRememberList";
	
    private static final int DATABASE_VERSION = 1;

    private FoodOpenHelper mDbHelper;
	private SQLiteDatabase mDb;
    private static final HashMap<String,String> mColumnMap = buildColumnMap();

    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_TEXT, KEY_TEXT);
        map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
        return map;
    }

    public Cursor getText(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};
		String sortOrder= KEY_TEXT+" ASC";
        return query(selection, selectionArgs, columns, sortOrder);
        // SELECT <columns> FROM <table> WHERE rowid = <rowId>
    }

    public Cursor getTextMatches(String query, String[] columns) {
		String str=String.valueOf('"');// "-teken verwijderen
		query = query.replaceAll(str," ");

		String selection = KEY_TEXT + " MATCH ?";
		query=query.trim();
		String[] strings = TextUtils.split(query, " ");

		String q="";
		for (int i = 0; i < strings.length; i++) {
			if (!strings[i].isEmpty()){
			    strings[i]=strings[i]+"*"; // "*" werkt enkel rechtse kant
			    q+=strings[i];
			}
		}
        String[] selectionArgs = new String[] {q};//query+"*"
		String sortOrder= KEY_TEXT+" ASC";
        return query(selection, selectionArgs, columns, sortOrder);
        // SELECT <columns> FROM <table> WHERE <KEY_WORD> MATCH 'query*' //rowid=_id
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns, String sortOrder) {
        //contentprovider moet geen kolomnamen kennen
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(mColumnMap);
        Cursor cursor = builder.query(mDbHelper.getReadableDatabase(),             
									  columns, selection, selectionArgs, null, null, sortOrder); //DESC  ASC
		if (cursor == null) {                                                     
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

	public Cursor getAllText(){
		String[] columns = new String[] {
			BaseColumns._ID,
			RememberListDatabase.KEY_TEXT};
		String sortOrder= KEY_TEXT+" ASC";	
     	return query(null, null, columns, sortOrder);
	}

	private static final String FTS_TABLE_CREATE =
	"CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
	" USING fts3 (" +
	KEY_TEXT + "); "; //DATETIME TEXT INTEGER INT REAL

	private final Context mCtx;
	private boolean isExternalStorage;

	//This creates/opens the database.
    private static class FoodOpenHelper extends SQLiteOpenHelper {

		FoodOpenHelper(Context context,final String DATABASE_NAME) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
			Log.w(TAG,FTS_TABLE_CREATE);
			db.execSQL(FTS_TABLE_CREATE);
        }

		@Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				  + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
    }		

	public RememberListDatabase(Context ctx, boolean bool) {
		this.isExternalStorage=bool;
		this.mCtx = ctx;
	}

    public RememberListDatabase open() throws SQLException {
		String DATABASE_NAME = "RememberList.db";       
		if (isExternalStorage){ DATABASE_NAME= "/sdcard/AppProjects/RememberList.db";}
		mDbHelper = new FoodOpenHelper(mCtx,DATABASE_NAME);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
/*	public RememberListDatabase open2() throws SQLException {//test
		String DATABASE_NAME = "RememberList.db";       //AppProjects
		if (isExternalStorage){ DATABASE_NAME= "/sdcard/AppProjects/RememberList.db";}
		mDbHelper = new FoodOpenHelper(mCtx,DATABASE_NAME);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}*/

	public void close() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}

	public void addText(String text) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TEXT, text);
		mDb.insert(FTS_VIRTUAL_TABLE, null, initialValues);
    }

	public void deleteText(long id) {
		mDb.delete(FTS_VIRTUAL_TABLE, "rowid = "+ id,null);		
    }

	public boolean deleteTextList() {
		int doneDelete = 0;
		doneDelete = mDb.delete(FTS_VIRTUAL_TABLE, null , null);
		return doneDelete > 0;		
	}

	public void toast(String msg){
        Toast.makeText(this.mCtx, msg, Toast.LENGTH_SHORT).show();
    }
}
