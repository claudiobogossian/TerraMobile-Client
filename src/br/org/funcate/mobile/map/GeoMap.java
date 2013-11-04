package br.org.funcate.mobile.map;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.Utility;
import br.org.funcate.mobile.form.GeoForm;

public class GeoMap extends Activity {

	private MapView mapView;
	private MapController controller;

	// other activities
	private static final int GEOFORM = 101;

	protected LocationManager locationManager;
	protected ArrayList<OverlayItem> overlayItems;
	private LayoutInflater controlInflater = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_geomap);

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);
		controller = mapView.getController();
		controller.setZoom(16);
		controller.setCenter(new GeoPoint(-23.1791, -45.8872));

		controlInflater = LayoutInflater.from(getBaseContext());
		View viewControl = controlInflater.inflate(R.layout.geomap_control,
				null);
		LayoutParams layoutParamsControl = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.addContentView(viewControl, layoutParamsControl);

		// overlay location
		overlayItems = new ArrayList<OverlayItem>();
		DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(
				this);
		MyItemizedIconOverlay myItemizedIconOverlay = new MyItemizedIconOverlay(
				overlayItems, null, defaultResourceProxyImpl);
		mapView.getOverlays().add(myItemizedIconOverlay);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Location lastLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastLocation != null) {
			updateLoc(lastLocation);
		}

		// buttons
		ImageButton bt_form = (ImageButton) findViewById(R.id.geomap_control_bt_form);
		ImageButton bt_back = (ImageButton) findViewById(R.id.geomap_control_bt_back);

		bt_form.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(GeoMap.this, GeoForm.class);
				i.putExtra("CURRENT_LOCATION", locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER));
				startActivityForResult(i, GEOFORM);
			}
		});

		bt_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED, new Intent());
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, myLocationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(myLocationListener);
	}

	private void updateLoc(Location loc) {
		GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(),
				loc.getLongitude());
		controller.setCenter(locGeoPoint);
		setOverlayLoc(loc);
		mapView.invalidate();
	}

	private void setOverlayLoc(Location overlayloc) {
		GeoPoint overlocGeoPoint = new GeoPoint(overlayloc);
		overlayItems.clear();
		OverlayItem newMyLocationItem = new OverlayItem("My Location",
				"My Location", overlocGeoPoint);
		overlayItems.add(newMyLocationItem);
	}

	private final LocationListener myLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			updateLoc(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	};

	private class MyItemizedIconOverlay extends
			ItemizedIconOverlay<OverlayItem> {

		public MyItemizedIconOverlay(
				List<OverlayItem> pList,
				ItemizedIconOverlay.OnItemGestureListener<OverlayItem> pOnItemGestureListener,
				ResourceProxy pResourceProxy) {
			super(pList, pOnItemGestureListener, pResourceProxy);
		}

		@Override
		public void draw(Canvas canvas, MapView mapview, boolean arg2) {
			super.draw(canvas, mapview, arg2);
			if (!overlayItems.isEmpty()) {
				GeoPoint in = overlayItems.get(0).getPoint();
				Point out = new Point();
				mapview.getProjection().toPixels(in, out);
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_menu_mylocation);
				canvas.drawBitmap(bm, out.x - bm.getWidth() / 2,
						out.y - bm.getHeight() / 2, null);
			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event, MapView mapView) {
			return true;
		}

		@Override
		protected boolean onSingleTapUpHelper(int index, OverlayItem item,
				MapView mapView) {
			return super.onSingleTapUpHelper(index, item, mapView);
		}
	}

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
			return true;
		case R.id.btnContextGetJobs:
			return true;
		case R.id.btnContextNewForm:
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