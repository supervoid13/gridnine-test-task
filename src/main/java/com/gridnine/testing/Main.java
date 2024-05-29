package com.gridnine.testing;

import java.util.List;

public class Main {
    public static void main( String[] args ) {
        List<Flight> flights = FlightBuilder.createFlights();

        FlightFilter filter1 = new FlightFilter(flights);
        List<Flight> flights1 = filter1.excludeDepartureBeforeCurrentTime().doFilter();
        System.out.println(flights1);

        FlightFilter filter2 = new FlightFilter(flights);
        List<Flight> flights2 = filter2.excludeArrivalBeforeDeparture().doFilter();
        System.out.println(flights2);

        FlightFilter filter3 = new FlightFilter(flights);
        List<Flight> flights3 = filter3.excludeTimeOnGround().doFilter();
        System.out.println(flights3);
    }
}
