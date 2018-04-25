/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.MessageListener;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author Jeroen
 */
public class MessageReceiverGateway {

    private Connection connection;
    private Session session;
    private Queue queue;
    private MessageConsumer consumer;

    public MessageReceiverGateway(String channelName) {
        try {
            //created ConnectionFactory object for creating connection
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
            factory.setTrustAllPackages(true);
            //Establish the connection
            Connection connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = session.createQueue(channelName);
            consumer = session.createConsumer(queue);
        } catch (JMSException ex) {
            Logger.getLogger(MessageReceiverGateway.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setListener(MessageListener ml) {
        try {
            consumer.setMessageListener(ml);
        } catch (JMSException ex) {
            Logger.getLogger(MessageReceiverGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
