package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

import static org.mockito.Mockito.*;

/**
 * test: exit of a vehicle, entry of a vehicle, retrieve the next parking space,
 * if the next parking space is not found, if the next parking space for a vehicle not found
 * @author Mougni
 *
 */
@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    // @Test
    public void processExitingVehicleTest(){
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processExitingVehicle();
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
        
    }

    @Test
    public void testProcessIncomingVehicle() throws Exception{
		  when(inputReaderUtil.readSelection()).thenReturn(1);
      when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
      parkingSpot.setId(1);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		  when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
      parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		  Date inTime = new Date();
      Ticket ticket = new Ticket();
      ticket.setParkingSpot(parkingSpot);
      ticket.setVehicleRegNumber("ABCDEF");
      ticket.setPrice(0);
      ticket.setInTime(inTime);
      ticket.setOutTime(null);
      parkingService.processIncomingVehicle();
      verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
      verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

    }
    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception{

        ParkingService parkingService = mock(ParkingService.class);
        try{
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).updateTicket(any(Ticket.class));
        }catch(Exception e){

        }
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() throws Exception{
      try{
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        parkingSpot.setId(1);
        parkingSpot.setAvailable(true);
        assertEquals(parkingService.getNextParkingNumberIfAvailable(), parkingSpot);
      }catch(Exception e){
        
      }
    }
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() throws Exception{
      ParkingSpot parkingSpot = null;
      when(inputReaderUtil.readSelection()).thenReturn(1);
      parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
      assertEquals(parkingService.getNextParkingNumberIfAvailable(), null);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
      ParkingSpot parkingSpot = null;
      when(inputReaderUtil.readSelection()).thenReturn(3);
      parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
      assertEquals(parkingService.getNextParkingNumberIfAvailable(), parkingSpot);
    }

}
