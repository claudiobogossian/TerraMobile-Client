package br.inpe.mobile.database;

import android.app.Application;
import br.inpe.mobile.user.SessionManager;

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
