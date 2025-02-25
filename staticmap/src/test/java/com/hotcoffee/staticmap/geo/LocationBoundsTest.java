package com.hotcoffee.staticmap.geo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LocationBoundsTest {

    @ParameterizedTest
    @CsvSource({
            "1.0, 2.0, 3.0, 4.0, 1.0, 2.0, 3.0, 4.0",   // Basic case
            "-10.5, -20.5, 15.5, 25.5, -10.5, -20.5, 15.5, 25.5", // Negative coordinates
            "0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0"    // Zero coordinates
    })
    void should_parse_bbox(double expectedXmin, double expectedYmin, double expectedXmax, double expectedYmax,
                           double xmin, double ymin, double xmax, double ymax) {
        // Given a bbox string
        String bboxString = xmin + "," + ymin + "," + xmax + "," + ymax;

        // When the string gets parsed
        LocationBounds result = LocationBounds.parseBBOX(bboxString);

        // The correct location bounds should be calculated
        Assertions.assertThat(result)
                .isNotNull()
                .extracting("xmin", "ymin", "xmax", "ymax")
                .containsExactly(expectedXmin, expectedYmin, expectedXmax, expectedYmax);
    }

    @Test
    void should_yield_null_on_invalid_input() {
        // Given an invalid string
        String invalidBboxString = "invalid,input,string";

        // When it gets parsed
        LocationBounds result = LocationBounds.parseBBOX(invalidBboxString);

        // Then a null result should be returned
        Assertions.assertThat(result).isNull(); // Expect null for invalid input
    }

    @Test
    void it_should_calculate_bounds() {
        // Given a location and a distance
        Location location = new Location(48.8566, 2.3522); // Paris coordinates
        long distance = 1000; // 1 km around the location

        // When the location bound is calculated
        LocationBounds result = LocationBounds.getBounds(location, distance);

        // Then the bounds should be correct
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.xmin).isLessThan(result.xmax);
        Assertions.assertThat(result.ymin).isLessThan(result.ymax);
    }

    @ParameterizedTest
    @CsvSource({
            "1.0, 2.0, 3.0, 4.0, 2.5, 1.5, true",  // Point inside the BBOX
            "1.0, 2.0, 3.0, 4.0, 5.0, 6.0, false" // Point outside the BBOX
    })
    void it_should_know_if_location_is_contained(double xmin, double ymin, double xmax, double ymax,
                                                 double lat, double lon, boolean expectedContains) {
        // Given a location bound and a point
        LocationBounds bbox = new LocationBounds(xmin, xmax, ymin, ymax);
        Location location = new Location(lat, lon);

        // When we try to check if the point is contained in the bounds
        boolean result = bbox.contains(location);

        // Then it should return the correct result
        Assertions.assertThat(result).isEqualTo(expectedContains);
    }

    @ParameterizedTest
    @CsvSource({
            "2.0, 3.0, 4.0, 5.0, 1.0, 2.0, 3.0, 4.0, true",  // BBOX fully contained
            "1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, false"  // BBOX outside
    })
    void it_should_know_if_a_bbox_contains_another(double xmin1, double ymin1, double xmax1, double ymax1,
                                                  double xmin2, double ymin2, double xmax2, double ymax2,
                                                  boolean expectedContains) {
        // Given two bboxes
        LocationBounds bbox1 = new LocationBounds(xmin1, xmax1, ymin1, ymax1);
        LocationBounds bbox2 = new LocationBounds(xmin2, xmax2, ymin2, ymax2);

        // When we try to know if a bbox contains the other
        boolean result = bbox1.contains(bbox2, false);

        // Then it should return a correct result
        Assertions.assertThat(result).isEqualTo(expectedContains);
    }

    @ParameterizedTest
    @CsvSource({
            "1.0, 2.0, 3.0, 4.0, 49422.35798114911"  // Small BBOX area (example value, can be tuned)
    })
    void it_should_calculate_correct_area(double xmin, double ymin, double xmax, double ymax, double expectedArea) {
        // Given a correct location bound
        LocationBounds bbox = new LocationBounds(xmin, xmax, ymin, ymax);

        // When we try to calculte its area
        double result = bbox.getAreaKm2();

        // Then the result should be correct
        Assertions.assertThat(result).isEqualTo(expectedArea);
    }
}