package br.inova.mobile.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.widget.Toast;
import br.inova.mobile.Utility;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.rest.RestTemplateFactory;

import com.j256.ormlite.dao.CloseableIterator;

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
        
        int                  progress = 0;
        
        public UploadTasks(String userHash, TaskActivity taskActivity) {
                this.userHash = userHash;
                this.taskActivity = taskActivity;
        }
        
        @Override
        protected String doInBackground(String... urls) {
                String message = null;
                
                List<Integer> tasksToRemove = new ArrayList<Integer>();
                
                for (String url : urls) {
                        CloseableIterator<Task> iterator = TaskDao.getIteratorForFinishedTasks();
                        
                        try {
                                while (iterator.hasNext()) {
                                        try {
                                                Task task = (Task) iterator.next();
                                                
                                                Task[] responseTasks = new RestTemplateFactory().postForObject(url, new Task[] { task }, Task[].class, userHash);
                                                
                                                List<Task> receivedTasks = new ArrayList<Task>(Arrays.asList(responseTasks));
                                                
                                                Task responseTask = receivedTasks.get(0);
                                                
                                                if (responseTask != null) {
                                                        tasksToRemove.add(task.getId());
                                                }
                                                
                                                progress++;
                                                publishProgress("Enviando tarefas...", "" + progress);
                                        }
                                        catch (Exception e) {
                                                message = "Sincronização efetuada, mas alguns registros não foram enviados.";
                                                // String error = e.getResponseBodyAsString();
                                                ExceptionHandler.saveLogFile(e);
                                        }
                                }
                        }
                        catch (Exception e) {
                                message = "Ocorreu um erro ao enviar as imagens.";
                                // String error = e.getResponseBodyAsString();
                                ExceptionHandler.saveLogFile(e);
                        }
                        finally {
                                iterator.closeQuietly();
                        }
                }
                
                TaskDao.removeTasksByIds(tasksToRemove);
                
                return message;
        }
        
        @Override
        protected void onPreExecute() {
                super.onPreExecute();
                
                Long countOfRegisters = TaskDao.getCountOfCompletedTasks();
                
                if (countOfRegisters != 0) {
                        publishProgress("Enviando Tarefas...", "0", "" + countOfRegisters); // set Max Length of progress                                                                                       // dialog
                }
        }
        
        @Override
        protected void onProgressUpdate(String... progress) {
                taskActivity.onProgressUpdate(progress);
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
