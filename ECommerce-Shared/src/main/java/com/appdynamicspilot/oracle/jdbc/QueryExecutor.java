/*
 * Copyright 2015 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamicspilot.oracle.jdbc;

import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryExecutor {

    private final static Logger LOGGER = Logger.getLogger(QueryExecutor.class.getName());
    private String oracleQueryString;
    private DataSource dataSource = null;

    public String getOracleQueryString() {
        return oracleQueryString;
    }
    public void setOracleQueryString(String oracleQueryString) {
        this.oracleQueryString = oracleQueryString;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Integer executeOracleQuery() {
        Connection connection = null;
        Statement stmt = null;
        try {
            LOGGER.info("Oracle Query String :" +  getOracleQueryString());
            connection = getDataSource().getConnection();
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(getOracleQueryString());
            while (rs.next()) {
                Integer output = rs.getInt("count");
                return output;
            }
        } catch (SQLException sqle) {
            LOGGER.error("This may be ignored in case of Oracle is not setup");
            LOGGER.error(sqle.getMessage());
        }  finally {
            if (connection != null) {try{connection.close();}catch (SQLException sqle) {}}
            if (stmt != null) {try{stmt.close();}catch (SQLException sqle) {}}
        }
        return 0;
    }
}