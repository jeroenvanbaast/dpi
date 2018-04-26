/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gateway;

import forms.LoanBrokerFrame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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
public class BankAppGateway
{

    private MessageSenderGateway messageSenderGatewayIng;
    private MessageSenderGateway messageSenderGatewayAbn;
    private MessageSenderGateway messageSenderGatewayRabo;
    private MessageReceiverGateway messageReceiverGateway;
    private LoanBrokerFrame frame;
    private HashMap<UUID, List<BankInterestReply>> messages;

    public BankAppGateway(LoanBrokerFrame frame)
    {
        this.messageSenderGatewayIng = new MessageSenderGateway("bankRequestING");
        this.messageSenderGatewayAbn = new MessageSenderGateway("bankRequestABN");
        this.messageSenderGatewayRabo = new MessageSenderGateway("bankRequestRabo");
        this.messageReceiverGateway = new MessageReceiverGateway("bankReply");
        this.frame = frame;
        messages = new HashMap<>();
    }

    public void sendBankRequest(BankInterestRequest request)
    {
        if (request.getAmount() <= 100000 && request.getTime() <= 10)
        {
            ObjectMessage message = messageSenderGatewayIng.createMessage(request);
            this.messageSenderGatewayIng.send(message);
        }
        if (request.getAmount() >= 200000 && request.getAmount() <= 300000 && request.getTime() <= 20)
        {
            ObjectMessage message = messageSenderGatewayAbn.createMessage(request);
            this.messageSenderGatewayAbn.send(message);
        }
        if (request.getAmount() <= 250000 && request.getTime() <= 15)
        {
            ObjectMessage message = messageSenderGatewayRabo.createMessage(request);
            this.messageSenderGatewayRabo.send(message);
        }
    }

    public void onBankReplyArrived()
    {
        messageReceiverGateway.setListener(new MessageListener()
        {
            @Override
            public void onMessage(Message msg)
            {
                if (msg instanceof ObjectMessage)
                {
                    try
                    {
                        //TODO-Jeroen zorgen dat hij pas doorgaat waneer alle berichten ontvangen zijn.                      
                        BankInterestReply reply = (BankInterestReply) ((ObjectMessage) msg).getObject();
                        int times = expectedReplys(reply);
                        UUID Uuid = reply.getLoanRequest().getUuid();
                        if (messages.containsKey(Uuid))
                        {
                            List<BankInterestReply> list = messages.get(Uuid);
                            list.add(reply);
                            messages.put(reply.getLoanRequest().getUuid(), list);
                            if (times == list.size())
                            {
                                sendReply(list);
                            }
                        } else
                        {
                            ArrayList<BankInterestReply> list = new ArrayList<>();
                            list.add(reply);
                            messages.put(Uuid, list);
                        }
                    } catch (JMSException ex)
                    {
                        Logger.getLogger(BankAppGateway.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    public void sendReply(List<BankInterestReply> list)
    {
        double interest = 9999999;
        BankInterestReply reply = null;
        for (BankInterestReply bankReply : list)
        {
            if (bankReply.getInterest() < interest)
            {
                interest = bankReply.getInterest();
                reply = bankReply;
            }
        }
        if (reply != null)
        {
            frame.recievedBankReply(reply);
        }
    }

    public int expectedReplys(BankInterestReply reply)
    {
        LoanRequest request = reply.getLoanRequest();
        int times = 0;
        if (request.getAmount() <= 100000 && request.getTime() <= 10)
        {
            times++;
        }
        if (request.getAmount() >= 200000 && request.getAmount() <= 300000 && request.getTime() <= 20)
        {
            times++;
        }
        if (request.getAmount() <= 250000 && request.getTime() <= 15)
        {
            times++;
        }
        return times;
    }

}
