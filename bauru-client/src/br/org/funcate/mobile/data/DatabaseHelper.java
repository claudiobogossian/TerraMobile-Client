package br.org.funcate.mobile.data;

import android.app.Application;

public class DatabaseHelper extends Application {
	
	private DatabaseAdapter db;
	private static DatabaseHelper self;
	
	private DatabaseHelper() {}
	
	/**
	 * 
	 * Singleton Class.
	 * 
	 * */
	public static DatabaseHelper getInstance(){
		if(self == null){
			self = new DatabaseHelper();
			self.db = DatabaseAdapter.getInstance(self);
		}
		return self;
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		self.db.close();
		self.db = null;
	}
	
	/**
	 * 
	 *  Create new DatabaseAdapter, based on this instance.
	 * 
	 * */
	public DatabaseAdapter getDatabase(){
		return self.db;
	}
}
