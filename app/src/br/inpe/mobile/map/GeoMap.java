package br.inpe.mobile.map;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import br.inpe.mobile.R;
import br.inpe.mobile.Utility;
import br.inpe.mobile.address.Address;
import br.inpe.mobile.exception.ExceptionHandler;
import br.inpe.mobile.form.FormActivity;
import br.inpe.mobile.location.LocationProvider;
import br.inpe.mobile.task.Task;
import br.inpe.mobile.task.TaskActivity;
import br.inpe.mobile.task.TaskDao;
import br.inpe.mobile.user.SessionManager;

public class GeoMap extends Activity {
        
        private MapView                                mapView;
        
        private MapController                          controller;
        
        private Location                               location;
        
        private POIInfoWindow                          poiInfoWindow;
        
        private OverlayItem                            myLocationOverlayItem;
        
        // other activities
        private static final int                       GEOFORM = 101;
        
        private static final int                       TASK    = 103;
        
        ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;
        
        ItemizedIconOverlay<OverlayItem>               currentLocationOverlay;
        
        private GeoMap                                 self    = this;
        
        private LayoutInflater                         inflater;
        
        public static String                           tileSourcePath;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
                
                this.requestWindowFeature(Window.FEATURE_NO_TITLE);
                setContentView(R.layout.activity_geomap);
                
                inflater = LayoutInflater.from(getBaseContext());
                View viewControl = inflater.inflate(R.layout.geomap_gps, null);
                LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
                this.addContentView(viewControl, layoutParamsControl);
                
                self.createButtonUpdateLocation();
                self.createMapView();
        }
        
        public void createButtonUpdateLocation() {
                ImageButton imageButton = (ImageButton) findViewById(R.id.btn_update_location);
                imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                self.createMyLocationItem();
                        }
                });
        }
        
        public void createMapView() {
                
                mapView = (MapView) findViewById(R.id.mapview);
                mapView.setMaxZoomLevel(20);
                mapView.setBuiltInZoomControls(true);
                mapView.setMultiTouchControls(true);
                
                OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
                tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";
                
                final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
                final ITileSource tileSource = new XYTileSource("MapquestOSM", ResourceProxy.string.mapquest_osm, 11, 21, 256, ".png", "http://tile.openstreetmap.org/");              
                
                tileProvider.setTileSource(tileSource);
                final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getBaseContext());
                tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                mapView.getOverlays().add(tilesOverlay);
                
                mapView.setTileSource(tileSource);
                mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.
                
                MapOverlay movl = new MapOverlay(this);
                mapView.getOverlays().add(movl);
                
                poiInfoWindow = new POIInfoWindow(mapView);
                
                final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
                poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, poiItems, mapView, poiInfoWindow);
                
                location = LocationProvider.getInstance(this).getLocation();
                controller = (MapController) mapView.getController();
                
                if (location != null) {
                        self.createMyLocationItem();
                }
                else {
                        //controller.setZoom(12);
                        //controller.setCenter(new GeoPoint(-22.317773, -49.059534));
                        
                        controller.setZoom(20);
                        controller.setCenter(new GeoPoint(-22.32261,-49.028732));
                }
                
                // new GeoPoint(-22.317773, -49.059534) // Bauru
                // new GeoPoint(-23.157221, -45.792443) // SJC
                
                // POI markers:
                
                // poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this,
                // poiItems, mapView);
                mapView.getOverlays().add(poiMarkers);
                mapView.invalidate();
        }
        
        public void createMyLocationItem() {
                location = LocationProvider.getInstance(this).getLocation();
                
                if (location != null) {
                        poiInfoWindow.close();
                        
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        
                        myLocationOverlayItem = new OverlayItem("", "", geoPoint);
                        Drawable myCurrentLocationMarker = getResources().getDrawable(R.drawable.person);
                        myLocationOverlayItem.setMarker(myCurrentLocationMarker);
                        
                        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
                        items.add(myLocationOverlayItem);
                        
                        if (currentLocationOverlay != null) {
                                currentLocationOverlay.removeAllItems();
                        }
                        else { // (creation) first time set the zoom.
                                controller.setZoom(16);
                        }
                        
                        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
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
        
        public void openGeoform() {
                long count = TaskDao.getCountOfTasks();
                
                if (count == 0) {
                        Utility.showToast("Você não tem nenhum registro salvo, sincronize seu aplicativo.", Toast.LENGTH_LONG, GeoMap.this);
                }
                else {
                        Intent i = new Intent(self, FormActivity.class);
                        startActivityForResult(i, GEOFORM);
                }
        }
        
        public void openTaskScreen() {
                Intent taskIntent = new Intent(self, TaskActivity.class);
                startActivityForResult(taskIntent, TASK);
        }
        
        public void finishThisScreen() {
                SessionManager.logoutUser();
                setResult(RESULT_CANCELED, new Intent());
                finish();
        }
        
        /**
         * In order for the MapView to start loading a new tile archive file,
         * you need the MapTileFileArchiveProvider class to call its
         * findArchiveFiles() function. As it is coded now, this only happens
         * when the MapTileFileArchiveProvider class is constructed, and when
         * the system sends a ACTION_MEDIA_UNMOUNTED/ACTION_MEDIA_MOUNTED
         * notification.
         * 
         * */
        public void refreshMapView() {
                
                Uri uri = Uri.parse("file://" + this.tileSourcePath);
                
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, uri));
        }
        
        @Override
        protected void onResume() {
                super.onResume();
                self.refreshMapView();
                self.showLandmarks();
        }
        
        @Override
        protected void onPause() {
                super.onPause();
        }
        
        /**
         * Update all landmarks from the map.
         * 
         * @author Paulo Luan
         * @param tasks
         */
        public void updateLandmarks(List<Task> tasks) {}
        
        public void showLandmarks() {
                List<Task> features = TaskDao.getAllTasks();
                
                if (poiMarkers != null) {
                        poiMarkers.removeAllItems();
                }
                
                for (Task feature : features) {
                        
                        Double lat, lon;
                        
                        lat = feature.getAddress().getCoordy();
                        lon = feature.getAddress().getCoordx();
                        
                        if (lat != null && lon != null) {
                                ExtendedOverlayItem poiMarker = createOverlayItem(feature);
                                poiMarkers.addItem(poiMarker);
                        }
                }
        }
        
        /**
         * Creates an overlay item.
         * 
         * @return
         */
        public ExtendedOverlayItem createOverlayItem(Task feature) {
                Address address = feature.getAddress();
                Double latitude = address.getCoordy();
                Double longitude = address.getCoordx();
                GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                
                // GeoPoint geoPoint = new GeoPoint(-22.318567, -49.060907);
                
                String featureString = "Nome : " + feature.getAddress().getName() + "\nNúmero : " + feature.getAddress().getNumber() + "\nLote : " + feature.getAddress().getFeatureId();
                
                ExtendedOverlayItem poiMarker = new ExtendedOverlayItem(featureString, "", geoPoint, this);
                Drawable marker = null;
                
                if (feature.isDone()) {
                        marker = getResources().getDrawable(R.drawable.ic_landmark_green);
                }
                else {
                        marker = getResources().getDrawable(R.drawable.ic_landmark_red);
                }
                
                poiMarker.setMarker(marker);
                // poiMarker.setMarkerHotspot(poiMarker.HotspotPlace.CENTER);
                
                // thumbnail loading moved in POIInfoWindow.onOpen for better
                // performances.
                poiMarker.setRelatedObject(feature);
                
                // controller.setCenter(geoPoint);
                
                return poiMarker;
        }
        
        // TODO: listar todos os tasks e fazer landmarks disto, abrindo a tela de
        // formulário baseado no objeto clicado.
        
        /*
         * 
         * Menu de contexto, ao clicar no botão de opções, automaticamente o
         * menu "geomap.xml" é exibido ao usuário.
         */
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                getMenuInflater().inflate(R.menu.geomap, menu);
                return true;
        }
        
        /*
         * Esta função é executada ao ser clicado em qualquer um dos itens do
         * menu de contexto desta tela.
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                        case R.id.btnContextExit:
                                self.finishThisScreen();
                                return true;
                        case R.id.btnContextGetTasks:
                                self.openTaskScreen();
                                return true;
                        case R.id.btnContextNewForm:
                                self.openGeoform();
                                return true;
                        default:
                                return super.onOptionsItemSelected(item);
                }
        }
        
        @Override
        public void onActivityResult(
                                     int requestCode,
                                     int resultCode,
                                     Intent data) {
                
                if (resultCode == 999) {
                        finish();
                }
                
                if (requestCode == GEOFORM) {
                        if (resultCode == RESULT_OK) {
                                String result = data.getExtras().getString("RESULT");
                                Utility.showToast(result, Toast.LENGTH_LONG, GeoMap.this);
                        }
                        else if (resultCode == RESULT_CANCELED) {}
                }
                if (requestCode == TASK) {
                        if (resultCode == RESULT_OK) {
                                
                        }
                }
        }
        
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
