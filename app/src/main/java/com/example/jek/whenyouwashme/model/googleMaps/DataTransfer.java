package com.example.jek.whenyouwashme.model.googleMaps;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by jek on 28.07.2017.
 */

public class DataTransfer {
    private GoogleMap googleMap;
    private String url;

    public DataTransfer(GoogleMap googleMap, String url) {
        this.googleMap = googleMap;
        this.url = url;
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
