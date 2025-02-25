package com.hotcoffee.staticmap;

import com.hotcoffee.staticmap.geo.Location;
import com.hotcoffee.staticmap.geo.LocationBounds;
import com.hotcoffee.staticmap.geo.LocationPath;
import com.hotcoffee.staticmap.layers.components.LineString;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StaticMapTest {

	@Test
	void testShould_initialize_map_object() {
		// Given a simple static map object
		// When it get initialized
		StaticMap staticMap = new StaticMap(10, 10);

		// Then it should have correct parameters
		assertThat(staticMap.getHeight()).isEqualTo(10);
		assertThat(staticMap.getWidth()).isEqualTo(10);
	}

	@Test
	void it_should_fit_bounds() throws IOException {
		// Given
		StaticMap staticMap = new StaticMap(1000, 1000);
		List<Location> locationList = new ArrayList<>();
		Path resourceDirectory = Paths.get("src", "test", "resources");
		Path locations = resourceDirectory.resolve("locations.csv");
		try (Stream<String> lines = Files.lines(locations)) {
			lines.forEach(line -> {
				String[] parts = line.split(";");
				double lat = Double.parseDouble(parts[0]);
				double lon = Double.parseDouble(parts[1]);
				locationList.add(new Location(lat, lon));
			});
		}
		LocationBounds bounds = new LocationBounds(locationList);

		// When
		LocationPath locationPath = new LocationPath();
		locationList.forEach(locationPath::addLocation);
		LineString lineString = new LineString(locationPath);
		lineString.strokeWidth(3);
		lineString.strokeColor(Color.BLUE);
		staticMap.fitBounds(bounds);

		// Then
		assertThat(staticMap.getLocation().mLatitude()).isEqualTo(42.487528999999995);
		assertThat(staticMap.getLocation().mLongitude()).isEqualTo(8.8796785);
		assertThat(staticMap.getZoom()).isEqualTo(14);
	}

}