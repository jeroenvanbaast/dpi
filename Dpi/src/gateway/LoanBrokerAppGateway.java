/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway;

import forms.JMSBankFrame;
import forms.LoanBrokerFrame;
import forms.LoanClientFrame;
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
public class LoanBrokerAppGateway {

    private MessageSenderGateway messageSenderGateway;
    private MessageReceiverGateway messageReceiverGateway;

    private MessageSenderGateway messageSenderGatewayBank;
    private MessageReceiverGateway messageReceiverGatewayBank;
    
    private LoanClientFrame loanClientFrame;
    private JMSBankFrame jmsBankFrame;

    public LoanBrokerAppGateway(LoanClientFrame frame) {
        this.messageSenderGateway = new MessageSenderGateway("loanRequest");
        this.messageReceiverGateway = new MessageReceiverGateway("loanReply");
        this.loanClientFrame = frame;
    }

    public LoanBrokerAppGateway(JMSBankFrame frame, String name) {
        this.messageSenderGatewayBank = new MessageSenderGateway("bankReply");
        this.messageReceiverGatewayBank = new MessageReceiverGateway("bankRequest" + name);
        this.jmsBankFrame = frame;
    }

    public void applyForLoan(LoanRequest request) {
        ObjectMessage message = messageSenderGateway.createMessage(request);
        messageSenderGateway.send(message);
    }

    public void responseToBroker(BankInterestReply reply){
        ObjectMessage message = messageSenderGatewayBank.createMessage(reply);
        messageSenderGatewayBank.send(message);
    }

    public void onLoanReplyArrived() {
        messageReceiverGateway.setListener((Message msg) -> {
            if (msg instanceof ObjectMessage) {
                try {
                    LoanReply reply = (LoanReply) ((ObjectMessage) msg).getObject();
                    loanClientFrame.add(reply);
                } catch (JMSException ex) {
                    Logger.getLogger(LoanBrokerAppGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void onBankReplyArrived() {
        messageReceiverGatewayBank.setListener((Message msg) ->
        {
            if(msg instanceof ObjectMessage){
                try {
                    BankInterestRequest reply = (BankInterestRequest) ((ObjectMessage) msg).getObject();
                    jmsBankFrame.add(reply);
                } catch (JMSException ex) {
                    Logger.getLogger(LoanBrokerAppGateway.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
