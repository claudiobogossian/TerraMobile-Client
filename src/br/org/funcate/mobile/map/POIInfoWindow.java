package br.org.funcate.mobile.map;

import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.overlays.DefaultInfoWindow;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.views.MapView;

import android.view.View;
import android.widget.Button;
import br.org.funcate.mobile.R;
import br.org.funcate.mobile.task.Task;

/**
 * A customized InfoWindow handling POIs. 
 * We inherit from DefaultInfoWindow as it already provides most of what we want. 
 * And we just add support for a "more info" button. 
 * 
 * @author M.Kergall
 */
public class POIInfoWindow extends DefaultInfoWindow {
	
	private Task task;
	
	NominatimPOIProvider poiProvider = new NominatimPOIProvider();
	
	public POIInfoWindow(MapView mapView) {
		super(R.layout.bubble_white, mapView);
		
		Button btn = (Button)(mView.findViewById(R.id.bubble_moreinfo));
			//bonuspack_bubble layouts already contain a "more info" button. 
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				/*
				// TODO: Fazer com que Intent abra com o objeto agregado
				if (mSelectedPOI.mUrl != null){
					Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSelectedPOI.mUrl));
					view.getContext().startActivity(myIntent);
				}
				*/
			}
		});
	}

	@Override public void onOpen(Object item){
		ExtendedOverlayItem eItem = (ExtendedOverlayItem)item;
		task = (Task) eItem.getRelatedObject();
		
		super.onOpen(item);
		
		mView.findViewById(R.id.bubble_moreinfo).setVisibility(View.VISIBLE);	
	}
}