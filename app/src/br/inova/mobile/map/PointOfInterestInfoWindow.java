package br.inova.mobile.map;

import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.overlays.DefaultInfoWindow;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.views.MapView;

import android.content.Intent;
import android.view.View;
import br.inova.mobile.form.FormActivity;
import br.inova.mobile.task.Task;
import br.inpe.mobile.R;

/**
 * A customized InfoWindow handling POIs. We inherit from DefaultInfoWindow as
 * it already provides most of what we want. And we just add support for a
 * "more info" button.
 * 
 * @author M.Kergall
 */
public class PointOfInterestInfoWindow extends DefaultInfoWindow {
        
        private Task         task;
        
        NominatimPOIProvider poiProvider = new NominatimPOIProvider();
        
        public PointOfInterestInfoWindow(MapView mapView) {
                super(R.layout.bubble_white, mapView);
                
                View view = getView().findViewById(R.id.linearLayoutBubble);
                // bonuspack_bubble layouts already contain a "more info" button.
                view.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                                Intent intent = new Intent(view.getContext(), FormActivity.class);
                                intent.putExtra("task", task);
                                view.getContext().startActivity(intent);
                                close();
                        }
                });
        }
        
        @Override
        public void onOpen(Object item) {
                ExtendedOverlayItem eItem = (ExtendedOverlayItem) item;
                task = (Task) eItem.getRelatedObject();
                super.onOpen(item);
        }
}
