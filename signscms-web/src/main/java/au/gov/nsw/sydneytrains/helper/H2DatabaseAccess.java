package au.gov.nsw.sydneytrains.helper;

import au.gov.nsw.sydneytrains.dao.CnfDao;
import au.gov.nsw.sydneytrains.dao.ContactUsDao;
import au.gov.nsw.sydneytrains.dao.SchedulerDao;
import au.gov.nsw.sydneytrains.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Class description:
 * <p>
 * H2DatabaseAccess provides access to the H2 database.
 * It provides all the necessary functions to connect, add, delete, read and write.
 */
public class H2DatabaseAccess {

    private static H2DatabaseAccess sH2DBINSTANCE;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Get H2 DB Connection.
     *
     * @return Connection.
     */
    public static Connection getDbConnection() {

        Connection conn = null;
        if (sH2DBINSTANCE == null) {
            sH2DBINSTANCE = new H2DatabaseAccess();
        }
        conn = sH2DBINSTANCE.getCnx();
        return conn;
    }

    /**
     * Get the connection to the database. Note that there is a difference in connecting to the app or to Amazon.
     * See comment in the function.
     *
     * @return Connection
     */
    private Connection getCnx() {

        Connection cnx = null;
        try {
            Class.forName("org.h2.Driver");
            final String jdbcUrl = "jdbc:h2:~/signscms;AUTO_SERVER=TRUE";

            // For Amazon Ec2 comment above and uncomment below
           // final String jdbcUrl = "jdbc:h2:/var/lib/tomcat8/webapps/ROOT/signscms;AUTO_SERVER=TRUE";

            final Properties prop = new Properties();
            prop.setProperty("user", "sa");
            prop.setProperty("password", "sa");
            cnx = DriverManager.getConnection(jdbcUrl, prop);
        } catch (SQLException e) {
            log.error(e.getMessage());
        } catch (ClassNotFoundException s) {
            log.error(s.getMessage());
        }
        return cnx;
    }

    /**
     * Get CnfDAO.
     *
     * @return CnfDAO.
     */
    public static CnfDao getCnfDao() {

        final CnfDao dao = new CnfDao();
        dao.setDbConnection(H2DatabaseAccess.getDbConnection());
        return dao;
    }

    public static UserDao getUserDao() {

        final UserDao dao = new UserDao();
        dao.setDbConnection(H2DatabaseAccess.getDbConnection());
        return dao;
    }

    public static SchedulerDao getSchedulerDao() {

        final SchedulerDao dao = new SchedulerDao();
        dao.setDbConnection(H2DatabaseAccess.getDbConnection());
        return dao;
    }

    public static ContactUsDao getContactUsDao() {

        final ContactUsDao dao = new ContactUsDao();
        dao.setDbConnection(H2DatabaseAccess.getDbConnection());
        return dao;
    }

    /*
     * Initialise the H2Database: read Users.csv into USERS table.
     * The other tables are only read into the database when instructed through an Administrator's function.
     */
    public static boolean initialiseUsers() {
        boolean result = false;

        try {
            sH2DBINSTANCE.dropTable("USERS");
            result = sH2DBINSTANCE.initialiseUsersTable();
        } catch (SQLException e) {
            System.out.println("ERROR, failed to initialise H2Database: " + e.getMessage());
        }
        return result;
    }

    /*
     * Initialise ContactUs
     */

    public static boolean initialiseContactUs() {
        boolean result = false;
        String tableName = "CONTACT_US";
        String tableSchema = "(USER_NAME VARCHAR(100), EMAIL VARCHAR(100), MOBILE VARCHAR(100), SUBJECT VARCHAR(1000), MESSAGE VARCHAR(1000), DATE_LOGGED TIMESTAMP, STATUS VARCHAR(50), FILE_NAME VARCHAR(100), FILE BLOB)";
        try {
            initialiseTable(tableName, tableSchema);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public static void initialiseTable(String tableName, String tableSchema) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getDbConnection();
            statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS " + tableName + tableSchema);
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }


    public static boolean initialiseSchedule() {
        boolean result = false;
        String tableName = "SCHEDULE";
        String tableSchema = "(SCHEDULE_ID VARCHAR(100), DEVICE_ID VARCHAR(100), SCHEDULE_VIEW_ID VARCHAR(100), PREVIOUS_VIEW_ID VARCHAR(100), START_DATE TIMESTAMP, END_DATE TIMESTAMP, STATUS VARCHAR(50), IS_DELETED VARCHAR(50), CREATED_BY VARCHAR(50), CREATED_DATE TIMESTAMP, MODIFIED_DATE TIMESTAMP)";
        try {
            initialiseTable(tableName, tableSchema);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void dropTable(String strTable) throws SQLException {
        Statement statement = null;
        Connection con = null;
        try {
            con = getDbConnection();
            statement = con.createStatement();
            statement.execute("DROP TABLE " + strTable);
        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    /*
      * Read the contents of the Users.csv file into the USERS table
     */
    private boolean initialiseUsersTable() {
        final String cnfUsersFilename = "Users.csv";

        ClassLoader classLoader = getClass().getClassLoader();
        File Users = new File(classLoader.getResource(cnfUsersFilename).getFile());

        boolean success = true;
        try {
            // Add Users to Database
            addTable(
                    "USERS",
                    "(FirstName VARCHAR(50), LastName VARCHAR(50), UserName VARCHAR(50), Password VARCHAR(50), Role VARCHAR(50), Area VARCHAR(50))",
                    Users);
        } catch (Exception e) {
            success = false;
            log.error(e.getMessage());
        }

        return success;
    }

    /*
     * Read one CSV file into a table
     */
    private void addTable(String tableName, String tableSchema, File file)
            throws SQLException {

        Statement statement = null;
        Connection con = null;
        try {
            con = getDbConnection();
            statement = con.createStatement();
            statement.execute("CREATE TABLE " + tableName + tableSchema
                    + " AS SELECT * FROM CSVREAD('" + file.getPath() + "');");
        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

} // End of class
