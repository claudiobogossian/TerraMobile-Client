package br.inova.mobile.photo;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
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
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

public class PhotoDao {
        
        private static final String        LOG_TAG  = "#PHOTODAO";
        
        private static DatabaseAdapter     db       = DatabaseHelper.getDatabase();
        
        private static Dao<Task, Integer>  taskDao  = db.getTaskDao();
        
        private static Dao<Form, Integer>  formDao  = db.getFormDao();
        
        private static Dao<User, Integer>  userDao  = db.getUserDao();
        
        private static Dao<Photo, Integer> photoDao = db.getPhotoDao();
        
        /**
         * Returns an iterator of all the pictures of the current users.
         * 
         * @return {@link CloseableIterator} the iterator of the database
         *         registers.
         * @author PauloLuan
         * */
        public synchronized CloseableIterator<Photo> getIteratorForNotSyncPhotos() {
                
                // when you are done, prepare your query and build an iterator
                CloseableIterator<Photo> iterator = null;
                
                try {
                        QueryBuilder<Photo, Integer> photoQueryBuilder = getQueryBuilderForUser();
                        photoQueryBuilder.iterator();
                        
                        iterator = photoDao.iterator(photoQueryBuilder.prepare());
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return iterator;
        }
        
        private synchronized static QueryBuilder<Photo, Integer> getQueryBuilderForUser() throws SQLException {
                Dao<Task, Integer> taskDao = db.getDao(Task.class);
                Dao<Form, Integer> formDao = db.getDao(Form.class);
                Dao<User, Integer> userDao = db.getDao(User.class);
                Dao<Photo, Integer> photoDao = db.getDao(Photo.class);
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<Form, Integer> formQueryBuilder = formDao.queryBuilder();
                QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                String userHash = SessionManager.getInstance().getUserHash();
                userQueryBuilder.where().eq("hash", userHash);
                
                taskQueryBuilder.join(userQueryBuilder);
                formQueryBuilder.join(taskQueryBuilder);
                
                photoQueryBuilder.join(formQueryBuilder);
                
                return photoQueryBuilder;
        }
        
        /**
         * 
         * Get a List of photos related to a form.
         * 
         * @param Form
         *                The Form object that we use to create the query.
         * 
         * @return List<Photo> Collection of pictures that the user was
         *         captured.
         * 
         * @author Paulo Luan
         */
        public synchronized static List<Photo> getPhotosByForm(Form form) {
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
         * Get a List of ID's of all photos .
         * 
         * @param id
         *                The Photo ID.
         * 
         * @return List<Photo> Collection of pictures that the user was
         *         captured.
         * 
         * @author Paulo Luan
         */
        public synchronized static List<Integer> getListOfPhotosIds() {
                List<Integer> photosIds = new ArrayList<Integer>();
                
                try {
                        QueryBuilder<Photo, Integer> photoQueryBuilder = getQueryBuilderForUser();
                        photoQueryBuilder.selectColumns("id");
                        String query = photoQueryBuilder.prepareStatementString();
                        
                        GenericRawResults<String[]> rawResults = photoDao.queryRaw(query);
                        List<String[]> results = rawResults.getResults();
                        
                        for (String[] strings : results) {
                                for (String s : strings)
                                        photosIds.add(Integer.valueOf(s));
                        }
                        
                }
                catch (Exception e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return photosIds;
        }
        
        /**
         * 
         * Get a Photo by ID.
         * 
         * @param id
         *                The Photo ID.
         * 
         * @return List<Photo> Collection of pictures that the user was
         *         captured.
         * 
         * @author Paulo Luan
         */
        public synchronized static Photo getPhotosById(Integer id) {
                Photo photo = null;
                
                QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
                
                try {
                        photoQueryBuilder.where().eq("id", id);
                        photo = photoQueryBuilder.queryForFirst();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return photo;
        }
        
        /**
         * 
         * Verify if the path of the picture exists on the database.
         * 
         * @param Path
         *                The String that will be query at the database.
         * 
         * @return Boolean.
         * 
         * @author Paulo Luan
         */
        public synchronized static Boolean isPictureOnDatabase(String path) {
                Boolean exists = false;
                
                QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
                
                try {
                        photoQueryBuilder.where().eq("path", path);
                        Photo photo = photoQueryBuilder.queryForFirst();
                        
                        if (photo != null && photo.getForm() != null) {
                                exists = true;
                        }
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return exists;
        }
        
        /**
         * 
         * Delete photos locally.
         * 
         * @author Paulo Luan
         * @return Boolean result
         */
        public synchronized static Boolean deletePhotos(List<Photo> photos) {
                Boolean result = false;
                
                try {
                        for (Photo photo : photos) {
                                File file = new File(photo.getPath());
                                file.delete();
                                deleteWithDeleteBuilder(photo.getId());
                                result = true;
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
        public synchronized static boolean deletePhoto(Photo photo) {
                boolean result = false;
                
                try {
                        File file = new File(photo.getPath());
                        
                        if (file.exists()) {
                                file.delete();
                        }
                        
                        if (photo.getId() != null) {
                                deleteWithDeleteBuilder(photo.getId());
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
         * Save a list of photos into local database.
         * 
         * @author Paulo Luan
         * @param List
         *                <Photo> Photos that will be saved into database.
         */
        public synchronized static boolean savePhotos(List<Photo> photos) {
                boolean isSaved = false;
                
                if (photos != null) {
                        Dao<Photo, Integer> photoDao = db.getPhotoDao();
                        
                        try {
                                for (Photo photo : photos) {
                                        if (photo.getId() == null) {
                                                photoDao.create(photo);
                                                Log.d(LOG_TAG, "Foto Salva com sucesso! ID: " + photo.getId());
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
         * Save a list of photos into local database.
         * 
         * @author Paulo Luan
         * @param List
         *                <Photo> Photos that will be saved into database.
         */
        public synchronized static boolean savePhoto(Photo photo) {
                boolean isSaved = false;
                
                if (photo != null) {
                        Dao<Photo, Integer> photoDao = db.getPhotoDao();
                        
                        try {
                                if (photo.getId() == null) {
                                        photoDao.create(photo);
                                        Log.d(LOG_TAG, "Foto Salva com sucesso! ID: " + photo.getId());
                                }
                                else {
                                        photoDao.update(photo);
                                        Log.d(LOG_TAG, "Foto Atualizada com sucesso! ID: " + photo.getId());
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
        public synchronized static Long getCountOfCompletedPhotos() {
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
                        
                        Log.d(LOG_TAG, "COUNT de todas as fotos: " + count);
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return count;
        }
        
        public static synchronized boolean deleteWithDeleteBuilder(
                                                                   Integer photoId) throws SQLException {
                Boolean isRemoved = false;
                
                Dao<Photo, Integer> dao = db.getPhotoDao();
                DeleteBuilder<Photo, Integer> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("id", photoId);
                Integer isDeleted = dao.delete(deleteBuilder.prepare());
                
                if (isDeleted == 1) {
                        isRemoved = true;
                        Log.d(LOG_TAG, "Excluiu com sucesso! ID: " + photoId);
                }
                else {
                        Log.d(LOG_TAG, "Não excluiu!! ID: " + photoId);
                }
                
                return isRemoved;
        }
        
        public synchronized static void removePhotosByIds(
                                                          List<Integer> photosIds) {
                
                for (Integer photoId : photosIds) {
                        if (photoId != null) {
                                try {
                                        deleteWithDeleteBuilder(photoId);
                                }
                                catch (SQLException exception) {
                                        ExceptionHandler.saveLogFile(exception);
                                }
                                
                        }
                }
        }
        
}
