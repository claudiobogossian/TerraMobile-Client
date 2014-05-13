package br.inova.mobile.location;

import java.util.List;

import android.graphics.PointF;
import android.util.Log;
import br.inova.mobile.address.AddressDao;
import br.inova.mobile.task.Task;

/**
 * 
 * http://stackoverflow.com/questions/3695224/android-sqlite-getting-nearest-
 * locations-with-latitude-and-longitude
 * 
 * */
public class DistanceFilter {
        
        /**
         * get the nearest tasks from a point of the map.
         * 
         * @param center
         *                the central point that the query will be maked.
         * 
         * @param distance
         *                the distance of the center point. (the radius
         * 
         * @return A List with the nearest tasks of that region.
         * */
        public static List<Task> getNearestAddresses(
                                                     PointF center,
                                                     Double distance) {
                final double mult = 1; // mult = 1.1; is more reliable
                //final double radius = 1; //meters? 
                
                PointF abovePoint = calculateDerivedPosition(center, mult * distance, 0);
                PointF rightPoint = calculateDerivedPosition(center, mult * distance, 90);
                PointF belowPoint = calculateDerivedPosition(center, mult * distance, 180);
                PointF leftPoint = calculateDerivedPosition(center, mult * distance, 270);
                
                List<Task> tasks = AddressDao.queryForAddresses(abovePoint, rightPoint, belowPoint, leftPoint);
                
                for (Task task : tasks) {
                        Double distanceBetweenPoints = getDistanceBetweenTwoPoints(center, new PointF(task.getAddress().getCoordy().floatValue(), task.getAddress().getCoordx().floatValue()));
                        Log.d("", "" + distanceBetweenPoints);
                }
                
                return tasks;
        }
        
        /**
         * Calculates the end-point from a given source at a given range
         * (meters) and bearing (degrees). This methods uses simple geometry
         * equations to calculate the end-point.
         * 
         * @param point
         *                Point of origin
         * @param range
         *                Range in meters
         * @param bearing
         *                Bearing in degrees
         * @return End-point from the source given the desired range and
         *         bearing.
         */
        private static PointF calculateDerivedPosition(
                                                       PointF point,
                                                       double range,
                                                       double bearing) {
                double EarthRadius = 6371000; // m
                
                double latA = Math.toRadians(point.x);
                double lonA = Math.toRadians(point.y);
                double angularDistance = range / EarthRadius;
                double trueCourse = Math.toRadians(bearing);
                
                double lat = Math.asin(Math.sin(latA) * Math.cos(angularDistance) + Math.cos(latA) * Math.sin(angularDistance) * Math.cos(trueCourse));
                
                double dlon = Math.atan2(Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latA), Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));
                
                double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;
                
                lat = Math.toDegrees(lat);
                lon = Math.toDegrees(lon);
                
                PointF newPoint = new PointF((float) lat, (float) lon);
                
                return newPoint;
                
        }
        
        private static boolean pointIsInCircle(
                                               PointF pointForCheck,
                                               PointF center,
                                               double radius) {
                if (getDistanceBetweenTwoPoints(pointForCheck, center) <= radius) return true;
                else return false;
        }
        
        private static double getDistanceBetweenTwoPoints(PointF p1, PointF p2) {
                double R = 6371000; // m
                double dLat = Math.toRadians(p2.x - p1.x);
                double dLon = Math.toRadians(p2.y - p1.y);
                double lat1 = Math.toRadians(p1.x);
                double lat2 = Math.toRadians(p2.x);
                
                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double d = R * c;
                
                return d;
        }
        
}
