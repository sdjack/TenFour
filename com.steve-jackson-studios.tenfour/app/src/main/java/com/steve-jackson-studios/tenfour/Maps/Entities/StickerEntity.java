package com.steve-jackson-studios.tenfour.Maps.Entities;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sjackson on 9/22/2017.
 * StickerEntity
 */

public class StickerEntity {

    private GroundOverlay _overlay;
    private boolean _visible;

    public StickerEntity(GroundOverlay overlay) {
        this._overlay = overlay;
    }

    public void setVisible(boolean isVisible) {
        this._visible = isVisible;
        this._overlay.setVisible(isVisible);
    }

    public boolean isReady() {
        return !_visible;
    }

    public void load(Bitmap bitmap, LatLng location) {
        this._visible = true;
        this._overlay.setVisible(true);
        this._overlay.setImage(BitmapDescriptorFactory.fromBitmap(bitmap));
        this._overlay.setPosition(location);
    }
}
