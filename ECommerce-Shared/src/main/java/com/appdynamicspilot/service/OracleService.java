package com.appdynamicspilot.service;

import com.appdynamicspilot.model.Item;
import com.appdynamicspilot.persistence.ItemPersistence;
import com.appdynamicspilot.persistence.OraclePersistence;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by swetha.ravichandran on 10/12/15.
 */
public class OracleService {
    /**
     * Logger Class
     */
    private static final Logger log = Logger.getLogger(ItemService.class);

    /**
     * Ref to OraclePersistence class
     */
    private OraclePersistence oraclePersistence;
    public void setOraclePersistence(OraclePersistence oraclePersistence) {
        this.oraclePersistence = oraclePersistence;
    }

    public void createOracleConnection() {
         oraclePersistence.createOracleConnection();
    }
}
