package br.inova.mobile.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;

import br.inova.mobile.Utility;
import br.inova.mobile.photo.Photo;
import br.inova.mobile.photo.PhotoDao;
import br.inova.mobile.task.Task;
import br.inova.mobile.task.TaskDao;

import com.j256.ormlite.dao.CloseableIterator;

/**
 * @author PauloLuan
 */
public class DatabaseBackup {
        
        /**
         * Creates two array, one of all the completely tasks and other with all
         * photos saved on the database, and saves it on unique json file into
         * the sdcard.
         */
        public static void makeBackup() {
                JSONArray jsonArray = new JSONArray();
                
                //Task task = new Task(123, new Address(123, "name", "number", "extra", new Double(23234342), new Double(23234342), "postalCode", "city", "state", "featureId", "neighborhood"), new User(123, "name", "login", "password", "hash"), new Form(123, new Date(), new Double(12344444), new Double(12344444), "info1", "coordx", "numberConfirmation", "variance", "otherNumbers", "primaryUse", "secondaryUse", "pavimentation", "asphaltGuide", "publicIlumination", "energy", "pluvialGallery"), true);
                //Photo photo = new Photo(123, "base63", "patttth", new Form(123, new Date(), new Double(12344444), new Double(12344444), "info1", "coordx", "numberConfirmation", "variance", "otherNumbers", "primaryUse", "secondaryUse", "pavimentation", "asphaltGuide", "publicIlumination", "energy", "pluvialGallery"));
                
                JSONArray taskJsonArray = createTaskJsonArray();
                JSONArray photoJsonArray = createPhotoJsonArray();
                
                jsonArray.put(taskJsonArray);
                jsonArray.put(photoJsonArray);
                
                File backupFile = createBackupFile();
                String json = jsonArray.toString();
                
                writeTextOnFile(backupFile, json);
        }
        
        /**
         * 
         * Iterate over all register on the database and returns the JSONArray
         * in json format.
         * 
         * @return {@link JSONArray} the array that contains all the pictures.
         * 
         */
        public static JSONArray createTaskJsonArray() {
                JSONArray taskJsonArray = new JSONArray();
                CloseableIterator<Task> taskIterator = TaskDao.getIteratorForAllTasksForCurrentUser();
                
                try {
                        while (taskIterator.hasNext()) {
                                Task task = (Task) taskIterator.next();
                                taskJsonArray.put(task);
                        }
                }
                catch (Exception e) {
                        e.printStackTrace();
                }
                finally {
                        taskIterator.closeQuietly();
                }
                
                return taskJsonArray;
        }
        
        /**
         * 
         * Iterate over all register on the database and returns the JSONArray
         * in json format.
         * 
         * @return {@link JSONArray} the array that contains all the pictures.
         * 
         */
        public static JSONArray createPhotoJsonArray() {
                JSONArray photoJsonArray = new JSONArray();
                CloseableIterator<Photo> photoIterator = PhotoDao.getIteratorForAllPhotos();
                
                try {
                        while (photoIterator.hasNext()) {
                                Photo photo = (Photo) photoIterator.next();
                                photoJsonArray.put(photo);
                        }
                }
                catch (Exception e) {
                        e.printStackTrace();
                }
                finally {
                        photoIterator.closeQuietly();
                }
                
                return photoJsonArray;
        }
        
        /**
         * 
         * Create a file into the sdcard and create the path if it not exists.
         * 
         * @return {@link File} the file on the sdcard that will be saved the
         *         backup.
         * 
         */
        public static File createBackupFile() {
                File path = new File(Utility.getExternalSdCardPath() + "/inova/" + "/dados" + "/backup/");
                
                if(!path.exists()) {
                        path.mkdirs();
                }
                
                SimpleDateFormat simpleDate = new SimpleDateFormat("dd_MM_yyyy HH_mm_ss");
                String stringDate = simpleDate.format(new Date());
                String fileName = "Backup_" + stringDate + ".json";
                
                File backupFile = new File(path, fileName);
                
                if (!backupFile.exists()) {
                        try {
                                backupFile.createNewFile();
                        }
                        catch (IOException e) {
                                e.printStackTrace();
                        }
                }
                
                return backupFile;
        }
        
        /**
         * 
         * Write a text into a file.
         * 
         * @param File
         *                the file that will be write the text
         * 
         * @param Text
         *                The text that will be write on the file.
         * 
         * */
        public static void writeTextOnFile(File file, String text) {
                try {
                        BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
                        buf.append(text);
                        buf.newLine();
                        buf.close();
                }
                catch (IOException e) {
                        e.printStackTrace();
                }
        }
        
}
