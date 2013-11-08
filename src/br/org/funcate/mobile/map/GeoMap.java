package br.org.funcate.mobile.map;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.Utility;
import br.org.funcate.mobile.form.GeoForm;
import br.org.funcate.mobile.task.Task;
import br.org.funcate.mobile.task.TaskActivity;
import br.org.funcate.mobile.task.TaskDatabase;
import br.org.funcate.mobile.task.TaskDatabaseHelper;

import com.j256.ormlite.dao.Dao;

public class GeoMap extends Activity {

	private MapView mapView;
	private MapController controller;

	// other activities
	private static final int GEOFORM = 101;

	protected LocationManager locationManager;
	protected ArrayList<OverlayItem> overlayItems;
	// private LayoutInflater controlInflater = null;

	private TaskDatabase db;

	private Location lastLocation;
	private GeoMap self = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_geomap);
		
		db = TaskDatabaseHelper.getDatabase(this);

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);

		controller = mapView.getController();
		controller.setZoom(16);
		controller.setCenter(new GeoPoint(-23.1791, -45.8872));

		// overlay location
		overlayItems = new ArrayList<OverlayItem>();
		//DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
		//MyItemizedIconOverlay myItemizedIconOverlay = new MyItemizedIconOverlay(overlayItems, null, defaultResourceProxyImpl, "green");
		//mapView.getOverlays().add(myItemizedIconOverlay);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (lastLocation != null) {
			updateLoc(lastLocation);
		}

		/*
		 * 
		 * controlInflater = LayoutInflater.from(getBaseContext()); View
		 * viewControl = controlInflater.inflate(R.layout.geomap_control, null);
		 * LayoutParams layoutParamsControl = new
		 * LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		 * this.addContentView(viewControl, layoutParamsControl);
		 * 
		 * // buttons ImageButton bt_form = (ImageButton)
		 * findViewById(R.id.geomap_control_bt_form); ImageButton bt_back =
		 * (ImageButton) findViewById(R.id.geomap_control_bt_back); ImageButton
		 * bt_tasks = (ImageButton)
		 * findViewById(R.id.geomap_control_bt_update_tasks);
		 * 
		 * bt_form.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { self.openGeoform(); } });
		 * 
		 * bt_back.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { self.finishThisScreen(); }
		 * });
		 * 
		 * bt_tasks.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { self.openTaskScreen(); } });
		 */
	}

	public void openGeoform() {
		Intent i = new Intent(GeoMap.this, GeoForm.class);
		i.putExtra("CURRENT_LOCATION", self.lastLocation);
		startActivityForResult(i, GEOFORM);
	}

	public void openTaskScreen() {
		Intent occupantNewIntent = new Intent(GeoMap.this, TaskActivity.class);
		startActivity(occupantNewIntent);
	}

	public void finishThisScreen() {
		setResult(RESULT_CANCELED, new Intent());
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//locationManager.removeUpdates(myLocationListener);
	}

	private void updateLoc(Location loc) {
		GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		controller.setCenter(locGeoPoint);
		//setOverlayLoc(loc);
		mapView.invalidate();
	}

	private synchronized void addLandmarkToMap(Location overlayloc) {
		GeoPoint overlocGeoPoint = new GeoPoint(overlayloc);
		// overlayItems.clear();
		OverlayItem newMyLocationItem = new OverlayItem("ID", "My Location",
				"My Location", overlocGeoPoint);
		overlayItems.add(newMyLocationItem);
	}

	/**
	 * Update all landmarks from the map.
	 * 
	 * @author Paulo Luan
	 * @param tasks
	 */
	public void updateLandmarks(List<Task> tasks) {
	}

	public void showLandmarks() {
		try {
			Dao<Task, Integer> dao = db.getTaskDao();
			List<Task> features = dao.queryForAll();

			for (Task feature : features) {
				Double latitude = feature.getLatitude();
				Double longitude = feature.getLongitude();

				GeoPoint geoPoint = new GeoPoint(latitude, longitude);
				MyItemizedOverlay overlayItem = createItemOverlay(geoPoint, "red");
				
				this.addLandmarksToMap(overlayItem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates an overlay item.
	 * @return
	 */
	public MyItemizedOverlay createItemOverlay(GeoPoint p, String color) {		
		ArrayList<OverlayItem> overlayItemArray = new ArrayList<OverlayItem>();         
		MyItemizedOverlay overlay = new MyItemizedOverlay(this, overlayItemArray);
		
		OverlayItem overlayItem = new OverlayItem("Test Overlay", "Teste 123", p);
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		
		Drawable marker;
		
		if(color == "green"){
			marker = getResources().getDrawable(R.drawable.ic_landmark_green);
		} else {
			marker = getResources().getDrawable(R.drawable.ic_landmark_red);
		}
		
		overlayItem.setMarker(marker);
		
		overlay.addItem(overlayItem);
		
		return overlay;
	}
	
	public void addLandmarksToMap(MyItemizedOverlay overlay){		
		mapView.getOverlays().add(overlay);
		mapView.invalidate();
	}

	// TODO: listar todos os tasks e fazer landmarks disto, abrindo a tela de formulário baseado no objeto clicado.

	/*
	 * 
	 * Menu de contexto, ao clicar no botão de opções, automaticamente o menu
	 * "geomap.xml" é exibido ao usuário.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.geomap, menu);
		return true;
	}

	/*
	 * Esta função é executada ao ser clicado em qualquer um dos itens do menu
	 * de contexto desta tela.
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GEOFORM) {
			if (resultCode == RESULT_OK) {
				String result = data.getExtras().getString("RESULT");
				Utility.showToast(result, Toast.LENGTH_SHORT, GeoMap.this);
			} else if (resultCode == RESULT_CANCELED) {
			}
		}
	}

}