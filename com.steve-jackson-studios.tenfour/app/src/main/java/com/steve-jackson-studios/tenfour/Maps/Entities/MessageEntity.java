package com.steve-jackson-studios.tenfour.Maps.Entities;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by sjackson on 9/20/2017.
 * MessageEntity
 */

public class MessageEntity {

    private Marker _marker;
    private boolean _visible;

    protected MessageEntity(Marker marker) {
        this._marker = marker;
    }

    public void setVisible(boolean isVisible) {
        this._visible = isVisible;
        this._marker.setVisible(isVisible);
    }

    public boolean isReady() {
        return !_visible;
    }

    public void load(Bitmap bitmap, LatLng location) {
        this._visible = true;
        this._marker.setVisible(true);
        this._marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        this._marker.setPosition(location);
    }

}
