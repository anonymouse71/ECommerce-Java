package com.appdynamics.inventory;

import com.appdynamicspilot.exception.InventoryServerException;
import com.appdynamicspilot.util.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

public class OrderDaoImpl implements OrderDao {

    Client dbClient = ClientBuilder.newClient();
    WebTarget webTarget = dbClient
            .target("http://rds-dbwrapper:8080/rds-dbwrapper/query/execute");
    Invocation.Builder invocationBuilder = null;
    private Logger logger = LoggerFactory.getLogger(OrderDaoImpl.class);
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

    public Long createOrder(OrderRequest orderRequest) throws InventoryServerException {
        try {
            EntityManager entityManager = getEntityManagerFactory().createEntityManager();

            if (orderRequest.getItemId() == 5) {
                throw new InventoryServerException("Error in creating order for " + orderRequest.getItemId(), null);
            }
            if (entityManager != null) {
                Query q = entityManager.createNativeQuery(this.selectQuery);
                q.getResultList();
                entityManager.close();
            }

            logger.info("Querying oracle db - inventory");
            QueryExecutor oracleItems = (QueryExecutor) SpringContext.getBean("queryExecutor");
            oracleItems.executeOracleQuery();

            if (orderRequest != null)
                return processOrder(orderRequest);
            else
                logger.info("OrderRequest is null");

            //Call to slow db calls
            Random randInteger = new Random();
            int randomizeSlowQuery = randInteger.nextInt(5);

            if (randomizeSlowQuery == 0) {
                this.slowQueryParam = true;
                dbQuery(this.queryType, this.slowQueryParam, "oracle");
            } else {
                this.slowQueryParam = false;
                dbQuery(this.queryType, this.slowQueryParam, "oracle");
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

                        logger.info("order stored is: " + order.getId() + " " + order.getQuantity() + " " + order.getCreatedOn());

                        entityManager.getTransaction().begin();
                        entityManager.persist(order);
                        entityManager.getTransaction().commit();

                        Thread.sleep(500);

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
}