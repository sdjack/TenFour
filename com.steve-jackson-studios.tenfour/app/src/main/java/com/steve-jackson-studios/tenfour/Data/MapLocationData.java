package com.steve-jackson-studios.tenfour.Data;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.steve-jackson-studios.tenfour.Maps.Entities.EventEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sjackson on 7/17/2017.
 * MapLocationData
 */

public class MapLocationData {
    public final String id;
    private final String _subDivisionId;
    private final LatLng[] _quadrants = new LatLng[5];
    private LatLngBounds _bounds;
    private final ArrayList<EventEntity> _streams = new ArrayList<>();

    public MapLocationData(String locationId, String parentId, double[] boundaries, double lat, double lon) {
        this.id = locationId;
        this._subDivisionId = parentId;
        this._quadrants[0] = new LatLng(lat, lon);
        this._quadrants[1] = new LatLng(boundaries[0],boundaries[2]);
        this._quadrants[2] = new LatLng(boundaries[1],boundaries[2]);
        this._quadrants[3] = new LatLng(boundaries[0],boundaries[3]);
        this._quadrants[4] = new LatLng(boundaries[1],boundaries[3]);

        LatLngBounds.Builder builder = LatLngBounds.builder();
        builder.include(new LatLng(boundaries[0],boundaries[2]));
        builder.include(new LatLng(boundaries[1],boundaries[3]));
        this._bounds = builder.build();

        updateStreamEntities();
    }

    public String getSubDivisionID() {
        return _subDivisionId;
    }

    public ArrayList<EventEntity> getStreamEntities() {
        return _streams;
    }

    public LatLng[] getQuadrants() {
        return _quadrants;
    }

    public LatLng getLocation() {
        return _quadrants[0];
    }

    public boolean contains(LatLng location) {
        return (_bounds != null && location != null) && _bounds.contains(location);
    }

    private void updateStreamEntities() {
        _streams.clear();
        HashMap<String,EventDataModel> modelEvents = EventData.getEventData(id);
        for (Object o : modelEvents.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            EventDataModel eventModel = (EventDataModel)entry.getValue();
            _streams.add(new EventEntity(eventModel));
        }
    }
}
