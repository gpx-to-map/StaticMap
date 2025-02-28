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
package com.hotcoffee.staticmap.layers;

import com.hotcoffee.staticmap.StaticMap;
import com.hotcoffee.staticmap.geo.Location;
import com.hotcoffee.staticmap.geo.PointF;
import com.hotcoffee.staticmap.geo.Tile;
import com.hotcoffee.staticmap.geo.projection.MercatorProjection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Christophe
 */
public abstract class TileLayer implements Layer {

	private float mOpacity = 1.0f;

	public static double longitudeFromTile(int x, int z) {
		return (x / Math.pow(2, z) * 360 - 180);
	}

	public static double latitudeFromTile(int y, int z) {
		final double latRadians = StrictMath.PI - (2.0 * StrictMath.PI) * y / (1 << z);
		return StrictMath.atan(StrictMath.exp(latRadians)) / StrictMath.PI * 360.0 - 90.0;
	}

	public static int tileXFromLongitude(double lon, int z) {
		return ((int) Math.floor((lon + 180) / 360 * (1 << z)));
	}

	public static int tileYFromLatitude(double lat, int z) {
		final double alpha = Math.toRadians(lat);

		return (int) StrictMath.floor((float) ((1.0 - StrictMath.log((StrictMath.sin(alpha) + 1.0) / StrictMath.cos(alpha)) / StrictMath.PI) * 0.5 * (1 << z)));
	}

	/**
	 * Returns the opacity of the layer, between 0 and 1.
	 */
	public float getOpacity() {
		return mOpacity;
	}

	public void setOpacity(float opacity) {
		mOpacity = opacity;
	}

	public abstract Image getTile(int tileX, int tileY, int tileZ);

	@Override
	public void draw(Graphics2D graphics, StaticMap mp) {
		// Apply opacity
		float alpha = getOpacity();
		AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		graphics.setComposite(composite);

		MercatorProjection proj = mp.getProjection();
		int tileSize = proj.getTileSize();
		int tileZ = mp.getZoom();

		// Get the top left point.
		PointF topLeftPixels = new PointF(0 + mp.getOffset().x(),
		                                  0 + mp.getOffset().y());
		Location topLeftLocation = proj.project(topLeftPixels, mp.getZoom());
		Tile topLeftTile = new Tile(
				tileXFromLongitude(topLeftLocation.mLongitude(), mp.getZoom()),
				tileYFromLatitude(topLeftLocation.mLatitude(), mp.getZoom()),
				mp.getZoom());

		// Get the bottom right point.
		PointF bottomRightPixels = new PointF(mp.getWidth() + mp.getOffset().x(),
		                                      mp.getHeight() + mp.getOffset().y());
		Location bottomRightLocation = proj.project(bottomRightPixels, mp.getZoom());
		Tile bottomRightTile = new Tile(
				tileXFromLongitude(bottomRightLocation.mLongitude(), mp.getZoom()),
				tileYFromLatitude(bottomRightLocation.mLatitude(), mp.getZoom()),
				mp.getZoom());

		// Get the top left corner or the top left tile before looping.
		double topLeftCornerLat = latitudeFromTile(topLeftTile.y(), mp.getZoom());
		double topLeftCornerLon = longitudeFromTile(topLeftTile.x(), mp.getZoom());
		Location topLeftLoc = new Location(topLeftCornerLat, topLeftCornerLon);
		PointF topLeftCorner = proj.unproject(topLeftLoc, mp.getZoom());
		List<TileResult> tiles = new ArrayList<>();
		IntStream.rangeClosed(topLeftTile.y(), bottomRightTile.y()).parallel()
		         .forEach(y -> IntStream.rangeClosed(topLeftTile.x(), bottomRightTile.x())
		                                .forEach(x -> tiles.add(new TileResult(x, y, getTile(x, y, tileZ)))));

		tiles.forEach(tile -> {
			// Get the "true" pos.
			PointF truePos = new PointF(topLeftCorner.x() + (tileSize * tile.x()),
			                            topLeftCorner.y() + (tileSize * tile.y()));

			// Get the pos.
			PointF tilePos = new PointF(truePos.x() - mp.getOffset().x(),
			                            truePos.y() - mp.getOffset().y());

			// Draw the tile.
			graphics.drawImage(tile.tile(),
			                   (int) tilePos.x(),
			                   (int) tilePos.y(),
			                   tileSize,
			                   tileSize,
			                   null);
		});
		// Reset composite.
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
		graphics.setComposite(composite);
	}

}
