package au.gov.nsw.sydneytrains.dao;

import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by administrator on 13/5/17.
 */
public class UserDao {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Connection dbConnection;

    public Connection getDbConnection() {

        return dbConnection;
    }

    public void setDbConnection(Connection dbConn) {

        this.dbConnection = dbConn;
    }

    /*
     * Read Users.csv into H2Database
     */
    public boolean initialiseDb() {
        return H2DatabaseAccess.initialiseUsers();
    }

    public User authenticateUser(String userName, String password) throws SQLException {
        User user = new User();
        user.setAuthenticated(false);
        PreparedStatement statement = null;
        try {
            final String userQuery = "SELECT FirstName, LastName, UserName, Password, Role, Area FROM USERS WHERE UserName =? AND PASSWORD =? ";
            statement = dbConnection.prepareStatement(userQuery);
            statement.setString(1, userName);
            statement.setString(2, password);
            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String firstName = resultSet.getString("FirstName");
                String lastName = resultSet.getString("LastName");
                String uName = resultSet.getString("UserName");
                String pwd = resultSet.getString("Password");
                String role = resultSet.getString("Role");
                String area = resultSet.getString("Area");

                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setUserName(uName);
                user.setPassword(pwd);
                user.setRole(role);
                user.setArea(area);
                user.setAuthenticated(true);

            }

        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
        }


        return user;
    }

}
