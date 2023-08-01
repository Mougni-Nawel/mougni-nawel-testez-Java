package com.parkit.parkingsystem.model;

import com.parkit.parkingsystem.config.Generated;
import com.parkit.parkingsystem.constants.ParkingType;


/**
 * represents a parkingSpot
 * @author Mougni
 *
 */
@Generated
public class ParkingSpot {
    private int number;
    private ParkingType parkingType;
    private boolean isAvailable;

    /**
     * constructor.
     * @param number of the parking spot
     * @param parkingType is the type of vehicle
     * @param isAvailable is the availabality of the spot
     *
     */
    public ParkingSpot(int number, ParkingType parkingType, boolean isAvailable) {
        this.number = number;
        this.parkingType = parkingType;
        this.isAvailable = isAvailable;
    }

    public int getId() {
        return number;
    }

    public void setId(int number) {
        this.number = number;
    }

    public ParkingType getParkingType() {
        return parkingType;
    }

    public void setParkingType(ParkingType parkingType) {
        this.parkingType = parkingType;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

}
