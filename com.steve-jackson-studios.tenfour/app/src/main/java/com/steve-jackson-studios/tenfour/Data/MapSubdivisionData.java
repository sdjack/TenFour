package com.steve-jackson-studios.tenfour.Data;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.JsonSyntaxException;
import com.steve-jackson-studios.tenfour.AppConstants;
import com.steve-jackson-studios.tenfour.Maps.Entities.EventEntity;

import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.impl.PointImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by sjackson on 7/31/2017.
 * MapSubdivisionData
 */

public class MapSubdivisionData {

    private final HashMap<String, MapLocationData> locations = new HashMap<>();
    private final ArrayList<String> locationIds = new ArrayList<>();
    private final ArrayList<EventEntity> streams = new ArrayList<>();
    public final String id;

    public boolean isRegistered;
    public double latitude;
    public double longitude;

    private LatLngBounds bounds;
    private double distance = 0.0;
    private double[] boundaries = new double[4];
    private LatLng[] quadrants = new LatLng[5];
    private Point point;

    /**
     * Instantiates a new GeoFenceModel.
     */
    public MapSubdivisionData(String subdivisionId, JSONObject data) {
        this.id = subdivisionId;
        try {
            this.boundaries[0] = data.getDouble(MapData.Fields.SD_LAT_START);
            this.boundaries[1] = data.getDouble(MapData.Fields.SD_LAT_END);
            this.boundaries[2] = data.getDouble(MapData.Fields.SD_LONG_START);
            this.boundaries[3] = data.getDouble(MapData.Fields.SD_LONG_END);

            LatLngBounds.Builder builder = LatLngBounds.builder();
            builder.include(new LatLng(boundaries[0],boundaries[2]));
            builder.include(new LatLng(boundaries[1],boundaries[3]));
            this.bounds = builder.build();

            this.quadrants[0] = this.bounds.getCenter();
            this.quadrants[1] = new LatLng(boundaries[0],boundaries[2]);
            this.quadrants[2] = new LatLng(boundaries[1],boundaries[2]);
            this.quadrants[3] = new LatLng(boundaries[0],boundaries[3]);
            this.quadrants[4] = new LatLng(boundaries[1],boundaries[3]);

            if (this.point == null) {
                this.point = new PointImpl(this.latitude, this.longitude, SpatialContext.GEO);
            }

        } catch (IllegalStateException | JsonSyntaxException | JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void load(String locationId, JSONObject data) throws JSONException {
        if (null == locations.get(locationId)) {
            try {
                double lon = data.getDouble(MapData.Fields.LONGITUDE);
                double lat = data.getDouble(MapData.Fields.LATITUDE);
                double[] bounds = new double[4];
                bounds[0] = data.getDouble(MapData.Fields.LAT_START);
                bounds[1] = data.getDouble(MapData.Fields.LAT_END);
                bounds[2] = data.getDouble(MapData.Fields.LONG_START);
                bounds[3] = data.getDouble(MapData.Fields.LONG_END);

                locations.put(locationId, new MapLocationData(locationId, id, bounds, lat, lon));
                locationIds.add(locationId);

            } catch (IllegalStateException | JsonSyntaxException | JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public double[] getBoundaries() {
        return boundaries;
    }

    public LatLng[] getQuadrants() {
        return quadrants;
    }

    public LatLng[] getLocationQuadrants(String locationId) {
        if (null != locations.get(locationId)) {
            MapLocationData location = locations.get(locationId);
            return location.getQuadrants();
        }
        return quadrants;
    }

    public boolean contains(LatLng location) {
        return (bounds != null && location != null) && bounds.contains(location);
    }

    public String getContainingId(LatLng location) {
        Set set = locations.entrySet();
        for (Object aSet : set) {
            Map.Entry entry = (Map.Entry) aSet;
            MapLocationData entity = (MapLocationData) entry.getValue();
            if (entity.contains(location)) {
                return entity.id;
            }
        }
        return null;
    }

    public HashMap<String, MapLocationData> getEntities() {
        return locations;
    }

    public MapLocationData getEntity(String locationId) {
        return locations.get(locationId);
    }

    public ArrayList<String> getLocationIds() {
        return locationIds;
    }

    public LatLng getLocation() {
        return this.bounds.getCenter();
    }


    public ArrayList<EventEntity> getStreamEntities() {
        streams.clear();
        Set set = locations.entrySet();
        for (Object aSet : set) {
            Map.Entry entry = (Map.Entry) aSet;
            MapLocationData entity = (MapLocationData) entry.getValue();
            HashMap<String,EventDataModel> modelEvents = EventData.getEventData(entity.id);
            for (Object o : modelEvents.entrySet()) {
                Map.Entry event = (Map.Entry) o;
                EventDataModel eventModel = (EventDataModel)event.getValue();
                streams.add(new EventEntity(eventModel));
            }
        }
        return streams;
    }
    /**
     * Gets distance.
     *
     * @return the distance
     */
    public double getDistance() {
        if (null != AppConstants.POINT_OF_ORIGIN && null != this.point) {
            //double d = AppConstants.distCalc.distance(AppConstants.POINT_OF_ORIGIN, this.point);
            double d = AppConstants.geoCalculator.distance(AppConstants.POINT_OF_ORIGIN, this.point);
            distance = Math.ceil(d * 10000);
        }
        return distance;
    }
    /**
     * Gets direction.
     *
     * @return the direction
     */
    public double getDirection() {
        return MapData.findDirection(longitude, latitude);
    }
}
