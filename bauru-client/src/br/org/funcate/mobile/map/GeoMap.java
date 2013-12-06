package br.org.funcate.mobile.map;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.Utility;
import br.org.funcate.mobile.address.Address;
import br.org.funcate.mobile.form.FormActivity;
import br.org.funcate.mobile.task.Task;
import br.org.funcate.mobile.task.TaskActivity;
import br.org.funcate.mobile.task.TaskDao;
import br.org.funcate.mobile.user.SessionManager;

public class GeoMap extends Activity implements LocationListener {

	private MapView mapView;
	private MapController controller;

	private LocationManager locationManager;
	private Location location;

	// other activities
	private static final int GEOFORM = 101;
	private static final int TASK = 103;

	ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;

	private GeoMap self = this;

	private LayoutInflater inflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_geomap);

		inflater = LayoutInflater.from(getBaseContext());
		View viewControl = inflater.inflate(R.layout.geomap_gps, null);
		LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.addContentView(viewControl, layoutParamsControl);
		ImageButton imageButton = (ImageButton) findViewById(R.id.btn_update_location);
		imageButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (location != null) {
					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					controller.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
				}
			}
		});

		self.createMapView();
	}

	public void createMapView() {
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);
		// mapView.setUseDataConnection(false); // keeps the mapView from
		// loading online tiles using network connection.
		// mapView.setUseDataConnection(true);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		controller = (MapController) mapView.getController();

		if (location != null) {
			controller.setZoom(16);
			controller.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
			// habilita botão
			// mostra landmark da posição atual.
			MyLocationOverlay myLocationOverlay = new MyLocationOverlay(getApplicationContext(), mapView);
			mapView.getOverlays().add(myLocationOverlay);
		    myLocationOverlay.enableCompass();
		    myLocationOverlay.enableMyLocation();
		    myLocationOverlay.enableFollowLocation();
		} else {
			controller.setZoom(4);
			controller.setCenter(new GeoPoint(-22.317773, -49.059534));
		}

		// new GeoPoint(-22.317773, -49.059534) // Bauru
		// new GeoPoint(-23.157221, -45.792443) // SJC

		// POI markers:
		final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
		poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this,
				poiItems, mapView, new POIInfoWindow(mapView));
		// poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this,
		// poiItems, mapView);
		mapView.getOverlays().add(poiMarkers);
	}

	public void openGeoform() {
		Intent i = new Intent(self, FormActivity.class);
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
		// self.showLandmarks();
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
	public void updateLandmarks(List<Task> tasks) {
	}

	public void showLandmarks() {
		List<Task> features = TaskDao.getNotFinishedTasks();

		for (Task feature : features) {

			Double lat, lon;

			lat = feature.getAddress().getCoordx();
			lon = feature.getAddress().getCoordy();

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
		Double latitude = address.getCoordx();
		Double longitude = address.getCoordy();
		GeoPoint geoPoint = new GeoPoint(latitude, longitude);

		// GeoPoint geoPoint = new GeoPoint(-22.318567, -49.060907);

		ExtendedOverlayItem poiMarker = new ExtendedOverlayItem(
				"Dados do Terreno", feature.toString(), geoPoint, this);
		Drawable marker = null;

		if (feature.isDone()) {
			marker = getResources().getDrawable(R.drawable.ic_landmark_green);
		} else {
			marker = getResources().getDrawable(R.drawable.ic_landmark_red);
		}

		poiMarker.setMarker(marker);
		// poiMarker.setMarkerHotspot(poiMarker.HotspotPlace.CENTER);

		// thumbnail loading moved in POIInfoWindow.onOpen for better
		// performances.
		poiMarker.setRelatedObject(feature);

		// TODO: remove
		controller.setCenter(geoPoint);

		return poiMarker;
	}

	// TODO: listar todos os tasks e fazer landmarks disto, abrindo a tela de
	// formulário baseado no objeto clicado.

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

		if (resultCode == 999) {
			finish();
		}

		if (requestCode == GEOFORM) {
			if (resultCode == RESULT_OK) {
				String result = data.getExtras().getString("RESULT");
				Utility.showToast(result, Toast.LENGTH_LONG, GeoMap.this);
			} else if (resultCode == RESULT_CANCELED) {
			}
		}
		if (requestCode == TASK) {
			if (resultCode == RESULT_OK) {

			}
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
}
