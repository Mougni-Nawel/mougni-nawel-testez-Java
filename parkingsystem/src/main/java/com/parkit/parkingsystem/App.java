package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.Generated;
import com.parkit.parkingsystem.service.InteractiveShell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class.
 * @author Mougni
 *
 */
@Generated
public class App {

    private static final Logger logger = LogManager.getLogger("App");

    /**
     * Main method call all the methods.
     * @param args The command line arguments.
     */
    public static void main(String args[]){
        logger.info("Initializing Parking System");
        InteractiveShell.loadInterface();
    }
}
