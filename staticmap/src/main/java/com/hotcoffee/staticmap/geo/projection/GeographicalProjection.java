/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hotcoffee.staticmap.geo.projection;


import com.hotcoffee.staticmap.StaticMap;
import com.hotcoffee.staticmap.geo.Location;

/**
 * @author cbrasseur
 */
public interface GeographicalProjection<T> {

    /**
     * Gets a point from a latitude/longitude coordinate. Doesn't include the {@link StaticMap} offset.
     */
    T unproject(Location location, int zoom);

    /**
     * Gets a location from a point in the map. {@link StaticMap} offset must be included before requesting.
     */
    Location project(T pt, int zoom);

}
