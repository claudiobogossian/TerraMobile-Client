package br.inpe.mobile.task;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.j256.ormlite.dao.CloseableIterator;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import br.inpe.mobile.Utility;
import br.inpe.mobile.exception.ExceptionHandler;
import br.inpe.mobile.rest.RestTemplateFactory;

/**
 * Async object implementation to PostTasks to server
 * 
 * @param String
 *                ... urls URL's that will called.
 * @author Paulo Luan
 */
public class UploadTasks extends AsyncTask<String, String, String> {
        
        private String       userHash;
        
        private TaskActivity taskActivity;
        
        public UploadTasks(String userHash, TaskActivity taskActivity) {
                this.userHash = userHash;
                this.taskActivity = taskActivity;
        }
        
        @Override
        protected String doInBackground(String... urls) {
                String message = null;
                
                for (String url : urls) {
                        CloseableIterator<Task> iterator = TaskDao.getIteratorForNotFinishedTasks();
                        
                        try {
                                while (iterator.hasNext()) {
                                        Task task = iterator.current();
                                        Task[] responseTasks = new RestTemplateFactory().postForObject(url, new Task[] { task }, Task[].class, userHash);
                                        
                                        List<Task> receivedTasks = new ArrayList<Task>(Arrays.asList(responseTasks));
                                        
                                        Task responseTask = receivedTasks.get(0);
                                        
                                        if (responseTask != null) {
                                                TaskDao.deleteTask(task);
                                        }
                                        
                                        //publishProgress("Enviando tarefas... " + (i + 1) + " de " + tasks.size());
                                }
                        }
                        catch (Exception e) {
                                message = "Ocorreu um erro de conexão ao enviar as tarefas.";
                                // String error = e.getResponseBodyAsString();
                                ExceptionHandler.saveLogFile(e);
                                
                        }
                        finally {
                                iterator.closeQuietly();
                        }
                        
                }
                
                return message;
        }
        
        @Override
        protected void onPreExecute() {
                super.onPreExecute();
                taskActivity.showLoadingMask("Enviando as Tarefas, aguarde...");
        }
        
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
