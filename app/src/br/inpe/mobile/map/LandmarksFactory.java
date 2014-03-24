package br.inpe.mobile.map;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.MotionEvent;
import br.inpe.mobile.R;
import br.inpe.mobile.address.Address;
import br.inpe.mobile.location.LocationProvider;
import br.inpe.mobile.task.Task;
import br.inpe.mobile.task.TaskDao;

import com.j256.ormlite.dao.CloseableIterator;

public class LandmarksFactory {
        
        /** The user location landmark . */
        private OverlayItem                                           myLocationOverlayItem;
        ItemizedIconOverlay<OverlayItem>                              currentLocationOverlay;
        
        /** the window that appear the informations about the Task */
        private static PointOfInterestInfoWindow                      poiInfoWindow;
        
        /** The landmarks that represents the List of Tasks objects. */
        private static ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;
        
        public static Context                                         context;
        
        public LandmarksFactory(Context context) {
                this.context = context;
        }
        
        public void createPoiMarkers(MapView mapView, Context context) {
                
                poiInfoWindow = new PointOfInterestInfoWindow(mapView);
                final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
                
                poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(context, poiItems, mapView, poiInfoWindow);
                poiMarkers = createPoiMarkers();
                
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
        public static void updatePoiMarkers(Task feature) {
                
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
        }
        
        /**
         * Sets an Task, updating the landmarks on the map.
         * 
         * @param Task
         *                the task that will be updated.
         * */
        public void setTask(final Task task) {
                
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
        private ItemizedOverlayWithBubble<ExtendedOverlayItem> createPoiMarkers() {
                CloseableIterator<Task> taskIterator = TaskDao.getIteratorForAllTasks();
                
                try {
                        while (taskIterator.hasNext()) {
                                Task feature = (Task) taskIterator.next();
                                
                                Double lat, lon;
                                
                                lat = feature.getAddress().getCoordy();
                                lon = feature.getAddress().getCoordx();
                                
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
                        taskIterator.closeQuietly();
                }
                
                return poiMarkers;
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
        public ExtendedOverlayItem createOverlayItem(Task feature) {
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
        
        public void createMyLocationItem(MapView mapView) {
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
        
        /**
         * Creates an map overlay to handler the touch on the map and closes the
         * popup of the landmark
         * 
         * @param MapView
         *                the map view that will be added the overlay.
         * */
        public void createMapOverlayHandler(MapView mapView, Context context) {
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
        }
        
}
