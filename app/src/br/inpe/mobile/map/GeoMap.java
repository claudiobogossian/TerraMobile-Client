package br.inpe.mobile.map;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
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
import br.inpe.mobile.photo.Photo;
import br.inpe.mobile.photo.PhotoDao;
import br.inpe.mobile.task.Task;
import br.inpe.mobile.task.TaskActivity;
import br.inpe.mobile.task.TaskDao;
import br.inpe.mobile.user.SessionManager;

import com.j256.ormlite.dao.CloseableIterator;

public class GeoMap extends Activity {
        
        /** the canvas of the Map. */
        private MapView                                mapView;
        
        /** the controller of the map. */
        private MapController                          controller;
        
        /** the location of the user */
        private Location                               location;
        
        /** the window that appear the informations about the Task */
        private PointOfInterestInfoWindow              poiInfoWindow;
        
        /** The user location landmark . */
        private OverlayItem                            myLocationOverlayItem;
        ItemizedIconOverlay<OverlayItem>               currentLocationOverlay;
        
        /**
         * the code of the Activitise, used on the callback 'onActivityResult'
         * function.
         */
        private static final int                       GEOFORM = 101;
        private static final int                       TASK    = 103;
        
        /** The landmarks that represents the List of Tasks objects. */
        ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;
        
        private GeoMap                                 self    = this;
        
        private LayoutInflater                         inflater;
        
        public static String                           tileSourcePath;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
                /**
                 * Defines the default exception handler to log unexpected
                 * android errors
                 */
                
                //createFakeDataIntoTasks(); //TODO: remove
                
                this.requestWindowFeature(Window.FEATURE_NO_TITLE);
                setContentView(R.layout.activity_geomap);
                
                inflater = LayoutInflater.from(getBaseContext());
                View viewControl = inflater.inflate(R.layout.geomap_gps, null);
                LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
                this.addContentView(viewControl, layoutParamsControl);
                
                self.createButtonUpdateLocation();
                self.createMapView();
        }
        
        /**
         * 
         * Function to create some task to test the application.
         * 
         * */
        public void createFakeDataIntoTasks() {
                try {
                        Photo basePhoto = PhotoDao.getIteratorForNotSyncPhotos().first();
                        CloseableIterator<Task> taskIterator = TaskDao.getIteratorForFinishedTasks();
                        
                        while (taskIterator.hasNext()) {
                                Task task = (Task) taskIterator.next();
                                
                                task.setDone(true);
                                
                                Photo newPhoto = new Photo();
                                newPhoto.setPath(basePhoto.getPath());
                                newPhoto.setBase64(basePhoto.getBase64());
                                newPhoto.setForm(task.getForm());
                                PhotoDao.savePhotos(Arrays.asList(newPhoto));
                                
                                TaskDao.updateTask(task);
                        }
                }
                catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
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
                final ITileSource tileSource = new XYTileSource("MapquestOSM", ResourceProxy.string.mapquest_osm, 6, 20, 256, ".png", new String[] { "http://tile.openstreetmap.org/" });
                
                tileProvider.setTileSource(tileSource);
                final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getBaseContext());
                tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                mapView.getOverlays().add(tilesOverlay);
                
                mapView.setTileSource(tileSource);
                mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.
                
                MapOverlay movl = new MapOverlay(this);
                mapView.getOverlays().add(movl);
                
                poiInfoWindow = new PointOfInterestInfoWindow(mapView);
                
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
                        
                        controller.setZoom(12);
                        controller.setCenter(new GeoPoint(-22.32261, -49.028732));
                }
                
                // new GeoPoint(-25.50116, -54.62678)
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
        
        @Override
        protected void onResume() {
                super.onResume();
                self.showLandmarks();
        }
        
        @Override
        protected void onPause() {
                super.onPause();
        }
        
        /**
         * Show all landmarks to the map.
         * 
         * @author Paulo Luan
         * @param tasks
         */
        public void showLandmarks() {
                CloseableIterator<Task> taskIterator = TaskDao.getIteratorForFinishedTasks();
                
                if (poiMarkers != null) {
                        poiMarkers.removeAllItems();
                }
                
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
                
                // thumbnail loading moved in PointOfInterestInfoWindow.onOpen for better
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
                        //self.refreshMapView();
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
        
        @Override
        public void onBackPressed() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GeoMap.this);
                alertDialogBuilder.setTitle("Atenção");
                alertDialogBuilder.setMessage("Deseja realmente sair?").setCancelable(false).setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finish();
                        }
                }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                        }
                });
                
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
        }
        
}
