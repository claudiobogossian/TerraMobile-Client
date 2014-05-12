package br.inova.mobile.map;

import java.sql.SQLException;
import java.util.Arrays;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import br.inova.mobile.Utility;
import br.inova.mobile.exception.ExceptionHandler;
import br.inova.mobile.form.FormActivity;
import br.inova.mobile.location.LocationProvider;
import br.inova.mobile.photo.Photo;
import br.inova.mobile.photo.PhotoDao;
import br.inova.mobile.task.Task;
import br.inova.mobile.task.TaskActivity;
import br.inova.mobile.task.TaskDao;
import br.inova.mobile.user.SessionManager;
import br.inpe.mobile.R;

import com.j256.ormlite.dao.CloseableIterator;

public class GeoMap extends Activity {
        
        /** the canvas of the Map. */
        private MapView          mapView;
        
        /** the controller of the map. */
        private MapController    controller;
        
        /** the location of the user */
        private Location         location;
        
        /**
         * the code of the Activities, used on the callback 'onActivityResult'
         * function.
         */
        private static final int GEOFORM = 101;
        private static final int TASK    = 103;
        
        private GeoMap           self    = this;
        
        private LayoutInflater   inflater;
        
        private LandmarksManager landmarksManager;
        
        public static String     tileSourcePath;
        
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
                
                self.createInflatorForGpsButton();
                self.createMapView();
                self.createTileSource();
                self.createLandmarks();
                self.initializeLocation();
                self.createButtonUpdateLocation();
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
        
        /**
         * Creates the inflator Layout to show the gps button.
         */
        public void createInflatorForGpsButton() {
                inflater = LayoutInflater.from(getBaseContext());
                View viewControl = inflater.inflate(R.layout.geomap_gps, null);
                LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
                this.addContentView(viewControl, layoutParamsControl);
        }
        
        /**
         * Creates and handler the button that creates the user location update.
         * */
        public void createButtonUpdateLocation() {
                ImageButton imageButton = (ImageButton) findViewById(R.id.btn_update_location);
                imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                landmarksManager.createMyLocationItem();
                        }
                });
        }
        
        /**
         * Creates the MapView object, the main object of the activity,
         * responsible for controller of all the map.
         * */
        public void createMapView() {
                mapView = (MapView) findViewById(R.id.mapview);
                mapView.setMaxZoomLevel(20);
                mapView.setBuiltInZoomControls(true);
                mapView.setMultiTouchControls(true);
        }
        
        /**
         * Creates the tile source of the application, that will be responsible
         * for the map tile reading from the zips.
         * */
        private void createTileSource() {
                OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
                tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";
                
                final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
                final ITileSource tileSource = new XYTileSource("MapquestOSM", ResourceProxy.string.mapquest_osm, 6, 20, 256, ".png", new String[] { "http://tile.openstreetmap.org/" });
                
                tileProvider.setTileSource(tileSource);
                final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getBaseContext());
                tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
                mapView.getOverlays().add(tilesOverlay);
                
                tileProvider.setTileRequestCompleteHandler(new SimpleInvalidationHandler(mapView));
                
                mapView.setTileSource(tileSource);
                mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.
        }
        
        /**
         * Creates an instance of LandMark Factory and call to the initializers
         * of the landmarks.
         * */
        private void createLandmarks() {
                landmarksManager = new LandmarksManager(mapView, this);
                landmarksManager.createMapOverlayHandler();
                landmarksManager.initializePoiMarkers();
        }
        
        /**
         * 
         * Initialize the location object and update the initial zoom and Position on the map.
         * 
         * */
        private void initializeLocation() {
                location = LocationProvider.getInstance(this).getLocation();
                controller = (MapController) mapView.getController();

                controller.setZoom(12);
                controller.setCenter(new GeoPoint(-22.32261, -49.028732));
                
                /*if (location != null) {
                        landmarksManager.createMyLocationItem();
                }
                else {
                        controller.setZoom(12);
                        controller.setCenter(new GeoPoint(-22.32261, -49.028732));
                }*/
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
        }
        
        @Override
        protected void onPause() {
                super.onPause();
        }
        
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
