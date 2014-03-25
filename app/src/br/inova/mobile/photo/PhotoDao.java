package br.inova.mobile.photo;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import android.util.Log;
import br.inova.mobile.database.DatabaseAdapter;
import br.inova.mobile.database.DatabaseHelper;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.form.Form;
import br.inova.mobile.task.Task;
import br.inova.mobile.user.SessionManager;
import br.inova.mobile.user.User;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class PhotoDao {
        
        private static final String        LOG_TAG  = "#PHOTODAO";
        
        private static DatabaseAdapter     db       = DatabaseHelper.getDatabase();
        
        private static Dao<Task, Integer>  taskDao  = db.getTaskDao();
        
        private static Dao<Form, Integer>  formDao  = db.getFormDao();
        
        private static Dao<User, Integer>  userDao  = db.getUserDao();
        
        private static Dao<Photo, Integer> photoDao = db.getPhotoDao();
        
        public static List<Photo> getNotSyncPhotos() {
                List<Photo> photos = null;
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<Form, Integer> formQueryBuilder = formDao.queryBuilder();
                QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                try {
                        String userHash = SessionManager.getInstance().getUserHash();
                        userQueryBuilder.where().eq("hash", userHash);
                        
                        taskQueryBuilder.join(userQueryBuilder);
                        formQueryBuilder.join(taskQueryBuilder);
                        
                        photoQueryBuilder.join(formQueryBuilder);
                        
                        photos = photoQueryBuilder.query();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return photos;
        }
        
        /**
         * Returns an iterator of all the pictures of the current users.
         * 
         * @return {@link CloseableIterator} the iterator of the database
         *         registers.
         * @author PauloLuan
         * */
        public static CloseableIterator<Photo> getIteratorForNotSyncPhotos() {
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<Form, Integer> formQueryBuilder = formDao.queryBuilder();
                QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                // when you are done, prepare your query and build an iterator
                CloseableIterator<Photo> iterator = null;
                
                try {
                        String userHash = SessionManager.getInstance().getUserHash();
                        userQueryBuilder.where().eq("hash", userHash);
                        
                        taskQueryBuilder.join(userQueryBuilder);
                        formQueryBuilder.join(taskQueryBuilder);
                        
                        photoQueryBuilder.join(formQueryBuilder);
                        
                        photoQueryBuilder.iterator();
                        
                        iterator = photoDao.iterator(photoQueryBuilder.prepare());
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return iterator;
        }
        
        /**
         * 
         * Get a List of photos related to a form.
         * 
         * @param Form
         *                The Form object that we use to create the query.
         * 
         * @return List<Photo> photos Collection of pictures that the user was
         *         captured.
         * 
         * @author Paulo Luan
         */
        public static List<Photo> getPhotosByForm(Form form) {
                List<Photo> photos = null;
                
                QueryBuilder<Form, Integer> formQueryBuilder = formDao.queryBuilder();
                QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
                
                try {
                        formQueryBuilder.where().eq("id", form.getId());
                        photoQueryBuilder.join(formQueryBuilder);
                        
                        photos = photoQueryBuilder.query();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return photos;
        }
        
        /**
         * 
         * Delete photos locally.
         * 
         * @author Paulo Luan
         * @return Boolean result
         */
        public static Integer deletePhotos(List<Photo> photos) {
                Dao<Photo, Integer> dao = db.getPhotoDao();
                Integer result = 0;
                
                try {
                        for (Photo photo : photos) {
                                File file = new File(photo.getPath());
                                file.delete();
                                result = dao.delete(photo);
                        }
                }
                catch (SQLException e) {
                        Log.e(LOG_TAG, e.getMessage());
                        ExceptionHandler.saveLogFile(e);
                }
                
                return result;
        }
        
        /**
         * 
         * Delete photo from local database.
         * 
         * @author Paulo Luan
         * @return Boolean result
         */
        public static boolean deletePhoto(Photo photo) {
                Dao<Photo, Integer> dao = db.getPhotoDao();
                boolean result = false;
                
                try {
                        File file = new File(photo.getPath());
                        file.delete();
                        
                        if (photo.getId() != null) {
                                dao.delete(photo);
                        }
                        
                        result = true;
                }
                catch (SQLException e) {
                        Log.e(LOG_TAG, e.getMessage());
                        ExceptionHandler.saveLogFile(e);
                }
                
                return result;
        }
        
        /**
         * Verify all pictures and delete from database if it not exists on File
         * system.
         * 
         * @author Paulo Luan
         * */
        public static void verifyIntegrityOfPictures() {
                
                CloseableIterator<Photo> iterator = getIteratorForNotSyncPhotos();
                
                while (iterator.hasNext()) {
                        Photo picture = (Photo) iterator.next();
                        
                        File file = new File(picture.getPath());
                        
                        if (!file.exists()) {
                                PhotoDao.deletePhoto(picture);
                        }
                }
                
                //TODO: verificar ao contrário, ou seja, fotos que estão no PATH mas não estão no banco.
        }
        
        /**
         * Save a list of photos into local database.
         * 
         * @author Paulo Luan
         * @param List
         *                <Photo> Photos that will be saved into database.
         */
        public static boolean savePhotos(List<Photo> photos) {
                boolean isSaved = false;
                
                if (photos != null) {
                        Dao<Photo, Integer> photoDao = db.getPhotoDao();
                        
                        try {
                                for (Photo photo : photos) {
                                        if (photo.getId() == null) {
                                                photoDao.create(photo);
                                        }
                                }
                                
                                isSaved = true;
                        }
                        catch (SQLException e) {
                                ExceptionHandler.saveLogFile(e);
                        }
                }
                
                return isSaved;
        }
        
        /**
         * Get Count of completed photos.
         * 
         * @author Paulo Luan
         */
        public static Long getCountOfCompletedPhotos() {
                long count = 0;
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<Form, Integer> formQueryBuilder = formDao.queryBuilder();
                QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                try {
                        String userHash = SessionManager.getInstance().getUserHash();
                        userQueryBuilder.where().eq("hash", userHash);
                        
                        taskQueryBuilder.join(userQueryBuilder);
                        formQueryBuilder.join(taskQueryBuilder);
                        
                        photoQueryBuilder.join(formQueryBuilder);
                        
                        count = photoQueryBuilder.countOf();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return count;
        }
        
}