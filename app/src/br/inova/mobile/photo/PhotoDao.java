package br.inova.mobile.photo;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.util.Log;
import br.inova.mobile.Utility;
import br.inova.mobile.database.DatabaseAdapter;
import br.inova.mobile.database.DatabaseHelper;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.form.Form;
import br.inova.mobile.task.Task;
import br.inova.mobile.user.SessionManager;
import br.inova.mobile.user.User;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
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
         * @return List<Photo> Collection of pictures that the user was
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
         * Verify if the path of the picture exists on the database.
         * 
         * @param Path
         *                The String that will be query at the database.
         * 
         * @return Boolean.
         * 
         * @author Paulo Luan
         */
        public static Boolean isPictureOnDatabase(String path) {
                Boolean exists = false;
                
                QueryBuilder<Photo, Integer> photoQueryBuilder = photoDao.queryBuilder();
                
                try {
                        photoQueryBuilder.where().eq("path", path);
                        Photo photo = photoQueryBuilder.queryForFirst();
                        
                        if (photo != null) {
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
        public static Boolean deletePhotos(List<Photo> photos) {
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
        public static boolean deletePhoto(Photo photo) {
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
         * Verify all pictures and delete from filesystem if it not exists on
         * Database.
         * 
         * @author Paulo Luan
         * */
        public static void verifyIntegrityOfPictures() {
                verifyBase64();
                removeFilesFromExternalMemoryIfNotInDatabase();
        }
        
        /**
         * 
         * Iterate over all database photos registers, verifying the consistency
         * on the Base64. If on the construction of the image occurs an error
         * the application will save the file without base64 (null). That
         * function creates again the base64 of the picture and save it into the
         * database.
         * 
         * */
        private static void verifyBase64() {
                CloseableIterator<Photo> iterator = PhotoDao.getIteratorForNotSyncPhotos();
                List<Integer> photosToRemove = new ArrayList<Integer>();
                
                try {
                        while (iterator.hasNext()) {
                                try {
                                        Photo photo = (Photo) iterator.next();
                                        
                                        if (photo.getBase64() == null) {
                                                File photoFile = new File(photo.getPath());
                                                
                                                if (!photoFile.exists()) {
                                                        photosToRemove.add(photo.getId()); // NÃO TEM O QUE FAZER, FOTO NÃO EXISTE, NÃO TEM COMO TIRAR BASE64. FOTO PERDIDA.
                                                }
                                                else {
                                                        photo.setBase64(CreatePhotoAsync.getBytesFromImage(photo.getPath()));
                                                }
                                        }
                                }
                                catch (OutOfMemoryError exception) {
                                        ExceptionHandler.saveLogFile("OutofMemory ao verificar consistência das imagens antes de enviar para o servidor." + exception.getLocalizedMessage() + exception.getMessage());
                                }
                                catch (Exception exception) {
                                        ExceptionHandler.saveLogFile(exception);
                                }
                        }
                }
                catch (Exception e) {
                        ExceptionHandler.saveLogFile(e);
                }
                finally {
                        iterator.closeQuietly();
                }
                
                PhotoDao.removePhotosByIds(photosToRemove);
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
                                        pictures[i].delete();
                                }
                        }
                }
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
        
        public static void deletePhotosTest() {
                CloseableIterator<Photo> iterator = PhotoDao.getIteratorForNotSyncPhotos();
                
                try {
                        while (iterator.hasNext()) {
                                Photo photo = (Photo) iterator.next();
                                PhotoDao.deletePhoto(photo);
                        }
                }
                catch (Exception e) {
                        e.printStackTrace();
                }
                finally {
                        iterator.closeQuietly();
                }
                
        }
        
        private static void deleteWithDeleteBuilder(Integer photoId) throws SQLException {
                Dao<Photo, Integer> dao = db.getPhotoDao();
                DeleteBuilder<Photo, Integer> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("id", photoId);
                dao.delete(deleteBuilder.prepare());
        }
        
        public static void removePhotosByIds(List<Integer> photosIds) {
                
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
