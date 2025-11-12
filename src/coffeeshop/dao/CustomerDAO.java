package coffeeshop.dao;

import coffeeshop.db.DatabaseConnection;
import coffeeshop.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer operations
 * Handles all database operations related to customers
 */
public class CustomerDAO {
    private final DatabaseConnection dbConnection;

    public CustomerDAO() {
        this.dbConnection = new DatabaseConnection(); // dùng trực tiếp, không còn getInstance()
    }

    // Create a new customer
    public int createCustomer(Customer customer) {
        String query = "INSERT INTO customers (name, email, phone_number, loyalty_points) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhoneNumber());
            pstmt.setDouble(4, customer.getLoyaltyPoints());
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating customer: " + e.getMessage());
        }
        return -1;
    }

    // Get customer by ID
    public Customer getCustomerById(int customerId) {
        String query = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createCustomerFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer by ID: " + e.getMessage());
        }
        return null;
    }

    // Get customer by email
    public Customer getCustomerByEmail(String email) {
        String query = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createCustomerFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer by email: " + e.getMessage());
        }
        return null;
    }

    // Get all customers
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers ORDER BY name";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                customers.add(createCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all customers: " + e.getMessage());
        }
        return customers;
    }

    // Update customer info
    public boolean updateCustomer(Customer customer) {
        String query = "UPDATE customers SET name = ?, email = ?, phone_number = ?, loyalty_points = ?, updated_at = CURRENT_TIMESTAMP WHERE customer_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhoneNumber());
            pstmt.setDouble(4, customer.getLoyaltyPoints());
            pstmt.setInt(5, customer.getCustomerId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
        }
        return false;
    }

    public List<Customer> searchCustomersByName(String name) {
    List<Customer> customers = new ArrayList<>();
    String query = "SELECT * FROM customers WHERE name LIKE ?";

    try (Connection conn = dbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, "%" + name + "%");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Customer c = new Customer(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone")
            );
            customers.add(c);
        }

    } catch (SQLException e) {
        System.err.println("Error searching customers: " + e.getMessage());
    }

    return customers;
}

    public static class CustomerStats {
        private int totalCustomers;
        private int activeCustomers;
        private int inactiveCustomers;
        private String topCustomerName;
        private double totalSpending;

        public CustomerStats(int totalCustomers, int activeCustomers, int inactiveCustomers, String topCustomerName, double totalSpending) {
            this.totalCustomers = totalCustomers;
            this.activeCustomers = activeCustomers;
            this.inactiveCustomers = inactiveCustomers;
            this.topCustomerName = topCustomerName;
            this.totalSpending = totalSpending;
        }

        public int getTotalCustomers() { return totalCustomers; }
        public int getActiveCustomers() { return activeCustomers; }
        public int getInactiveCustomers() { return inactiveCustomers; }
        public String getTopCustomerName() { return topCustomerName; }
        public double getTotalSpending() { return totalSpending; }
    }

    // --- Hàm lấy thống kê ---
    public CustomerStats getCustomerStats() {
        String totalQuery = "SELECT COUNT(*) AS total FROM customers";
        String activeQuery = "SELECT COUNT(*) AS active FROM customers WHERE status = 'ACTIVE'";
        String inactiveQuery = "SELECT COUNT(*) AS inactive FROM customers WHERE status = 'INACTIVE'";
        String topCustomerQuery = """
            SELECT c.name, SUM(o.total_amount) AS total_spent
            FROM customers c
            JOIN orders o ON c.customer_id = o.customer_id
            GROUP BY c.name
            ORDER BY total_spent DESC
            LIMIT 1
        """;
        String totalSpendingQuery = "SELECT SUM(total_amount) AS total_spent FROM orders";

        int total = 0, active = 0, inactive = 0;
        double totalSpending = 0.0;
        String topCustomer = "N/A";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            try (Statement stmt = conn.createStatement()) {
                ResultSet rs1 = stmt.executeQuery(totalQuery);
                if (rs1.next()) total = rs1.getInt("total");

                ResultSet rs2 = stmt.executeQuery(activeQuery);
                if (rs2.next()) active = rs2.getInt("active");

                ResultSet rs3 = stmt.executeQuery(inactiveQuery);
                if (rs3.next()) inactive = rs3.getInt("inactive");

                ResultSet rs4 = stmt.executeQuery(topCustomerQuery);
                if (rs4.next()) topCustomer = rs4.getString("name");

                ResultSet rs5 = stmt.executeQuery(totalSpendingQuery);
                if (rs5.next()) totalSpending = rs5.getDouble("total_spent");
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving customer statistics: " + e.getMessage());
        }

        return new CustomerStats(total, active, inactive, topCustomer, totalSpending);
    }


    // Delete customer
    public boolean deleteCustomer(int customerId) {
        String query = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
        }
        return false;
    }

    // Add loyalty points
    public boolean addLoyaltyPoints(int customerId, double pointsToAdd) {
        String query = "UPDATE customers SET loyalty_points = loyalty_points + ?, updated_at = CURRENT_TIMESTAMP WHERE customer_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDouble(1, pointsToAdd);
            pstmt.setInt(2, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding loyalty points: " + e.getMessage());
        }
        return false;
    }

    // Helper: Convert ResultSet → Customer object
    private Customer createCustomerFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("customer_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone_number");
        double points = rs.getDouble("loyalty_points");

        Customer c = new Customer(id, name, email, phone);
        c.addLoyaltyPoints(points);
        return c;
    }
}
