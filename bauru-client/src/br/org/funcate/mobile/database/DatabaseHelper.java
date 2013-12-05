package br.org.funcate.mobile.database;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public class DatabaseHelper {

	private static DatabaseAdapter databaseHelper;

	public static DatabaseAdapter getDatabase() {
		return databaseHelper;
	}

	public static void setHelper(Context context) {
		databaseHelper = OpenHelperManager.getHelper(context, DatabaseAdapter.class);
	}

	public static void releaseHelper() {
		OpenHelperManager.releaseHelper();
		databaseHelper = null;
	}
}
