package com.appdynamicspilot.persistence;

import com.appdynamicspilot.oracle.jdbc.OracleQueryExecutor;
import com.appdynamicspilot.oracle.jdbc.QueryExecutor;
import com.appdynamicspilot.util.SpringContext;
import org.apache.log4j.Logger;

/**
 * Created by swetha.ravichandran on 10/12/15.
 */
public class OraclePersistence extends BasePersistenceImpl {
    /**
     * Logger for this class
     */
    private static final Logger LOGGER = Logger.getLogger(ItemPersistence.class);

    /**
     * Call to mysql db to get all the items
     * Connects to Oracle in case of slow fire
     * @return List<Item>
     */
    public void createOracleConnection() {

        //DEMO-367 Calling Oracle db in certain percentage
        //Supressed the slow query to make it generic

        if (shouldFireSlow()) {
            LOGGER.info("Querying oracle db");
            if (Math.random() >= 0.7) {
                LOGGER.error("Critical transaction Error, rolling back changes. Order execution aborted.");
            }

            QueryExecutor oracleItems = (QueryExecutor) SpringContext
                    .getBean("queryExecutor");
            oracleItems.executeOracleQuery();
        }
    }
}
