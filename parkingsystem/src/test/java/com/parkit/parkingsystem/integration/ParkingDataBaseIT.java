package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.model.ParkingSpot;
import java.util.Date;
import com.parkit.parkingsystem.constants.ParkingType;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.Fare;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
// import org.junit.jupiter.api.Assertions.assertThat;





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
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("DHIJK");
        //dataBasePrepareService.clearDataBaseEntries();
    }

    //@AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() throws Exception{
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("DHIJK");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        LocalDate inTimeExpected = LocalDate.now();
        System.out.println(inTimeExpected);
        Connection con = null;
        con = ticketDAO.dataBaseConfig.getConnection();
        PreparedStatement ps = null;
        ps = con.prepareStatement("select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE, t.VEHICLE_REG_NUMBER from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=?");
        ps.setString(1,"DHIJK");
        ResultSet rs = ps.executeQuery();
        rs.next();
        Ticket ticketDB = new Ticket();
        ticketDB.setParkingSpot(parkingSpot);
        ticketDB.setId(rs.getInt(2));
        ticketDB.setVehicleRegNumber(rs.getString(7));
        ticketDB.setPrice(rs.getDouble(3));
        ticketDB.setInTime(rs.getTimestamp(4));
        ticketDB.setOutTime(rs.getTimestamp(5));
        LocalDate inTimeValue = LocalDate.ofInstant(ticketDB.getInTime().toInstant(), ZoneId.systemDefault());
        // assertEquals(ticket.getInTime(), ticketDAO.getTicket("DHIJK").getInTime());
        assertEquals("DHIJK", ticketDB.getVehicleRegNumber());
        assertTrue(inTimeExpected.isAfter(inTimeValue));
        assertFalse(ticketDAO.getTicket("DHIJK").getParkingSpot().isAvailable());
    }

    @Test
    public void testParkingLotExit() throws Exception{
        //ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        Connection con = null;
        con = ticketDAO.dataBaseConfig.getConnection();
        PreparedStatement ps = null;
        ps = con.prepareStatement("select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? order by t.OUT_TIME DESC");
        ps.setString(1,"DHIJK");
        ResultSet rs = ps.executeQuery();
        rs.next();
        Ticket ticketDB = new Ticket();
                //ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticketDB.setParkingSpot(parkingSpot);
                ticketDB.setId(rs.getInt(2));
                ticketDB.setVehicleRegNumber(rs.getString(1));
                ticketDB.setPrice(rs.getDouble(3));
                ticketDB.setInTime(rs.getTimestamp(4));
                ticketDB.setOutTime(rs.getTimestamp(5));
        
        
        LocalDate outTimeValue = LocalDate.ofInstant(ticketDB.getOutTime().toInstant(), ZoneId.systemDefault());
        LocalDate outTimeExpected = LocalDate.now();
        //verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        //assertThat(outTime<ticketDAO.getTicket("DHIJK").getOutTime());
        //assertEquals(0.00, outTimeValue);
        //assertTrue(outTimeExpected.isAfter(outTimeValue));
        assertEquals(outTimeExpected,outTimeValue);
    }

   @Test
    public void testParkingLotExitRecurringUser() throws Exception{
      // test le calcul d'un prix d'un ticket via l'appel de processIncomingVehicle et processExitingVehicle  pour user recurrent
      // entrer du vehicule
      testParkingLotExit();
      // testParkingLotExit();
      // testParkingLotExit();
      // recuperer le nombre de ticket
      Date outE = new Date();
      Connection con = null;
      con = ticketDAO.dataBaseConfig.getConnection();
    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
      PreparedStatement s = con.prepareStatement("SELECT COUNT(*) AS recordCount FROM ticket where VEHICLE_REG_NUMBER = ? and OUT_TIME < ?");
      s.setString(1, "DHIJK");
      s.setTimestamp(2, new Timestamp(outE.getTime()));
      ResultSet r = s.executeQuery();
      r.next();
      int count = r.getInt("recordCount");
      r.close();
      Ticket ticketDB = new Ticket();
      ParkingSpot parkingSpot = new ParkingSpot(2,ParkingType.CAR,false);
      Date inTime = new Date();
      inTime.setTime( System.currentTimeMillis() - (  120 * 60 * 1000) );
      ticketDB.setParkingSpot(parkingSpot);
      // ticketDB.setId(rs.getInt(2));
      ticketDB.setVehicleRegNumber("DHIJK");
      // ticketDB.setPrice(rs.getDouble(3));
      ticketDB.setInTime(inTime);
      // ticketDB.setOutTime(rs.getTimestamp(5));
      ticketDAO.saveTicket(ticketDB);
      parkingService.processExitingVehicle();
      assertTrue(1<count); // a faire avec 1 heure de park pour avoir la reduction
      // assertEquals(ticketDAO.getTicket("DHIJK").getPrice(), 1.425);
      assertEquals(ticketDAO.getTicket("DHIJK").getOutTime(), ticketDAO.getTicket("DHIJK").getInTime());
      
    }

}
