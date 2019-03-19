package au.gov.nsw.sydneytrains.dao;

import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.ContactUsForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by administrator on 13/5/17.
 */
public class ContactUsDao {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Connection dbConnection;

    private static java.sql.Timestamp getCurrentTimeStamp() {

        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());

    }

    private static java.sql.Timestamp getTimestampForDate(Date date) {
        return new java.sql.Timestamp(date.getTime());
    }

    public Connection getDbConnection() {

        return dbConnection;
    }

    public void setDbConnection(Connection dbConn) {

        this.dbConnection = dbConn;
    }

    /*
     * Initalise ContactUs Database
     */
    public boolean initialiseDb() {
        return H2DatabaseAccess.initialiseContactUs();
    }

    public boolean insertContactUsForm(String username, String email, String phone, String subject, String message, String status) throws SQLException {
        boolean result = false;
        PreparedStatement insertCntUsStmt = null;
        try {
            final String insertStmt = "INSERT INTO CONTACT_US (USER_NAME, EMAIL, MOBILE, SUBJECT, MESSAGE, DATE_LOGGED, STATUS) VALUES(?,?,?,?,?,?,?)";
            insertCntUsStmt = dbConnection.prepareStatement(insertStmt);
            insertCntUsStmt.setString(1, username);
            insertCntUsStmt.setString(2, email);
            insertCntUsStmt.setString(3, phone);
            insertCntUsStmt.setString(4, subject);
            insertCntUsStmt.setString(5, message);
            insertCntUsStmt.setTimestamp(6, getCurrentTimeStamp());
            insertCntUsStmt.setString(7, status);
            insertCntUsStmt.executeUpdate();
            result = true;

        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            if (insertCntUsStmt != null) {
                insertCntUsStmt.close();
            }
        }

        return result;
    }

    public boolean insertContactUsForm(String username, String email, String phone, String subject, String message, String status, String fileName, File file) throws SQLException {
        boolean result = false;
        PreparedStatement insertCntUsStmt = null;
        try {
            final String insertStmt = "INSERT INTO CONTACT_US (USER_NAME, EMAIL, MOBILE, SUBJECT, MESSAGE, DATE_LOGGED, STATUS, FILE_NAME, FILE) VALUES(?,?,?,?,?,?,?,?,?)";

            FileInputStream fileInputStream = new FileInputStream(file);
            insertCntUsStmt = dbConnection.prepareStatement(insertStmt);
            insertCntUsStmt.setString(1, username);
            insertCntUsStmt.setString(2, email);
            insertCntUsStmt.setString(3, phone);
            insertCntUsStmt.setString(4, subject);
            insertCntUsStmt.setString(5, message);
            insertCntUsStmt.setTimestamp(6, getCurrentTimeStamp());
            insertCntUsStmt.setString(7, status);
            insertCntUsStmt.setString(8, fileName);
            insertCntUsStmt.setBinaryStream(9, fileInputStream, (int) file.length());
            insertCntUsStmt.executeUpdate();
            result = true;

        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            if (insertCntUsStmt != null) {
                insertCntUsStmt.close();
            }
        }

        return result;
    }

    public ArrayList<ContactUsForm> getOpenContactUsForms() throws SQLException {
        ArrayList<ContactUsForm> cntUsForms = new ArrayList<ContactUsForm>();
        PreparedStatement cntUsStmt = null;
        try {
            final String cntUsStmtQuery = "SELECT USER_NAME, EMAIL, MOBILE, SUBJECT, MESSAGE, DATE_LOGGED, STATUS, FILE_NAME, FILE  FROM CONTACT_US WHERE STATUS = \'OPEN\'";
            cntUsStmt = dbConnection.prepareStatement(cntUsStmtQuery);
            final ResultSet rs = cntUsStmt.executeQuery();

            while (rs.next()) {
                ContactUsForm cntUsForm = new ContactUsForm();
                cntUsForm.setUsername(rs.getString("USER_NAME"));
                cntUsForm.setEmail(rs.getString("EMAIL"));
                cntUsForm.setPhone(rs.getString("MOBILE"));
                cntUsForm.setSubject(rs.getString("SUBJECT"));
                cntUsForm.setMessage(rs.getString("MESSAGE"));
                Timestamp sD = rs.getTimestamp("DATE_LOGGED");
                cntUsForm.setDatelogged(new java.util.Date(sD.getTime()));
                cntUsForm.setStatus(rs.getString("STATUS"));
                String fileName = rs.getString("FILE_NAME");
                cntUsForm.setFileName(fileName);

                if (fileName != null) {
                    InputStream inputStream = rs.getBinaryStream("FILE");

                    String rootPath = System.getProperty("catalina.home");
                    File dir = new File(rootPath + File.separator + "emailFiles");
                    if (!dir.exists())
                        dir.mkdirs();

                    File file = new File(dir.getAbsolutePath() + File.separator + fileName);
                    System.out.println("Email File" + file.getAbsolutePath());

                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1];
                    while (inputStream.read(buffer) > 0) {
                        fileOutputStream.write(buffer);
                    }
                    fileOutputStream.close();
                    cntUsForm.setFile(file);
                }
                cntUsForms.add(cntUsForm);


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cntUsStmt != null) {
                cntUsStmt.close();
            }
        }

        return cntUsForms;

    }

    public boolean updateContactUsFormStatus(ContactUsForm contactUsForm) throws SQLException {
        boolean result = false;

        PreparedStatement updateCntUsStmt = null;
        try {
            final String updateQuery = "UPDATE CONTACT_US SET STATUS=? WHERE USER_NAME = ? AND DATE_LOGGED = ? ";
            updateCntUsStmt = dbConnection.prepareStatement(updateQuery);
            updateCntUsStmt.setString(1, contactUsForm.getStatus());
            updateCntUsStmt.setString(2, contactUsForm.getUsername());
            updateCntUsStmt.setTimestamp(3, getTimestampForDate(contactUsForm.getDatelogged()));

            updateCntUsStmt.executeUpdate();
            result = true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (updateCntUsStmt != null) {
                updateCntUsStmt.close();
            }
        }


        return result;
    }

    public boolean doesContactUsTableExists() {
        boolean result = false;
        try {
            DatabaseMetaData dbm = dbConnection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "CONTACT_US", null);
            if (tables.next()) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
