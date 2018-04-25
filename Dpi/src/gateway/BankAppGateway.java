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

    private MessageSenderGateway messageSenderGatewayIng;
    private MessageSenderGateway messageSenderGatewayAbn;
    private MessageSenderGateway messageSenderGatewayRabo;
    private MessageReceiverGateway messageReceiverGateway;
    private LoanBrokerFrame frame;

    public BankAppGateway(LoanBrokerFrame frame) {
        this.messageSenderGatewayIng = new MessageSenderGateway("bankRequestING");
        this.messageSenderGatewayAbn = new MessageSenderGateway("bankRequestABN");
        this.messageSenderGatewayRabo = new MessageSenderGateway("bankRequestRabo");
        this.messageReceiverGateway = new MessageReceiverGateway("bankReply");
        this.frame = frame;
    }

    public void sendBankRequest(BankInterestRequest request) {
        int replys = expectedReplys(request);
        try {
            if (request.getAmount() <= 100000 && request.getTime() <= 10) {
                ObjectMessage message = messageSenderGatewayIng.createMessage(request);
                message.setIntProperty("times", replys);
                this.messageSenderGatewayIng.send(message);
            }
            if (request.getAmount() >= 200000 && request.getAmount() <= 300000 && request.getTime() <= 20) {
                ObjectMessage message = messageSenderGatewayAbn.createMessage(request);
                message.setIntProperty("times", replys);
                this.messageSenderGatewayAbn.send(message);
            }
            if (request.getAmount() <= 250000 && request.getTime() <= 15) {
                ObjectMessage message = messageSenderGatewayRabo.createMessage(request);
                message.setIntProperty("times", replys);
                this.messageSenderGatewayRabo.send(message);
            }
        } catch (JMSException ex) {
            Logger.getLogger(BankAppGateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void onBankReplyArrived() {
        messageReceiverGateway.setListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) {
                if (msg instanceof ObjectMessage) {
                    try {
                        msg.getIntProperty("times");
                        //TODO-Jeroen zorgen dat hij pas doorgaat waneer alle berichten ontvangen zijn.
                        BankInterestReply reply = (BankInterestReply) ((ObjectMessage) msg).getObject();
                        frame.recievedBankReply(reply);
                    } catch (JMSException ex) {
                        Logger.getLogger(BankAppGateway.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    public int expectedReplys(BankInterestRequest request) {
        int times = 0;
        if (request.getAmount() <= 100000 && request.getTime() <= 10) {
            times++;
        }
        if (request.getAmount() >= 200000 && request.getAmount() <= 300000 && request.getTime() <= 20) {
            times++;
        }
        if (request.getAmount() <= 250000 && request.getTime() <= 15) {
            times++;
        }
        return times;
    }

}
