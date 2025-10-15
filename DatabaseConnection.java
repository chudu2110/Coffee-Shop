import java.sql.*;
import java.io.File;

/**
 * DatabaseConnection class for managing SQLite database connections
 * Implements singleton pattern for connection management
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String DATABASE_NAME = "coffee_shop.db";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;
    
    // Private constructor for singleton pattern
    private DatabaseConnection() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DATABASE_URL);
            
            // Enable foreign key constraints
            Statement stmt = connection.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.close();
            
            System.out.println("Database connection established successfully.");
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
            System.err.println("Please add sqlite-jdbc jar to your classpath.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }
    
    // Get singleton instance
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    // Get database connection
    public Connection getConnection() {
        try {
            // Check if connection is still valid
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DATABASE_URL);
                
                // Enable foreign key constraints
                Statement stmt = connection.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to get database connection: " + e.getMessage());
        }
        return connection;
    }
    
    // Initialize database with schema
    public boolean initializeDatabase() {
        try {
            // Ensure connection is valid
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DATABASE_URL);
                Statement stmt = connection.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.close();
            }

            // Ensure DB file exists (will be created on first connection use)
            File dbFile = new File(DATABASE_NAME);
            boolean isNewDatabase = !dbFile.exists();

            // Always ensure tables/indexes exist
            createTables();

            // Migrate schema/data if needed (e.g., switch to VND pricing)
            migrateToVNDIfNeeded();

            // Seed sample data on first run OR when critical tables are empty
            if (isNewDatabase || isTableEmpty("menu_items")) {
                insertSampleData();
                System.out.println("Database seeded with sample data.");
            } else {
                System.out.println("Database exists with data.");
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            return false;
        }
    }

    // Check if a table has no rows
    private boolean isTableEmpty(String tableName) {
        String sql = "SELECT COUNT(1) AS c FROM " + tableName;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("c") == 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to check table '" + tableName + "': " + e.getMessage());
        }
        return false;
    }
    
    // Create database tables
    private void createTables() throws SQLException {
        String[] createTableQueries = {
            // Menu items table
            "CREATE TABLE IF NOT EXISTS menu_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name VARCHAR(100) NOT NULL," +
            "description TEXT," +
            "base_price DECIMAL(10,3) NOT NULL," +
            "category VARCHAR(50) NOT NULL," +
            "item_type VARCHAR(50) NOT NULL," +
            "coffee_type VARCHAR(50)," +
            "is_available BOOLEAN DEFAULT TRUE," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            
            // Customers table
            "CREATE TABLE IF NOT EXISTS customers (" +
            "customer_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name VARCHAR(100) NOT NULL," +
            "email VARCHAR(150) UNIQUE," +
            "phone_number VARCHAR(20)," +
            "loyalty_points DECIMAL(10,2) DEFAULT 0.00," +
            "registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            
            // Tables table
            "CREATE TABLE IF NOT EXISTS tables (" +
            "table_number INTEGER PRIMARY KEY," +
            "capacity INTEGER NOT NULL," +
            "status VARCHAR(20) DEFAULT 'AVAILABLE'," +
            "current_customer_id INTEGER," +
            "occupied_since TIMESTAMP," +
            "reserved_until TIMESTAMP," +
            "notes TEXT," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (current_customer_id) REFERENCES customers(customer_id)" +
            ")",
            
            // Orders table
            "CREATE TABLE IF NOT EXISTS orders (" +
            "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "customer_id INTEGER NOT NULL," +
            "status VARCHAR(20) DEFAULT 'PENDING'," +
            "service_type VARCHAR(20) NOT NULL," +
            "table_number INTEGER," +
            "subtotal DECIMAL(10,3) NOT NULL," +
            "tax DECIMAL(10,3) NOT NULL," +
            "discount DECIMAL(10,3) DEFAULT 0.00," +
            "total_amount DECIMAL(10,3) NOT NULL," +
            "special_instructions TEXT," +
            "order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "completion_time TIMESTAMP," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)," +
            "FOREIGN KEY (table_number) REFERENCES tables(table_number)" +
            ")",
            
            // Order items table
            "CREATE TABLE IF NOT EXISTS order_items (" +
            "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "order_id INTEGER NOT NULL," +
            "menu_item_id INTEGER NOT NULL," +
            "quantity INTEGER NOT NULL," +
            "unit_price DECIMAL(10,3) NOT NULL," +
            "total_price DECIMAL(10,3) NOT NULL," +
            "customizations TEXT," +
            "size VARCHAR(20)," +
            "is_hot BOOLEAN," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE," +
            "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)" +
            ")",
            
            // Payments table
            "CREATE TABLE IF NOT EXISTS payments (" +
            "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "order_id INTEGER NOT NULL," +
            "payment_method VARCHAR(20) NOT NULL," +
            "status VARCHAR(20) DEFAULT 'PENDING'," +
            "amount DECIMAL(10,3) NOT NULL," +
            "amount_paid DECIMAL(10,3) DEFAULT 0.00," +
            "change_given DECIMAL(10,3) DEFAULT 0.00," +
            "transaction_reference VARCHAR(100)," +
            "card_last_four_digits VARCHAR(4)," +
            "failure_reason TEXT," +
            "payment_time TIMESTAMP," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (order_id) REFERENCES orders(order_id)" +
            ")",
            
            // Ingredients table
            "CREATE TABLE IF NOT EXISTS ingredients (" +
            "ingredient_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name VARCHAR(100) NOT NULL," +
            "description TEXT," +
            "unit VARCHAR(20) NOT NULL," +
            "current_stock DECIMAL(10,3) DEFAULT 0.000," +
            "minimum_stock DECIMAL(10,3) NOT NULL," +
            "maximum_stock DECIMAL(10,3) NOT NULL," +
            "cost_per_unit DECIMAL(10,3) NOT NULL," +
            "expiration_date DATE," +
            "supplier VARCHAR(100)," +
            "is_active BOOLEAN DEFAULT TRUE," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        };
        
        Statement stmt = connection.createStatement();
        
        for (String query : createTableQueries) {
            stmt.execute(query);
        }
        
        // Create indexes
        String[] indexQueries = {
            "CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id)",
            "CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)",
            "CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id)",
            "CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id)",
            "CREATE INDEX IF NOT EXISTS idx_tables_status ON tables(status)",
            "CREATE INDEX IF NOT EXISTS idx_menu_items_category ON menu_items(category)"
        };
        
        for (String query : indexQueries) {
            stmt.execute(query);
        }
        
        stmt.close();
    }
    
    // Insert sample data
    private void insertSampleData() throws SQLException {
        // Sample menu items (Vietnamese categories and items)
        String insertCoffee = "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type) VALUES " +
            "('Cà phê đen nóng', 'Đậm đà, truyền thống', 20000, 'Cà phê', 'Drink', NULL), " +
            "('Cà phê đen đá', 'Đậm đà, dùng với đá', 20000, 'Cà phê', 'Drink', NULL), " +
            "('Cà phê sữa nóng', 'Sữa đặc và cà phê rang xay', 25000, 'Cà phê', 'Drink', NULL), " +
            "('Cà phê sữa đá', 'Bạc xỉu kiểu Việt', 25000, 'Cà phê', 'Drink', NULL), " +
            "('Bạc xỉu', 'Sữa nhiều, cà phê ít', 28000, 'Cà phê', 'Drink', NULL), " +
            "('Espresso', 'Rich and bold espresso shot', 25000, 'Cà phê', 'Coffee', 'ESPRESSO'), " +
            "('Cappuccino', 'Espresso với sữa nóng và foam', 38000, 'Cà phê', 'Coffee', 'CAPPUCCINO'), " +
            "('Latte', 'Espresso với sữa nóng', 40000, 'Cà phê', 'Coffee', 'LATTE'), " +
            "('Mocha', 'Espresso, chocolate, sữa', 42000, 'Cà phê', 'Coffee', 'MOCHA'), " +
            "('Americano', 'Espresso pha nước nóng', 30000, 'Cà phê', 'Coffee', 'AMERICANO'), " +
            "('Cold Brew', 'Ủ lạnh 12-24h', 35000, 'Cà phê', 'Drink', NULL)";

        String insertTea = "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type) VALUES " +
            "('Trà đào cam sả', 'Trà đào, cam, sả tươi', 28000, 'Trà', 'Drink', NULL), " +
            "('Trà chanh', 'Trà đen với chanh tươi', 20000, 'Trà', 'Drink', NULL), " +
            "('Trà đào', 'Trà đen vị đào', 25000, 'Trà', 'Drink', NULL), " +
            "('Trà vải', 'Trà đen vị vải', 25000, 'Trà', 'Drink', NULL), " +
            "('Trà sữa trân châu', 'Trà sữa kèm trân châu', 32000, 'Trà', 'Drink', NULL), " +
            "('Matcha latte', 'Bột trà xanh và sữa', 35000, 'Trà', 'Drink', NULL), " +
            "('Trà gạo rang (Hojicha)', 'Hương gạo rang đặc trưng', 32000, 'Trà', 'Drink', NULL)";

        String insertSmoothieJuice = "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type) VALUES " +
            "('Sinh tố xoài', 'Xoài chín xay mịn', 30000, 'Sinh tố & Nước ép', 'Drink', NULL), " +
            "('Sinh tố bơ', 'Bơ sáp béo mịn', 35000, 'Sinh tố & Nước ép', 'Drink', NULL), " +
            "('Sinh tố dâu', 'Dâu tươi xay', 32000, 'Sinh tố & Nước ép', 'Drink', NULL), " +
            "('Nước ép cam', 'Cam vắt nguyên chất', 28000, 'Sinh tố & Nước ép', 'Drink', NULL), " +
            "('Nước ép dưa hấu', 'Lạnh mát, ít đường', 25000, 'Sinh tố & Nước ép', 'Drink', NULL), " +
            "('Nước ép táo', 'Táo ép tươi', 28000, 'Sinh tố & Nước ép', 'Drink', NULL), " +
            "('Nước ép cà rốt', 'Cà rốt ép tươi', 25000, 'Sinh tố & Nước ép', 'Drink', NULL)";

        String insertOthers = "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type) VALUES " +
            "('Soda chanh', 'Sảng khoái, vị chanh', 22000, 'Đồ uống khác', 'Drink', NULL), " +
            "('Soda việt quất', 'Vị việt quất nhẹ', 25000, 'Đồ uống khác', 'Drink', NULL), " +
            "('Chocolate nóng', 'Sô cô la nóng', 32000, 'Đồ uống khác', 'Drink', NULL), " +
            "('Chocolate đá', 'Sô cô la mát lạnh', 32000, 'Đồ uống khác', 'Drink', NULL), " +
            "('Yaourt đá', 'Sữa chua dầm đá', 25000, 'Đồ uống khác', 'Drink', NULL), " +
            "('Nước suối', 'Đóng chai', 10000, 'Đồ uống khác', 'Drink', NULL)";

        // Sample customers
        String insertCustomers = "INSERT INTO customers (name, email, phone_number, loyalty_points) VALUES " +
            "('John Doe', 'john.doe@email.com', '555-0101', 25.50), " +
            "('Jane Smith', 'jane.smith@email.com', '555-0102', 15.75), " +
            "('Bob Johnson', 'bob.johnson@email.com', '555-0103', 42.25)";
        
        // Sample tables
        String insertTables = "INSERT INTO tables (table_number, capacity) VALUES " +
            "(1, 2), (2, 4), (3, 2), (4, 6), (5, 4)";
        
        Statement stmt = connection.createStatement();
        stmt.execute(insertCoffee);
        stmt.execute(insertTea);
        stmt.execute(insertSmoothieJuice);
        stmt.execute(insertOthers);
        stmt.execute(insertCustomers);
        stmt.execute(insertTables);
        stmt.close();
    }

    // Migrate existing data to VND scale if appears to be USD-like values
    private void migrateToVNDIfNeeded() {
        try (Statement stmt = connection.createStatement()) {
            // Heuristic: if average base_price < 100 (likely USD scale), multiply by 10000 to get VND-like values
            ResultSet rs = stmt.executeQuery("SELECT AVG(base_price) AS avgp FROM menu_items");
            double avg = rs.next() ? rs.getDouble("avgp") : 0.0;
            rs.close();
            if (avg > 0 && avg < 100) {
                connection.setAutoCommit(false);
                // Update menu_items prices
                stmt.executeUpdate("UPDATE menu_items SET base_price = ROUND(base_price * 10000, 3)");
                // Update existing orders and order_items monetary columns accordingly
                stmt.executeUpdate("UPDATE order_items SET unit_price = ROUND(unit_price * 10000, 3), total_price = ROUND(total_price * 10000, 3)");
                stmt.executeUpdate("UPDATE orders SET subtotal = ROUND(subtotal * 10000, 3), tax = ROUND(tax * 10000, 3), discount = ROUND(discount * 10000, 3), total_amount = ROUND(total_amount * 10000, 3)");
                stmt.executeUpdate("UPDATE payments SET amount = ROUND(amount * 10000, 3), amount_paid = ROUND(amount_paid * 10000, 3), change_given = ROUND(change_given * 10000, 3)");
                connection.commit();
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {}
            System.err.println("Migration to VND failed: " + e.getMessage());
        }
    }
    
    // Test database connection
    public boolean testConnection() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            boolean hasResult = rs.next();
            rs.close();
            stmt.close();
            return hasResult;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    // Close database connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }
    
    // Execute query and return ResultSet
    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }
    
    // Execute update query
    public int executeUpdate(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        int result = stmt.executeUpdate(query);
        stmt.close();
        return result;
    }
    
    // Prepare statement
    public PreparedStatement prepareStatement(String query) throws SQLException {
        return connection.prepareStatement(query);
    }
    
    // Begin transaction
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }
    
    // Commit transaction
    public void commitTransaction() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }
    
    // Rollback transaction
    public void rollbackTransaction() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
    }
}