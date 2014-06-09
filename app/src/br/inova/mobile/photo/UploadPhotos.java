package br.inova.mobile.photo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.widget.Toast;
import br.inova.mobile.Utility;
import br.inova.mobile.constants.Constants;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.rest.RestTemplateFactory;
import br.inova.mobile.task.TaskActivity;
import br.inova.mobile.task.UploadTasks;

/**
 * Async object implementation to Post Photos to server
 * 
 * @param String
 *                ... urls URL's that will called.
 * @author Paulo Luan
 */
public class UploadPhotos extends AsyncTask<String, String, String> {
        
        private String       userHash;
        private String       message  = null;
        
        private TaskActivity taskActivity;
        
        int                  progress = 0;
        
        public UploadPhotos(String userHash, TaskActivity taskActivity) {
                this.userHash = userHash;
                this.taskActivity = taskActivity;
        }
        
        public void analiseAndSendTask(String url, Integer photoId) {
                try {
                        Photo photo = PhotoDao.getPhotosById(photoId);
                        
                        if (photo.getBase64() == null || photo.getBase64().equals("")) {
                                File photoFile = new File(photo.getPath());
                                
                                if (!photoFile.exists()) {
                                        PhotoDao.deletePhoto(photo); // NÃO TEM O QUE FAZER, FOTO NÃO EXISTE, NÃO TEM COMO TIRAR BASE64. FOTO PERDIDA.
                                }
                                else {
                                        String blob = CreatePhotoAsync.getBytesFromImage(photo.getPath());
                                        photo.setBase64(blob);
                                }
                        }
                        
                        if (photo.getBase64() != null) {
                                Photo[] responsePhotos = new RestTemplateFactory().postForObject(url, new Photo[] { photo }, Photo[].class, userHash);
                                
                                if (responsePhotos != null) {
                                        List<Photo> receivedPhotos = new ArrayList<Photo>(Arrays.asList(responsePhotos));
                                        
                                        if (!receivedPhotos.isEmpty()) {
                                                Photo responsePhoto = receivedPhotos.get(0);
                                                
                                                if (responsePhoto != null) {
                                                        PhotoDao.deletePhoto(photo);
                                                }
                                        }
                                }
                        }
                        
                        progress++;
                        publishProgress("Enviando Imagens...", "" + progress);
                }
                catch (OutOfMemoryError exception) {
                        System.gc();
                        ExceptionHandler.saveLogFile("OutOfMemory ao enviar Foto para o servidor. " + exception.getLocalizedMessage() + exception.getMessage());
                }
                catch (Exception e) {
                        message = "Sincronização efetuada, mas alguns registros não foram enviados.";
                        // String error = e.getResponseBodyAsString();
                        ExceptionHandler.saveLogFile(e);
                }
        }
        
        @Override
        protected String doInBackground(String... urls) {
                PhotoDBAnalyzer.verifyIntegrityOfPictures();
                
                List<Integer> photosIds = PhotoDao.getListOfPhotosIds();
                
                for (String url : urls) {
                        for (Integer photoId : photosIds) {
                                analiseAndSendTask(url, photoId);
                        }
                }
                
                return message;
        }
        
        @Override
        protected void onPostExecute(String message) {
                taskActivity.hideLoadingMask();
                
                if (message != null) {
                        Utility.showToast(message, Toast.LENGTH_LONG, taskActivity);
                }
                
                this.uploadTasks();
        }
        
        @Override
        protected void onPreExecute() {
                super.onPreExecute();
                
                Long countOfRegisters = PhotoDao.getCountOfCompletedPhotos();
                
                if (countOfRegisters != 0) {
                        publishProgress("Enviando Imagens...", "0", "" + countOfRegisters); // set Max Length of progress                                                                                       // dialog                
                }
        }
        
        @Override
        protected void onProgressUpdate(String... progress) {
                taskActivity.onProgressUpdate(progress);
        }
        
        public void uploadTasks() {
                String url = Utility.getServerUrl() + Constants.TASKS_REST;
                UploadTasks remote = new UploadTasks(userHash, taskActivity);
                remote.execute(new String[] { url });
        }
        
}
