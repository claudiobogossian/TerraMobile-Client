package br.org.funcate.mobile.task;

import java.util.List;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.Utility;
import br.org.funcate.mobile.user.SessionManager;

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
    //private final String   hostUrl = "http://200.144.100.34:8080/";

    private TextView       txtIncompleteTasks;
    private TextView       txtNotSyncRegisters;

    private ProgressDialog mProgressDialog;
    private TaskActivity   self    = this;

    public RestTemplate    restTemplate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_task);

        txtIncompleteTasks = (TextView) findViewById(R.id.txt_count_incompleted_tasks);
        txtNotSyncRegisters = (TextView) findViewById(R.id.txt_count_completed_tasks);

        this.setButtonsListeners();
        this.initializeRestTemplate();
        this.updateCountLabels();
    }

    public void setButtonsListeners() {
        Button btn_get_tasks = (Button) findViewById(R.id.btn_get_tasks);
        btn_get_tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.isNetworkAvailable(self)) {
                    try {
                        self.syncronizeWithServer();
                    } catch (Exception e) {
                        self.hideLoadingMask();
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
                    Log.i(self.LOG_TAG, "Sem conexão com a internet.");
                }
            }
        });

        String name = SessionManager.getUserName();
        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setText(Html.fromHtml("Sair <b>" + name + "</b>"));
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Clear the session data This will clear all session data and redirect user to LoginActivity
                setResult(999);
                SessionManager.logoutUser();
                finish();
            }
        });

        Button btn_get_tiles = (Button) findViewById(R.id.btn_get_tiles);
        btn_get_tiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.isNetworkAvailable(self)) {
                    try {
                        self.getRemoteZipBaseMap();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Sem conexão com a internet.", Toast.LENGTH_LONG).show();
                    Log.i(self.LOG_TAG, "Sem conexão com a internet.");
                }
            }
        });
    }

    public void initializeRestTemplate() {
        this.restTemplate = new RestTemplate();

        // Add converters, Note I use the Jackson Converter, I removed the http form converter  because it is not needed when posting String, used for multipart forms.
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        // Set the request factory IMPORTANT: This section I had to add for POST request. Not needed for GET
        this.restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    /**
     * 
     * Update the labels that show to user the count of registers on the local
     * database.
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
     * Instantiate a service object, that realize AJAX call to persist the local
     * data with the remote database via Rest.
     * 
     * @author Paulo Luan
     * @return List<Task>
     */
    public void getRemoteTasks() {
        String userHash = SessionManager.getUserHash();
        String url = Utility.hostUrl + "bauru-server/rest/tasks?user={user_hash}";
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
        String userHash = SessionManager.getUserHash();
        List<Task> tasks = TaskDao.getFinishedTasks();

        if (tasks != null && !tasks.isEmpty()) {
            //String url = "http://200.144.100.34:8080/bauru-server/rest/tasks?user={user_hash}";
            String url = Utility.hostUrl + "bauru-server/rest/tasks?user={user_hash}";
            UploadTasks remote = new UploadTasks(tasks, userHash, this);
            remote.execute(new String[] { url });
        }
        else {
            self.getRemoteTasks();
        }
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
    }

    public void onProgressUpdate(String... progress) {
        mProgressDialog.setMessage(progress[0]);

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
     * This function is responsible to request do ServiceBaseMap to get cached
     * tiles zip file from server
     ******************************************************************************************************************/
    public void getRemoteZipBaseMap() {
        String url = Utility.hostUrl + "bauru-server/rest/tiles/zip";
        //String url = "http://200.144.100.34:8080/bauru-server/rest/tiles/zip";
        new DownloadZipAsync(this).execute(url);
    }
}
