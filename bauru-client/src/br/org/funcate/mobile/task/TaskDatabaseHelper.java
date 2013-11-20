package br.org.funcate.mobile.task;

import android.app.Application;
import android.content.Context;

public class TaskDatabaseHelper extends Application {
	
	private static TaskDatabase mDbHelper;

	/**
	 * Called when the application is starting, before any other application
	 * objects have been created. Implementations should be as quick as
	 * possible...
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mDbHelper = TaskDatabase.getInstance(this);
	}

	/**
	 * Called when the application is stopping. There are no more application
	 * objects running and the process will exit. Note: never depend on this
	 * method being called; in many cases an unneeded application process will
	 * simply be killed by the kernel without executing any application code...
	 */
	@Override
	public void onTerminate() {
		super.onTerminate();
		mDbHelper.close();
		mDbHelper = null;
	}

	public static TaskDatabase getDatabase(Context context) {
		if(mDbHelper == null){
			mDbHelper = TaskDatabase.getInstance(context);
		}
		return mDbHelper;
	}
}