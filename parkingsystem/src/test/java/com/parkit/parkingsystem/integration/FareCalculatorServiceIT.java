package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FareCalculatorServiceIT {

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
        // lors de la phase d'entrée, l'horaire de sortie n'est pas renseigné
        //assertNull(ticketDB.getOutTime());
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

    // Bike
    @Test
    public void testParkingLotExitBike() throws Exception{
        //given
        dataBasePrepareService.clearDataBaseEntries();
        saveTicketBike();
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
        System.out.println("PRIX: "+ticketDB.getPrice());
    }

    private static void saveTicketBike() {
        Ticket ticketDB = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  2*60 * 60 * 1000) );
        ticketDB.setParkingSpot(parkingSpot);
        ticketDB.setVehicleRegNumber("BIKETEST");
        ticketDB.setInTime(inTime);
        ticketDAO.saveTicket(ticketDB);
    }



}
