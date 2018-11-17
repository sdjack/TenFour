package com.steve-jackson-studios.tenfour.Maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Maps.Entities.EventEntity;
import com.steve-jackson-studios.tenfour.Misc.MultiDrawable;
import com.steve-jackson-studios.tenfour.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sjackson on 7/18/2017.
 * EventClusterManager
 */

public class EventClusterManager implements ClusterManager.OnClusterClickListener<EventEntity>, ClusterManager.OnClusterInfoWindowClickListener<EventEntity>, ClusterManager.OnClusterItemClickListener<EventEntity>, ClusterManager.OnClusterItemInfoWindowClickListener<EventEntity>, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "StreamMarkerCM";

    private ClusterManager<EventEntity> clusterManager;
    private IconGenerator g;
    private GoogleMap map;
    private GoogleMapFragment fragment;
    private Context context;
    private final Map<String, Marker> markerCache = new HashMap<>();

    public void init(Context context, GoogleMap map, GoogleMapFragment fragment) {
        this.context = context;
        this.map = map;
        this.fragment = fragment;

        this.clusterManager = new ClusterManager<>(context, map);
        this.clusterManager.setRenderer(new StreamClusterRenderer());
        this.clusterManager.setOnClusterClickListener(this);
        this.clusterManager.setOnClusterInfoWindowClickListener(this);
        this.clusterManager.setOnClusterItemClickListener(this);
        this.clusterManager.setOnClusterItemInfoWindowClickListener(this);
    }

    public void clear() {
        this.clusterManager.clearItems();
    }

    public ClusterManager<EventEntity> getClusterManager() {
        return clusterManager;
    }

    public void onInfoWindowClick(Marker marker) {
//        LatLng geo = marker.getPosition();
//        String geoUri = "http://maps.google.com/maps?q=loc:" + geo.latitude + "," + geo.longitude + " ("
//                + "MainActivity%20Conversation)";
//        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(geoUri));
//        context.startActivity(intent);

        clusterManager.onInfoWindowClick(marker);
    }

    public boolean onMarkerClick(Marker marker) {
        return clusterManager.onMarkerClick(marker);
    }

    public void onCameraIdle() {
        clusterManager.onCameraIdle();
    }

    public void addItem(EventEntity stream) {
        clusterManager.addItem(stream);
    }

    public void removeItem(EventEntity stream) {
        clusterManager.removeItem(stream);
    }

    public void cluster() {
        clusterManager.cluster();
    }

    @Override
    public boolean onClusterClick(Cluster<EventEntity> cluster) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<EventEntity> cluster) {

    }

    @Override
    public boolean onClusterItemClick(EventEntity eventEntity) {
        String locId = eventEntity.getParentId();
        String eventId = eventEntity.getId();
        AppConstants.MAP_ACTIVE_LOCATION_ID = locId;
        AppConstants.MAP_ACTIVE_EVENT_ID = eventId;
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(EventEntity eventEntity) {

    }

    private class StreamClusterRenderer extends DefaultClusterRenderer<EventEntity> {

        private final MarkerIconGenerator mIconGenerator = new MarkerIconGenerator(context);
        private final IconGenerator mClusterIconGenerator = new IconGenerator(context);
        private final ImageView mClusterImageView;
        private final ImageView mImageView;
        private final LinearLayout pinBody;
        private final FrameLayout pinTail;
        private final int mDimension;

        public StreamClusterRenderer() {
            super(context, map, clusterManager);
            mDimension = (int) context.getResources().getDimension(R.dimen.map_marker_image);
            LayoutInflater inflater = LayoutInflater.from(context);
            View clusterView = inflater.inflate(R.layout.map_cluster_layout, null);
            mClusterIconGenerator.setContentView(clusterView);
            mClusterImageView = (ImageView) clusterView.findViewById(R.id.image);
            int color = ResourcesCompat.getColor(context.getResources(), R.color.primary, null);
            mClusterIconGenerator.setColor(color);

            View markerView = inflater.inflate(R.layout.map_pin_layout, null);
            mIconGenerator.setContentView(markerView);
            mImageView = (ImageView) markerView.findViewById(R.id.image);
            pinBody = (LinearLayout) markerView.findViewById(R.id.body);
            pinTail = (FrameLayout) markerView.findViewById(R.id.tail);
        }

        @Override
        protected void onClusterItemRendered(EventEntity clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
            markerCache.put(clusterItem.getId(), marker);
        }

//        @Override
//        protected void onBeforeClusterItemRendered(EventEntity stream, MarkerOptions markerOptions) {
//            Bitmap icon = getStreamBitmap(stream);
//            pinBody.setImageBitmap(icon);
//            Bitmap bitmap = mIconGenerator.makeIcon();
//            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
//        }

        @Override
        protected void onBeforeClusterItemRendered(EventEntity entity, MarkerOptions markerOptions) {
            int size = entity.getSize(mDimension);
            if (size > 0) {
                int color = entity.getColorResource();
                mImageView.setImageResource(entity.getIconResource());
                pinBody.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                pinTail.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                Bitmap bitmap = mIconGenerator.makeIcon();
                markerOptions.visible(true).icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            } else if (entity.isMyEvenet()) {
                int color = entity.getColorResource();
                mImageView.setImageResource(entity.getIconResource());
                pinBody.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                pinTail.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                Bitmap bitmap = mIconGenerator.makeIcon();
                markerOptions.visible(true).icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            } else {
                markerOptions.visible(false);
            }
        }
//
//        @Override
//        protected void onBeforeClusterRendered(Cluster<EventEntity> cluster, MarkerOptions markerOptions) {
//            int count = cluster.getSize();
//            Bitmap bitmap = mClusterIconGenerator.makeIcon(String.valueOf(count));
//            bitmap = getClusterLayeredBitmap(bitmap, count);
//            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap)).zIndex(3);
//        }

        @Override
        protected void onBeforeClusterRendered(Cluster<EventEntity> cluster, MarkerOptions markerOptions) {
            int count = cluster.getSize();
            List<Drawable> streamIcons = new ArrayList<Drawable>(Math.min(4, count));
            for (EventEntity entity : cluster.getItems()) {
                // Draw 4 at most.
                if (streamIcons.size() == 4) break;
                Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), entity.getIconResource(), null);
                if (drawable != null) {
                    drawable.setBounds(0, 0, mDimension, mDimension);
                    streamIcons.add(drawable);
                }
            }
            MultiDrawable multiDrawable = new MultiDrawable(streamIcons);
            multiDrawable.setBounds(0, 0, mDimension, mDimension);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(count));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 1;
        }
    }

}
