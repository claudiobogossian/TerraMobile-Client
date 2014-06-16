package br.inova.mobile.address;

import java.sql.SQLException;
import java.util.List;

import android.graphics.PointF;
import android.util.Log;
import br.inova.mobile.database.DatabaseAdapter;
import br.inova.mobile.database.DatabaseHelper;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.task.Task;
import br.inova.mobile.user.SessionManager;
import br.inova.mobile.user.User;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class AddressDao {
        
        private static DatabaseAdapter       db         = DatabaseHelper.getDatabase();
        private static Dao<Task, Integer>    taskDao    = db.getTaskDao();
        private static Dao<User, Integer>    userDao    = db.getUserDao();
        private static Dao<Address, Integer> addressDao = db.getAddressDao();
        private static SessionManager        session    = SessionManager.getInstance();
        
        public static List<Task> queryForAddresses(
                                                   PointF abovePoint,
                                                   PointF rightPoint,
                                                   PointF belowPoint,
                                                   PointF leftPoint) {
                List<Task> tasks = null;
                
                String latitudeColumn = "coordy";
                String longitudeColumn = "coordx";
                
                QueryBuilder<Task, Integer> taskQueryBuilder = taskDao.queryBuilder();
                QueryBuilder<User, Integer> userQueryBuilder = userDao.queryBuilder();
                QueryBuilder<Address, Integer> addressQueryBuilder = addressDao.queryBuilder();
                
                try {
                        String userHash = session.getUserHash();
                        userQueryBuilder.where().eq("hash", userHash);
                        taskQueryBuilder.join(userQueryBuilder);
                        
                        //@formatter:off
                        addressQueryBuilder.where()
                             .gt(latitudeColumn, String.valueOf(belowPoint.x)).and()
                             .lt(latitudeColumn, String.valueOf(abovePoint.x)).and()
                             .gt(longitudeColumn, String.valueOf(leftPoint.y)).and()
                             .lt(longitudeColumn, String.valueOf(rightPoint.y));
                        
                        //@formatter:on
                        
                        String query = addressQueryBuilder.prepareStatementString();
                        Log.i("QUERY FOR NEAREST POINTS ", query);
                        
                        taskQueryBuilder.join(addressQueryBuilder);
                        tasks = taskQueryBuilder.query();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return tasks;
        }
        
}
