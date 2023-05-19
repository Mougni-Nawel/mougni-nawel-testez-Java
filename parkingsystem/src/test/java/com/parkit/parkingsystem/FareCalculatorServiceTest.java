package com.parkit.parkingsystem;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Date;
import java.util.function.Supplier;

public class FareCalculatorServiceTest {
    
    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }


    @Test
    public void calculateFareCar() throws Exception{
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        double duration = ticket.getOutTime().getTime() - ticket.getInTime().getTime();
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        double price = Fare.CAR_RATE_PER_HOUR*(duration/(1000.0*60*60)); 
        System.out.println(ticket.getPrice());
        assertEquals(ticket.getPrice(), price);   
    }

    @Test
    public void calculateFareBike() throws Exception{
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        double duration = ticket.getOutTime().getTime() - ticket.getInTime().getTime();
        ticket.setParkingSpot(parkingSpot);
        double price = Fare.BIKE_RATE_PER_HOUR*(duration/(1000.0*60*60)); 
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), price);
    }

    @Test
    public void calculateFareUnkownType() throws Exception{
    	Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
//  Throwable thrown = assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket), "Unkown Parking Type");
//     Assertions.assertEquals("Unkown Parking Type", exception.getMessage());
        assertThrows(IllegalArgumentException.class, ()->{
          fareCalculatorService.calculateFare(ticket);
        } , "Unkown Parking Type");
        
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
       assertThrows(IllegalArgumentException.class, () -> {fareCalculatorService.calculateFare(ticket);},"Unkown Parking Type");
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() throws Exception{
    	Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );

    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() throws Exception{
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() throws Exception{
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }
    
    
    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() throws Exception{
    	Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  29 * 60 * 1000) );//25 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(0.00, ticket.getPrice());
    }
    
    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() throws Exception{
    	Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  25 * 60 * 1000) );//25 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(0.00, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithDiscount() throws Exception{
      Date inTime = new Date();
      inTime.setTime(System.currentTimeMillis() - (35 * 60 * 1000) );
      Date outTime = new Date();
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
      
      ticket.setInTime(inTime);
      ticket.setOutTime(outTime);
      ticket.setParkingSpot(parkingSpot);
      double duration = (ticket.getOutTime().getTime() - ticket.getInTime().getTime())/(1000.0*60*60);
      double price = Fare.CAR_RATE_PER_HOUR*(duration);
      fareCalculatorService.calculateFare(ticket, true);
      assertEquals((price - (price*0.05)), ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithDiscount() throws Exception{
      Date inTime = new Date();
      inTime.setTime(System.currentTimeMillis() - (35 * 60 * 1000) );
      Date outTime = new Date();
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
      
      ticket.setInTime(inTime);
      ticket.setOutTime(outTime);
      ticket.setParkingSpot(parkingSpot);
      double duration = (ticket.getOutTime().getTime() - ticket.getInTime().getTime())/(1000.0*60*60);
      double price = Fare.BIKE_RATE_PER_HOUR*(duration);
      fareCalculatorService.calculateFare(ticket, true);
      assertEquals((price - (price*0.05)), ticket.getPrice());
    }


}
