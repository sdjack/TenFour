package com.steve-jackson-studios.tenfour.Maps.Entities;

import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Data.FriendEntity;

/**
 * Created by sjackson on 9/13/2017.
 * UserEntity
 */

public class UserEntity {

    public final String id;
    public final String title;
    public final int color;

    private LatLng _center;
    private Marker _marker;
    private BitmapDescriptor _icon;
    private FriendEntity entity;

    public UserEntity(double lat, double lon, String id, String title) {
        this.id = id;
        this.title = title;
        this.color = AppConstants.AVATAR_COLORS[0];
        this._center = new LatLng(lat, lon);
    }

    public UserEntity(FriendEntity friendEntity) {
        this.id = friendEntity.getId();
        this.title = friendEntity.getTitle();
        this.color = friendEntity.getColor();
        this._center = friendEntity.getPosition();
        this.entity = friendEntity;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getColor() {
        return color;
    }

    public void setLocation(LatLng target) {
        this._center = target;
    }

    public LatLng getLocation() {
        return _center;
    }

    public boolean hasMarker() {
        return (_marker != null);
    }

    public void setMarker(Marker marker) {
        this._marker = marker;
    }

    public void setMarkerIcon(BitmapDescriptor markerIcon) {
        this._icon = markerIcon;
    }

    public boolean isOnline() {
        return entity.online;
    }
    public void debug() {
        Log.d("DEVDEBUG",entity.userName);
    }

    public void friendUpdate() {
        if (_marker != null && _icon != null && _center != null) {
            _marker.setPosition(_center);
            _marker.setIcon(_icon);
            _marker.setVisible(entity.online);
        }
    }

    public void update() {
        if (_marker != null && _icon != null && _center != null) {
            _marker.setPosition(_center);
            _marker.setIcon(_icon);
            _marker.setVisible(true);
        }
    }

    public void setVisible(boolean isVisible) {
        if (hasMarker()) {
            _marker.setVisible(isVisible);
        }
    }
}
