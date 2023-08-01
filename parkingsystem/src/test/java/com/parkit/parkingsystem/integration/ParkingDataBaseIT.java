package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.model.ParkingSpot;
import java.sql.*;
import java.text.DateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import com.parkit.parkingsystem.constants.ParkingType;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;



/**
 * vehicle entry and exit tests as well as the recurrent vehicle exit test
 * @author Mougni
 *
 */

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    private static ParkingSpot parkingSpot;

    private static Ticket ticket;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticket = new Ticket();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("DHIJK");
        dataBasePrepareService.clearDataBaseEntries();
    }

    //@AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() throws Exception{
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("DHIJK");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //when
        parkingService.processIncomingVehicle();
        //then
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        Ticket ticketDB = getTicketDB("DHIJK");
        Instant inTimeValue = ticketDB.getInTime().toInstant();
        Instant inTimeExpected = Instant.now().plus(2, ChronoUnit.SECONDS);

        System.out.println("inTimeExpected " + inTimeExpected);
        System.out.println("inTimeValue " + inTimeValue);
        System.out.println("inTimeExpected.isAfter(inTimeValue) " + inTimeExpected.isAfter(inTimeValue));
        assertEquals("DHIJK", ticketDB.getVehicleRegNumber());
        assertNotNull(ticketDB.getInTime());
        assertNull(ticketDB.getOutTime());
        assertTrue(inTimeExpected.isAfter(inTimeValue));
    }



    @Test
    public void testParkingABike() throws Exception{
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BIKETEST");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //when
        parkingService.processIncomingVehicle();
        //then
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        Ticket ticketDB = getTicketDB("BIKETEST");
        Instant inTimeValue = ticketDB.getInTime().toInstant();
        Instant inTimeExpected = Instant.now().plus(2, ChronoUnit.SECONDS);

        System.out.println("inTimeExpected " + inTimeExpected);
        System.out.println("inTimeValue " + inTimeValue);
        System.out.println("inTimeExpected.isAfter(inTimeValue) " + inTimeExpected.isAfter(inTimeValue));
        assertEquals("BIKETEST", ticketDB.getVehicleRegNumber());
        assertNotNull(ticketDB.getInTime());
        assertNull(ticketDB.getOutTime());
        assertTrue(inTimeExpected.isAfter(inTimeValue));
    }

    private static Ticket getTicketDB(String vehiculeRegNumber) throws ClassNotFoundException, SQLException {
        Connection con = ticketDAO.dataBaseConfig.getConnection();
        PreparedStatement ps = con.prepareStatement("select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE, t.VEHICLE_REG_NUMBER from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? order by  t.OUT_TIME desc limit 1");
        ps.setString(1, vehiculeRegNumber);
        ResultSet rs = ps.executeQuery();
        rs.next();
        Ticket ticketDB = new Ticket();
        ticketDB.setParkingSpot(parkingSpot);
        ticketDB.setId(rs.getInt(2));
        ticketDB.setVehicleRegNumber(rs.getString(7));
        ticketDB.setPrice(rs.getDouble(3));
        ticketDB.setInTime(rs.getTimestamp(4));
        ticketDB.setOutTime(rs.getTimestamp(5));
        return ticketDB;
    }

    @Test
    public void testParkingLotExit() throws Exception{
        //given
        dataBasePrepareService.clearDataBaseEntries();
        saveTicket() ;
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("DHIJK");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //when
        parkingService.processExitingVehicle();

        //then
        Ticket ticketDB = getTicketDB("DHIJK");
        System.out.println("ticket " + ticketDB);
        Instant outTimeValue = ticketDB.getOutTime().toInstant();
        Instant outTimeExpected = Instant.now();
        assertTrue(ticketDB.getPrice()>0);
        assertTrue(ticketDB.getOutTime().toInstant().isAfter(ticketDB.getInTime().toInstant()));
    }

    // Bike
    @Test
    public void testParkingLotExitBike() throws Exception{
        //given
        dataBasePrepareService.clearDataBaseEntries();
        saveTicketBike() ;
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BIKETEST");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //when
        parkingService.processExitingVehicle();

        //then
        Ticket ticketDB = getTicketDB("BIKETEST");
        System.out.println("ticket " + ticketDB);
        Instant outTimeValue = ticketDB.getOutTime().toInstant();
        Instant outTimeExpected = Instant.now();
        assertTrue(ticketDB.getPrice()>0);
        assertTrue(ticketDB.getOutTime().toInstant().isAfter(ticketDB.getInTime().toInstant()));
    }

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

    private static void saveTicketBike() {
        Ticket ticketDB = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(1,ParkingType.BIKE,false);
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  2*60 * 60 * 1000) );
        ticketDB.setParkingSpot(parkingSpot);
        ticketDB.setVehicleRegNumber("BIKETEST");
        ticketDB.setInTime(inTime);
        ticketDAO.saveTicket(ticketDB);
    }
    private void saveTicketWithOutTime(Date inTime,Date outTime) {
        Ticket ticketDB = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(2,ParkingType.CAR,false);

        ticketDB.setParkingSpot(parkingSpot);
        ticketDB.setVehicleRegNumber("DHIJK");
        ticketDB.setInTime(inTime);
        ticketDB.setOutTime(outTime);
        ticketDAO.saveTicket(ticketDB);
    }

    private void saveTicketWithOutTimeBike(Date inTime,Date outTime) {
        Ticket ticketDB = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(2,ParkingType.BIKE,false);

        ticketDB.setParkingSpot(parkingSpot);
        ticketDB.setVehicleRegNumber("BIKETEST");
        ticketDB.setInTime(inTime);
        ticketDB.setOutTime(outTime);
        ticketDAO.saveTicket(ticketDB);
    }
   @Test()
   @Tag("test le calcul d'un prix d'un ticket via l'appel de processIncomingVehicle et processExitingVehicle  pour user recurrent")
    public void testParkingLotExitRecurringUser() throws Exception{
     //Given
       when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("DHIJK");
       ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
       Date inTime = new Date();
       inTime.setTime( System.currentTimeMillis() - (  2*60 * 60 * 1000) );
       Date outTime = new Date();
       outTime.setTime(System.currentTimeMillis() - (  3*60 * 60 * 1000));

      // entrer du vehicule
       saveTicketWithOutTime(inTime,outTime);
       saveTicketWithOutTime(inTime,outTime);
       saveTicket();

       // recuperer le nombre de ticket
       int count = getCountTicket();
       assertTrue(2<count);

     // a faire avec 1 heure de park pour avoir la reduction
       //when
       parkingService.processExitingVehicle();
         //then
       Ticket ticketDB = getTicketDB("DHIJK");
       
       double priceX100 = Math.round(ticketDB.getPrice()*100);
       double price =  priceX100/100;
         System.out.println("ticket " + ticketDB);
         System.out.println("Math.round(ticketDB.getPrice()) " + price + ticketDB.getPrice());
       assertEquals(price,2.85);
      assertTrue(ticketDAO.getTicket("DHIJK").getOutTime().toInstant().isAfter(ticketDAO.getTicket("DHIJK").getInTime().toInstant()));
      
    }

    @Test()
    @Tag("test le calcul d'un prix d'un ticket via l'appel de processIncomingVehicle et processExitingVehicle  pour user recurrent")
    public void testParkingLotExitRecurringUserBike() throws Exception{
        //Given
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BIKETEST");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  2*60 * 60 * 1000) );
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() - (  3*60 * 60 * 1000));

        // entrer du vehicule
        saveTicketWithOutTimeBike(inTime,outTime);
        saveTicketWithOutTimeBike(inTime,outTime);
        saveTicketBike();

        // recuperer le nombre de ticket
        int count = getCountTicketBike();
        assertTrue(2<count);

        // a faire avec 1 heure de park pour avoir la reduction
        //when
        parkingService.processExitingVehicle();
        //then
        Ticket ticketDB = getTicketDB("BIKETEST");

        double priceX100 = Math.round(ticketDB.getPrice()*100);
        double price =  priceX100/100;
        System.out.println("ticket " + ticketDB);
        System.out.println("Math.round(ticketDB.getPrice()) " + price + ticketDB.getPrice());
        assertEquals(price,2.85);
        assertTrue(ticketDAO.getTicket("BIKETEST").getOutTime().toInstant().isAfter(ticketDAO.getTicket("BIKETEST").getInTime().toInstant()));

    }

    private static int getCountTicket() throws ClassNotFoundException, SQLException {
        Connection con = null;
        con = ticketDAO.dataBaseConfig.getConnection();

        PreparedStatement s = con.prepareStatement("SELECT COUNT(*) AS recordCount FROM ticket where VEHICLE_REG_NUMBER = ? ");
        s.setString(1, "DHIJK");
        ResultSet r = s.executeQuery();
        r.next();
        int count = r.getInt("recordCount");
        r.close();
        return count;
    }

    private static int getCountTicketBike() throws ClassNotFoundException, SQLException {
        Connection con = null;
        con = ticketDAO.dataBaseConfig.getConnection();

        PreparedStatement s = con.prepareStatement("SELECT COUNT(*) AS recordCount FROM ticket where VEHICLE_REG_NUMBER = ? ");
        s.setString(1, "BIKETEST");
        ResultSet r = s.executeQuery();
        r.next();
        int count = r.getInt("recordCount");
        r.close();
        return count;
    }


}
