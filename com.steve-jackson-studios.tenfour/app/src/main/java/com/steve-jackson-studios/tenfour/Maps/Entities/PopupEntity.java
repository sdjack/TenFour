package com.steve-jackson-studios.tenfour.Maps.Entities;

import android.graphics.Bitmap;
import android.os.Handler;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.steve-jackson-studios.tenfour.R;

import java.util.ArrayList;

/**
 * Created by sjackson on 6/30/2017.
 * PopupEntity
 */

public class PopupEntity {

    private final Handler handler = new Handler();
    private final LatLng[] _quadrants = new LatLng[5];
    private LatLng _position;
    private int nextQuadrant = 1;
    private final MessageEntity _message;
    private final StickerEntity[] _stickers = new StickerEntity[4];
    private ArrayList<String> activeStickers = new ArrayList<>();

    public PopupEntity(GoogleMap map, LatLng location) {
        for (int x = 0; x < 5; x++) {
            _quadrants[x] = location;
        }
        for (int i = 0; i < 4; i++) {
            GroundOverlay overlay = map.addGroundOverlay(new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.invisible))
                    .transparency(0.4f)
                    .bearing(0)
                    .position(location, 100f));
            _stickers[i] = new StickerEntity(overlay);
        }
        Marker marker = map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.invisible))
                .position(location)
                .zIndex(5));
        _message = new MessageEntity(marker);
    }

    public void assign(LatLng[] newQuadrants) {
        System.arraycopy(newQuadrants, 0, _quadrants, 0, 5);
        _position = _quadrants[0];
    }

    public void assign(LatLng newPosition) {
        _position = newPosition;
        //_message.setVisible(false);
    }

    public void unassign() {
        activeStickers.clear();
        for (int i = 0; i < 4; i++) {
            _stickers[i].setVisible(false);
        }
        _message.setVisible(false);
    }

    public void refresh() {
        _message.setVisible(false);
    }

    public void showMessage(String id, Bitmap newBitmap) {
        if (_message.isReady() && newBitmap != null) {
            handler.post(new MessageLoader(_message, newBitmap, _position));
        }
    }

    private static class MessageLoader implements Runnable {

        private final MessageEntity mEntity;
        private final Bitmap mBitmap;
        private final LatLng mLoc;

        MessageLoader(MessageEntity entity, Bitmap bitmap, LatLng position) {
            mEntity = entity;
            mBitmap = bitmap;
            mLoc = position;
        }

        @Override
        public void run() {
            mEntity.setVisible(false);
            mEntity.load(mBitmap, mLoc);
        }
    }

    public void showSticker(String id, Bitmap newBitmap) {
        StickerEntity nextSticker = nextSticker();
        if (nextSticker != null && newBitmap != null) {
            nextSticker.setVisible(false);
            LatLng newPosition = nextQuadrant();
            activeStickers.add(id);
            handler.post(new StickerLoader(nextSticker, newBitmap, newPosition));
        }
    }

    private static class StickerLoader implements Runnable {

        private final StickerEntity mEntity;
        private final Bitmap mBitmap;
        private final LatLng mLoc;

        StickerLoader(StickerEntity entity, Bitmap bitmap, LatLng position) {
            mEntity = entity;
            mBitmap = bitmap;
            mLoc = position;
        }

        @Override
        public void run() {
            mEntity.setVisible(false);
            mEntity.load(mBitmap, mLoc);
        }
    }

    private StickerEntity nextSticker() {
        for (int i = 0; i < 4; i++) {
            if (_stickers[i].isReady()) {
                return _stickers[i];
            }
        }
        return null;
    }

    private LatLng nextQuadrant() {
        nextQuadrant++;
        if (nextQuadrant > 4) {
            nextQuadrant = 1;
        }
        return _quadrants[nextQuadrant];
    }
}
