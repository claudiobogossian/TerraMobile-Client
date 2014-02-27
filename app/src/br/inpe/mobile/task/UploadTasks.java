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
 *                ... urls URL's that will called.
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
                        
                        for (int i = 0; i < this.tasks.size(); i++) {
                                
                                try {
                                        Task task = tasks.get(i);
                                        
                                        Task[] responseTasks = taskActivity.restTemplate.postForObject(url, new Task[] { task }, Task[].class, userHash);
                                        List<Task> receivedTasks = new ArrayList<Task>(Arrays.asList(responseTasks));
                                        
                                        Task responseTask = receivedTasks.get(0);
                                        
                                        if (responseTask != null) {
                                                TaskDao.deleteTask(task);
                                        }
                                        
                                        int currenValue = i + 1;
                                        publishProgress("Enviando tarefas... " + currenValue + " de " + tasks.size());
                                }
                                catch (Exception e) {
                                        message = "Ocorreu um erro de conexão ao enviar as imagens.";
                                        // String error = e.getResponseBodyAsString();
                                        ExceptionHandler.saveLogFile(e);
                                }
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
