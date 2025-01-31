import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;

public class MySQLDao {
    private Properties properties = new Properties();
    private Connection conn;

    public MySQLDao() {
    }

    /**
     * Constructor that loads properties from a file.
     *
     * @param propFilename the name of the properties file to load
     */
    public MySQLDao(String propFilename) {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(propFilename)) {
            if (input == null) {
                throw new IOException("Property file '" + propFilename + "' not found in the classpath.");
            }
            properties.load(input);
            // Debugging loaded properties
            System.out.println("Loaded properties: " + properties);
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public MySQLDao(Connection conn){
        this.conn = conn;
    }

    /**
     * Creates connection with database using the database.properties file as param
     *
     * @return A Connection object if successful, null otherwise.
     */
    public Connection getConnection() {
        try {
            if (properties.isEmpty()) {
                throw new IllegalStateException("Database properties are not loaded.");
            }

            if (conn == null || conn.isClosed()) {
                String driver = properties.getProperty("driver");
                String url = properties.getProperty("url");
                String database = properties.getProperty("database");
                String username = properties.getProperty("username");
                String password = properties.getProperty("password", "");

                // Debugging logs
                System.out.println("Connecting with:");
                System.out.println("Driver: " + driver);
                System.out.println("URL: " + url + database);
                System.out.println("Username: " + username);

                Class.forName(driver);
                conn = DriverManager.getConnection(url + database, username, password);
            }
        } catch (Exception e) {
            System.err.println("Error getting connection: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Closes the connection to the database.
     *
     * @param conn the connection to close
     */
    public void freeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println(LocalDateTime.now() + ": Failed to close database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}