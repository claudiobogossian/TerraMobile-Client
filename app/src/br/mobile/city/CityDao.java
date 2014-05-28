package br.mobile.city;

import java.sql.SQLException;

import br.inova.mobile.database.DatabaseAdapter;
import br.inova.mobile.database.DatabaseHelper;
import br.inova.mobile.exception.ExceptionHandler;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

public class CityDao {
        
        private static DatabaseAdapter    db      = DatabaseHelper.getDatabase();
        private static Dao<City, Integer> cityDao = db.getCityDao();
        
        /**
         * Save a city into local database.
         * 
         * @author Paulo Luan
         * @param List
         *                <City> the city that will be saved into database.
         */
        public static boolean saveCity(City city) {
                boolean isSaved = false;
                
                if (city != null) {
                        
                        try {
                                cityDao.createIfNotExists(city);
                                isSaved = true;
                        }
                        catch (SQLException e) {
                                ExceptionHandler.saveLogFile(e);
                        }
                }
                
                return isSaved;
        }
        
        /**
         * Get Count of all cities.
         * 
         * @author Paulo Luan
         */
        public static long getCountOfCities() {
                long count = 0;
                
                QueryBuilder<City, Integer> cityQueryBuilder = cityDao.queryBuilder();
                
                try {
                        count = cityQueryBuilder.countOf();
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                
                return count;
        }
        
        /**
         * Get a register by ID.
         * 
         * @param id
         *                the ID that will be searched on database.
         * @author Paulo Luan
         */
        public static City getCityById(int id) {
                City city = null;
                try {
                        city = cityDao.queryForId(id);
                }
                catch (SQLException e) {
                        ExceptionHandler.saveLogFile(e);
                }
                return city;
        }
        
}
