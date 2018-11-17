package com.steve-jackson-studios.tenfour.Maps.Entities;

import com.google.android.gms.maps.model.LatLng;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Data.EventDataModel;

/**
 * Created by sjackson on 7/18/2017.
 * EventEntity
 */

public class EventEntity extends ClusterEntity {

    private EventDataModel _model;

    public EventEntity(EventDataModel model) {
        super(0, model.getId(), model.getLocationId());
        this._model = model;
    }

    public int getColor() {
        return AppConstants.MAPDATA_ACTIVE_COLORS[_model.getRating()];
    }

    public int getSize(double dimen) {
        return (int) (dimen * _model.getScalingValue());
    }

    public int getIconResource() {
        return _model.getResIcon();
    }

    public int getColorResource() {
        return _model.getResColor();
    }

    public boolean isMyEvenet() {
        return _model.isMyEvenet();
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(_model.getLatitude(), _model.getLongitude());
    }

    @Override
    public String getTitle() {
        return _model.getTitle();
    }
}
