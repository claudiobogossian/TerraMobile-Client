package br.inpe.mobile.photo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.widget.Toast;
import br.inpe.mobile.Utility;
import br.inpe.mobile.exception.ExceptionHandler;
import br.inpe.mobile.task.Task;
import br.inpe.mobile.task.TaskActivity;
import br.inpe.mobile.task.TaskDao;
import br.inpe.mobile.task.UploadTasks;

/**
 * Async object implementation to Post Photos to server
 * 
 * @param String
 *                ... urls URL's that will called.
 * @author Paulo Luan
 */
public class UploadPhotos extends AsyncTask<String, String, String> {
        
        private List<Photo>  photos;
        
        private String       userHash;
        
        private TaskActivity taskActivity;
        
        public UploadPhotos(
                            List<Photo> photos,
                            String userHash,
                            TaskActivity taskActivity) {
                this.photos = photos;
                this.userHash = userHash;
                this.taskActivity = taskActivity;
        }
        
        @Override
        protected String doInBackground(String... urls) {
                String message = null;
                
                PhotoDao.verifyIntegrityOfPictures();
                
                for (String url : urls) {
                        try {
                                Photo[] responsePhotos = taskActivity.restTemplate.postForObject(url, this.photos, Photo[].class, userHash);
                                List<Photo> photos = new ArrayList<Photo>(Arrays.asList(responsePhotos));
                                
                                if (photos != null) {
                                        publishProgress("Verificando se existem imagens não utilizadas no aparelho...", "0", "" + photos.size());
                                        
                                        int progress = 0;
                                        
                                        for (Photo photo : photos) {
                                                PhotoDao.deletePhoto(photo);
                                                progress++;
                                                publishProgress("Verificando se existem imagens não utilizadas no aparelho...", "" + progress);
                                        }
                                }
                        }
                        catch (Exception e) {
                                message = "Ocorreu um erro ao enviar as imagens.";
                                // String error = e.getResponseBodyAsString();
                                StringWriter errors = new StringWriter();
                                e.printStackTrace(new PrintWriter(errors));
                                ExceptionHandler.saveLogFile(errors.toString());
                        }
                }
                return message;
        }
        
        public void uploadTasks() {
                List<Task> tasks = TaskDao.getFinishedTasks();
                String url = Utility.hostUrl + "rest/tasks?user={user_hash}";
                UploadTasks remote = new UploadTasks(tasks, userHash, taskActivity);
                remote.execute(new String[] { url });
        }
        
        @Override
        protected void onPreExecute() {
                super.onPreExecute();
                taskActivity.showLoadingMask("Enviando as Fotos, aguarde...");
        }
        
        @Override
        protected void onProgressUpdate(String... progress) {
                super.onProgressUpdate(progress);
                taskActivity.onProgressUpdate(progress);
        }
        
        @Override
        protected void onPostExecute(String message) {
                taskActivity.hideLoadingMask();
                
                if (message != null) {
                        Utility.showToast(message, Toast.LENGTH_LONG, taskActivity);
                }
                else {
                        this.uploadTasks();
                }
        }
        
}
