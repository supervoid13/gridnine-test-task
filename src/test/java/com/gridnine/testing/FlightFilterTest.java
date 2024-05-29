package com.gridnine.testing;

import com.gridnine.testing.exceptions.NoFilterSpecificationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlightFilterTest {

    FlightFilter flightFilter;
    LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);

    @BeforeEach
    public void setup() {
        List<Flight> flights = Arrays.asList(
                //A normal flight with two hour duration
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
                //A normal multi segment flight
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
                //A flight departing in the past
                createFlight(threeDaysFromNow.minusDays(6), threeDaysFromNow),
                //A flight that departs before it arrives
                createFlight(threeDaysFromNow, threeDaysFromNow.minusHours(6)),
                //A flight with more than two hours ground time
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(5), threeDaysFromNow.plusHours(6)),
                //Another flight with more than two hours ground time
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(4),
                        threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(7)));

        flightFilter = new FlightFilter(flights);
    }

    private static Flight createFlight(final LocalDateTime... dates) {
        if ((dates.length % 2) != 0) {
            throw new IllegalArgumentException(
                    "you must pass an even number of dates");
        }
        List<Segment> segments = new ArrayList<>(dates.length / 2);
        for (int i = 0; i < (dates.length - 1); i += 2) {
            segments.add(new Segment(dates[i], dates[i + 1]));
        }
        return new Flight(segments);
    }


    @Test
    public void shouldExcludeFlightsWithDepartureBeforeCurrentTime() {
        List<Flight> flights = flightFilter.excludeDepartureBeforeCurrentTime().doFilter();

        Flight excluded = createFlight(threeDaysFromNow.minusDays(6), threeDaysFromNow);

        Assertions.assertFalse(flights.contains(excluded));
        Assertions.assertEquals(5, flights.size());
    }

    @Test
    public void shouldExcludeFlightsHaveArrivalBeforeDeparture() {
        List<Flight> flights = flightFilter.excludeArrivalBeforeDeparture().doFilter();

        Flight excluded = createFlight(threeDaysFromNow, threeDaysFromNow.minusHours(6));

        Assertions.assertFalse(flights.contains(excluded));
        Assertions.assertEquals(5, flights.size());
    }

    @Test
    public void shouldExcludeFlightsWithTimeOnGroundMoreThanTwoHours() {
        List<Flight> flights = flightFilter.excludeTimeOnGround().doFilter();

        Flight excluded1 = createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                threeDaysFromNow.plusHours(5), threeDaysFromNow.plusHours(6));
        Flight excluded2 = createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(4),
                threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(7));

        Assertions.assertFalse(flights.contains(excluded1));
        Assertions.assertFalse(flights.contains(excluded2));

        Assertions.assertEquals(4, flights.size());
    }

    @Test
    public void shouldThrowNoFilterSpecificationException() {
        Assertions.assertThrows(
                NoFilterSpecificationException.class,
                () -> flightFilter.doFilter(),
                "You didn't specify any filters"
        );
    }
}
