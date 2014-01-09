package br.inpe.mobile.database;

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
