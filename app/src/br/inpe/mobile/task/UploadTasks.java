package br.inpe.mobile.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import br.inpe.mobile.Utility;
import br.inpe.mobile.exception.ExceptionHandler;

/**
 * Async object implementation to PostTasks to server
 * 
 * @param String
 *            ... urls URL's that will called.
 * @author Paulo Luan
 */
public class UploadTasks extends AsyncTask<String, String, String> {
    
    private List<Task>   tasks;
    
    private String       userHash;
    
    private TaskActivity taskActivity;
    
    public UploadTasks(
                       List<Task> tasks,
                       String userHash,
                       TaskActivity taskActivity) {
        this.tasks = tasks;
        this.userHash = userHash;
        this.taskActivity = taskActivity;
    }
    
    @Override
    protected String doInBackground(String... urls) {
        String message = null;
        
        for (String url : urls) {
            try {
                publishProgress("Enviando o seu trabalho para o servidor...");
                Task[] responseTasks = taskActivity.restTemplate.postForObject(url, this.tasks, Task[].class, userHash);
                List<Task> response = new ArrayList<Task>(Arrays.asList(responseTasks));
                if (response != null) {
                    publishProgress("Excluindo tarefas concluídas...");
                    TaskDao.deleteTasks(tasks);
                }
            }
            catch (Exception e) {
                message = "Erro ao enviar as fotos.";
                //String error = e.getResponseBodyAsString();
                ExceptionHandler.saveLogFile(e.toString());
            }
        }
        
        return message;
    }
    
    @Override
    protected void onPreExecute() {}
    
    @Override
    protected void onProgressUpdate(String... values) {
        taskActivity.setLoadMaskMessage(values[0]);
        Log.i(taskActivity.LOG_TAG, " Progress: " + values);
    }
    
    @Override
    protected void onPostExecute(String message) {
        taskActivity.hideLoadingMask();
        
        if (message != null) {
            Utility.showToast(message, Toast.LENGTH_LONG, taskActivity);
        }
        
        taskActivity.getRemoteTasks();
    }
}
