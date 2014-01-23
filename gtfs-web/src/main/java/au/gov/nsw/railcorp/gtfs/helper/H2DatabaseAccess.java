// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.helper;

import au.gov.nsw.railcorp.gtfs.dao.TripDao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * H2 Database Access.
 * @author Paritosh
 */
public class H2DatabaseAccess {

    private static H2DatabaseAccess sH2DBINSTANCE;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Get H2 DB Connection.
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

    private Connection getCnx() {

        Connection cnx = null;
        try {
            Class.forName("org.h2.Driver");
            final String jdbcUrl = "jdbc:h2:~/transitBundle;AUTO_SERVER=TRUE";
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
     * Get TripDAO.
     * @return TripDAO.
     */
    public static TripDao getTripDao() {

        final TripDao dao = new TripDao();
        dao.setDbConnection(H2DatabaseAccess.getDbConnection());
        return dao;
    }
}
