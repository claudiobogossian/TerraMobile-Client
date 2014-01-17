package br.inpe.mobile.database;

import br.inpe.mobile.user.SessionManager;
import android.app.Application;

public class ApplicationMemory extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.createSession(getApplicationContext());
        DatabaseHelper.setHelper(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        DatabaseHelper.releaseHelper();
        super.onTerminate();
    }
}
