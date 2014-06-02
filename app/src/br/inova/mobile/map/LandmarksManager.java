package br.inova.mobile.map;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.MotionEvent;
import br.inova.mobile.address.Address;
import br.inova.mobile.location.DistanceFilter;
import br.inova.mobile.location.LocationProvider;
import br.inova.mobile.task.Task;
import br.inova.mobile.task.TaskDao;
import br.inpe.mobile.R;

import com.j256.ormlite.dao.CloseableIterator;

public class LandmarksManager {
        
        /** The user location landmark . */
        private OverlayItem                                           myLocationOverlayItem;
        ItemizedIconOverlay<OverlayItem>                              currentLocationOverlay;
        
        /** the window that appear the informations about the Task */
        private static PointOfInterestInfoWindow                      poiInfoWindow;
        
        /** The landmarks that represents the List of Tasks objects. */
        private static ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;
        
        public static Context                                         context;
        
        private static MapView                                        mapView;
        
        public LandmarksManager(MapView mapView, Context context) {
                this.mapView = mapView;
                this.context = context;
        }
        
        public void initializePoiMarkers() {
                poiInfoWindow = new PointOfInterestInfoWindow(mapView);
                final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
                
                poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(context, poiItems, mapView, poiInfoWindow);
                createPoiMarkers();
                
                // POI markers:
                mapView.getOverlays().add(poiMarkers);
                mapView.invalidate();
        }
        
        public PointOfInterestInfoWindow getPoiWindow() {
                return poiInfoWindow;
        }
        
        /**
         * Set the object in the List of poiMarkers, updating the UI.
         * 
         * @param Task
         *                the task that will be updated into the map.
         * */
        private static void updatePoiMarkers(Task feature) {
                
                for (int i = 0; i < poiMarkers.size(); i++) {
                        ExtendedOverlayItem item = poiMarkers.getItem(i);
                        
                        Task relatedTask = (Task) item.getRelatedObject();
                        boolean isIdEquals = relatedTask.getId().equals(feature.getId());
                        
                        if (isIdEquals) {
                                Drawable marker = null;
                                
                                if (feature.isDone()) {
                                        marker = context.getResources().getDrawable(R.drawable.ic_landmark_green);
                                }
                                else {
                                        marker = context.getResources().getDrawable(R.drawable.ic_landmark_red);
                                }
                                
                                item.setMarker(marker);
                                item.setRelatedObject(feature);
                        }
                }
                
                mapView.invalidate();
        }
        
        /**
         * Sets an Task, updating the landmarks on the map.
         * 
         * @param Task
         *                the task that will be updated.
         * */
        public static void setTask(final Task task) {
                
                Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                                updatePoiMarkers(task);
                        }
                };
                
                new Thread(runnable).start();
        }
        
        /**
         * Makes an Iterator of database and gets the registers from database,
         * then, creates an Overlay Item List of all that registers.
         * 
         * @param Context
         *                the context used by the function "createOverlayItem"
         * */
        public static void createPoiMarkers() {
                CloseableIterator<Task> taskIterator = TaskDao.getIteratorForAllTasksForCurrentUser();
                poiMarkers.removeAllItems();
                
                try {
                        while (taskIterator.hasNext()) {
                                Task feature = (Task) taskIterator.next();
                                
                                Double lat = feature.getAddress().getCoordy();
                                Double lon = feature.getAddress().getCoordx();
                                
                                if (lat != null && lon != null) {
                                        ExtendedOverlayItem poiMarker = createOverlayItem(feature);
                                        poiMarkers.addItem(poiMarker);
                                }
                        }
                }
                catch (Exception e) {
                        e.printStackTrace();
                }
                finally {
                        mapView.invalidate();
                        taskIterator.closeQuietly();
                }
        }
        
        /**
         * Creates an overlay item.
         * 
         * @param Task
         *                the task that will be a Landmark
         * @param Context
         *                the context that will be used to get the informations
         *                of drawable image and to construct the poiMarker
         *                object.
         * @return
         */
        public static ExtendedOverlayItem createOverlayItem(Task feature) {
                Address address = feature.getAddress();
                Double latitude = address.getCoordy();
                Double longitude = address.getCoordx();
                GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                
                String featureString = "Nome : " + feature.getAddress().getName() + "\nNúmero : " + feature.getAddress().getNumber() + "\nLote : " + feature.getAddress().getFeatureId();
                
                ExtendedOverlayItem poiMarker = new ExtendedOverlayItem(featureString, "", geoPoint, context);
                Drawable marker = null;
                
                if (feature.isDone()) {
                        marker = context.getResources().getDrawable(R.drawable.ic_landmark_green);
                }
                else {
                        marker = context.getResources().getDrawable(R.drawable.ic_landmark_red);
                }
                
                poiMarker.setMarker(marker);
                
                // thumbnail loading moved in PointOfInterestInfoWindow.onOpen for better performances.
                poiMarker.setRelatedObject(feature);
                
                return poiMarker;
        }
        
        public void createMyLocationItem() {
                Location location = LocationProvider.getInstance(context).getLocation();
                
                /** the controller of the map. */
                MapController controller = (MapController) mapView.getController();
                
                if (location != null) {
                        poiInfoWindow.close();
                        
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        
                        myLocationOverlayItem = new OverlayItem("", "", geoPoint);
                        Drawable myCurrentLocationMarker = context.getResources().getDrawable(R.drawable.person);
                        myLocationOverlayItem.setMarker(myCurrentLocationMarker);
                        
                        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
                        items.add(myLocationOverlayItem);
                        
                        if (currentLocationOverlay != null) {
                                currentLocationOverlay.removeAllItems();
                        }
                        else { // (creation) first time set the zoom.
                                controller.setZoom(16);
                        }
                        
                        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(context.getApplicationContext());
                        currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                public boolean onItemSingleTapUp(
                                                                 final int index,
                                                                 final OverlayItem item) {
                                        return true;
                                }
                                
                                public boolean onItemLongPress(
                                                               final int index,
                                                               final OverlayItem item) {
                                        return true;
                                }
                        }, resourceProxy);
                        
                        mapView.getOverlays().add(currentLocationOverlay);
                        
                        controller.setCenter(geoPoint);
                }
        }
        
        public void createTestLandmark(GeoPoint geoPoint) {
                /** the controller of the map. */
                MapController controller = (MapController) mapView.getController();
                
                myLocationOverlayItem = new OverlayItem("", "", geoPoint);
                Drawable myCurrentLocationMarker = context.getResources().getDrawable(R.drawable.ic_landmark_red);
                myLocationOverlayItem.setMarker(myCurrentLocationMarker);
                
                final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
                items.add(myLocationOverlayItem);
                
                controller.setZoom(16);
                
                ResourceProxy resourceProxy = new DefaultResourceProxyImpl(context.getApplicationContext());
                currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        public boolean onItemSingleTapUp(
                                                         final int index,
                                                         final OverlayItem item) {
                                return true;
                        }
                        
                        public boolean onItemLongPress(
                                                       final int index,
                                                       final OverlayItem item) {
                                return true;
                        }
                }, resourceProxy);
                
                mapView.getOverlays().add(currentLocationOverlay);
                
                controller.setCenter(geoPoint);
        }
        
        /**
         * 
         * Filter the landmarks by touching on the map.
         * 
         * @param Geopoint
         *                the point that user has clicked.
         * 
         * */
        public static void filterByGeopoint(IGeoPoint geoPoint) {
                PointF center = new PointF((float) geoPoint.getLatitude(), (float) geoPoint.getLongitude());
                
                List<Task> tasks = DistanceFilter.getNearestAddresses(center, 10.0);
                
                //Utility.showToast("Filtro efetuado! " + geoPoint, Toast.LENGTH_SHORT, context);
        }
        
        /**
         * Creates an map overlay to handler the touch on the map and closes the
         * popup of the landmark
         * */
        public void createMapOverlayHandler() {
                MapOverlay movl = new MapOverlay(context);
                mapView.getOverlays().add(movl);
        }
        
        /**
         * This class is used to close the landmark pop up when the user clicks
         * out of the popup
         * */
        public class MapOverlay extends org.osmdroid.views.overlay.Overlay {
                
                public MapOverlay(Context ctx) {
                        super(ctx);
                }
                
                @Override
                protected void draw(Canvas c, MapView osmv, boolean shadow) {}
                
                @Override
                public boolean onTouchEvent(MotionEvent e, MapView mapView) {
                        if (e.getAction() == MotionEvent.ACTION_DOWN) if (poiInfoWindow.isOpen()) poiInfoWindow.close();
                        return false;
                }
                
                @Override
                public boolean onLongPress(MotionEvent e, MapView mapView) {
                        Projection proj = mapView.getProjection();
                        IGeoPoint geoPoint = proj.fromPixels(e.getX(), e.getY());
                        
                        LandmarksManager.filterByGeopoint(geoPoint);
                        
                        return false;
                }
        }
        
}
