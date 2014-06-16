package br.inova.mobile.photo;

import java.io.File;
import java.sql.SQLException;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import br.inova.mobile.Utility;
import br.inova.mobile.database.DatabaseAdapter;
import br.inova.mobile.database.DatabaseHelper;
import br.inova.mobile.exception.ExceptionHandler;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class PhotoDBAnalyzer extends AsyncTask<String, String, String> {
        
        private static final String    LOG_TAG = "PhotoDBAnalyzer";
        private static DatabaseAdapter db      = DatabaseHelper.getDatabase();
        
        /**
         * Returns an the count of all the pictures from database.
         * 
         * @return the count of the database photo registers
         * @author PauloLuan
         * */
        public static long getCountPhotos() {
                CloseableIterator<Photo> iterator = null;
                long count = 0;
                
                try {
                        Dao<Photo, Integer> photoDao = db.getDao(Photo.class);
                        QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
                        count = photoQueryBuilder.countOf();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                Log.i(LOG_TAG, "COUNT das fotos no BD: " + count);
                
                return count;
        }
        
        /**
         * Returns an iterator of all the pictures from database.
         * 
         * @return {@link CloseableIterator} the iterator of the database
         *         registers.
         * @author PauloLuan
         * */
        public static CloseableIterator<Photo> getIteratorToAnalyzePhotos() {
                CloseableIterator<Photo> iterator = null;
                
                try {
                        Dao<Photo, Integer> photoDao = db.getDao(Photo.class);
                        QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
                        iterator = photoDao.iterator(photoQueryBuilder.prepare());
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return iterator;
        }
        
        /**
         * 
         * Iterate over the files on the photo's directory and compare on the
         * database either the register exists or not. If the register don't
         * exists on the database the file would be removed from the filesystem.
         * 
         * */
        private static void removeFilesFromExternalMemoryIfNotInDatabase() {
                File mediaStorageDir = new File(Utility.getExternalSdCardPath() + "/inova/" + "/dados" + "/fotos/");
                
                if (!mediaStorageDir.canWrite()) {
                        mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/inova/" + "/dados" + "/fotos/");
                }
                
                File pictures[] = mediaStorageDir.listFiles();
                
                if (pictures != null) {
                        for (int i = 0; i < pictures.length; i++) {
                                String path = pictures[i].getPath();
                                Boolean pictureExists = PhotoDao.isPictureOnDatabase(path);
                                
                                if (!pictureExists) {
                                        Log.i(LOG_TAG, "Removendo Arquivo da foto: " + path);
                                        pictures[i].delete();
                                }
                        }
                }
        }
        
        /**
         * 
         * Checks in the database if the pictures still have their source form.
         * 
         * */
        private static void removePhotoIfFormIsNull() {
                CloseableIterator<Photo> iterator = getIteratorToAnalyzePhotos();
                
                try {
                        while (iterator.hasNext()) {
                                try {
                                        Photo photo = (Photo) iterator.next();
                                        
                                        if (photo.getForm() == null) {
                                                Log.i(LOG_TAG, "Excluindo a foto: " + photo.getId() + "Restando: " + getCountPhotos());
                                                PhotoDao.deleteWithDeleteBuilder(photo.getId());
                                        }
                                }
                                catch (IllegalStateException exception) {
                                        Log.i(LOG_TAG, "Erro, IllegalStateException");
                                }
                        }
                }
                catch (SQLException exception) {
                        ExceptionHandler.saveLogFile(exception);
                }
                finally {
                        if (iterator != null) {
                                iterator.closeQuietly();
                        }
                }
        }
        
        public synchronized static void verifyIntegrityOfPictures() {
                Log.i(LOG_TAG, "#### ANALISANDO AS FOTOS ####");
                removePhotoIfFormIsNull();
                removeFilesFromExternalMemoryIfNotInDatabase();
        }
        
        public PhotoDBAnalyzer() {
                this.execute();
        }
        
        /**
         * Verify all pictures and delete from filesystem if it not exists on
         * Database.
         * 
         * @author Paulo Luan
         * */
        @Override
        protected String doInBackground(String... params) {
                verifyIntegrityOfPictures();
                return "Finished";
        }
        
}
