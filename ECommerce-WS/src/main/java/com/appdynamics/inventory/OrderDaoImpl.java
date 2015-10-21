package com.appdynamics.inventory;

import com.appdynamicspilot.exception.InventoryServerException;
import com.appdynamicspilot.service.OracleService;
import com.appdynamicspilot.util.SpringContext;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;

public class OrderDaoImpl implements OrderDao {

    private static final int DEFAULT_INTERNAL = 15;

    private int interval = DEFAULT_INTERNAL;

    Client dbClient = ClientBuilder.newClient();
    WebTarget webTarget = dbClient
            .target("http://rds-dbwrapper:8080/rds-dbwrapper/query/execute");
    Invocation.Builder invocationBuilder = null;
    private static final Logger logger = Logger.getLogger(OrderDaoImpl.class);
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private String queryType = "join";
    private boolean slowQueryParam;
    private String selectQuery = null;

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    public OracleService getOracleService() {
        return (OracleService) SpringContext.getBean("oracleService");
    }

    public Long createOrder(OrderRequest orderRequest) throws InventoryServerException {
        try {

            getOracleService().createOracleConnection();

            if (orderRequest != null)
                return processOrder(orderRequest);
            else
                logger.info("OrderRequest is null");

        } catch (Exception e) {
            logger.error(e.getMessage());
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            e.printStackTrace(pw);
            String errorDetail = writer.toString();
            logger.error(errorDetail);
        }
        return new Long(0);
    }

    private Long processOrder(OrderRequest orderRequest) {
        try {
            EntityManager entityManager = getEntityManagerFactory().createEntityManager();
            if (entityManager != null) {
                InventoryItem item = entityManager.find(InventoryItem.class,
                        orderRequest.getItemId());

                if (item != null) {
                    Order order = new Order(orderRequest, item);
                    if (order != null) {
                        order.setQuantity(orderRequest.getQuantity());

                        entityManager.getTransaction().begin();
                        entityManager.persist(order);
                        entityManager.getTransaction().commit();

                        entityManager.getTransaction().begin();
                        entityManager.remove(order);
                        entityManager.getTransaction().commit();

                        logger.info("order created is: " + order.getId() + " " + order.getQuantity() + " " + order.getCreatedOn());
                        return order.getId();
                    }
                }
                entityManager.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            e.printStackTrace(pw);
            String errorDetail = writer.toString();
            logger.error(errorDetail);
        }
        return new Long(0);
    }

    public void dbQuery(String queryType, boolean slowQueryParam, String dbName) {
        logger.info(queryType + " " + slowQueryParam + " " + dbName);
        WebTarget queryWebTarget = webTarget.path(queryType + "/" + slowQueryParam + "/" + dbName);
        invocationBuilder = queryWebTarget
                .request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        logger.info("the response for the target is: " + response.getStatus());
        logger.info(response.readEntity(String.class));
    }

    /**
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(int interval) {
        if (interval < 0) {
            logger.warn("Invalid interval: " + interval + "; setting to default: " + DEFAULT_INTERNAL);
            this.interval = DEFAULT_INTERNAL;
        } else {
            this.interval = interval;
        }
    }

    protected boolean shouldFireSlow() {
        return (Math.random() * 100) <= interval;
    }
}