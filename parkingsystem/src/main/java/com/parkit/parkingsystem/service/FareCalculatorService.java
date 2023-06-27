package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;


/**
 * methods related to fare base on time and the type of vehicle.
 * @author Mougni
 *
 */
public class FareCalculatorService {

    /**
     * this method calculate the fare of a vehicle based on the time in and exit of the ticket and if there is a discount eligible for this vehicle
     * @param ticket represents the ticket of the vehicle
     * @param discount represents the eligibility of a vehicle to get a discouht in type of boolean
     * @throws IllegalArgumentException if the ticket exit time is earlier than the entry time or if the exit time is null
     * @throws IllegalArgumentException if the parking type is neither bike or car.
     */
        public void calculateFare(Ticket ticket, Boolean discount) throws Exception{
    	
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();
        double duration = (ticket.getOutTime().getTime()-ticket.getInTime().getTime())/(1000.0*60*60);
        
        if(ticket.getParkingSpot().getParkingType() == null) {
        	throw new IllegalArgumentException("Unkown Parking Type");
        }
        
        switch (ticket.getParkingSpot().getParkingType()){
        
            case CAR: {
            	if(duration >= 0.5) {
                    if(discount == true){
                        double price = duration * Fare.CAR_RATE_PER_HOUR;
                        ticket.setPrice(price - (price*0.05)); 
                    }else{
                        ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    }
            		
            		System.out.println("fonction : "+ticket.getPrice());
            	
                
            	}else {
            		ticket.setPrice(0.00);
            	}
            	break;
            	
            }
            case BIKE: {
            	if(duration >= 0.5) {
            		if(discount == true){
                  double price = duration * Fare.BIKE_RATE_PER_HOUR;
                  ticket.setPrice(price - (price*0.05)); 
                }else{
                  ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                }
            }else {
            		ticket.setPrice(0.00);
            	}
                break;
            }
            default: {
                System.out.println("dans la boucle");
            	throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    	
    }

    /**
     * this method call the function calculateFare to calculate the fare of a vehicle based on the time in and exit of the ticket but with the discount false for eligibility of a vehicle
     * @param ticket represents the ticket of the vehicle
     */
    public void calculateFare(Ticket ticket) throws Exception{
      calculateFare(ticket, false);
    }
}