/*
 * Copyright (C) 2017 doubotis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.hotcoffee.staticmap.geo;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LocationBounds {

    public double xmax;
    public double xmin;
    public double ymax;
    public double ymin;

    /**
     * Creates a location bounds given the specified bounds
     *
     * @param xmin x smallest value
     * @param xmax x biggest value
     * @param ymin y smallest value
     * @param ymax y biggest value
     */
    public LocationBounds(double xmin, double xmax, double ymin, double ymax) {
        this.xmax = xmax;
        this.xmin = xmin;
        this.ymax = ymax;
        this.ymin = ymin;
    }

    /**
     * Creates a {@link LocationBounds} given a list of location. The bounds are automatically calculated.
     *
     * @param locations a {@link List} of {@link Location}
     */
    public LocationBounds(List<Location> locations) {
        AtomicReference<Double> xmin = new AtomicReference<>();
        AtomicReference<Double> xmax = new AtomicReference<>();
        AtomicReference<Double> ymin = new AtomicReference<>();
        AtomicReference<Double> ymax = new AtomicReference<>();
        locations.forEach(location -> {
            if (xmin.get() == null || location.mLongitude() < xmin.get()) {
                xmin.set(location.mLongitude());
            }
            if (xmax.get() == null || location.mLongitude() > xmax.get()) {
                xmax.set(location.mLongitude());
            }
            if (ymin.get() == null || location.mLatitude() < ymin.get()) {
                ymin.set(location.mLatitude());
            }
            if (ymax.get() == null || location.mLatitude() > ymax.get()) {
                ymax.set(location.mLatitude());
            }
        });
        this.xmin = xmin.get();
        this.xmax = xmax.get();
        this.ymin = ymin.get();
        this.ymax = ymax.get();
    }

    public static LocationBounds parseBBOX(String string) {
        try {
            String[] parsed = string.split(",");
            double xmin = Double.parseDouble(parsed[0]);
            double ymin = Double.parseDouble(parsed[1]);
            double xmax = Double.parseDouble(parsed[2]);
            double ymax = Double.parseDouble(parsed[3]);
            return new LocationBounds(xmin, xmax, ymin, ymax);
        } catch (NumberFormatException pee) {
            return null;
        }
    }

    /**
     * Creates a BBOX that wraps a specified LatLng, with an area in meters around the point.
     *
     * @param distance The distance, in
     */
    public static LocationBounds getBounds(Location latLng, long distance) {

        LocationBounds bbox = new LocationBounds(0, 0, 0, 0);

        double[] llcenter = new double[]{latLng.mLatitude(), latLng.mLongitude()};
        double latitude = llcenter[0];
        double longitude = llcenter[1];
        double[] location1 = new double[]{latitude, longitude - 0.5};
        double[] location2 = new double[]{latitude, longitude + 0.5};
        double mpdlon = Location.distanceBetween(location1[0], location1[1],
                location2[0], location2[1]);
        bbox.xmin = longitude - ((distance * 1f) / mpdlon);
        bbox.xmax = longitude + ((distance * 1f) / mpdlon);

        location1 = new double[]{latitude - 0.5, longitude};
        location2 = new double[]{latitude + 0.5, longitude};

        double mpdlat = Location.distanceBetween(location1[0], location1[1],
                location2[0], location2[1]);
        bbox.ymin = latitude - ((distance * 1f) / mpdlat);
        bbox.ymax = latitude + ((distance * 1f) / mpdlat);

        return bbox;
    }

    public Location getCenter() {
        double xmiddle = xmin + ((xmax - xmin) / 2);
        double ymiddle = ymin + ((ymax - ymin) / 2);

        return new Location(ymiddle, xmiddle);
    }

    @Override
    public String toString() {
        return xmin + "," + ymin + "," + xmax + "," + ymax;
    }

    /**
     * Traduit ce BBOX en Well-Known-Text, afin d'?tre compris par une base Spatialite
     * par exemple.
     *
     * @return WKT de retour.
     */
    public String BBOXToWKT() {
        String geom = "POLYGON((";
        geom += xmin + " " + ymin + ",";        // Point haut-gauche
        geom += xmin + " " + ymax + ",";        // Point bas-gauche
        geom += xmax + " " + ymax + ",";        // Point bas-droite
        geom += xmax + " " + ymin + ",";        // Point haut-droite
        geom += xmin + " " + ymin;
        geom += "))";
        return geom;
    }

    /**
     * Compute the area of the BBOX and returns a value in kilometers.
     */
    public double getAreaKm2() {
        double maxlon = xmax;
        double minlon = xmin;
        double maxlat = ymax;
        double minlat = ymin;

        double bboxwidth = Location.distanceBetween(minlat, minlon,
                minlat, maxlon) / 1000;
        double bboxheight = Location.distanceBetween(minlat, minlon,
                maxlat, minlon) / 1000;
        return bboxwidth * bboxheight;
    }

    /**
     * Returns true if the <code>GLatLng</code> is contained inside this BBOX. False otherwise.
     */
    public boolean contains(Location latLng) {
        double y = latLng.mLatitude();
        double x = latLng.mLongitude();

        return y > ymin && y < ymax && x > xmin && x < xmax;
    }

    /**
     * Returns true if the <code>BBOX</code> inside this BBOX. False otherwise.
     *
     * @param inclusive Flag indicates that true must be returned only if the parameter BBOX is fully contained
     *                  into this BBOX.
     */
    public boolean contains(LocationBounds bbox, boolean inclusive) {
        int count = 0;

        if (bbox.xmin > xmin && bbox.xmin < xmax)
            count++;

        if (bbox.xmax > xmin && bbox.xmax < xmax)
            count++;

        if (bbox.ymin > ymax && bbox.ymin < ymin)
            count++;

        if (bbox.ymax > ymax && bbox.ymax < ymin)
            count++;

        if (inclusive)
            return (count == 4);
        else
            return (count > 0);
    }

    public Location getLowerLeftCorner() {
        return new Location(ymax, xmin);
    }

    public Location getUpperLeftCorner() {
        return new Location(ymin, xmin);
    }

    public Location getUpperRightCorner() {
        return new Location(ymin, xmax);
    }

    public Location getLowerRightCorner() {
        return new Location(ymax, xmax);
    }

}
