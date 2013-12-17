package br.org.funcate.mobile.task;

import java.sql.SQLException;
import java.util.List;

import android.util.Log;
import br.org.funcate.mobile.address.Address;
import br.org.funcate.mobile.database.DatabaseAdapter;
import br.org.funcate.mobile.database.DatabaseHelper;
import br.org.funcate.mobile.form.Form;
import br.org.funcate.mobile.user.SessionManager;
import br.org.funcate.mobile.user.User;

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
        } catch (SQLException e) {
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
        } catch (SQLException e) {

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
    public static Integer deleteTasks(List<Task> tasks) {
        Integer result = 0;

        try {
            result = taskDao.delete(tasks);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static List<Task> getFinishedTasks() {
        List<Task> tasks = null;

        QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
        QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();

        try {
            String userHash = SessionManager.getUserHash();

            userQueryBuilder.where().eq("hash", userHash);
            taskQueryBuilder.where().eq("done", Boolean.TRUE);
            taskQueryBuilder.join(userQueryBuilder);

            tasks = taskQueryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public static List<Task> getNotFinishedTasks() {
        List<Task> tasks = null;

        QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
        QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();

        try {
            String userHash = SessionManager.getUserHash();

            userQueryBuilder.where().eq("hash", userHash);
            taskQueryBuilder.where().eq("done", Boolean.FALSE);
            taskQueryBuilder.join(userQueryBuilder);

            tasks = taskQueryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
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
            String userHash = SessionManager.getUserHash();
            userQueryBuilder.where().eq("hash", userHash);

            taskQueryBuilder.join(userQueryBuilder);
            taskQueryBuilder.where().eq("done", Boolean.FALSE);

            count = taskQueryBuilder.countOf();
        } catch (SQLException e) {
            e.printStackTrace();
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
            String userHash = SessionManager.getUserHash();
            userQueryBuilder.where().eq("hash", userHash);

            taskQueryBuilder.where().eq("done", Boolean.TRUE);
            taskQueryBuilder.join(userQueryBuilder);

            count = taskQueryBuilder.countOf();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Get Count of completed tasks.
     * 
     * @author Paulo Luan
     */
    public static long getCountOfTasks() {
        long count = 0;

        QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
        QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();

        try {
            String userHash = SessionManager.getUserHash();
            userQueryBuilder.where().eq("hash", userHash);
            taskQueryBuilder.join(userQueryBuilder);

            count = taskQueryBuilder.countOf();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Save a task into local database.
     * 
     * @author Paulo Luan
     * @param List
     *            <Task> Tasks that will be saved into database.
     */
    public static boolean saveTask(Task task) {
        boolean isSaved = false;
        Task persistedTask = null;

        if (task != null) {

            try {
                persistedTask = getTaskById(task.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (persistedTask == null) {
                    formDao.create(task.getForm());
                    addressDao.create(task.getAddress());
                    taskDao.create(task);
                }

                isSaved = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isSaved;
    }

    /**
     * Update an existing task into local database.
     * 
     * @author Paulo Luan
     * @param List
     *            <Task> Tasks that will be saved into database.
     */
    public static boolean updateTask(Task task) {
        boolean isSaved = false;

        if (task != null) {

            try {
                formDao.update(task.getForm());
                addressDao.update(task.getAddress());
                taskDao.update(task);

                isSaved = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isSaved;
    }

    public static Task getTaskById(int id) {
        Task task = null;
        try {
            task = taskDao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return task;
    }

    public static Task getTaskByAddressId(int addressId) {
        Task task = null;

        try {
            task = taskDao.queryBuilder().where()
                    .eq("address_id", addressId)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return task;
    }
}
