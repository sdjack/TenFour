package com.steve-jackson-studios.tenfour.Media;

/**
 * Created by sjackson on 2/1/2017.
 * MediaViewItem
 */

public class MediaViewItem {
    public final boolean isDummy;
    public final boolean isSticker;
    public final String animatedPath;
    public final String stillPath;

    public MediaViewItem() {
        super();
        this.isDummy = true;
        this.isSticker = false;
        this.animatedPath = "";
        this.stillPath = "";
    }

    public MediaViewItem(boolean isSticker) {
        super();
        this.isDummy = true;
        this.isSticker = isSticker;
        this.animatedPath = "";
        this.stillPath = "";
    }

    public MediaViewItem(String animatedPath, String stillPath) {
        super();
        this.isDummy = false;
        this.isSticker = false;
        this.animatedPath = animatedPath;
        this.stillPath = stillPath;
    }

    public MediaViewItem(String animatedPath, String stillPath, boolean isSticker) {
        super();
        this.isDummy = false;
        this.isSticker = isSticker;
        this.animatedPath = animatedPath;
        this.stillPath = stillPath;
    }
}
