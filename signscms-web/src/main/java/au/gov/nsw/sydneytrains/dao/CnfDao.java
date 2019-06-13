package au.gov.nsw.sydneytrains.dao;

import au.gov.nsw.sydneytrains.helper.H2DatabaseAccess;
import au.gov.nsw.sydneytrains.model.CnfDevice;
import au.gov.nsw.sydneytrains.model.CnfLink;
import au.gov.nsw.sydneytrains.model.CnfSection;
import au.gov.nsw.sydneytrains.model.CnfView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by administrator on 25/4/17.
 */
public class CnfDao {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Connection dbConnection;

    public Connection getDbConnection() {

        return dbConnection;
    }

    public void setDbConnection(Connection dbConn) {

        this.dbConnection = dbConn;
    }

    /*
     * Check for the configuration tables to exist and create them if they don't.
     */
    public boolean initialiseDb() {
        boolean result = false;

        if (null == dbConnection) {
            System.out.println("DbConnection is null");
            return false;
        }
        try {
            initialiseCnfDevice();
            initialiseCnfView();
            initialiseCnfViewImageLinks();
            initialiseCnfViewDeviceLink();
            initialiseCnfSections();
            result = true;
        } catch (SQLException s) {
            log.debug(s.getMessage());
        }
        return result;
    }

    /*
     * Check for table to exist
     */
    private boolean doesTableExist(String tableName) throws SQLException {
        boolean result = false;
        PreparedStatement statement = null;

        try {
            final String cnfViewQuery = "SELECT * FROM " + tableName;
            statement = dbConnection.prepareStatement(cnfViewQuery);
            final ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                result = true;
            }
        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return result;
    }

    private void addTable(String tableName, String tableSchema) throws SQLException {

        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute("CREATE TABLE " + tableName + tableSchema);
        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
        }

    }

    private void dropTable(String tableName) throws SQLException{
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute("DROP TABLE  " + tableName);
        } catch (SQLException e) {
            log.debug(e.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
        }

    }
    private void initialiseCnfDevice () throws SQLException {
        if (!doesTableExist("CNFDEVICE")) {
            addTable( "CNFDEVICE",
                    "(DeviceId VARCHAR(15), DeviceName VARCHAR(50), DeviceDescription VARCHAR(150), PI_Name VARCHAR(50), PixelsHorizontal VARCHAR(10), PixelsVertical VARCHAR(10), Location VARCHAR(999))");
        } else {
            //DROP TABLE - CREATE NEW - This is added for any Schema changes
            dropTable("CNFDEVICE");
            addTable( "CNFDEVICE",
                    "(DeviceId VARCHAR(15), DeviceName VARCHAR(50), DeviceDescription VARCHAR(150), PI_Name VARCHAR(50), PixelsHorizontal VARCHAR(10), PixelsVertical VARCHAR(10), Location VARCHAR(999))");

        }
    }

    private void initialiseCnfView () throws SQLException {
        if (!doesTableExist("CNFVIEW")) {
            addTable( "CNFVIEW",
                    "(ViewId VARCHAR(15), ViewName VARCHAR(50), ViewDescription VARCHAR(150), PixelsHorizontal VARCHAR(10), PixelsVertical VARCHAR(10), Associated_Device VARCHAR(50) )");
        }else {
            //DROP TABLE - CREATE NEW - This is added for any Schema changes
            dropTable("CNFVIEW");
            addTable( "CNFVIEW",
                    "(ViewId VARCHAR(15), ViewName VARCHAR(50), ViewDescription VARCHAR(150), PixelsHorizontal VARCHAR(10), PixelsVertical VARCHAR(10), Associated_Device VARCHAR(50) )");
        }
    }

    private void initialiseCnfViewImageLinks () throws SQLException {
        if (!doesTableExist("CNFVIEWIMAGELINKS")) {
            addTable("CNFVIEWIMAGELINKS",
                    "(ViewId VARCHAR(15), Image VARCHAR(100) )");
        }
    }

    private void initialiseCnfViewDeviceLink () throws SQLException {
        if (!doesTableExist("CNFVIEWDEVICELINK")) {
            addTable("CNFVIEWDEVICELINK",
                    "(DeviceId VARCHAR(15), ViewId VARCHAR(15) )");
        }
    }

    private void initialiseCnfSections () throws SQLException {
        if (!doesTableExist("CNFSECTIONS")) {
            addTable("CNFSECTIONS",
                    "(SectionId VARCHAR(15), DeviceId VARCHAR(15), PositionId VARCHAR(15), V_OffSet VARCHAR(15) )");
        }
    }

    /*
     * READ table CNFDEVICE
     */
    public HashMap<String, CnfDevice> getAllCnfDevices() throws SQLException {
        PreparedStatement cnfDeviceStmt = null;
        HashMap<String, CnfDevice> cnfDevice = new HashMap<>();
        try {
            final String cnfDeviceQuery = "SELECT DeviceId, DeviceName, DeviceDescription, PI_Name, PixelsHorizontal, PixelsVertical FROM CNFDEVICE";
            cnfDeviceStmt = dbConnection.prepareStatement(cnfDeviceQuery);
            final ResultSet rs = cnfDeviceStmt.executeQuery();

            while (rs.next()) {

                String deviceId = rs.getString("DeviceId");
                String deviceName = rs.getString("DeviceName");
                String deviceDescription = rs.getString("DeviceDescription");
                String pi_name = rs.getString("PI_Name");
                String pixelsHorizontal = rs.getString("PixelsHorizontal");
                String pixelsVertical = rs.getString("PixelsVertical");
                String location = rs.getString("Location");

                cnfDevice.put(deviceId, new CnfDevice(deviceId, deviceName, deviceDescription,pi_name, Integer.valueOf(pixelsHorizontal), Integer.valueOf(pixelsVertical),location));
            }

        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (cnfDeviceStmt != null) {
                cnfDeviceStmt.close();
            }
        }
        return cnfDevice;

    }

    /*
     * WRITE table CNFDEVICE: empty table then add all CnfDevice records
     */
    public void setAllCnfDevices(HashMap<String, CnfDevice> devices) throws SQLException {
        PreparedStatement cnfDeviceStmt = null;
        try {
            // Empty the table
            final String sqlDelete = "DELETE FROM CNFDEVICE";
            cnfDeviceStmt = dbConnection.prepareStatement(sqlDelete);
            cnfDeviceStmt.executeUpdate();

            // Add the record one by one
            Iterator<String> keyIter = devices.keySet().iterator();

            while (keyIter.hasNext()) {
                String key = keyIter.next();
                CnfDevice device = devices.get(key);

                final String sqlUpdate = "INSERT INTO CNFDEVICE VALUES("
                        + "'" + device.getDeviceId() + "',"
                        + "'" + device.getDeviceName() + "',"
                        + "'" + device.getDescription() + "',"
                        + "'" + device.getPi_name() + "',"
                        + "'" + device.getPixelsHorizontal() + "',"
                        + "'" + device.getPixelsVertical() + "',"
                        + "'" + device.getLocation() + "')";
                cnfDeviceStmt = dbConnection.prepareStatement(sqlUpdate);
                cnfDeviceStmt.executeUpdate();
            }
        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (cnfDeviceStmt != null) {
                cnfDeviceStmt.close();
            }
        }
    }

    /*
     * READ table CNFVIEW
     */
    public HashMap<String, CnfView> getAllCnfViews() throws SQLException {
        PreparedStatement cnfViewStmt = null;
        HashMap<String, CnfView> cnfViewHashMap = new HashMap<>();
        try {
            final String cnfViewQuery = "SELECT ViewId, ViewName, ViewDescription, PixelsHorizontal, PixelsVertical, Associated_Device FROM CNFVIEW";
            cnfViewStmt = dbConnection.prepareStatement(cnfViewQuery);
            final ResultSet rs = cnfViewStmt.executeQuery();

            while (rs.next()) {

                String viewId = rs.getString("ViewId");
                String viewName = rs.getString("ViewName");
                String viewDescription = rs.getString("ViewDescription");
                String viewPixHorz = rs.getString("PixelsHorizontal");
                String viewPixVert = rs.getString("PixelsVertical");
                String assoc_device = rs.getString("Associated_Device");
                String[] images = getAllImagesForView(viewId);
                cnfViewHashMap.put(viewId, new CnfView(viewId, viewName, viewDescription, Integer.valueOf(viewPixHorz), Integer.valueOf(viewPixVert), assoc_device,images));
            }

        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (cnfViewStmt != null) {
                cnfViewStmt.close();
            }
        }
        return cnfViewHashMap;

    }

    /*
     * WRITE table CNFVIEW: empty table then add all CnfView records
     */
    public void setAllCnfViews(HashMap<String, CnfView> views) throws SQLException {
        PreparedStatement cnfDeviceStmt = null;
        try {
            // Empty the table
            final String sqlDelete = "DELETE FROM CNFVIEW";
            cnfDeviceStmt = dbConnection.prepareStatement(sqlDelete);
            cnfDeviceStmt.executeUpdate();

            // Add the record one by one
            Iterator<String> keyIter = views.keySet().iterator();
            int i=0;
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                CnfView view = views.get(key);
                i++;
                final String sqlUpdate = "INSERT INTO CNFVIEW VALUES("
                        + "'" + view.getViewId() + "',"
                        + "'" + view.getViewName() + "',"
                        + "'" + view.getDescription() + "',"
                        + "'" + view.getPixelsHorizontal() + "',"
                        + "'" + view.getPixelsVertical() + "',"
                        + "'" + view.getAssociated_Device() + "')";

                cnfDeviceStmt = dbConnection.prepareStatement(sqlUpdate);
                cnfDeviceStmt.executeUpdate();
            }
            System.out.print("Rows inserted " + i);
        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (cnfDeviceStmt != null) {
                cnfDeviceStmt.close();
            }
        }
    }

    /*
     * READ table CNFVIEWIMAGELINKS for a specific view
     */
    public String[] getAllImagesForView(String viewId) throws SQLException {
        String[] images = null;
        List<String> imageArr = new ArrayList<String>();
        PreparedStatement imageStmt = null;
        if (viewId != null) {
            // Query database for viewId
            try {
                final String imageQuery = "SELECT IMAGE FROM CNFVIEWIMAGELINKS WHERE VIEWID = ?";
                imageStmt = dbConnection.prepareStatement(imageQuery);
                imageStmt.setString(1, viewId);
                final ResultSet rs = imageStmt.executeQuery();

                while (rs.next()) {
                    String image = rs.getString("IMAGE");
                    imageArr.add(image);
                }

            } catch (SQLException s) {
                log.debug(s.getMessage());
            } finally {
                if (imageStmt != null) {
                    imageStmt.close();
                }
            }
        }
        if (imageArr.size() > 0) {
            images = imageArr.toArray(new String[0]);
        }
        return images;
    }

    /*
     * WRITE table CNFVIEWIMAGELINKS: empty table then add all image records
     */
    public void setAllCnfViewLinks(HashMap<String, CnfView> views) throws SQLException {
        PreparedStatement cnfDeviceStmt = null;
        try {
            // Empty the table
            final String sqlDelete = "DELETE FROM CNFVIEWIMAGELINKS";
            cnfDeviceStmt = dbConnection.prepareStatement(sqlDelete);
            cnfDeviceStmt.executeUpdate();

            // Add the record one by one
            Iterator<String> keyIter = views.keySet().iterator();

            while (keyIter.hasNext()) {
                String key = keyIter.next();
                CnfView view = views.get(key);

                String[] images = view.getImages();
                for (String image : images) {
                    final String sqlUpdate = "INSERT INTO CNFVIEWIMAGELINKS VALUES("
                            + "'" + view.getViewId() + "',"
                            + "'" + image + "')";
                    cnfDeviceStmt = dbConnection.prepareStatement(sqlUpdate);
                    cnfDeviceStmt.executeUpdate();
                }
            }
        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (cnfDeviceStmt != null) {
                cnfDeviceStmt.close();
            }
        }
    }

    /*
     * READ table CNFVIEWDEVICELINK
     */
    public HashMap<String, CnfLink> getAllDeviceViewLinks() throws SQLException {
        PreparedStatement cnfLinkStmt = null;
        HashMap<String, CnfLink> cnfLinkHashMap = new HashMap<>();
        try {
            final String cnfDeviceQuery = "SELECT DeviceId, ViewId FROM CNFVIEWDEVICELINK";
            cnfLinkStmt = dbConnection.prepareStatement(cnfDeviceQuery);
            final ResultSet rs = cnfLinkStmt.executeQuery();

            while (rs.next()) {

                String deviceId = rs.getString("DeviceId");
                String viewId = rs.getString("ViewId");

                cnfLinkHashMap.put(deviceId, new CnfLink(deviceId, viewId));
            }

        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (cnfLinkStmt != null) {
                cnfLinkStmt.close();
            }
        }
        return cnfLinkHashMap;

    }

    /*
     * WRITE table CNFVIEWDEVICELINK: empty table then add all CnfLink records
     */
    public void setAllCnfLinks(HashMap<String, CnfLink> links) throws SQLException {
        PreparedStatement cnfDeviceStmt = null;
        try {
            // Empty the table
            final String sqlDelete = "DELETE FROM CNFVIEWDEVICELINK";
            cnfDeviceStmt = dbConnection.prepareStatement(sqlDelete);
            cnfDeviceStmt.executeUpdate();

            // Add the record one by one
            Iterator<String> keyIter = links.keySet().iterator();

            while (keyIter.hasNext()) {
                String key = keyIter.next();
                CnfLink link = links.get(key);

                final String sqlUpdate = "INSERT INTO CNFVIEWDEVICELINK VALUES("
                        + "'" + link.getDeviceId() + "',"
                        + "'" + link.getViewId() + "')";
                cnfDeviceStmt = dbConnection.prepareStatement(sqlUpdate);
                cnfDeviceStmt.executeUpdate();
            }
        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (cnfDeviceStmt != null) {
                cnfDeviceStmt.close();
            }
        }
    }

    /*
     * READ table CNFSECTIONS
     */
    public HashMap<String, CnfSection> getAllSections() throws SQLException {
        PreparedStatement cnfSectionStmt = null;
        HashMap<String, CnfSection> cnfSectionHashMap = new HashMap<>();
        try {
            final String cnfSectionQuery = "SELECT SectionId, DeviceId, PositionId, V_Offset FROM CNFSECTIONS";
            cnfSectionStmt = dbConnection.prepareStatement(cnfSectionQuery);
            final ResultSet rs = cnfSectionStmt.executeQuery();

            while (rs.next()) {
                String sectionId = rs.getString("SectionId");
                String deviceId = rs.getString("DeviceId");
                String positionId = rs.getString("PositionId");
                String v_offset = rs.getString("V_Offset");

                cnfSectionHashMap.put(deviceId, new CnfSection(sectionId, deviceId, positionId, v_offset));
            }

        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (cnfSectionStmt != null) {
                cnfSectionStmt.close();
            }
        }
        return cnfSectionHashMap;
    }

    /*
     * WRITE table CNFSECTIONS: empty table then add all CnfSection records
     */
    public void setAllCnfSections(HashMap<String, CnfSection> sections) throws SQLException {
        PreparedStatement cnfDeviceStmt = null;
        try {
            // Empty the table
            final String sqlDelete = "DELETE FROM CNFSECTIONS";
            cnfDeviceStmt = dbConnection.prepareStatement(sqlDelete);
            cnfDeviceStmt.executeUpdate();

            // Add the record one by one
            Iterator<String> keyIter = sections.keySet().iterator();

            while (keyIter.hasNext()) {
                String key = keyIter.next();
                CnfSection section = sections.get(key);

                final String sqlUpdate = "INSERT INTO CNFSECTIONS VALUES("
                        + "'" + section.getSectionId() + "',"
                        + "'" + section.getDeviceId() + "',"
                        + "'" + section.getPositionId() + "',"
                        + "'" + section.getV_offset() + "')";
                cnfDeviceStmt = dbConnection.prepareStatement(sqlUpdate);
                cnfDeviceStmt.executeUpdate();
            }
        } catch (SQLException s) {
            log.debug(s.getMessage());
        } finally {
            if (cnfDeviceStmt != null) {
                cnfDeviceStmt.close();
            }
        }
    }

    public boolean updateDeviceView(String deviceId, String viewId) throws SQLException {
        boolean update = false;
        PreparedStatement updateStmt = null;
        System.out.println("Updating link device " + deviceId + " with view " + viewId);
        try {
            final String updt = "UPDATE CNFVIEWDEVICELINK SET ViewId = ? WHERE DeviceId = ?";
            updateStmt = dbConnection.prepareStatement(updt);
            updateStmt.setString(1,viewId);
            updateStmt.setString(2, deviceId);
            updateStmt.executeUpdate();
            update = true;
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            if (updateStmt != null) {
                updateStmt.close();
            }
        }

        return update;
    }

    public boolean addDeviceView(String deviceId, String viewId)  throws SQLException {
        boolean result = false;
        PreparedStatement statement = null;
        //System.out.println("Adding link device " + deviceId + " with view " + viewId);
        try {
            final String updt = "INSERT INTO CNFVIEWDEVICELINK VALUES(?,?)";
            statement = dbConnection.prepareStatement(updt);
            statement.setString(1, deviceId);
            statement.setString(2, viewId);
            result = true;
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }

        return result;
    }

    public boolean clearCnfTables() throws SQLException {
        boolean result = false;
        PreparedStatement statement = null;

        String[] tables = { "CNFDEVICE", "CNFVIEW", "CNFVIEWIMAGELINKS", "CNFVIEWDEVICELINK", "CNFSECTIONS"};
        try {
            for (String table: tables) {
                final String query = "DELETE FROM " + table;
                statement = dbConnection.prepareStatement(query);
                statement.executeUpdate();
            }
            result = true;
        } catch (SQLException s) {
            s.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return result;
    }
}


