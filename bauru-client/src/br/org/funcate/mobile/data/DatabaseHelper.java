package br.org.funcate.mobile.data;

import android.app.Application;
import br.org.funcate.mobile.DatabaseAdapter;

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
		}
		return self;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.db = DatabaseAdapter.getInstance(this);
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		this.db.close();
		this.db = null;
	}
	
	/**
	 * 
	 *  Create new DatabaseAdapter, based on this instance.
	 * 
	 * */
	public DatabaseAdapter getDatabase(){
		return this.db;
	}
}
