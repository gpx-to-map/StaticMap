package com.hotcoffee.staticmap.geo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @ParameterizedTest
    @CsvSource({
            "48.8566, 2.3522, 40.7128, -74.0060, 5837264",
            "35.6762, 139.6503, 37.7749, -122.4194, 8274217",
            "-33.9249, 18.4241, 51.5074, -0.1278, 9670536",
            "34.0522, -118.2437, 36.1699, -115.1398, 367260",
            "-34.6037, -58.3816, -22.9068, -43.1729, 1965807"
    })
    void distance_between_points_should_be_correct(double lat1, double lon1, double lat2, double lon2, float expectedDistance) {
        // Given two points
        // When the distance between these points is calculated
        float result = Location.distanceBetween(lat1, lon1, lat2, lon2);

        // Then this distance should be approximately correct
        assertThat(result)
                .isCloseTo(expectedDistance, Assertions.offset(3000.0f));
    }


}