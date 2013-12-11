package br.org.funcate.mobile.task;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Async class implementation to get tasks from server.
 * 
 * @author Paulo Luan
 * 
 * @param String
 *            ... urls
 *            URL's that will called.
 */
public class DownloadTasks extends AsyncTask<String, String, ArrayList<Task>> {

    private String       userHash = "";
    private TaskActivity taskActivity;

    public DownloadTasks(String userHash, TaskActivity taskActivity) {
        this.userHash = userHash;
        this.taskActivity = taskActivity;
    }

    @Override
    protected ArrayList<Task> doInBackground(String... urls) {
        ArrayList<Task> list = null;

        for (String url : urls) {
            try {
                publishProgress("Fazendo Download das tarefas...");
                ResponseEntity<Task[]> response = taskActivity.restTemplate.getForEntity(url, Task[].class, userHash);
                Task[] tasks = response.getBody();
                list = new ArrayList<Task>(Arrays.asList(tasks));

                publishProgress("Salvando tarefas no banco de dados local...");
                taskActivity.saveTasksIntoLocalSqlite(list);
            } catch (HttpClientErrorException e) {
                String error = e.getResponseBodyAsString();
                e.printStackTrace();
            }
        }

        return list;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        taskActivity.setLoadMaskMessage(values[0]);
    }

    protected void onPostExecute(ArrayList<Task> tasks) {
        taskActivity.hideLoadMask();
        Log.i("#TASKSERVICE", "DoPostExecute!");
    }
}
