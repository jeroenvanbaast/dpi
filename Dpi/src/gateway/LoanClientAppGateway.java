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
import models.LoanReply;
import models.LoanRequest;

/**
 *
 * @author Jeroen
 */
public class LoanClientAppGateway {

    private MessageSenderGateway messageSenderGateway;
    private MessageReceiverGateway messageReceiverGateway;
    private LoanBrokerFrame frame;

    public LoanClientAppGateway(LoanBrokerFrame frame) throws JMSException {
        this.messageSenderGateway = new MessageSenderGateway("loanReply");
        this.messageReceiverGateway = new MessageReceiverGateway("loanRequest");
        this.frame = frame;
    }

    public void SendLoanReply(LoanReply reply) throws JMSException {
        ObjectMessage message = messageSenderGateway.createMessage(reply);
        this.messageSenderGateway.send(message);
    }

    public void onLoanRequestArrived() throws JMSException {
        messageReceiverGateway.setListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) {
                  if (msg instanceof ObjectMessage) {
                      try {
                          LoanRequest request = (LoanRequest) ((ObjectMessage) msg).getObject();
                          frame.add(request);
                          frame.recievedLoanRequest(request);
                      } catch (JMSException ex) {
                          Logger.getLogger(LoanClientAppGateway.class.getName()).log(Level.SEVERE, null, ex);
                      }
                  }
            }
        });
    }
}
