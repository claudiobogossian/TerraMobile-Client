package br.inova.mobile.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.AsyncTask;
import android.os.Environment;
import br.inova.mobile.Utility;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.task.TaskActivity;

/**
 * @author PauloLuan
 */
public class DatabaseBackup extends AsyncTask<String, String, String> {
        
        private TaskActivity taskActivity;
        
        public DatabaseBackup(TaskActivity taskActivity) {
                this.taskActivity = taskActivity;
                this.execute();
        }
        
        @Override
        protected String doInBackground(String... arg0) {
                makeSqliteBackupToSdCard(taskActivity);
                return null;
        }
        
        @Override
        protected void onPreExecute() {
                taskActivity.showLoadingMask("Fazendo Backup do banco de dados.");
        }
        
        @Override
        protected void onPostExecute(String result) {
                taskActivity.hideLoadingMask();
        }
        
        /**
         * 
         * Make a copy of the tasks database into the sdcard.
         * 
         * */
        private static void makeSqliteBackupToSdCard(TaskActivity taskActivity) {
                
                try {
                        String backupPath = "/inova/dados/backup/";
                        
                        File sd = new File(Utility.getExternalSdCardPath() + backupPath);
                        File data = Environment.getDataDirectory();
                        
                        if (!sd.exists()) {
                                sd.mkdirs();
                        }
                        
                        if (sd.canWrite()) {
                                String currentDBPath = "//data//br.inpe.mobile//databases//tasks.db";
                                
                                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                                String backupDBPath = "DB_" + timeStamp + ".db";
                                
                                File currentDB = new File(data, currentDBPath);
                                File backupDB = new File(sd, backupDBPath);
                                
                                if (currentDB.exists()) {
                                        FileChannel inputDatabaseStream = new FileInputStream(currentDB).getChannel();
                                        FileChannel outputDatabaseStream = new FileOutputStream(backupDB).getChannel();
                                        outputDatabaseStream.transferFrom(inputDatabaseStream, 0, inputDatabaseStream.size());
                                        inputDatabaseStream.close();
                                        outputDatabaseStream.close();
                                }
                        }
                }
                catch (Exception exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
        }
        
        /**
         * Creates two array, one of all the completely tasks and other with all
         * photos saved on the database, and saves it on unique json file into
         * the sdcard.
         * 
         * public static void makeBackup() { JSONArray jsonArray = new
         * JSONArray();
         * 
         * //Task task = new Task(123, new Address(123, "name", "number",
         * "extra", new Double(23234342), new Double(23234342), "postalCode",
         * "city", "state", "featureId", "neighborhood"), new User(123, "name",
         * "login", "password", "hash"), new Form(123, new Date(), new
         * Double(12344444), new Double(12344444), "info1", "coordx",
         * "numberConfirmation", "variance", "otherNumbers", "primaryUse",
         * "secondaryUse", "pavimentation", "asphaltGuide", "publicIlumination",
         * "energy", "pluvialGallery"), true); //Photo photo = new Photo(123,
         * "base63", "patttth", new Form(123, new Date(), new Double(12344444),
         * new Double(12344444), "info1", "coordx", "numberConfirmation",
         * "variance", "otherNumbers", "primaryUse", "secondaryUse",
         * "pavimentation", "asphaltGuide", "publicIlumination", "energy",
         * "pluvialGallery"));
         * 
         * JSONArray taskJsonArray = createTaskJsonArray(); JSONArray
         * photoJsonArray = createPhotoJsonArray();
         * 
         * jsonArray.put(taskJsonArray); jsonArray.put(photoJsonArray);
         * 
         * File backupFile = createBackupFile(); String json =
         * jsonArray.toString();
         * 
         * writeTextOnFile(backupFile, json); }
         * 
         * /**
         * 
         * Iterate over all register on the database and returns the JSONArray
         * in json format.
         * 
         * @return {@link JSONArray} the array that contains all the pictures.
         * 
         * 
         *         public static JSONArray createTaskJsonArray() { JSONArray
         *         taskJsonArray = new JSONArray(); CloseableIterator<Task>
         *         taskIterator =
         *         TaskDao.getIteratorForAllTasksForCurrentUser();
         * 
         *         try { while (taskIterator.hasNext()) { Task task = (Task)
         *         taskIterator.next(); taskJsonArray.put(task); } } catch
         *         (Exception e) { e.printStackTrace(); } finally {
         *         taskIterator.closeQuietly(); }
         * 
         *         return taskJsonArray; }
         * 
         *         /**
         * 
         *         Iterate over all register on the database and returns the
         *         JSONArray in json format.
         * 
         * @return {@link JSONArray} the array that contains all the pictures.
         * 
         * 
         *         public static JSONArray createPhotoJsonArray() { JSONArray
         *         photoJsonArray = new JSONArray(); CloseableIterator<Photo>
         *         photoIterator = new PhotoDao().getIteratorForNotSyncPhotos();
         * 
         *         try { while (photoIterator.hasNext()) { Photo photo = (Photo)
         *         photoIterator.next(); photoJsonArray.put(photo); } } catch
         *         (Exception e) { e.printStackTrace(); } finally {
         *         photoIterator.closeQuietly(); }
         * 
         *         return photoJsonArray; }
         * 
         *         /**
         * 
         *         Create a file into the sdcard and create the path if it not
         *         exists.
         * 
         * @return {@link File} the file on the sdcard that will be saved the
         *         backup.
         * 
         * 
         *         public static File createBackupFile() { File path = new
         *         File(Utility.getExternalSdCardPath() + "/inova/" + "/dados" +
         *         "/backup/");
         * 
         *         if (!path.exists()) { path.mkdirs(); }
         * 
         *         SimpleDateFormat simpleDate = new
         *         SimpleDateFormat("dd_MM_yyyy HH_mm_ss"); String stringDate =
         *         simpleDate.format(new Date()); String fileName = "Backup_" +
         *         stringDate + ".json";
         * 
         *         File backupFile = new File(path, fileName);
         * 
         *         if (!backupFile.exists()) { try { backupFile.createNewFile();
         *         } catch (IOException e) { e.printStackTrace(); } }
         * 
         *         return backupFile; }
         * 
         *         /**
         * 
         *         Write a text into a file.
         * 
         * @param File
         *                the file that will be write the text
         * 
         * @param Text
         *                The text that will be write on the file.
         * 
         *                * public static void writeTextOnFile(File file, String
         *                text) { try { BufferedWriter buf = new
         *                BufferedWriter(new FileWriter(file, true));
         *                buf.append(text); buf.newLine(); buf.close(); } catch
         *                (IOException e) { e.printStackTrace(); } }
         */
        
}
