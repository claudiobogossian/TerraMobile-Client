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
public class DownloadTasks extends AsyncTask<String, String, ArrayList<Task>> {

    private String       userHash = "";
    private TaskActivity taskActivity;

    public DownloadTasks(String userHash, TaskActivity taskActivity) {
        this.userHash = userHash;
        this.taskActivity = taskActivity;
    }

    @Override
    protected ArrayList<Task> doInBackground(String... urls) {
        ArrayList<Task> tasks = null;

        for (String url : urls) {
            try {
                publishProgress("Fazendo Download das tarefas...");
                ResponseEntity<Task[]> response = taskActivity.restTemplate.getForEntity(url, Task[].class, userHash);
                Task[] responseTasks = response.getBody();
                tasks = new ArrayList<Task>(Arrays.asList(responseTasks));

                publishProgress("Salvando tarefas no banco de dados local...", "0", "" + tasks.size()); // set Max Length of progress dialog

                int progress = 0;

                for (Task task : tasks) {
                    TaskDao.saveTask(task);
                    progress++;
                    publishProgress("Salvando tarefas no banco de dados local...", "" + progress);
                }

                taskActivity.saveTasksIntoLocalSqlite(tasks);
            } catch (HttpClientErrorException e) {
                Utility.showToast("Ocorreu um erro ao baixar as atividades.", Toast.LENGTH_LONG, taskActivity);
                String error = e.getResponseBodyAsString();
                e.printStackTrace();
            }
        }

        return tasks;
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
    protected void onPostExecute(ArrayList<Task> result) {
        taskActivity.updateCountLabels();
        taskActivity.hideLoadingMask();
    }

}
