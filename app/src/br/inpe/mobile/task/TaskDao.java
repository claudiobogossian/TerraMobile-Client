package br.inpe.mobile.task;

import java.sql.SQLException;
import java.util.List;

import android.util.Log;
import br.inpe.mobile.address.Address;
import br.inpe.mobile.database.DatabaseAdapter;
import br.inpe.mobile.database.DatabaseHelper;
import br.inpe.mobile.exception.ExceptionHandler;
import br.inpe.mobile.form.Form;
import br.inpe.mobile.user.SessionManager;
import br.inpe.mobile.user.User;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

public class TaskDao {
        
        private static final String          LOG_TAG    = "#TASKDAO";
        
        private static DatabaseAdapter       db         = DatabaseHelper.getDatabase();
        
        private static Dao<Task, Integer>    taskDao    = db.getTaskDao();
        
        private static Dao<Form, Integer>    formDao    = db.getFormDao();
        
        private static Dao<User, Integer>    userDao    = db.getUserDao();
        
        private static Dao<Address, Integer> addressDao = db.getAddressDao();
        
        private static SessionManager        session    = SessionManager.getInstance();
        
        /**
         * 
         * This function return the local data, persisted in SQLite Database.
         * 
         * @author Paulo Luan
         * @return List<Task>
         */
        public static List<Task> getLocalTasks() {
                List<Task> list = null;
                try {
                        // query for all of the data objects in the database
                        list = taskDao.queryForAll();
                }
                catch (SQLException e) {
                        Log.e(LOG_TAG, "Database exception", e);
                }
                return list;
        }
        
        /**
         * 
         * Delete the rows where is syncronized with the server.
         * 
         * @author Paulo Luan
         * @return Boolean result
         */
        public static Integer deleteSincronizedTasks() {
                DeleteBuilder<Task, Integer> deleteBuilder = taskDao.deleteBuilder();
                Integer result = 0;
                
                try {
                        // only delete the rows where syncronized is true
                        deleteBuilder.where().eq("done", Boolean.TRUE);
                        result = deleteBuilder.delete();
                }
                catch (SQLException e) {
                        
                }
                
                return result;
        }
        
        /**
         * 
         * Delete a collection of Tasks from local database.
         * 
         * @author Paulo Luan
         * @return Boolean result
         */
        public static boolean deleteTasks(List<Task> tasks) {
                boolean result = false;
                
                for (Task task : tasks) {
                        result = deleteTask(task);
                }
                
                return result;
        }
        
        /**
         * 
         * Delete a Task from local database.
         * 
         * @author Paulo Luan
         * @return Boolean result
         */
        public static boolean deleteTask(Task task) {
                boolean result = false;
                
                try {
                        addressDao.delete(task.getAddress());
                        formDao.delete(task.getForm());
                        taskDao.delete(task);
                        
                        result = true;
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return result;
        }
        
        public static List<Task> getFinishedTasks() {
                List<Task> tasks = null;
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                try {
                        String userHash = session.getUserHash();
                        
                        userQueryBuilder.where().eq("hash", userHash);
                        taskQueryBuilder.where().eq("done", Boolean.TRUE);
                        taskQueryBuilder.join(userQueryBuilder);
                        
                        tasks = taskQueryBuilder.query();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return tasks;
        }
        
        public static List<Task> getNotFinishedTasks() {
                List<Task> tasks = null;
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                try {
                        String userHash = session.getUserHash();
                        
                        userQueryBuilder.where().eq("hash", userHash);
                        taskQueryBuilder.where().eq("done", Boolean.FALSE);
                        taskQueryBuilder.join(userQueryBuilder);
                        
                        tasks = taskQueryBuilder.query();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return tasks;
        }
        
        /**
         * Get the cursor of finished tasks based of the current user.
         * 
         * @return Cursor cursor of finished tasks of the user.
         * @author Paulo Luan
         */
        public static CloseableIterator<Task> getIteratorForFinishedTasks() {
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                // when you are done, prepare your query and build an iterator
                CloseableIterator<Task> iterator = null;
                
                try {
                        String userHash = session.getUserHash();
                        userQueryBuilder.where().eq("hash", userHash);
                        taskQueryBuilder.join(userQueryBuilder);
                        
                        taskQueryBuilder.where().eq("done", Boolean.TRUE);
                        
                        iterator = taskDao.iterator(taskQueryBuilder.prepare());                        
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }

                return iterator;
        }
        
        /**
         * Get the cursor of all tasks based of the current user.
         * 
         * @return Cursor cursor of all the tasks of the user.
         * @author Paulo Luan
         */
        public static CloseableIterator<Task> getIteratorForAllTasks() {
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                // when you are done, prepare your query and build an iterator
                CloseableIterator<Task> iterator = null;
                
                try {
                        String userHash = session.getUserHash();
                        userQueryBuilder.where().eq("hash", userHash);
                        taskQueryBuilder.join(userQueryBuilder);
                        
                        iterator = taskDao.iterator(taskQueryBuilder.prepare());                        
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }

                return iterator;
        }
        
        /**
         * Get Count of incompleted tasks.
         * 
         * @author Paulo Luan
         */
        public static long getCountOfIncompletedTasks() {
                long count = 0;
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                try {
                        String userHash = session.getUserHash();
                        userQueryBuilder.where().eq("hash", userHash);
                        
                        taskQueryBuilder.join(userQueryBuilder);
                        taskQueryBuilder.where().eq("done", Boolean.FALSE);
                        
                        count = taskQueryBuilder.countOf();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return count;
        }
        
        /**
         * Get Count of completed tasks.
         * 
         * @author Paulo Luan
         */
        public static long getCountOfCompletedTasks() {
                long count = 0;
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                try {
                        String userHash = session.getUserHash();
                        userQueryBuilder.where().eq("hash", userHash);
                        
                        taskQueryBuilder.where().eq("done", Boolean.TRUE);
                        taskQueryBuilder.join(userQueryBuilder);
                        
                        count = taskQueryBuilder.countOf();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return count;
        }
        
        /**
         * Get Count of all tasks.
         * 
         * @author Paulo Luan
         */
        public static long getCountOfTasks() {
                long count = 0;
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                try {
                        String userHash = session.getUserHash();
                        userQueryBuilder.where().eq("hash", userHash);
                        taskQueryBuilder.join(userQueryBuilder);
                        
                        count = taskQueryBuilder.countOf();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return count;
        }
        
        /**
         * Save a task into local database.
         * 
         * @author Paulo Luan
         * @param List
         *                <Task> Tasks that will be saved into database.
         */
        public static boolean saveTask(Task task) {
                boolean isSaved = false;
                Task persistedTask = null;
                
                if (task != null) {
                        
                        try {
                                persistedTask = getTaskById(task.getId());
                        }
                        catch (Exception e) {
                                ExceptionHandler.saveLogFile(e);
                        }
                        
                        try {
                                if (persistedTask == null) {
                                        formDao.create(task.getForm());
                                        addressDao.create(task.getAddress());
                                        taskDao.create(task);
                                        
                                        isSaved = true;
                                }
                        }
                        catch (SQLException e) {
                                ExceptionHandler.saveLogFile(e);
                        }
                }
                
                return isSaved;
        }
        
        /**
         * Update an existing task into local database.
         * 
         * @author Paulo Luan
         * @param List
         *                <Task> Tasks that will be saved into database.
         */
        public static boolean updateTask(Task task) {
                boolean isSaved = false;
                
                if (task != null) {
                        
                        try {
                                formDao.update(task.getForm());
                                addressDao.update(task.getAddress());
                                taskDao.update(task);
                                
                                isSaved = true;
                        }
                        catch (SQLException e) {
                                ExceptionHandler.saveLogFile(e);
                        }
                }
                
                return isSaved;
        }
        
        public static Task getTaskById(int id) {
                Task task = null;
                try {
                        task = taskDao.queryForId(id);
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                return task;
        }
        
        public static Task getTaskByAddressId(int addressId) {
                Task task = null;
                
                try {
                        task = taskDao.queryBuilder().where().eq("address_id", addressId).queryForFirst();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                return task;
        }
        
        /**
         * 
         * Eachs all forms with the same address id and changes their
         * infrastructure data.
         * 
         * @param Task
         *                The task that have the infrastructure information.
         * @author PauloLuan
         * */
        public static void updateInfrastructureDataFromAllForms(Task task) {
                List<Task> tasks = null;
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<Address, Integer> addressQueryBuilder = addressDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                
                try {
                        String userHash = session.getUserHash();
                        userQueryBuilder.where().eq("hash", userHash);
                        taskQueryBuilder.join(userQueryBuilder);
                        
                        String featureId = getFeatureId(task);
                        addressQueryBuilder.where().like("featureId", "%" + featureId + "%").and().like("name", "%" + task.getAddress().getName() + "%");
                        
                        taskQueryBuilder.join(addressQueryBuilder);
                        
                        String test = taskQueryBuilder.prepareStatementString(); // TODO: remove
                        
                        tasks = taskQueryBuilder.query();
                        
                        if (tasks != null && !tasks.isEmpty()) {
                                
                                Form taskForm = task.getForm();
                                
                                for (Task eachTask : tasks) {
                                        Form form = eachTask.getForm(); // gets each form and alters
                                                                        // their data.
                                        
                                        form.setPavimentation(taskForm.getPavimentation());
                                        form.setAsphaltGuide(taskForm.getAsphaltGuide());
                                        form.setPublicIlumination(taskForm.getPublicIlumination());
                                        form.setEnergy(taskForm.getEnergy());
                                        form.setPluvialGallery(taskForm.getPluvialGallery());
                                        
                                        formDao.update(form);
                                }
                        }
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
        }
        
        /**
         * 
         * Return the sector and the block from featureID of a task.
         * 
         * @param Task
         *                The task that have the address id information.
         * @author PauloLuan
         * */
        private static String getFeatureId(Task task) {
                String featureId = "";
                
                // gets the sector and block to the verification (0, 6).
                try {
                        featureId = task.getAddress().getFeatureId().substring(0, 6);
                }
                catch (Exception e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return featureId;
        }
}
