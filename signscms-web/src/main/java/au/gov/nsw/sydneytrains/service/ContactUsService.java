package au.gov.nsw.sydneytrains.service;

import au.gov.nsw.sydneytrains.dao.ContactUsDao;
import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.ContactUsForm;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ContactUsService {

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);


    public void startScheduler() throws Exception {
        try {
            ScheduledFuture<?> files = executorService.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    //System.out.println("running contact us service");
                    checkContactUsData();
                }
            }, 0, 60, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public void checkContactUsData() {
        try {
            ContactUsDao contactUsDao = H2DatabaseAccess.getContactUsDao();
            //Get all Open Contact Us Forms
            if (doesContactUsTableExists()) {
                ArrayList<ContactUsForm> contactUsForms = contactUsDao.getOpenContactUsForms();
                for (ContactUsForm contactUsForm : contactUsForms) {
                    // Send email
                    if (sendEmail(contactUsForm)) {
                        // Update the status of Contact Us Form -
                        contactUsForm.setStatus("EMAIL_SENT");
                        contactUsDao.updateContactUsFormStatus(contactUsForm);

                    }
                }
            } else {
                System.out.println("CONTACT US TABLE DOESN'T EXISTS");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean doesContactUsTableExists() {
        boolean result = false;
        ContactUsDao contactUsDao = H2DatabaseAccess.getContactUsDao();
        result = contactUsDao.doesContactUsTableExists();


        return result;
    }

    private boolean sendEmail(ContactUsForm contactUsForm) {

        boolean result = false;
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.user", "spi.sydneytrains");
        props.put("mail.smtp.password", "Paritosh1");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("spi.sydneytrains", "Paritosh1");
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("spi.sydneytrains@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("paritoshnandwani@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("paritosh.nandwani@transport.nsw.gov.au"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("leon.miller@transport.nsw.gov.au"));
            message.setSubject("User Feedback - " + contactUsForm.getSubject());

            String messageBody = "User: " + contactUsForm.getUsername() + "\n"
                    + "Email: " + contactUsForm.getEmail() + "\n"
                    + "Phone: " + contactUsForm.getPhone() + "\n"
                    + "Subject: " + contactUsForm.getSubject() + "\n"
                    + "Message: " + contactUsForm.getMessage() + "\n";
            //message.setText(messageBody);
            Multipart multipart = new MimeMultipart();
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(messageBody);
            multipart.addBodyPart(textBodyPart);

            if(contactUsForm.getFileName() != null) {
                MimeBodyPart messageBodyPart = new MimeBodyPart();

                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(contactUsForm.getFile());
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(contactUsForm.getFileName());
                multipart.addBodyPart(messageBodyPart);
            }

            message.setContent(multipart);
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", "spi.sydneytrains@gmail.com", "Paritosh1");
            transport.send(message);
            transport.close();

            result = true;

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

}
