package br.org.funcate.mobile.map;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

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
import br.org.funcate.mobile.address.Address;
import br.org.funcate.mobile.form.GeoForm;
import br.org.funcate.mobile.task.Task;
import br.org.funcate.mobile.task.TaskActivity;
import br.org.funcate.mobile.task.TaskDao;
import br.org.funcate.mobile.user.SessionManager;

public class GeoMap extends Activity {

	private MapView mapView;
	private MapController controller;

	// other activities
	private static final int GEOFORM = 101;
	private static final int TASK = 103;

	protected LocationManager locationManager;
	ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;

	private Location lastLocation;
	private GeoMap self = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_geomap);

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);
		//mapView.setUseDataConnection(false); // keeps the mapView from loading online tiles using network connection.
		//mapView.setUseDataConnection(true);

		controller = (MapController) mapView.getController();
		controller.setZoom(16);
		controller.setCenter(new GeoPoint(-22.317773, -49.059534));

		// new GeoPoint(-22.317773, -49.059534) // Bauru 
		// new GeoPoint(-23.157221, -45.792443) // SJC

		//POI markers:
		final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
		poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, poiItems, mapView, new POIInfoWindow(mapView));
		//poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, poiItems, mapView);
		mapView.getOverlays().add(poiMarkers);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (lastLocation != null) {
			//updateLoc(lastLocation);
		}
	}

	public void openGeoform() {
		Intent i = new Intent(self, GeoForm.class);
		i.putExtra("CURRENT_LOCATION", self.lastLocation);
		startActivityForResult(i, GEOFORM);
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
		//self.showLandmarks();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void updateLoc(Location loc) {
		Double latitude = loc.getLatitude(); // -23.157618544172863
		Double longitude = loc.getLongitude(); // -45.79068200523216
		GeoPoint locGeoPoint = new GeoPoint(latitude, longitude);
		controller.setCenter(locGeoPoint);
		//setOverlayLoc(loc);
		mapView.invalidate();
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
		List<Task> features = TaskDao.getNotFinishedTasks();

		for (Task feature : features) {

			Double lat, lon;

			lat = feature.getAddress().getCoordx();
			lon = feature.getAddress().getCoordy();

			if(lat != null && lon != null) {
				ExtendedOverlayItem poiMarker = createOverlayItem(feature);
				poiMarkers.addItem(poiMarker);
			}
		}
	}

	/**
	 * Creates an overlay item.
	 * @return
	 */
	public ExtendedOverlayItem createOverlayItem(Task feature) {
		Address address = feature.getAddress();
		Double latitude = address.getCoordx();
		Double longitude = address.getCoordy();
		GeoPoint geoPoint = new GeoPoint(latitude, longitude);

		//GeoPoint geoPoint = new GeoPoint(-22.318567, -49.060907);

		ExtendedOverlayItem poiMarker = new ExtendedOverlayItem("Dados do Terreno", feature.toString(), geoPoint, this);
		Drawable marker = null;

		if(feature.isDone()){
			marker = getResources().getDrawable(R.drawable.ic_landmark_green);
		} else {
			marker = getResources().getDrawable(R.drawable.ic_landmark_red);
		}

		poiMarker.setMarker(marker);
		//poiMarker.setMarkerHotspot(poiMarker.HotspotPlace.CENTER);

		//thumbnail loading moved in POIInfoWindow.onOpen for better performances. 
		poiMarker.setRelatedObject(feature);

		//TODO: remove
		controller.setCenter(geoPoint);

		return poiMarker;
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
		
		if(resultCode == 999) {
			finish();
		}
		
		if (requestCode == GEOFORM) {
			if (resultCode == RESULT_OK) {
				String result = data.getExtras().getString("RESULT");
				Utility.showToast(result, Toast.LENGTH_LONG, GeoMap.this);
			} else if (resultCode == RESULT_CANCELED) {
			}
		}
		if(requestCode == TASK){
			if(resultCode == RESULT_OK){

			}
		}
	}
}
