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
package com.hotcoffee.staticmap;

import com.hotcoffee.staticmap.geo.Location;
import com.hotcoffee.staticmap.geo.LocationBounds;
import com.hotcoffee.staticmap.geo.PointF;
import com.hotcoffee.staticmap.geo.projection.MercatorProjection;
import com.hotcoffee.staticmap.layers.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Core class of the StaticMAp library. Serves the final results.
 */
public class StaticMap {
    private static final int DEFAULT_TILE_SIZE = 256;

    private Location mLocation;
    private int mZoom = 3;

    private int mWidth;
    private int mHeight;
    private BufferedImage mImage = null;
    private MercatorProjection mProjection = new MercatorProjection();
    private final ArrayList<Layer> mLayers = new ArrayList<Layer>();
    private PointF mOffset;

    /**
     * Build a static map with the specified width and height. In pixels.
     */
    public StaticMap(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * Sets the location for this map.
     */
    public void setLocation(double lat, double lon) {
        mLocation = new Location(lat, lon);
    }

    /**
     * Returns the current location for this map.
     */
    public Location getLocation() {
        return mLocation;
    }

    /**
     * Sets the location for this map.
     */
    public void setLocation(Location l) {
        mLocation = l;
    }

    /**
     * Returns the current zoom level for this map.
     */
    public int getZoom() {
        return mZoom;
    }

    /**
     * Sets the zoom level for this map.
     */
    public void setZoom(int zoom) {
        mZoom = zoom;
    }

    /**
     * Sets the size of this map. In pixels.
     */
    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * Returns the width of this map, in pixels.
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Sets the width of this map. In pixels.
     */
    public void setWidth(int width) {
        mWidth = width;
    }

    /**
     * Returns the height of this map, in pixels.
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Sets the height of this map. In pixels.
     */
    public void setHeight(int height) {
        mHeight = height;
    }

    /**
     * Convert WGS84 coordinates to point on the map.
     */
    public PointF fromLatLngToPoint(double lat, double lng, int zoom) {
        final PointF offset = getOffset();

        Location loc = new Location(lat, lng);
        PointF pt = getProjection().unproject(loc, zoom);

        return new PointF(pt.x() - offset.x(), pt.y() - offset.y());
    }

    /**
     * convert point on the map to WGS84 coordinates.
     */
    public Location fromPointToLatLng(PointF pt, int zoom) {
        final PointF offset = getOffset();

        // Offset the point for computation.
        final PointF thePoint = new PointF(pt.x() + offset.x(), pt.y() + offset.y());

        return getProjection().project(thePoint, zoom);
    }

    /**
     * Returns the current projection. A projection is used to compute relations
     * between real locations and positions on the final picture.
     * See {@link MercatorProjection}.
     */
    public MercatorProjection getProjection() {
        return mProjection;
    }

    /**
     * Sets a custom projection. A projection is used to compute relations
     * between real locations and positions on the final picture.
     * See {@link MercatorProjection}.
     */
    public void setProjection(MercatorProjection projection) {
        mProjection = projection;
    }

    /**
     * Gets the offset between the values returned by the {@link MercatorProjection}
     * and the position on the final picture, depending on the size.
     */
    public PointF getOffset() {
        return mOffset;
    }

    private void prepare(CenterOffset centerOffset) {
        mOffset = computeRatioPixels(getZoom(), centerOffset);
    }

    private void proceedDraw(Graphics2D graphics, CenterOffset centerOffset) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics.setBackground(Color.WHITE);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, mWidth, mHeight);
        prepare(centerOffset);

        for (Layer layer : mLayers) {
            layer.draw(graphics, this);
        }
    }

    private void proceedDraw(CenterOffset centerOffset) {
        mImage = new BufferedImage(mWidth, mHeight,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = mImage.createGraphics();
        proceedDraw(graphics, centerOffset);
    }

    /**
     * Runs the procedure of drawing. Stores the result into the specified {@link File}.
     */
    public void drawInto(File file) throws IOException {
        proceedDraw(new CenterOffset(0, 0));
        ImageIO.write(mImage, "PNG", file);
    }

    /**
     * Runs the procedure of drawing. Stores the result into the specified {@link OutputStream}.
     */
    public void drawInto(OutputStream os) throws IOException {
        proceedDraw(new CenterOffset(0, 0));
        ImageIO.write(mImage, "PNG", os);
    }

    /**
     * Runs the drawing procedure on a given {@link Graphics2D}. This allows to draw
     * more things on the {@link Graphics2D} later on.
     *
     * @param graphics2D any suitable {@link Graphics2D} object (eg. {@link BufferedImage})
     */
    public void drawInto(Graphics2D graphics2D, CenterOffset centerOffset) {
        proceedDraw(graphics2D, centerOffset);
    }

    /**
     * Adds a {@link Layer} onto the map. The layer will be drawn from the first to the last.
     * For instance, you can add any {@link Layer}, {@link TMSLayer} or {@link WMSLayer}
     * object.
     */
    public void addLayer(Layer layer) {
        mLayers.add(layer);
    }

    /**
     * Inserts a {@link Layer} onto the map at the specified index. The layer will be drawn from the first to the last.
     * For instance, you can add any {@link Layer}, {@link TMSLayer} or {@link WMSLayer}
     * object.
     */
    public void insertLayer(Layer layer, int index) {
        mLayers.add(index, layer);
    }

    /**
     * Removes the spceified {@link Layer} from the map. The removed layer will
     * not been drawn anymore.
     */
    public void removeLayer(Layer layer) {
        mLayers.remove(layer);
    }

    /**
     * Tell the map to fit the bounds of a {@link LocationBounds}. This method will
     * sets location and zoom level depending on the size of the final picture
     * and the specified bounds.
     */
    public void fitBounds(LocationBounds bounds) {
        fitBounds(bounds, 3, 20, new Padding(0, 0, 0, 0));
    }

    /**
     * Tell the map to fit the bounds of a {@link LocationBounds}. This method will
     * sets location and zoom level depending on the size of the final picture
     * and the specified bounds.<br/>
     * You can specify a minimum and maximum zoom.
     */
    public void fitBounds(LocationBounds bounds, int minZoom, int maxZoom) {
        fitBounds(bounds, minZoom, maxZoom, new Padding(0, 0, 0, 0));
    }

    /**
     * Tell the map to fit the bounds of a {@link LocationBounds}. This method will
     * sets location and zoom level depending on the size of the final picture
     * and the specified bounds.<br/>
     * You can specify a padding to apply to the map.
     * This doesn't means the padding will be exactly matching, but while
     * computing the right zoom, the padding will be taken in account in order to
     * not allowing space in each side, lower than this padding.
     */
    public void fitBounds(LocationBounds bounds, Padding padding) {
        fitBounds(bounds, 3, 20, padding);
    }

    /**
     * Tell the map to fit the bounds of a {@link LocationBounds}. This method will
     * sets location and zoom level depending on the size of the final picture
     * and the specified bounds.<br/>
     * You can specify a padding to apply to the map.
     * This doesn't means the padding will be exactly matching, but while
     * computing the right zoom, the padding will be taken in account in order to
     * not allowing space in each side, lower than this padding.<br/>
     * You can specify a minimum and maximum zoom.
     */
    public void fitBounds(LocationBounds bounds, int minZoom, int maxZoom, Padding padding) {

        // Find which zoom value to set.
        setLocation(bounds.getCenter());
        MercatorProjection mp = getProjection();

        System.out.println("Trying to fit: " + bounds);

        int baseZoom = maxZoom;
        boolean inBounds = false;
        while (!inBounds) {
            baseZoom--;

            if (baseZoom < minZoom) {
                baseZoom = minZoom;
                break;
            }

            PointF rp = computeRatioPixels(baseZoom, new CenterOffset(0, 0));

            // Compute?
            PointF topLeftPixels = new PointF(0 + rp.x() + padding.left(), 0 + rp.y() + padding.top());
            Location topLeftLocation = mp.project(topLeftPixels, baseZoom);

            PointF bottomRightPixels = new PointF(mWidth + rp.x() + padding.bottom(),mHeight + rp.y() + padding.right());
            Location bottomRightLocation = mp.project(bottomRightPixels, baseZoom);

            // Test if in bounds
            LocationBounds bboxCalculation = new LocationBounds(topLeftLocation.mLongitude(),
                    bottomRightLocation.mLongitude(),
                    topLeftLocation.mLatitude(),
                    bottomRightLocation.mLatitude());

            System.out.println("Trying with " + baseZoom + "...");
            System.out.println(" - " + bboxCalculation);
            inBounds = bboxCalculation.contains(bounds, true);
        }
        mZoom = baseZoom;
    }

    private PointF computeRatioPixels(int zoom, CenterOffset centerOffset) {
        MercatorProjection proj = getProjection();
        PointF centerPixels = proj.unproject(getLocation(), zoom);

        // Le centre en 824, 539 est l'équivalent de 100,100 sur l'image.
        int centerImageX = mWidth / 2;
        int centerImageY = mHeight / 2;

        return new PointF(centerPixels.x() - centerImageX + centerOffset.x(),
                centerPixels.y() - centerImageY + centerOffset.y());
    }
}
