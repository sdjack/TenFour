package com.steve-jackson-studios.tenfour.Maps.Entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by sjackson on 9/20/2017.
 * ClusterEntity
 */

public abstract class ClusterEntity implements ClusterItem {

    protected int _type;
    protected String _id;
    protected String _parentId;
    protected String _title;
    protected String _snippet;
    protected LatLng _position;
    protected boolean _visible;

    public ClusterEntity(int type, String id, String parentId) {
        this._type = type;
        this._id = id;
        this._parentId = parentId;
        this._visible = false;
    }

    public int getType() {
        return _type;
    }

    public String getId() {
        return _id;
    }

    public String getParentId() {
        return _parentId;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    public void setSnippet(String snippet) {
        this._snippet = snippet;
    }

    public void setPosition(LatLng position) {
        this._position = position;
    }

    public void setVisible(boolean isVisible) {
        this._visible = isVisible;
    }

    public boolean getVisible() {
        return _visible;
    }

    @Override
    public String getTitle() {
        return _title;
    }

    @Override
    public String getSnippet() {
        return _snippet;
    }

    @Override
    public LatLng getPosition() {
        return _position;
    }
}
