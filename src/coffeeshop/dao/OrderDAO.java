package coffeeshop.dao;

import coffeeshop.db.DatabaseConnection;
import coffeeshop.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private DatabaseConnection dbConnection;
    private MenuItemDAO menuItemDAO;
    
    public OrderDAO() {
    this.dbConnection = DatabaseConnection.getInstance();
    this.menuItemDAO = new MenuItemDAO();
}


    // --- Tạo đơn hàng ---
    public int createOrder(Order order) {
        String orderQuery = "INSERT INTO orders (customer_id, status, service_type, subtotal, tax, discount, total_amount, special_instructions) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            pstmt.setInt(1, order.getCustomerId());
            pstmt.setString(2, order.getStatus().toString());
            pstmt.setString(3, order.getServiceType().toString());
            pstmt.setDouble(4, order.getSubtotal());
            pstmt.setDouble(5, order.getTax());
            pstmt.setDouble(6, order.getDiscount());
            pstmt.setDouble(7, order.getTotalAmount());
            pstmt.setString(8, order.getSpecialInstructions());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        if (insertOrderItems(orderId, order.getOrderItems(), conn)) {
                            conn.commit();
                            return orderId;
                        }
                    }
                }
            }

            conn.rollback();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // --- Thêm order items ---
    private boolean insertOrderItems(int orderId, List<OrderItem> orderItems, Connection conn) throws SQLException {
        String itemQuery = "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, total_price, customizations) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(itemQuery)) {
            for (OrderItem item : orderItems) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getMenuItem().getId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getUnitPrice());
                pstmt.setDouble(5, item.getItemTotal());
                pstmt.setString(6, item.getCustomizations());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;
        }
    }

    // --- Lấy order theo ID ---
    public List<Order> getOrdersByCustomerId(int customerId) {
    List<Order> orders = new ArrayList<>();
    String query = "SELECT * FROM orders WHERE customer_id = ?";

    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Order order = new Order(); // dùng constructor rỗng

            order.setOrderId(rs.getInt("order_id"));
            order.setCustomerId(rs.getInt("customer_id"));

            // Convert chuỗi sang Enum
            String statusStr = rs.getString("status");
            if (statusStr != null) {
                order.setStatus(Order.OrderStatus.valueOf(statusStr));
            }

            String serviceTypeStr = rs.getString("service_type");
            if (serviceTypeStr != null) {
                order.setServiceType(Order.ServiceType.valueOf(serviceTypeStr));
            }

            order.setDiscount(rs.getDouble("discount"));
            order.setSpecialInstructions(rs.getString("special_instructions"));

            // Gán thời gian
            Timestamp ts = rs.getTimestamp("order_date");
            if (ts != null) {
                order.setOrderTime(ts.toLocalDateTime());
            }

            // Lấy danh sách món
            order.getOrderItems().addAll(getOrderItems(order.getOrderId()));

            orders.add(order);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return orders;
}

    public List<Order> getAllOrders() {
    List<Order> orders = new ArrayList<>();
    String query = "SELECT * FROM orders ORDER BY order_time DESC";

    try (Connection conn = dbConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            Order order = createOrderFromResultSet(rs);
            order.getOrderItems().addAll(getOrderItems(order.getOrderId()));
            orders.add(order);
        }

    } catch (SQLException e) {
        System.err.println("Error retrieving all orders: " + e.getMessage());
    }

    return orders;
}


    // --- Lấy danh sách item theo order ---
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        String query = "SELECT * FROM order_items WHERE order_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int menuItemId = rs.getInt("menu_item_id");
                int quantity = rs.getInt("quantity");
                double unitPrice = rs.getDouble("unit_price");
                String customizations = rs.getString("customizations");

                MenuItem menuItem = menuItemDAO.getMenuItemById(menuItemId);
                if (menuItem != null) {
                    OrderItem orderItem = new OrderItem(menuItem, quantity, customizations);
                    orderItems.add(orderItem);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderItems;
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
    List<Order> orders = new ArrayList<>();
    String query = "SELECT * FROM orders WHERE status = ?";

    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, status.name());
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Order order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setCustomerId(rs.getInt("customer_id"));

            String serviceTypeStr = rs.getString("service_type");
            if (serviceTypeStr != null) {
                order.setServiceType(Order.ServiceType.valueOf(serviceTypeStr));
            }

            order.setStatus(status);

            Timestamp ts = rs.getTimestamp("order_date");
            if (ts != null) {
                order.setOrderTime(ts.toLocalDateTime());
            }

            order.getOrderItems().addAll(getOrderItems(order.getOrderId()));
            orders.add(order);
        }

    } catch (SQLException e) {
        System.err.println("Error retrieving orders by status: " + e.getMessage());
    }

    return orders;
}

    public Order getOrderById(int orderId) {
    String query = "SELECT * FROM orders WHERE order_id = ?";
    Order order = null;

    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, orderId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int customerId = rs.getInt("customer_id");
            String serviceTypeStr = rs.getString("service_type");
            String statusStr = rs.getString("status");
            String specialInstructions = rs.getString("special_instructions");

            // Khởi tạo order
            Order.ServiceType serviceType = Order.ServiceType.valueOf(serviceTypeStr);
            Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr);

            order = new Order(orderId, customerId, serviceType);
            order.setStatus(status);
            order.setSpecialInstructions(specialInstructions);

            // Lấy các item trong order
            order.setOrderItems(getOrderItems(orderId));
        }

    } catch (SQLException e) {
        System.err.println("Error retrieving order by ID: " + e.getMessage());
    }

    return order;
}

    public boolean updateOrderStatus(int orderId, Order.OrderStatus newStatus) {
    String query = "UPDATE orders SET status = ? WHERE order_id = ?";

    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, newStatus.toString());
        stmt.setInt(2, orderId);

        int rowsUpdated = stmt.executeUpdate();
        if (rowsUpdated > 0) {
            System.out.println("Order ID " + orderId + " updated to status: " + newStatus);
            return true;
        }

    } catch (SQLException e) {
        System.err.println("Error updating order status: " + e.getMessage());
    }

    return false;
}

    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    List<Order> orders = new ArrayList<>();
    String query = "SELECT * FROM orders WHERE order_date BETWEEN ? AND ? ORDER BY order_date DESC";

    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setTimestamp(1, Timestamp.valueOf(startDate));
        stmt.setTimestamp(2, Timestamp.valueOf(endDate));

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Order order = createOrderFromResultSet(rs);
            orders.add(order);
        }

    } catch (SQLException e) {
        System.err.println("Error retrieving orders by date range: " + e.getMessage());
    }

    return orders;
}

    public static class OrderStats {
        private int totalOrders;
        private int pendingOrders;
        private int completedOrders;
        private int cancelledOrders;
        private double avgOrderValue;
        private double totalRevenue;

        public OrderStats(int totalOrders, int pendingOrders, int completedOrders, int cancelledOrders,
                          double avgOrderValue, double totalRevenue) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.avgOrderValue = avgOrderValue;
            this.totalRevenue = totalRevenue;
        }

        public int getTotalOrders() { return totalOrders; }
        public int getPendingOrders() { return pendingOrders; }
        public int getCompletedOrders() { return completedOrders; }
        public int getCancelledOrders() { return cancelledOrders; }
        public double getAvgOrderValue() { return avgOrderValue; }
        public double getTotalRevenue() { return totalRevenue; }
    }

    public OrderStats getOrderStats() {
        String sql = """
            SELECT
                COUNT(*) AS totalOrders,
                SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pendingOrders,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedOrders,
                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelledOrders,
                AVG(total_amount) AS avgOrderValue,
                SUM(total_amount) AS totalRevenue
            FROM orders
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new OrderStats(
                    rs.getInt("totalOrders"),
                    rs.getInt("pendingOrders"),
                    rs.getInt("completedOrders"),
                    rs.getInt("cancelledOrders"),
                    rs.getDouble("avgOrderValue"),
                    rs.getDouble("totalRevenue")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving order statistics: " + e.getMessage());
        }

        return new OrderStats(0, 0, 0, 0, 0.0, 0.0);
    }

    // --- Helper tạo Order từ ResultSet ---
    private Order createOrderFromResultSet(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("order_id");
        int customerId = rs.getInt("customer_id");
        String serviceTypeStr = rs.getString("service_type");
        String statusStr = rs.getString("status");
        String notes = rs.getString("special_instructions");

        Order.ServiceType serviceType = Order.ServiceType.valueOf(serviceTypeStr);
        Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr);

        Order order = new Order(orderId, customerId, serviceType);
        order.setStatus(status);
        order.setSpecialInstructions(notes);

        return order;
    }


}
