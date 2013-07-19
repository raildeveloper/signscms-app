// RailCorp 2013

package au.gov.nsw.railcorp.gtfs.helper;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PublishBundle in H2 Database.
 * @author paritosh
 */
public class PublishBundle {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * publishBundleH2db.
     * @param bundle
     *            file
     * @return success
     */
    public boolean publishBundleH2db(File bundle) {

        boolean sucess = true;
        try {
            log.info("Recived request to publish bundle in H2 Db" + bundle);
            final ZipFile zipFile = new ZipFile(bundle);
            File fileShapes = null;
            File fileRoutes = null;
            File fileStops = null;
            File fileTrips = null;
            File fileStopTimes = null;
            final String absoultePath = bundle.getAbsolutePath();
            String filePath = absoultePath.substring(0, absoultePath.lastIndexOf(File.separator));
            filePath += File.separator;
            log.info("TransitBundle served from " + absoultePath);
            log.info("Exported bundle to " + filePath);
            fileRoutes = createDbFile("routes.txt", zipFile,
            filePath);
            fileShapes = createDbFile("shapes.txt", zipFile,
            filePath);
            fileStops = createDbFile("stops.txt", zipFile,
            filePath);
            fileStopTimes = createDbFile("stop_times.txt", zipFile,
            filePath);
            fileTrips = createDbFile("trips.txt", zipFile,
            filePath);

            log.info("Delete all H2 Objects");
            // Delete all objects before inserting new records
            deleteAllH2DbObjects();

            log.info("Create H2 Tables for Transit Bundle");
            addDbFile(
            "ROUTES",
            "(ROUTE_ID VARCHAR(15), AGENCY_ID VARCHAR(50), ROUTE_SHORT_NAME VARCHAR(10), ROUTE_LONG_NAME VARCHAR(255), "
            + "ROUTE_DESC VARCHAR(255), ROUTE_TYPE INT, ROUTE_URL VARCHAR(255), ROUTE_COLOR VARCHAR(255), ROUTE_TEXT_COLOR VARCHAR(255) )",
            fileRoutes);
            addDbFile(
            "SHAPES",
            "(SHAPE_ID VARCHAR(15), SHAPE_PT_LAT VARCHAR(50), SHAPE_PT_LON VARCHAR(50), SHAPE_PT_SEQUENCE INT, "
            + "SHAPE_DIST_TRAVELED VARCHAR(50))", fileShapes);
            addDbFile(
            "STOPS",
            "(STOP_ID VARCHAR(50) PRIMARY KEY, STOP_CODE VARCHAR(50), STOP_NAME VARCHAR(255), STOP_DESC VARCHAR(255), "
            + "STOP_LAT VARCHAR(50), STOP_LON VARCHAR(50), ZONE_ID VARCHAR(50), STOP_URL VARCHAR(255), LOCATION_TYPE INT, "
            + "PARENT_STATION VARCHAR(50) , STOP_TIMEZONE VARCHAR(50), WHEELCHAIR_BOARDING INT )",
            fileStops);
            addDbFile(
            "STOP_TIMES",
            "(TRIP_ID VARCHAR(100), ARRIVAL_TIME VARCHAR(50), DEPARTURE_TIME VARCHAR(50), STOP_ID VARCHAR(50), STOP_SEQUENCE INT, "
            + "STOP_HEADSIGN VARCHAR(50), PICKUP_TYPE INT, DROP_OFF_TYPE INT, SHAPE_DIST_TRAVELED VARCHAR(50))",
            fileStopTimes);
            addDbFile(
            "TRIPS",
            "(ROUTE_ID VARCHAR(15), SERVICE_ID VARCHAR(50), TRIP_ID VARCHAR(100), TRIP_HEADSIGN VARCHAR(50), TRIP_SHORT_NAME VARCHAR(50), "
            + "DIRECTION_ID INT, BLOCK_ID VARCHAR(50), SHAPE_ID VARCHAR(15), WHEELCHAIR_ACCESSIBLE VARCHAR(50))",
            fileTrips);
            // Create Required Index
            // statement.execute("CREATE INDEX stopIndex ON STOP_TIMES (TRIP_ID, STOP_SEQUENCE)");

            log.info("Create Index on STOP_TIMES - TRIP_ID & STOP_SEQUENCE");
            createIndex();
            log.info("Index created on STOP_TIMES - TRIP_ID & STOP_SEQUENCE");
        } catch (ZipException e) {

            sucess = false;
            log.error(e.getMessage());
        } catch (IOException e) {

            sucess = false;
            log.error(e.getMessage());

        } catch (SQLException e) {
            sucess = false;
            log.error(e.getMessage());
        }

        return sucess;

    }

    private void createIndex() throws SQLException {

        Connection con = null;
        Statement stmt = null;
        try {
            con = H2DatabaseAccess.getDbConnection();
            stmt = con.createStatement();
            stmt.execute("CREATE INDEX SI ON STOP_TIMES (TRIP_ID, STOP_SEQUENCE)");
        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                con.close();
            }
        }

    }

    private void addDbFile(String tableName, String tableSchem, File file)
    throws SQLException
    {

        Statement statement = null;
        Connection con = null;
        try {
            con = H2DatabaseAccess.getDbConnection();
            statement = con.createStatement();
            // statement.execute("DROP TABLE " + tableName + "");
            // statement.execute("DROP ALL OBJECTS");

            statement.execute("CREATE TABLE " + tableName + tableSchem
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

    private File createDbFile(String name, ZipFile zipfile, String path)
    throws IOException
    {

        // save shapes
        final ZipEntry entry = zipfile.getEntry(name);
        final InputStream stream = zipfile.getInputStream(entry);
        final InputSupplier<InputStream> supplier = new InputSupplier<InputStream>() {

            public InputStream getInput() {

                return stream;
            }
        };

        final File file = new File(path + entry.getName());
        Files.copy(supplier, file);

        return file;
    }

    private void deleteAllH2DbObjects() throws SQLException {

        Statement statement = null;
        Connection con = null;
        try {
            con = H2DatabaseAccess.getDbConnection();
            statement = con.createStatement();
            statement.execute("DROP ALL OBJECTS");
            statement.execute("SHUTDOWN COMPACT");

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

}
