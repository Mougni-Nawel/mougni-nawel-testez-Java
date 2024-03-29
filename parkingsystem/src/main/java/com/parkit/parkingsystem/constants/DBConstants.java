package com.parkit.parkingsystem.constants;


import com.parkit.parkingsystem.config.Generated;

/**
 * different constants that will represent sql request.
 * @author Mougni
 *
 */
@Generated
public class DBConstants {

    public static final String GET_NEXT_PARKING_SPOT = "select min(PARKING_NUMBER) from parking where AVAILABLE = true and TYPE = ?";
    public static final String UPDATE_PARKING_SPOT = "update parking set available = ? where PARKING_NUMBER = ?";

    public static final String SAVE_TICKET = "insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)";
    public static final String UPDATE_TICKET = "update ticket set PRICE=?, OUT_TIME=? where ID=?";
    public static final String GET_TICKET = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? order by t.OUT_TIME DESC";
    public static final String GET_COUNT = "SELECT COUNT(*) AS recordCount FROM ticket where VEHICLE_REG_NUMBER = ? and OUT_TIME IS NOT NULL";
    public static final String GET_TICKET_PRICE = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? and t.OUT_TIME IS NULL order by t.IN_TIME DESC";

}
