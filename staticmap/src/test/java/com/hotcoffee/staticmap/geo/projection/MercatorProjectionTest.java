package com.hotcoffee.staticmap.geo.projection;


import com.hotcoffee.staticmap.geo.Location;
import com.hotcoffee.staticmap.geo.PointF;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class MercatorProjectionTest {


    @ParameterizedTest
    @MethodSource("locationProvider")
    void it_should_unproject_correctly(Location location, int zoomLevel, PointF expectedPoint) {
        // Given a location and a zoom level
        MercatorProjection mercatorProjection = new MercatorProjection();

        // When the location in unprojected
        PointF actualPoint = mercatorProjection.unproject(location, zoomLevel);

        // Then it should return a correct point
        assertThat(actualPoint).isEqualTo(expectedPoint);
    }

    @ParameterizedTest
    @MethodSource("pointsLocationProvider")
    void it_should_project_correctly(Location expectedLocation, int zoomLevel, PointF pointF) {
        // Given a point and a zoom level
        MercatorProjection mercatorProjection = new MercatorProjection();

        // When projecting
        Location actualLocation = mercatorProjection.project(pointF, zoomLevel);

        // It should return the correct location
        assertThat(actualLocation).isEqualTo(expectedLocation);

    }

    public static Stream<Arguments> locationProvider() {
        return Stream.of(
                Arguments.of(new Location(48.8566, 2.3522), 5, new PointF(4149.525617777777, 2818.2806779296006)),
                Arguments.of(new Location(35.6762, 139.6503), 5, new PointF(7273.820159999999, 3225.9705148880676)),
                Arguments.of(new Location(-33.9249, 18.4241), 5, new PointF(4515.250631111111, 4917.493700711109)),
                Arguments.of(new Location(61.2181, -149.9003), 5, new PointF(684.9353955555557, 2322.4659428110353)),
                Arguments.of(new Location(-34.6037, -58.3816), 5, new PointF(2767.4942577777774, 4936.184074208889))
        );
    }

    public static Stream<Arguments> pointsLocationProvider() {
        return Stream.of(
                Arguments.of(new Location(48.85660000000001, 2.352199999999982), 5, new PointF(4149.525617777777, 2818.2806779296006)),
                Arguments.of(new Location(35.67620000000001, 139.65029999999996), 5, new PointF(7273.820159999999, 3225.9705148880676)),
                Arguments.of(new Location(-33.924900000000015, 18.424099999999996), 5, new PointF(4515.250631111111, 4917.493700711109)),
                Arguments.of(new Location(61.2181, -149.9003), 5, new PointF(684.9353955555557, 2322.4659428110353)),
                Arguments.of(new Location(-34.60370000000001, -58.38160000000001), 5, new PointF(2767.4942577777774, 4936.184074208889))
        );
    }

}