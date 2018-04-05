/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway;

import forms.LoanBrokerFrame;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import models.BankInterestReply;
import models.BankInterestRequest;
import models.LoanReply;
import models.LoanRequest;

/**
 *
 * @author Jeroen
 */
public class BankAppGateway {

    private MessageSenderGateway messageSenderGateway;
    private MessageReceiverGateway messageReceiverGateway;
    private LoanBrokerFrame frame;

    public BankAppGateway(LoanBrokerFrame frame) throws JMSException {
        this.messageSenderGateway = new MessageSenderGateway("bankRequest");
        this.messageReceiverGateway = new MessageReceiverGateway("bankReply");
        this.frame = frame;
    }

    public void sendBankRequest(BankInterestRequest request) throws JMSException {
        ObjectMessage message = messageSenderGateway.createMessage(request);
        this.messageSenderGateway.send(message);
    }

    public void onBankReplyArrived() throws JMSException {
        messageReceiverGateway.setListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) {
                 if (msg instanceof ObjectMessage) {
                     try {
                         BankInterestReply reply = (BankInterestReply) ((ObjectMessage) msg).getObject();
                         frame.recievedBankReply(reply);
                     } catch (JMSException ex) {
                         Logger.getLogger(BankAppGateway.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
            }
        });
    }
}
