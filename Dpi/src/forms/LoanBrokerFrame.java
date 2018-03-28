package forms;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import static java.awt.image.ImageObserver.WIDTH;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import models.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class LoanBrokerFrame extends JFrame implements MessageListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
    private JList<JListLine> list;

    private MessageProducer producerLoan;
    private MessageProducer producerBank;
    private Session session;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    LoanBrokerFrame frame = new LoanBrokerFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public LoanBrokerFrame() {
        try {
            createConnection();
        } catch (JMSException ex) {
            Logger.getLogger(LoanBrokerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        setTitle("Loan Broker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
        gbl_contentPane.rowHeights = new int[]{233, 23, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 7;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        contentPane.add(scrollPane, gbc_scrollPane);

        list = new JList<JListLine>(listModel);
        scrollPane.setViewportView(list);
    }

    private JListLine getRequestReply(LoanRequest request) {

        for (int i = 0; i < listModel.getSize(); i++) {
            JListLine rr = listModel.get(i);
            if (rr.getLoanRequest() == request) {
                return rr;
            }
        }

        return null;
    }

    public void add(LoanRequest loanRequest) {
        listModel.addElement(new JListLine(loanRequest));
    }

    public void add(LoanRequest loanRequest, BankInterestRequest bankRequest) {
        JListLine rr = getRequestReply(loanRequest);
        if (rr != null && bankRequest != null) {
            rr.setBankRequest(bankRequest);
            list.repaint();
        }
    }

    public void add(LoanRequest loanRequest, BankInterestReply bankReply) {
        JListLine rr = getRequestReply(loanRequest);
        if (rr != null && bankReply != null) {
            rr.setBankReply(bankReply);;
            list.repaint();
        }
    }

    public void createConnection() throws JMSException {
        //created ConnectionFactory object for creating connection 
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        factory.setTrustAllPackages(true);
        //Establish the connection
        Connection connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queueLoanRequest = session.createQueue("loanRequest");

        //Set up a consumer to consume messages off of the admin queue
        MessageConsumer consumer = session.createConsumer(queueLoanRequest);
        consumer.setMessageListener(this);

        Queue queueLoanReply = session.createQueue("loanReply");
        producerLoan = session.createProducer(queueLoanReply);

        //Set producer
        Queue queueBankRequest = session.createQueue("bankRequest");
        producerBank = session.createProducer(queueBankRequest);

        //consume response
        Queue queueBankReply = session.createQueue("bankReply");
        MessageConsumer responseConsumer = session.createConsumer(queueBankReply);
        responseConsumer.setMessageListener(this);
    }

    /**
     * Method that is called when a new message is recieved.
     *
     * @param msg the recieved message
     */
    @Override
    public void onMessage(Message msg) {
        try {
            if (msg instanceof ObjectMessage) {
                Object object = ((ObjectMessage) msg).getObject();

                if (object instanceof LoanRequest) {
                    LoanRequest loanRequest = (LoanRequest) object;
                    add(loanRequest);
                    sendBankInterestRequest(loanRequest);
                }

                if (object instanceof RequestReply) {
                    RequestReply reqeustReply = (RequestReply) object;
                    if (reqeustReply.getReply() instanceof BankInterestReply) {
                        sendLoanReply(reqeustReply);
                    }
                }
            }
        } catch (JMSException ex) {
            Logger.getLogger(LoanBrokerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendBankInterestRequest(LoanRequest loanRequest) throws JMSException {
        // create bankInterestRequest
        BankInterestRequest bankInterestRequest = new BankInterestRequest();
        bankInterestRequest.setAmount(loanRequest.getAmount());
        bankInterestRequest.setTime(loanRequest.getTime());
        bankInterestRequest.setLoanRequest(loanRequest);

        // send bankInterestRequest
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(bankInterestRequest);
        producerBank.send(objectMessage);

        // addToList
        add(loanRequest, bankInterestRequest);
    }

    public void sendLoanReply(RequestReply requestReply) throws JMSException {
        // create loanReply
        LoanReply loanReply = new LoanReply();
        BankInterestReply bankInterestReply = (BankInterestReply) requestReply.getReply();
        loanReply.setInterest(bankInterestReply.getInterest());
        loanReply.setQuoteID(bankInterestReply.getQuoteId());

         BankInterestRequest BankInterestRequest = (BankInterestRequest) requestReply.getRequest();
         LoanRequest loanRequest = BankInterestRequest.getLoanRequest();
        // create RequestReply to Send
        RequestReply returnRequestReply = new RequestReply(loanRequest,loanReply);
        
        // send loanReply
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(returnRequestReply);
        producerLoan.send(objectMessage);

        // addToList
       
        add(loanRequest, bankInterestReply);
    }
}
