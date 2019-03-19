package au.gov.nsw.sydneytrains.dao;

import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.CnfSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by administrator on 13/5/17.
 */
public class SchedulerDao {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Connection dbConnection;

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
        return H2DatabaseAccess.initialiseSchedule();
    }


    public ArrayList<CnfSchedule> getAllSchedules() throws SQLException {
        ArrayList<CnfSchedule> cnfSchedules = new ArrayList<CnfSchedule>();
        PreparedStatement cnfScheduleStmt = null;
        try {
            final String cnfScheduleStmtQuery = "SELECT SCHEDULE_ID, DEVICE_ID, SCHEDULE_VIEW_ID, PREVIOUS_VIEW_ID, START_DATE, END_DATE, STATUS, IS_DELETED FROM SCHEDULE WHERE IS_DELETED != \'TRUE\' ORDER BY DEVICE_ID";
            cnfScheduleStmt = dbConnection.prepareStatement(cnfScheduleStmtQuery);
            final ResultSet rs = cnfScheduleStmt.executeQuery();

            while (rs.next()) {
                CnfSchedule cnfSchedule = new CnfSchedule();
                cnfSchedule.setCnfScheduleId(rs.getString("SCHEDULE_ID"));
                cnfSchedule.setCnfDevice(rs.getString("DEVICE_ID"));
                cnfSchedule.setPreviousCnfView(rs.getString("PREVIOUS_VIEW_ID"));
                cnfSchedule.setScheduleCnfView(rs.getString("SCHEDULE_VIEW_ID"));
                Timestamp sD = rs.getTimestamp("START_DATE");
                cnfSchedule.setStartTime(new Date(sD.getTime()));
                Timestamp eD = rs.getTimestamp("END_DATE");
                cnfSchedule.setEndTime(new Date(eD.getTime()));
                cnfSchedule.setStatus(rs.getString("STATUS"));
                cnfSchedule.setIsDeleted(rs.getString("IS_DELETED"));

                cnfSchedules.add(cnfSchedule);


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cnfScheduleStmt != null) {
                cnfScheduleStmt.close();
            }
        }

        return cnfSchedules;

    }

    public boolean updateSchedule(CnfSchedule schedule) throws SQLException {
        boolean result = false;
        PreparedStatement updateSchdStmt = null;
        try {
            final String updateQuery = "UPDATE SCHEDULE SET DEVICE_ID = ?, SCHEDULE_VIEW_ID = ?, PREVIOUS_VIEW_ID = ?, START_DATE =?, " +
                    "END_DATE =?, STATUS=?, IS_DELETED=?, MODIFIED_DATE =? WHERE SCHEDULE_ID =? ";
            updateSchdStmt = dbConnection.prepareStatement(updateQuery);
            updateSchdStmt.setString(1, schedule.getCnfDevice());
            updateSchdStmt.setString(2, schedule.getScheduleCnfView());
            updateSchdStmt.setString(3, schedule.getPreviousCnfView());
            updateSchdStmt.setTimestamp(4, getTimestampForDate(schedule.getStartTime()));
            updateSchdStmt.setTimestamp(5, getTimestampForDate(schedule.getEndTime()));
            updateSchdStmt.setString(6, schedule.getStatus());
            updateSchdStmt.setString(7, schedule.getIsDeleted());
            updateSchdStmt.setTimestamp(8, getCurrentTimeStamp());
            updateSchdStmt.setString(9, schedule.getCnfScheduleId());

            updateSchdStmt.executeUpdate();
            result = true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (updateSchdStmt != null) {
                updateSchdStmt.close();
            }
        }


        return result;
    }


    public boolean insertSchedule(CnfSchedule schedule) throws SQLException {
        boolean result = false;
        PreparedStatement cnfScheduleInsertStmt = null;
        try {
            Random rand = new Random();
            final String cnfScheduleInsertStmtQuery = "INSERT INTO SCHEDULE (SCHEDULE_ID, DEVICE_ID, SCHEDULE_VIEW_ID, " +
                    "PREVIOUS_VIEW_ID, START_DATE, END_DATE, STATUS, IS_DELETED, CREATED_BY, CREATED_DATE, MODIFIED_DATE ) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            String schId = Integer.toString(rand.nextInt(999) + 1);

            cnfScheduleInsertStmt = dbConnection.prepareStatement(cnfScheduleInsertStmtQuery);
            cnfScheduleInsertStmt.setString(1, schId);
            cnfScheduleInsertStmt.setString(2, schedule.getCnfDevice());
            cnfScheduleInsertStmt.setString(3, schedule.getScheduleCnfView());
            cnfScheduleInsertStmt.setString(4, schedule.getPreviousCnfView());
            cnfScheduleInsertStmt.setTimestamp(5, getTimestampForDate(schedule.getStartTime()));
            cnfScheduleInsertStmt.setTimestamp(6, getTimestampForDate(schedule.getEndTime()));
            cnfScheduleInsertStmt.setString(7, schedule.getStatus());
            cnfScheduleInsertStmt.setString(8, schedule.getIsDeleted());
            cnfScheduleInsertStmt.setString(9, schedule.getUsername());
            cnfScheduleInsertStmt.setTimestamp(10, getCurrentTimeStamp());
            cnfScheduleInsertStmt.setTimestamp(11, getCurrentTimeStamp());


            cnfScheduleInsertStmt.executeUpdate();
            result = true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cnfScheduleInsertStmt != null) {
                cnfScheduleInsertStmt.close();
            }

        }
        return result;

    }

    public CnfSchedule getSchedule(String scheduleId) throws SQLException {
        CnfSchedule cnfSchedule = new CnfSchedule();

        PreparedStatement cnfScheduleStmt = null;
        try {
            final String cnfScheduleStmtQuery = "SELECT SCHEDULE_ID, DEVICE_ID, SCHEDULE_VIEW_ID, PREVIOUS_VIEW_ID, START_DATE, END_DATE, STATUS, IS_DELETED FROM SCHEDULE WHERE SCHEDULE_ID = ?";
            cnfScheduleStmt = dbConnection.prepareStatement(cnfScheduleStmtQuery);
            cnfScheduleStmt.setString(1,scheduleId);
            final ResultSet rs = cnfScheduleStmt.executeQuery();

            while (rs.next()) {
                cnfSchedule.setCnfScheduleId(rs.getString("SCHEDULE_ID"));
                cnfSchedule.setCnfDevice(rs.getString("DEVICE_ID"));
                cnfSchedule.setPreviousCnfView(rs.getString("PREVIOUS_VIEW_ID"));
                cnfSchedule.setScheduleCnfView(rs.getString("SCHEDULE_VIEW_ID"));
                Timestamp sD = rs.getTimestamp("START_DATE");
                cnfSchedule.setStartTime(new Date(sD.getTime()));
                Timestamp eD = rs.getTimestamp("END_DATE");
                cnfSchedule.setEndTime(new Date(eD.getTime()));
                cnfSchedule.setStatus(rs.getString("STATUS"));
                cnfSchedule.setIsDeleted(rs.getString("IS_DELETED"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cnfScheduleStmt != null) {
                cnfScheduleStmt.close();
            }
        }

        return cnfSchedule;

    }



    private static java.sql.Timestamp getCurrentTimeStamp() {

        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());

    }

    private static java.sql.Timestamp getTimestampForDate(Date date) {
        return new java.sql.Timestamp(date.getTime());
    }
}
