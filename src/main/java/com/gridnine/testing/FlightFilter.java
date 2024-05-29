package com.gridnine.testing;

import com.gridnine.testing.exceptions.NoFilterSpecificationException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FlightFilter {

    private final List<Flight> flights;
    private Predicate<Flight> totalPredicate;

    FlightFilter(List<Flight> flights) {
        // Create new ArrayList to avoid UnsupportedOperationException from Iterator.remove()
        this.flights = new ArrayList<>(flights);
    }


    FlightFilter excludeDepartureBeforeCurrentTime() {
        checkForNullBeforeAnd(FlightPredicate.DEPARTURE_BEFORE_CURRENT_TIME);
        return this;
    }

    FlightFilter excludeArrivalBeforeDeparture() {
        checkForNullBeforeAnd(FlightPredicate.ARRIVAL_BEFORE_DEPARTURE);
        return this;
    }

    FlightFilter excludeTimeOnGround() {
        checkForNullBeforeAnd(FlightPredicate.TIME_ON_GROUND);
        return this;
    }

    List<Flight> doFilter() {
        if (totalPredicate == null)
            throw new NoFilterSpecificationException("You didn't specify any filters");

        flights.removeIf(totalPredicate);
        return flights;
    }

    private void checkForNullBeforeAnd(Predicate<Flight> predicate) {
        if (totalPredicate == null)
            totalPredicate = predicate;
        else
            totalPredicate = totalPredicate.and(predicate);
    }

    static class FlightPredicate {
        static final Predicate<Flight> DEPARTURE_BEFORE_CURRENT_TIME = flight -> flight.getSegments()
                .get(0)
                .getDepartureDate()
                .isBefore(LocalDateTime.now());

        static final Predicate<Flight> ARRIVAL_BEFORE_DEPARTURE = flight -> flight.getSegments().stream()
                .anyMatch(segment -> segment.getArrivalDate().isBefore(segment.getDepartureDate()));

        static final Predicate<Flight> TIME_ON_GROUND = flight -> {
            List<Segment> segments = flight.getSegments();
            Duration total = Duration.ZERO;

            for (int i = 0; i < segments.size() - 1; i++) {
                Segment before = segments.get(i);
                Segment after = segments.get(i+1);

                Duration duration = Duration.between(before.getArrivalDate(), after.getDepartureDate()).abs();
                total = total.plus(duration);
            }
            return total.compareTo(Duration.ofHours(2)) > 0;
        };
    }
}
