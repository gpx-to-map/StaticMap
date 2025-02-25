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

import javax.imageio.ImageIO;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.Random;

public class TMSLayer extends TileLayer {
    private static final String[] SUBDOMAINS = new String[]{"a", "b", "c"};
    private static final Random RANDOM = new Random();

    protected String mPattern;

    public TMSLayer(String pattern) {
        mPattern = pattern;
    }

    @Override
    public Image getTile(int tileX, int tileY, int tileZ) {
        try {
            String buildedUrl = buildURL(tileX, tileY, tileZ);
            URL url = new URI(buildedUrl).toURL();
            return ImageIO.read(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    protected String buildURL(int tileX, int tileY, int tileZ) {
        String pattern = mPattern;
        int subDomainRandom = RANDOM.nextInt() * SUBDOMAINS.length;
        pattern = pattern.replace("{s}", SUBDOMAINS[subDomainRandom]);
        pattern = pattern.replace("{x}", "" + tileX);
        pattern = pattern.replace("{y}", "" + tileY);
        pattern = pattern.replace("{z}", "" + tileZ);

        return pattern;
    }

}
