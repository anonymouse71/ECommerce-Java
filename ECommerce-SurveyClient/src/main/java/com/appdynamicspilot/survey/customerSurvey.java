package com.appdynamicspilot.survey;

import javax.jms.ConnectionFactory;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageListener;
import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Override;
import java.lang.String;
import java.lang.Thread;
import java.util.Properties;

class JMSMessageListener implements MessageListener {
    @Override
    public void onMessage(javax.jms.Message msg) {
        try {
            if (! (msg instanceof TextMessage))
                throw new RuntimeException("no text message");
            TextMessage tm = (TextMessage) msg;
            System.out.println(tm.getText());                  // print message
        }
        catch (JMSException e) {
            System.err.println("Error Reading Msg: " + e.toString());
        }
    }
}

public class customerSurvey {

    /**
     * Main method.
     *
     * @param args  not used
     *
     */
    public static void main(String[] args) throws RuntimeException, IOException {
        Properties prop = new Properties();
        String propFileName = "jms.properties";

        InputStream inputStream = customerSurvey.class.getResourceAsStream(propFileName);

        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }

        String url = prop.getProperty("jms.url");
        String duration = prop.getProperty("duration");
        boolean runForever = false;
        int runDuration = 1;

        if (duration.trim().equalsIgnoreCase("forever")) {
            runForever = true;
        }
        else {
            if (duration.trim().matches("^[1-9]\\d*$")) {
                try {
                    runDuration = Integer.parseInt(duration.trim());
                } catch (NumberFormatException e) {
                    System.err.println("Duration is invalid: " + duration.trim() + " " + e.toString());
                    runDuration = 1;
                }
            }
        }
        System.out.println("Properties duration: " + duration.trim() + " runDuration = " + runDuration + " Forever = " + runForever);

        ConnectionFactory factory = new ActiveMQConnectionFactory(url);
        Connection connection = null;

        try {
            connection = factory.createConnection();
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("CustomerQueue");
            MessageConsumer consumer = session.createConsumer(queue);
            JMSMessageListener listener = new JMSMessageListener();
            consumer.setMessageListener(listener);
            connection.start();
            do {
                Thread.sleep(runDuration * 60 * 1000);
            } while (runForever);
        }
        catch (JMSException | InterruptedException e) {
            if (e instanceof JMSException)
                System.err.println("Error Connecting: " + e.toString());
        }
        finally {
            try {
                connection.close();
            }
            catch (JMSException e) {
                System.err.println("Error Disconnecting: " + e.toString());
            }
        }
    }
}