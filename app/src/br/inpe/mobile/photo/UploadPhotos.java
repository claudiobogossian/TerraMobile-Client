package br.inpe.mobile.photo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.widget.Toast;
import br.inpe.mobile.Utility;
import br.inpe.mobile.constants.Constants;
import br.inpe.mobile.exception.ExceptionHandler;
import br.inpe.mobile.rest.RestTemplateFactory;
import br.inpe.mobile.task.TaskActivity;
import br.inpe.mobile.task.UploadTasks;

import com.j256.ormlite.dao.CloseableIterator;

/**
 * Async object implementation to Post Photos to server
 * 
 * @param String
 *                ... urls URL's that will called.
 * @author Paulo Luan
 */
public class UploadPhotos extends AsyncTask<String, String, String> {
        
        private String       userHash;
        
        private TaskActivity taskActivity;
        
        int progress = 0;
        
        public UploadPhotos(
                            String userHash,
                            TaskActivity taskActivity) {
                this.userHash = userHash;
                this.taskActivity = taskActivity;
        }
        
        @Override
        protected String doInBackground(String... urls) {
                String message = null;
                
                PhotoDao.verifyIntegrityOfPictures();
                
                for (String url : urls) {
                        CloseableIterator<Photo> iterator = PhotoDao.getIteratorForNotSyncPhotos();
                        
                        try {
                                while (iterator.hasNext()) {
                                        try {
                                                Photo photo = (Photo) iterator.next();
                                                
                                                Photo[] responsePhotos = new RestTemplateFactory().postForObject(url, new Photo[] { photo }, Photo[].class, userHash);
                                                List<Photo> receivedPhotos = new ArrayList<Photo>(Arrays.asList(responsePhotos));
                                                
                                                Photo responsePhoto = receivedPhotos.get(0);
                                                
                                                if (responsePhoto != null) {
                                                        PhotoDao.deletePhoto(responsePhoto);
                                                }
                                                
                                                progress ++;
                                                publishProgress("Enviando Imagens...", "" + progress);
                                        }
                                        catch (Exception e) {
                                                message = "Sincronização efetuada, mas alguns registros não foram enviados.";
                                                // String error = e.getResponseBodyAsString();
                                                ExceptionHandler.saveLogFile(e);
                                        }
                                }   
                        } catch (Exception e) {
                                message = "Ocorreu um erro ao enviar as imagens.";
                                // String error = e.getResponseBodyAsString();
                                ExceptionHandler.saveLogFile(e);
                        }
                        finally {
                                iterator.closeQuietly();
                        } 
                }
                return message;
        }
        
        public void uploadTasks() {
                String url = Utility.getServerUrl() + Constants.TASKS_REST;
                UploadTasks remote = new UploadTasks(userHash, taskActivity);
                remote.execute(new String[] { url });
        }
        
        @Override
        protected void onPreExecute() {
                super.onPreExecute();

                Long countOfRegisters = PhotoDao.getCountOfCompletedPhotos();       
                publishProgress("Enviando Fotos...", "0", "" + countOfRegisters); // set Max Length of progress                                                                                       // dialog
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
                
                this.uploadTasks();
        }
        
}
