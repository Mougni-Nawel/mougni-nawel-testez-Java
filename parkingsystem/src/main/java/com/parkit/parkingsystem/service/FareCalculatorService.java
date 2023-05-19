package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

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

    public void calculateFare(Ticket ticket) throws Exception{
      calculateFare(ticket, false);
    }
}