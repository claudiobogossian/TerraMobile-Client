package br.inova.mobile.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import br.inova.mobile.Utility;
import br.inova.mobile.constants.Constants;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.map.BaseMapDownload;
import br.inova.mobile.photo.UploadPhotos;
import br.inova.mobile.user.SessionManager;
import br.inpe.mobile.R;
import br.inpe.mobile.R.string;

/**
 * Activity for loading layout resources
 * 
 * This activity is used to display the Task screen, that.
 * 
 * @author Paulo Luan
 * @version 1.0
 * @since 1.0
 */
public class TaskActivity extends Activity {
        
        public final String    LOG_TAG = "#" + getClass().getSimpleName();
        
        // private final String hostUrl = "http://200.144.100.34:8080/";
        
        private TextView       txtIncompleteTasks;
        
        private TextView       txtNotSyncRegisters;
        
        private ProgressDialog mProgressDialog;
        
        private TaskActivity   self    = this;
        
        private SessionManager session;
        
        private String         name;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
                /**
                 * Defines the default exception handler to log unexpected
                 * android errors
                 */
                
                session = SessionManager.getInstance();
                name = session.getUserName();
                
                // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
                setContentView(R.layout.activity_task);
                
                txtIncompleteTasks = (TextView) findViewById(R.id.txt_count_incompleted_tasks);
                txtNotSyncRegisters = (TextView) findViewById(R.id.txt_count_completed_tasks);
                
                this.createButtons();
                this.updateCountLabels();
        }
        
        public void createButtons() {
                createButtonGetTasks();
                createButtonLogout();
                createButtonGetTiles();
                createButtonGenerateTestTasks();
        }
        
        private void createButtonGenerateTestTasks() {
                String lowerName = name.toLowerCase();
                
                if (lowerName.equals("test") || lowerName.equals("bele")) {
                        Constants.changeToHomologMode();
                        
                        Button btn_generate_tasks = (Button) findViewById(R.id.btnTest);
                        btn_generate_tasks.setVisibility(View.VISIBLE);
                        btn_generate_tasks.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TaskActivity.this);
                                        alertDialogBuilder.setTitle(string.caution);
                                        alertDialogBuilder.setMessage(string.generate_registers_message).setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                                    DialogInterface dialog,
                                                                    int id) {
                                                        new TaskTestsGenerator(TaskActivity.this);
                                                }
                                        }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                                    DialogInterface dialog,
                                                                    int id) {
                                                        dialog.cancel();
                                                }
                                        });
                                        
                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.show();
                                }
                        });
                }
                else {
                        Constants.changeToProductionMode();
                }
        }
        
        private void createButtonGetTiles() {
                Button btn_get_tiles = (Button) findViewById(R.id.btn_get_tiles);
                btn_get_tiles.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if (Utility.isNetworkAvailable(self)) {
                                        try {
                                                self.getRemoteZipBaseMap();
                                        }
                                        catch (Exception e) {
                                                ExceptionHandler.saveLogFile(e);
                                        }
                                }
                                else {
                                        Toast.makeText(getApplicationContext(), "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
                                        Log.i(self.LOG_TAG, "Sem conexão com a internet.");
                                }
                        }
                });
                
        }
        
        private void createButtonLogout() {
                Button btnLogout = (Button) findViewById(R.id.btnLogout);
                btnLogout.setText(Html.fromHtml("Sair <b>" + name + "</b>"));
                btnLogout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TaskActivity.this);
                                alertDialogBuilder.setTitle(string.caution);
                                alertDialogBuilder.setMessage(string.logoff_message).setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                // Clear the session data This will clear all session data and
                                                // redirect user to LoginActivity
                                                setResult(999);
                                                SessionManager.logoutUser();
                                                finish();
                                        }
                                }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                dialog.cancel();
                                        }
                                });
                                
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                        }
                });
        }
        
        private void createButtonGetTasks() {
                Button btn_get_tasks = (Button) findViewById(R.id.btn_get_tasks);
                btn_get_tasks.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if (Utility.isNetworkAvailable(self)) {
                                        try {
                                                //DatabaseBackup.makeBackup();
                                        }
                                        catch (Exception e) {
                                                Utility.showToast("Ocorreu um problema ao fazer o Backup, comunique a equipe da Inova.", Toast.LENGTH_LONG, TaskActivity.this);
                                                ExceptionHandler.saveLogFile(e);
                                        }
                                        
                                        try {
                                                self.syncronizeWithServer();
                                        }
                                        catch (Exception e) {
                                                self.hideLoadingMask();
                                                ExceptionHandler.saveLogFile(e);
                                        }
                                }
                                else {
                                        Toast.makeText(getApplicationContext(), "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
                                        Log.i(self.LOG_TAG, "Sem conexão com a internet.");
                                }
                        }
                });
                
        }
        
        /**
         * 
         * Update the labels that show to user the count of registers on the
         * local database.
         * 
         */
        public void updateCountLabels() {
                String incompletedTasks = "" + TaskDao.getCountOfIncompletedTasks();
                String completedTasks = "" + TaskDao.getCountOfCompletedTasks();
                
                txtIncompleteTasks.setText(incompletedTasks);
                txtNotSyncRegisters.setText(completedTasks);
        }
        
        /**
         * 
         * Instantiate a service object, that realize AJAX call to persist the
         * local data with the remote database via Rest.
         * 
         * @author Paulo Luan
         * @return List<Task>
         */
        public void getRemoteTasks() {
                String userHash = session.getUserHash();
                String url = Utility.getServerUrl() + Constants.TASKS_REST;
                DownloadTasks remote = new DownloadTasks(userHash, this);
                remote.execute(new String[] { url });
        }
        
        /**
         * Save a list of Tasks, creating an object that send a post request to
         * server.
         * 
         * @author Paulo Luan
         */
        public void syncronizeWithServer() {
                String userHash = session.getUserHash();
                String url = Utility.getServerUrl() + Constants.PHOTOS_REST;
                UploadPhotos remote = new UploadPhotos(userHash, this);
                remote.execute(new String[] { url });
        }
        
        public void showDownloads(View view) {
                Intent i = new Intent();
                i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                startActivity(i);
        }
        
        public void showLoadingMask(String message) {
                mProgressDialog = new ProgressDialog(TaskActivity.this);
                mProgressDialog.setMessage(message);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
        }
        
        public void hideLoadingMask() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                }
        }
        
        public void setLoadMaskMessage(String message) {
                if (mProgressDialog == null || !mProgressDialog.isShowing()) {
                        this.showLoadingMask(message);
                }
                else {
                        mProgressDialog.setMessage(message);
                }
        }
        
        public void onProgressUpdate(String... progress) {
                this.setLoadMaskMessage(progress[0]);
                
                if (progress.length == 2) {
                        mProgressDialog.setProgress(Integer.parseInt(progress[1]));
                }
                
                if (progress.length == 3) {
                        this.hideLoadingMask();
                        
                        mProgressDialog = new ProgressDialog(TaskActivity.this);
                        mProgressDialog.setMessage(progress[0]);
                        mProgressDialog.setIndeterminate(false);
                        mProgressDialog.setMax(Integer.parseInt(progress[2]));
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.show();
                }
        }
        
        /******************************************************************************************************************
         * This function is responsible to request do ServiceBaseMap to get
         * cached tiles zip file from server
         ******************************************************************************************************************/
        public void getRemoteZipBaseMap() {
                String url = Utility.getServerUrl() + Constants.ZIP_REST;
                // String url = "http://200.144.100.34:8080/rest/tiles/zip";
                new BaseMapDownload(this).execute(url);
        }
}
