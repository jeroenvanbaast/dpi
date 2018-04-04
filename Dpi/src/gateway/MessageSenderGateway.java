/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway;

import java.io.Serializable;
import javax.jms.Session;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import models.RequestReply;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author Jeroen
 */
public class MessageSenderGateway {

    private Connection connection;
    private Session session;
    private Queue queue;
    private MessageProducer producer;

    public MessageSenderGateway(String channelName) throws JMSException {
        //created ConnectionFactory object for creating connection 
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        factory.setTrustAllPackages(true);
        //Establish the connection
        Connection connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = session.createQueue(channelName);

        //Added as a producer
        producer = session.createProducer(queue);
    }

    public ObjectMessage createMessage(Serializable object) throws JMSException {
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(object);
        return objectMessage;
    }

    public void send(ObjectMessage message) throws JMSException {
        this.producer.send(message);
    }
}
