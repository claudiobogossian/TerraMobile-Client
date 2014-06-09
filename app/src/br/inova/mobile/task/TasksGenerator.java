package br.inova.mobile.task;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.AsyncTask;
import android.os.Environment;
import br.inova.mobile.Utility;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.form.Form;
import br.inova.mobile.map.LandmarksManager;
import br.inova.mobile.photo.Photo;
import br.inova.mobile.photo.PhotoDao;

import com.j256.ormlite.dao.CloseableIterator;

public class TasksGenerator extends AsyncTask<String, String, String> {
        
        private static void createTask(
                                       Task iterateTask,
                                       Task baseTask,
                                       Photo basePhoto) {
                
                try {
                        iterateTask.setDone(true);
                        
                        Form iterateForm = iterateTask.getForm();
                        Form baseForm = baseTask.getForm();
                        
                        iterateForm.setAsphaltGuide(baseForm.getAsphaltGuide());
                        iterateForm.setDate(new Date());
                        iterateForm.setEnergy(baseForm.getEnergy());
                        iterateForm.setNumberConfirmation(baseForm.getNumberConfirmation());
                        iterateForm.setOtherNumbers(baseForm.getOtherNumbers());
                        iterateForm.setPavimentation(baseForm.getPavimentation());
                        iterateForm.setPluvialGallery(baseForm.getPluvialGallery());
                        iterateForm.setPrimaryUse(baseForm.getPrimaryUse());
                        iterateForm.setPublicIlumination(baseForm.getPublicIlumination());
                        iterateForm.setSecondaryUse(baseForm.getSecondaryUse());
                        iterateForm.setVariance(baseForm.getVariance());
                        
                        Photo replicatedPhoto = new Photo();
                        replicatedPhoto.setBase64(basePhoto.getBase64());
                        replicatedPhoto.setForm(iterateForm);
                        replicatedPhoto.setPath(basePhoto.getPath());
                        
                        String photosPath = "/inova/" + "/dados" + "/fotos/";
                        File mediaStorageDir = new File(Utility.getExternalSdCardPath() + photosPath);
                        
                        if (!mediaStorageDir.exists()) {
                                if (!mediaStorageDir.mkdirs()) {
                                        mediaStorageDir = new File(Environment.getExternalStorageDirectory() + photosPath);
                                        mediaStorageDir.mkdirs();
                                }
                        }
                        
                        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + System.currentTimeMillis() % 1000;
                        String outputFileName = File.separator + "IMG_" + timeStamp + ".jpg";
                        
                        Utility.copyFile(replicatedPhoto.getPath(), mediaStorageDir.getPath(), outputFileName);
                        replicatedPhoto.setPath(mediaStorageDir + outputFileName);
                        
                        PhotoDao.savePhoto(replicatedPhoto);
                        TaskDao.updateTask(iterateTask);
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                
        }
        
        private static Photo getBasePhoto() {
                CloseableIterator<Photo> photoIterator = new PhotoDao().getIteratorForNotSyncPhotos();
                Photo basePhoto = null;
                
                try {
                        while (photoIterator.hasNext()) {
                                basePhoto = (Photo) photoIterator.next();
                                break;
                        }
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                finally {
                        photoIterator.closeQuietly();
                }
                
                return basePhoto;
        }
        
        private static Task getBaseTask() {
                CloseableIterator<Task> taskIterator = TaskDao.getIteratorForFinishedTasks();
                Task baseTask = null;
                
                try {
                        while (taskIterator.hasNext()) {
                                baseTask = (Task) taskIterator.next();
                                break;
                        }
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                finally {
                        taskIterator.closeQuietly();
                }
                
                return baseTask;
        }
        
        private static void verifyRegisters() {
                CloseableIterator<Task> iterator = TaskDao.getIteratorForAllTasksForCurrentUser();
                
                try {
                        while (iterator.hasNext()) {
                                Task task = (Task) iterator.next();
                                
                                if (!task.isDone()) {
                                        task.isDone();
                                }
                        }
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                finally {
                        iterator.closeQuietly();
                }
        }
        
        private TaskActivity taskActivity;
        
        public TasksGenerator(TaskActivity taskActivity) {
                this.taskActivity = taskActivity;
                this.execute();
        }
        
        @Override
        protected String doInBackground(String... arg0) {
                CloseableIterator<Task> taskIterator = TaskDao.getIteratorForUnfinishedTasks();
                
                publishProgress("Criando Tarefas... ", "0", "" + TaskDao.getCountOfIncompletedTasks()); // set Max Length of progress                                                                                       // dialog
                int progress = 0;
                
                try {
                        Task baseTask = getBaseTask();
                        Photo basePhoto = getBasePhoto();
                        
                        if (baseTask != null && basePhoto != null) {
                                while (taskIterator.hasNext()) {
                                        Task iterateTask = (Task) taskIterator.next();
                                        createTask(iterateTask, baseTask, basePhoto);
                                        
                                        progress++;
                                        publishProgress("Criando Tarefas... ", "" + progress);
                                }
                        }
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                finally {
                        taskIterator.closeQuietly();
                }
                return null;
        }
        
        @Override
        protected void onPostExecute(String result) {
                LandmarksManager.createPoiMarkers();
                taskActivity.updateCountLabels();
                taskActivity.hideLoadingMask();
        }
        
        @Override
        protected void onPreExecute() {
                taskActivity.showLoadingMask("Criando registros de testes...");
        }
        
        @Override
        protected void onProgressUpdate(String... progress) {
                taskActivity.onProgressUpdate(progress);
        }
}
