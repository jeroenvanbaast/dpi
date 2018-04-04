/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway;

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

    public MessageReceiverGateway(String channelName) throws JMSException {
        //created ConnectionFactory object for creating connection 
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        factory.setTrustAllPackages(true);
        //Establish the connection
        Connection connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = session.createQueue(channelName);
        consumer = session.createConsumer(queue);

    }

    public void setListener(MessageListener ml) throws JMSException {
        consumer.setMessageListener(ml);
    }
}
