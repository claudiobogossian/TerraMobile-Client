package br.org.funcate.mobile.data;

import android.app.Application;

public class ApplicationMemory extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		DatabaseHelper.setHelper(getApplicationContext());
	}

	@Override
	public void onTerminate() {
		DatabaseHelper.releaseHelper();
		super.onTerminate();
	}
}