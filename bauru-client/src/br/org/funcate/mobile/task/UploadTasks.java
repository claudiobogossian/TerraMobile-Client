package br.org.funcate.mobile.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.client.HttpClientErrorException;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Async object implementation to PostTasks to server
 * 
 * @param String
 *            ... urls
 *            URL's that will called.
 * @author Paulo Luan
 */
public class UploadTasks extends AsyncTask<String, String, List<Task>> {

    private List<Task>   tasks;
    private String       userHash;
    private TaskActivity taskActivity;

    public UploadTasks(List<Task> tasks, String userHash, TaskActivity taskActivity) {
        this.tasks = tasks;
        this.userHash = userHash;
        this.taskActivity = taskActivity;
    }

    @Override
    protected List<Task> doInBackground(String... urls) {
        List<Task> response = null;

        for (String url : urls) {
            try {
                publishProgress("Enviando o seu trabalho para o servidor...");
                Task[] responseTasks = taskActivity.restTemplate.postForObject(url, this.tasks, Task[].class, userHash);
                response = new ArrayList<Task>(Arrays.asList(responseTasks));
                if (response != null) {
                    publishProgress("Excluindo tarefas concluídas...");
                    TaskDao.deleteTasks(tasks);
                }
            } catch (HttpClientErrorException e) {
                String error = e.getResponseBodyAsString();
                e.printStackTrace();
            }
        }

        return response;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(String... values) {
        taskActivity.setLoadMaskMessage(values[0]);
        Log.i(taskActivity.LOG_TAG, " Progress: " + values);
    }

    @Override
    protected void onPostExecute(List<Task> result) {
        taskActivity.getRemoteTasks();
    }
}
