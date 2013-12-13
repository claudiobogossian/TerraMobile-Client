package br.org.funcate.mobile.task;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import android.os.AsyncTask;
import android.widget.Toast;
import br.org.funcate.mobile.Utility;

/**
 * Async class implementation to get tasks from server.
 * 
 * @author Paulo Luan
 * 
 * @param String
 *            ... urls
 *            URL's that will called.
 */
public class DownloadTasks extends AsyncTask<String, String, String> {

    private String       userHash = "";
    private TaskActivity taskActivity;

    public DownloadTasks(String userHash, TaskActivity taskActivity) {
        this.userHash = userHash;
        this.taskActivity = taskActivity;
    }

    @Override
    protected String doInBackground(String... urls) {
         String message = null;

        for (String url : urls) {
            try {
                publishProgress("Fazendo Download das tarefas...");
                ResponseEntity<Task[]> response = taskActivity.restTemplate.getForEntity(url, Task[].class, userHash);
                Task[] responseTasks = response.getBody();
                ArrayList<Task> tasks = new ArrayList<Task>(Arrays.asList(responseTasks));

                publishProgress("Salvando tarefas no banco de dados local...", "0", "" + tasks.size()); // set Max Length of progress dialog

                int progress = 0;

                for (Task task : tasks) {
                    TaskDao.saveTask(task);
                    progress++;
                    publishProgress("Salvando tarefas no banco de dados local...", "" + progress);
                }

                taskActivity.saveTasksIntoLocalSqlite(tasks);
            } catch (HttpClientErrorException e) {
                message = "Ocorreu um erro ao fazer o download das tarefas.";
                //String error = e.getResponseBodyAsString();
                e.printStackTrace();
            }
        }

        return message;
    }

    @Override
    protected void onPreExecute() {
        taskActivity.showLoadingMask("Carregando, aguarde...");
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        taskActivity.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(String message) {
        if (message == null) {
            Utility.showToast("Ocorreu um erro ao baixar as atividades.", Toast.LENGTH_LONG, taskActivity);
        }

        taskActivity.updateCountLabels();
        taskActivity.hideLoadingMask();
    }

}
