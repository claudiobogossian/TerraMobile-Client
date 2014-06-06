package br.inova.mobile.rest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import br.inova.mobile.Utility;
import br.inova.mobile.constants.Constants;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.form.Form;
import br.inova.mobile.photo.CreatePhotoAsync;
import br.inova.mobile.photo.Photo;
import br.inova.mobile.photo.PhotoDBAnalyzer;
import br.inova.mobile.photo.PhotoDao;
import br.inova.mobile.task.Task;
import br.inova.mobile.task.TaskActivity;
import br.inova.mobile.task.TaskDao;
import br.inova.mobile.user.SessionManager;

public class SyncDataWithServer extends AsyncTask<String, String, String> {
        private String             userHash;
        
        private TaskActivity       taskActivity;
        
        private String             taskServerUrl  = Constants.getTasksUrl();
        private String             photoServerUrl = Constants.getPhotosUrl();
        
        int                        progress       = 0;
        
        private SyncDataWithServer self;
        
        private List<Long>         threads        = new ArrayList<Long>();
        
        private static long        timeStart;
        private static long        amountOfRegisters;
        
        private static String      datetimeBegin;
        private static String      datetimeEnd;
        
        public SyncDataWithServer(TaskActivity taskActivity) {
                this.userHash = SessionManager.getInstance().getUserHash();
                this.taskActivity = taskActivity;
                
                self = this;
                this.execute();
        }
        
        @Override
        protected String doInBackground(String... params) {
                PhotoDBAnalyzer.verifyIntegrityOfPictures();
                
                timeStart = System.currentTimeMillis();
                datetimeBegin = new SimpleDateFormat("HH:mm:ss").format(new Date());
                
                syncronizeDataWithServer();
                return null;
        }
        
        public void syncronizeDataWithServer() {
                List<Integer> tasksIds = TaskDao.getListOfTasksIds();
                List<List<Integer>> slicedTasks = spliceArrayIntoSubArrays(tasksIds, 50);
                
                amountOfRegisters = tasksIds.size();
                
                for (int i = 0; i < slicedTasks.size(); i++) {
                        iterateAndSendTasks(slicedTasks.get(i));
                }
                
                if (tasksIds.size() == 0) {
                        onFinishedWork(null);
                }
        }
        
        /**
         * Make the http operations in a separates thread.
         * 
         * @param tasksIds
         */
        public void iterateAndSendTasks(final List<Integer> tasksIds) {
                Thread thread = new Thread() {
                        @Override
                        public void run() {
                                for (Integer taskId : tasksIds) {
                                        Task task = TaskDao.getTaskById(taskId);
                                        Form form = task.getForm();
                                        List<Photo> photos = PhotoDao.getPhotosByForm(form);
                                        
                                        Log.d("THREAD", "Task: " + task.getId() + "Enviando pela THREAD: " + this.getName() + this.getId());
                                        
                                        sendData(task, photos);
                                }
                                
                                finishThread(this);
                        }
                };
                
                registerThread(thread);
                
                thread.start();
        }
        
        public void finishThread(Thread thread) {
                
                if (thread != null) {
                        Long threadId = thread.getId();
                        int index = threads.indexOf(threadId);
                        
                        if (index != -1) {
                                threads.remove(index);
                        }
                        
                        thread = null; // remove the thread object from memory.
                        
                        if (threads.isEmpty()) {
                                onFinishedWork(null);
                        }
                }
        }
        
        public void registerThread(Thread thread) {
                threads.add(thread.getId());
        }
        
        public void sendData(Task task, List<Photo> photos) {
                Boolean isSaved = false;
                
                /** One form cold get some pictures. */
                for (Photo photo : photos) {
                        isSaved = sendPhoto(photo);
                }
                
                /** Only send the task if the photo was saved on remote server **/
                if (isSaved || photos.isEmpty()) {
                        sendTask(task);
                }
                
                increaseProgress();
        }
        
        /**
         * Send a task to server and remove it from local database on success.
         * 
         * @param task
         *                the task that will be sync with the server
         * @return isSaved boolean that represents wether the photo was saved or
         *         not.
         */
        private boolean sendTask(Task task) {
                boolean isSaved = false;
                
                try {
                        /*
                         * ObjectMapper mapper = new ObjectMapper(); String
                         * photosJson = mapper.writeValueAsString(new Task[] {
                         * task });
                         * 
                         * isSaved = HttpClient.doPost(photoServerUrl,
                         * photosJson);
                         * 
                         * if (isSaved) { TaskDao.deleteTask(task); isSaved =
                         * true; }
                         */
                        
                        Task[] responseTasks = new RestTemplateFactory().postForObject(taskServerUrl, new Task[] { task }, Task[].class, userHash);
                        
                        List<Task> receivedTasks = new ArrayList<Task>(Arrays.asList(responseTasks));
                        Task responseTask = receivedTasks.get(0);
                        
                        if (responseTask != null) {
                                Log.i("SyncDataWithServer", "Task enviada com sucesso. " + task.getId());
                                removeTask(task);
                                isSaved = true;
                        }
                        
                        if (!isSaved) {
                                Log.i("SyncDataWithServer", "Erro ao enviar a Foto. " + task.getId());
                        }
                }
                catch (Exception e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return isSaved;
        }
        
        /**
         * 
         * Send a photo to server and remove it from local database on success.
         * 
         * @param photo
         *                the photo that will be sync with the server
         * @return isSaved boolean that represents wether the photo was saved or
         *         not.
         */
        private boolean sendPhoto(Photo photo) {
                boolean isSaved = false;
                
                try {
                        if (photo.getBase64() == null || photo.getBase64().equals("")) {
                                File photoFile = new File(photo.getPath());
                                
                                if (!photoFile.exists()) {
                                        removePhoto(photo); // NÃO TEM O QUE FAZER, FOTO NÃO EXISTE, NÃO TEM COMO TIRAR BASE64. FOTO PERDIDA.
                                }
                                else {
                                        String blob = CreatePhotoAsync.getBytesFromImage(photo.getPath());
                                        photo.setBase64(blob);
                                }
                        }
                        
                        if (photo.getBase64() != null) {
                                
                                /*
                                 * //ObjectMapper mapper = new ObjectMapper();
                                 * //String photosJson =
                                 * mapper.writeValueAsString(new Photo[] { photo
                                 * });
                                 * 
                                 * Gson gson = new Gson(); String photosJson =
                                 * gson.toJson(new Photo[] { photo });
                                 * 
                                 * isSaved = HttpClient.doPost(photoServerUrl,
                                 * photosJson);
                                 * 
                                 * if (isSaved) { PhotoDao.deletePhoto(photo);
                                 * isSaved = true; }
                                 */
                                
                                Photo[] responsePhotos = new RestTemplateFactory().postForObject(photoServerUrl, new Photo[] { photo }, Photo[].class, userHash);
                                
                                if (responsePhotos != null) {
                                        List<Photo> receivedPhotos = new ArrayList<Photo>(Arrays.asList(responsePhotos));
                                        
                                        if (!receivedPhotos.isEmpty()) {
                                                Photo responsePhoto = receivedPhotos.get(0);
                                                
                                                if (responsePhoto != null) {
                                                        Log.i("SyncDataWithServer", "Foto enviada com sucesso. " + photo.getId());
                                                        removePhoto(photo);
                                                        isSaved = true;
                                                }
                                        }
                                }
                                
                                if (!isSaved) {
                                        Log.i("SyncDataWithServer", "Erro ao enviar a Foto. " + photo.getId());
                                }
                        }
                }
                catch (OutOfMemoryError exception) {
                        System.gc();
                        ExceptionHandler.saveLogFile("OutOfMemory ao enviar Foto para o servidor. " + exception.getLocalizedMessage() + exception.getMessage() + " ID da Foto: " + photo.getId());
                }
                catch (Exception e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return isSaved;
        }
        
        public synchronized void removeTask(Task task) {
                TaskDao.deleteTask(task);
        }
        
        public synchronized void removePhoto(Photo photo) {
                PhotoDao.deletePhoto(photo);
        }
        
        public synchronized void increaseProgress() {
                progress++;
                publishProgress("Sincronizando...", "" + progress);
        }
        
        /**
         * 
         * Splice one array into N new arrays.
         * 
         * @param inputList
         *                the original list the wants to part.
         * @param slices
         *                the number of slices that the array will be parted.
         * @return partitions The ArrayList with sub arrays.
         */
        public static List<List<Integer>> spliceArrayIntoSubArrays(
                                                                   List<Integer> inputList,
                                                                   int slices) {
                List<List<Integer>> partitions = new LinkedList<List<Integer>>();
                
                int partitionSize = inputList.size() / slices;
                
                if (partitionSize == 0) {
                        partitionSize = inputList.size();
                }
                
                for (int i = 0; i < inputList.size(); i += partitionSize) {
                        partitions.add(inputList.subList(i, i + Math.min(partitionSize, inputList.size() - i)));
                }
                
                return partitions;
        }
        
        @Override
        protected void onPreExecute() {
                super.onPreExecute();
                
                Long countOfRegisters = TaskDao.getCountOfCompletedTasks();
                
                if (countOfRegisters != 0) {
                        publishProgress("Sincronizando...", "0", "" + countOfRegisters); // set Max Length of progress                                                                                       // dialog
                }
        }
        
        @Override
        protected void onProgressUpdate(String... progress) {
                taskActivity.onProgressUpdate(progress);
        }
        
        protected void onFinishedWork(final String message) {
                
                long timeEnd = System.currentTimeMillis();
                long timeDelta = timeEnd - timeStart;
                double elapsedSeconds = timeDelta / 1000.0;
                
                datetimeEnd = new SimpleDateFormat("HH:mm:ss").format(new Date());
                
                Log.e("", "###########################################");
                Log.d("TEMPO: ", "Início: " + datetimeBegin + "  Fim: " + datetimeEnd);
                Log.d("TEMPO DA SINCRONIZAÇÃO: ", "AMOUNT: " + amountOfRegisters + "  TEMPO DA SYNC: " + elapsedSeconds);
                Log.e("", "###########################################");
                
                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(taskActivity.getMainLooper());
                
                Runnable mainThreadHandler = new Runnable() {
                        @Override
                        public void run() {
                                taskActivity.hideLoadingMask();
                                
                                if (message != null) {
                                        Utility.showToast(message, Toast.LENGTH_LONG, taskActivity);
                                }
                                
                                taskActivity.getRemoteTasks();
                                self = null;
                        }
                };
                mainHandler.post(mainThreadHandler);
        }
        
}
