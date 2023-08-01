package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
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

    private static DataBaseConfig dataBaseConfig = new DataBaseConfig();

    private static DataBasePrepareService dataBasePrepareService = new DataBasePrepareService();

    private static void saveTicket() {
        Ticket ticketDB = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(1,ParkingType.CAR,false);
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  2*60 * 60 * 1000) );
        ticketDB.setParkingSpot(parkingSpot);
        ticketDB.setVehicleRegNumber("DHIJK");
        ticketDB.setInTime(inTime);
        ticketDAO.saveTicket(ticketDB);
    }

    @Test
    public void processExitingVehicleTest(){
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("DHIJK");


            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (120*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("DHIJK");
            when(ticketDAO.getTicketOut(anyString())).thenReturn(ticket);

            when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);


            parkingService.processExitingVehicle();
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, Mockito.times(1)).getNbTicket("DHIJK");

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
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFoundBike() throws Exception{
        ParkingSpot parkingSpot = null;
        when(inputReaderUtil.readSelection()).thenReturn(2);
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

    // test unitaire DAO partie
    @Test
    public void testTicketDAOSaveTicket() throws SQLException, ClassNotFoundException {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Date inTime = new Date();
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        ticket.setOutTime(null);

        TicketDAO dao = new TicketDAO();
        Boolean t = dao.saveTicket(ticket);

        System.out.println(t);

        Ticket ticketAdded = dao.getTicket("ABCDEF");

        assertEquals(ticket.getVehicleRegNumber(), ticketAdded.getVehicleRegNumber());
    }

   @Test
    public void testTicketDAOGetTicket() throws SQLException, ClassNotFoundException {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Date inTime = new Date();
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        ticket.setOutTime(null);

        TicketDAO dao = new TicketDAO();
        Boolean t = dao.saveTicket(ticket);

        System.out.println(t);

        Ticket ticketAdded = dao.getTicket("ADEF");

        assertEquals(null, ticketAdded);
    }

    @Test
    public void testTicketDaoUpdateTicket(){
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  2*60 * 60 * 1000) );
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() - (  1*60 * 60 * 1000));
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);

        TicketDAO dao = new TicketDAO();
        Boolean t = dao.updateTicket(ticket);

        System.out.println(t);


        Ticket ticketUpdated = dao.getTicket("ABCDEF");

        assertEquals(ticket.getVehicleRegNumber(), ticketUpdated.getVehicleRegNumber());
        assertEquals(new Timestamp(ticket.getOutTime().getDate()), new Timestamp(ticketUpdated.getOutTime().getDate()));
    }

    @Test
    public void testTicketDaoTicketOut(){
        dataBasePrepareService.clearDataBaseEntries();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  2*60 * 60 * 1000) );
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(inTime);

        TicketDAO dao = new TicketDAO();
        Boolean tt = dao.saveTicket(ticket);

        Ticket ticketOut = dao.getTicketOut("ABCDEF");


        assertEquals(ticket.getVehicleRegNumber(), ticketOut.getVehicleRegNumber());
        assertEquals(null, ticketOut.getOutTime());

    }

    @Test
    public void testTicketDaoGetNBTicket(){

        dataBasePrepareService.clearDataBaseEntries();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  2*60 * 60 * 1000) );
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() - (  1*60 * 60 * 1000));
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);

        TicketDAO dao = new TicketDAO();
        Boolean t1 = dao.saveTicket(ticket);
        Boolean t2 = dao.saveTicket(ticket);


        int nbTicket = dao.getNbTicket("ABCDEF");

        assertEquals(2, nbTicket);


    }

    // tests unitaire ParkingSpotDAO
    @Test
    public void testParkingSpotDAOGetNextAvailableSlot() throws SQLException, ClassNotFoundException {
        dataBasePrepareService.clearDataBaseEntries();
        ParkingSpotDAO dao = new ParkingSpotDAO();
        int availableSlot = dao.getNextAvailableSlot(ParkingType.CAR);

        assertEquals(1, availableSlot);
    }

    @Test
    public void testParkingSpotDAOUpdateParking(){
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ParkingSpotDAO dao = new ParkingSpotDAO();
        boolean updatedParking = dao.updateParking(parkingSpot);

        assertEquals(true, updatedParking);

    }


}
