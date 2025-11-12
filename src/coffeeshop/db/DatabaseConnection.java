package coffeeshop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseConnection
 * ------------------
 * Quản lý kết nối tới cơ sở dữ liệu MySQL cho ứng dụng Coffee Shop.
 * Tự động tạo bảng và chèn dữ liệu mẫu nếu chưa tồn tại.
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/coffee_shop";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    private static Connection connection;
    private static DatabaseConnection instance;

    // ==========================
    // Kết nối tới database
    // ==========================
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver"); // Thêm dòng này
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to MySQL successfully!");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Wrong connect: " + e.getMessage());
        }
        return connection;
    }


    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // ==========================
    // Kiểm tra kết nối
    // ==========================
    public static boolean testConnection() {
        try {
            getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            boolean ok = rs.next();
            rs.close();
            stmt.close();
            System.out.println("Connect fine!");
            return ok;
        } catch (SQLException e) {
            System.err.println("Wrong connection: " + e.getMessage());
            return false;
        }
    }

    // ==========================
    // Tạo bảng
    // ==========================

    

    public static void createTables() {
    try {
        Connection conn = getConnection(); // không đóng connection ở đây
        if (conn == null || conn.isClosed()) {
            conn = getConnection();
        }
        Statement stmt = conn.createStatement();

        // menu_items (sử dụng base_price)
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS menu_items (" +
            "  id INT AUTO_INCREMENT PRIMARY KEY," +
            "  name VARCHAR(255) NOT NULL," +
            "  description TEXT," +
            "  base_price DECIMAL(10,3) NOT NULL," +
            "  category VARCHAR(100) NOT NULL," +
            "  item_type VARCHAR(50) NOT NULL," +
            "  coffee_type VARCHAR(50)," +
            "  is_available TINYINT(1) DEFAULT 1," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB;"
        );

        // Deduplicate existing rows by name (keep the smallest id)
        stmt.executeUpdate(
            "DELETE mi1 FROM menu_items mi1 " +
            "JOIN menu_items mi2 ON mi1.name = mi2.name AND mi1.id > mi2.id"
        );

        // Enforce unique names to avoid duplicates in future inserts
        try {
            stmt.executeUpdate(
                "ALTER TABLE menu_items ADD CONSTRAINT uk_menu_items_name UNIQUE (name)"
            );
        } catch (SQLException ignore) {
            // Unique key may already exist; safely ignore
        }

        // customers
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS customers (" +
            "  customer_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  name VARCHAR(100) NOT NULL," +
            "  email VARCHAR(150) UNIQUE," +
            "  phone_number VARCHAR(20)," +
            "  loyalty_points DECIMAL(10,2) DEFAULT 0.00," +
            "  registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB;"
        );

        // tables (bàn)
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS tables (" +
            "  table_number INT PRIMARY KEY," +
            "  capacity INT NOT NULL," +
            "  status VARCHAR(20) DEFAULT 'AVAILABLE'," +
            "  current_customer_id INT NULL," +
            "  occupied_since TIMESTAMP NULL," +
            "  reserved_until TIMESTAMP NULL," +
            "  notes TEXT," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "  CONSTRAINT fk_tables_customer FOREIGN KEY (current_customer_id) REFERENCES customers(customer_id)" +
            ") ENGINE=InnoDB;"
        );

        // orders
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS orders (" +
            "  order_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  customer_id INT NOT NULL," +
            "  status VARCHAR(20) DEFAULT 'PENDING'," +
            "  service_type VARCHAR(20) NOT NULL," +
            "  table_number INT NULL," +
            "  subtotal DECIMAL(10,3) NOT NULL," +
            "  tax DECIMAL(10,3) NOT NULL," +
            "  discount DECIMAL(10,3) DEFAULT 0.00," +
            "  total_amount DECIMAL(10,3) NOT NULL," +
            "  special_instructions TEXT," +
            "  order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  completion_time TIMESTAMP NULL," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "  CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)," +
            "  CONSTRAINT fk_orders_table FOREIGN KEY (table_number) REFERENCES tables(table_number)" +
            ") ENGINE=InnoDB;"
        );

        // order_items
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS order_items (" +
            "  order_item_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  order_id INT NOT NULL," +
            "  menu_item_id INT NOT NULL," +
            "  quantity INT NOT NULL," +
            "  unit_price DECIMAL(10,3) NOT NULL," +
            "  total_price DECIMAL(10,3) NOT NULL," +
            "  customizations TEXT," +
            "  size VARCHAR(20)," +
            "  is_hot TINYINT(1)," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  CONSTRAINT fk_orderitems_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE," +
            "  CONSTRAINT fk_orderitems_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)" +
            ") ENGINE=InnoDB;"
        );

        // payments
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS payments (" +
            "  payment_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  order_id INT NOT NULL," +
            "  payment_method VARCHAR(20) NOT NULL," +
            "  status VARCHAR(20) DEFAULT 'PENDING'," +
            "  amount DECIMAL(10,3) NOT NULL," +
            "  amount_paid DECIMAL(10,3) DEFAULT 0.00," +
            "  change_given DECIMAL(10,3) DEFAULT 0.00," +
            "  transaction_reference VARCHAR(100)," +
            "  card_last_four_digits VARCHAR(4)," +
            "  failure_reason TEXT," +
            "  payment_time TIMESTAMP NULL," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(order_id)" +
            ") ENGINE=InnoDB;"
        );

        // ingredients + menu_item_ingredients
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS ingredients (" +
            "  ingredient_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  name VARCHAR(100) NOT NULL," +
            "  description TEXT," +
            "  unit VARCHAR(20) NOT NULL," +
            "  current_stock DECIMAL(10,3) DEFAULT 0.000," +
            "  minimum_stock DECIMAL(10,3) NOT NULL," +
            "  maximum_stock DECIMAL(10,3) NOT NULL," +
            "  cost_per_unit DECIMAL(10,3) NOT NULL," +
            "  expiration_date DATE," +
            "  supplier VARCHAR(100)," +
            "  is_active TINYINT(1) DEFAULT 1," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB;"
        );

        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS menu_item_ingredients (" +
            "  menu_item_id INT NOT NULL," +
            "  ingredient_id INT NOT NULL," +
            "  quantity_required DECIMAL(10,3) NOT NULL," +
            "  PRIMARY KEY (menu_item_id, ingredient_id)," +
            "  CONSTRAINT fk_mii_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE," +
            "  CONSTRAINT fk_mii_ing FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE CASCADE" +
            ") ENGINE=InnoDB;"
        );

        stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
        stmt.close();
        System.out.println("Tables created or verified successfully.");
    } catch (SQLException e) {
        System.err.println("Error creating tables: " + e.getMessage());
    }
}

    public static void insertSampleData() {
    try {
        if (connection == null || connection.isClosed()) {
            connection = getConnection();
        }
        Statement stmt = connection.createStatement();

        // Sample menu items - Coffee (upsert by unique name)
        stmt.executeUpdate(
            "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type, is_available) VALUES " +
            "('Espresso','Espresso đậm vị',25000.000,'Coffee','Coffee','ESPRESSO',1)," +
            "('Americano','Espresso pha nước nóng',30000.000,'Coffee','Coffee','AMERICANO',1)," +
            "('Latte','Espresso với sữa',45000.000,'Coffee','Coffee','LATTE',1)," +
            "('Cappuccino','Espresso với sữa và foam',40000.000,'Coffee','Coffee','CAPPUCCINO',1)," +
            "('Macchiato','Espresso với một lớp sữa',42500.000,'Coffee','Coffee','MACCHIATO',1)," +
            "('Mocha','Espresso với sô-cô-la và sữa',50000.000,'Coffee','Coffee','MOCHA',1)," +
            "('Frappuccino','Cà phê xay đá',55000.000,'Coffee','Coffee','FRAPPUCCINO',1) " +
            "ON DUPLICATE KEY UPDATE description=VALUES(description), base_price=VALUES(base_price), category=VALUES(category), item_type=VALUES(item_type), coffee_type=VALUES(coffee_type), is_available=VALUES(is_available)"
        );

        // Sample menu items - Food (upsert by unique name)
        stmt.executeUpdate(
            "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type, is_available) VALUES " +
            "('Croissant','Bánh sừng bò bơ',35000.000,'Pastry','Food',NULL,1)," +
            "('Muffin','Bánh muffin mới nướng',27500.000,'Pastry','Food',NULL,1)," +
            "('Sandwich','Bánh mì kẹp nướng',65000.000,'Food','Food',NULL,1)," +
            "('Bánh mì pate','Bánh mì pate truyền thống',25000.000,'Food','Food',NULL,1)," +
            "('Bánh mì thịt nướng','Bánh mì thịt nướng BBQ',45000.000,'Food','Food',NULL,1)," +
            "('Pizza mini','Pizza mini 4 mùa',80000.000,'Food','Food',NULL,1) " +
            "ON DUPLICATE KEY UPDATE description=VALUES(description), base_price=VALUES(base_price), category=VALUES(category), item_type=VALUES(item_type), coffee_type=VALUES(coffee_type), is_available=VALUES(is_available)"
        );

        // Sample menu items - Vietnamese Coffee
        stmt.executeUpdate(
            "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type, is_available) VALUES " +
            "('Cà phê sữa đá','Cà phê pha với sữa đặc',25000.000,'Coffee','Coffee','VIETNAMESE',1)," +
            "('Cà phê đen đá','Cà phê rang xay nguyên chất',20000.000,'Coffee','Coffee','VIETNAMESE',1)," +
            "('Cà phê sữa nóng','Cà phê sữa ấm nóng',25000.000,'Coffee','Coffee','VIETNAMESE',1) " +
            "ON DUPLICATE KEY UPDATE description=VALUES(description), base_price=VALUES(base_price), category=VALUES(category), item_type=VALUES(item_type), coffee_type=VALUES(coffee_type), is_available=VALUES(is_available)"
        );

        // Sample menu items - Tea Drinks
        stmt.executeUpdate(
            "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type, is_available) VALUES " +
            "('Trà đào cam sả','Trà đào với cam sả tươi',35000.000,'Tea','Drink',NULL,1)," +
            "('Trà sữa trân châu','Trà sữa với trân châu đen',40000.000,'Tea','Drink',NULL,1)," +
            "('Trà xanh matcha','Trà xanh matcha Nhật Bản',45000.000,'Tea','Drink',NULL,1)," +
            "('Trà hoa cúc','Trà hoa cúc thảo mộc',30000.000,'Tea','Drink',NULL,1) " +
            "ON DUPLICATE KEY UPDATE description=VALUES(description), base_price=VALUES(base_price), category=VALUES(category), item_type=VALUES(item_type), coffee_type=VALUES(coffee_type), is_available=VALUES(is_available)"
        );

        // Sample menu items - Desserts
        stmt.executeUpdate(
            "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type, is_available) VALUES " +
            "('Tiramisu','Bánh tiramisu Ý',55000.000,'Dessert','Food',NULL,1)," +
            "('Cheesecake','Bánh cheesecake New York',60000.000,'Dessert','Food',NULL,1)," +
            "('Brownie','Bánh brownie sô-cô-la',35000.000,'Dessert','Food',NULL,1)," +
            "('Ice cream','Kem tươi 3 vị',25000.000,'Dessert','Food',NULL,1) " +
            "ON DUPLICATE KEY UPDATE description=VALUES(description), base_price=VALUES(base_price), category=VALUES(category), item_type=VALUES(item_type), coffee_type=VALUES(coffee_type), is_available=VALUES(is_available)"
        );

        // Sample menu items - Smoothies & Juices
        stmt.executeUpdate(
            "INSERT INTO menu_items (name, description, base_price, category, item_type, coffee_type, is_available) VALUES " +
            "('Smoothie dâu','Sinh tố dâu tây tươi',40000.000,'Smoothie','Drink',NULL,1)," +
            "('Smoothie xoài','Sinh tố xoài nhiệt đới',35000.000,'Smoothie','Drink',NULL,1)," +
            "('Nước cam tươi','Nước cam vắt tươi',30000.000,'Juice','Drink',NULL,1)," +
            "('Nước chanh dây','Nước chanh dây mát lạnh',25000.000,'Juice','Drink',NULL,1) " +
            "ON DUPLICATE KEY UPDATE description=VALUES(description), base_price=VALUES(base_price), category=VALUES(category), item_type=VALUES(item_type), coffee_type=VALUES(coffee_type), is_available=VALUES(is_available)"
        );

        // Sample customers
        stmt.executeUpdate(
            "INSERT IGNORE INTO customers (name, email, phone_number, loyalty_points) VALUES " +
            "('Le Quoc Bao', 'lequocbao1352005@gmail.com', '0912345678', 25.5), " +
            "('Jane Smith','jane.smith@email.com','0905123456',15.75)," +
            "('Bob Johnson','bob.johnson@email.com','0905234567',42.25)," +
            "('Alice Brown','alice.brown@email.com','0905345678',8.00)," +
            "('Charlie Wilson','charlie.wilson@email.com','0905456789',33.50)"
        );

        // Sample tables
        stmt.executeUpdate(
            "INSERT IGNORE INTO tables (table_number, capacity, status) VALUES " +
            "(1, 2, 'AVAILABLE')," +
            "(2, 4, 'AVAILABLE')," +
            "(3, 2, 'AVAILABLE')," +
            "(4, 6, 'AVAILABLE')," +
            "(5, 4, 'AVAILABLE')," +
            "(6, 2, 'AVAILABLE')," +
            "(7, 4, 'AVAILABLE')," +
            "(8, 8, 'AVAILABLE')"
        );

        // Sample ingredients
        stmt.executeUpdate(
            "INSERT IGNORE INTO ingredients (name, description, unit, current_stock, minimum_stock, maximum_stock, cost_per_unit, supplier, is_active) VALUES " +
            "('Coffee Beans - Arabica','Premium Arabica coffee beans','KILOGRAMS',50.0,10.0,100.0,12.50,'Coffee Suppliers Inc',1)," +
            "('Coffee Beans - Robusta','Strong Robusta coffee beans','KILOGRAMS',30.0,5.0,80.0,10.00,'Coffee Suppliers Inc',1)," +
            "('Milk','Fresh whole milk','LITERS',25.0,5.0,50.0,1.50,'Local Dairy',1)," +
            "('Sugar','White granulated sugar','KILOGRAMS',15.0,3.0,30.0,2.00,'Sweet Supplies',1)," +
            "('Chocolate Syrup','Premium chocolate syrup','LITERS',8.0,2.0,20.0,5.50,'Chocolate Co',1)," +
            "('Vanilla Extract','Pure vanilla extract','LITERS',3.0,1.0,10.0,15.00,'Flavor House',1)"
        );

        stmt.close();
        System.out.println("✅ Sample data inserted successfully!");
    } catch (SQLException e) {
        System.err.println("❌ Error inserting sample data: " + e.getMessage());
        e.printStackTrace();
    }
}


    // ==========================
    // Chèn dữ liệu mẫu
    // ==========================
    // public static void insertSampleData() {
    //     try (Statement stmt = connection.createStatement()) {
    //         stmt.executeUpdate(
    //             "INSERT INTO menu_items (name, category, price) VALUES " +
    //             "('Cà phê sữa đá', 'Cà phê', 25000.00), " +
    //             "('Cà phê đen đá', 'Cà phê', 20000.00) " +
    //             "ON DUPLICATE KEY UPDATE name = VALUES(name);"
    //         );

    //         System.out.println("Inserted sample data into menu_items table!");
    //     } catch (SQLException e) {
    //         System.err.println("Wrong when inserting sample data: " + e.getMessage());
    //     }
    // }

    // ==========================
    // Đóng kết nối
    // ==========================
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("MySQL is closed");
            }
        } catch (SQLException e) {
            System.out.println("Wrong when connecting: " + e.getMessage());
        }
    }

    // ==========================
    // Khởi tạo database hoàn chỉnh
    // ==========================
    public static void initializeDatabase() {
        try {
            System.out.println("🔄 Initializing database...");
            getConnection();
            testConnection();
            createTables();
            insertSampleData();
            System.out.println("✅ Database initialization completed!");
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================
    // Chương trình test nhanh
    // ==========================
    public static void main(String[] args) {
        try {
            initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }
}
