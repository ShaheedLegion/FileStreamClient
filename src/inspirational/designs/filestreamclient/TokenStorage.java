package inspirational.designs.filestreamclient;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TokenStorage {

	private static TokenStorage instance = null;
	public static TokenStorage getInstance() {
		if (instance == null)
			instance = new TokenStorage();
		
		return instance;
	}
	
	private SQLiteDatabase database;
	private TokenStorage() {
		database = SQLiteDatabase.openOrCreateDatabase("file_stream_client", null, null);
		if (database != null)
			database.execSQL("CREATE TABLE IF NOT EXISTS TokenRegistry(AppToken VARCHAR);");
	}
	
	// This performs a query to get a token.
	public String getToken() {
		if (database != null) {
			Cursor resultSet = database.rawQuery("Select AppToken from TokenRegistry",null);
			if (resultSet.moveToFirst())
				return resultSet.getString(1);
		}
		
		return "";
	}
	
	public void setToken(String token) {
		if (database != null) {
			ContentValues contentValues = new ContentValues();
		    contentValues.put("AppToken", token);
		    database.insert("TokenRegistry", null, contentValues);
		}
	}
}
