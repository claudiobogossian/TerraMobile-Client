package br.inpe.mobile.photo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import br.inpe.mobile.Utility;
import br.inpe.mobile.constants.Constants;
import br.inpe.mobile.exception.ExceptionHandler;
import br.inpe.mobile.rest.RestTemplateFactory;
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
                        for (int i = 0; i < this.photos.size(); i++) {
                                try {
                                        Photo photo = photos.get(i);
                                        
                                        Photo[] responsePhotos = new RestTemplateFactory().postForObject(url, new Photo[] { photo }, Photo[].class, userHash);
                                        List<Photo> receivedPhotos = new ArrayList<Photo>(Arrays.asList(responsePhotos));
                                        
                                        Photo responsePhoto = receivedPhotos.get(0);
                                        
                                        if (responsePhoto != null) {
                                                PhotoDao.deletePhoto(responsePhoto);
                                        }
                                        
                                        publishProgress("Enviando imagens... " + (i + 1) + " de " + photos.size());
                                        Log.d("Imagem Enviada!!", "ID: " + responsePhoto.toString());
                                }
                                catch (Exception e) {
                                        message = "Ocorreu um erro ao enviar as imagens.";
                                        // String error = e.getResponseBodyAsString();
                                        ExceptionHandler.saveLogFile(e);
                                }
                        }
                }
                return message;
        }
        
        public void uploadTasks() {
                List<Task> tasks = TaskDao.getFinishedTasks();
                String url = Utility.getServerUrl() + Constants.TASKS_REST;
                UploadTasks remote = new UploadTasks(tasks, userHash, taskActivity);
                remote.execute(new String[] { url });
        }
        
        @Override
        protected void onPreExecute() {
                super.onPreExecute();
                taskActivity.showLoadingMask("Enviando as Fotos, aguarde...");
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
                
                this.uploadTasks();
        }
        
}
