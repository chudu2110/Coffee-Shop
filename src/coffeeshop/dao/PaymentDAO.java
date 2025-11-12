package coffeeshop.dao;

import coffeeshop.db.DatabaseConnection;
import coffeeshop.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Payment operations
 * Handles all database operations related to payments
 */
public class PaymentDAO {
    private DatabaseConnection dbConnection;

    public PaymentDAO() {
        this.dbConnection = new DatabaseConnection();
    }

    // Create a new payment
    public int createPayment(Payment payment) {
        String query = "INSERT INTO payments (order_id, payment_method, amount, status, transaction_reference) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, payment.getOrderId());
            pstmt.setString(2, payment.getPaymentMethod().toString());
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setString(4, payment.getStatus().toString());
            pstmt.setString(5, payment.getTransactionReference());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating payment: " + e.getMessage());
        }
        return -1;
    }

    // ✅ Inner class thống kê thanh toán
    public static class PaymentStats {
        private int totalPayments;
        private int completedPayments;
        private int cancelledPayments;
        private double totalRevenue;
        private double avgPaymentAmount;

        public PaymentStats(int totalPayments, int completedPayments, int cancelledPayments,
                            double totalRevenue, double avgPaymentAmount) {
            this.totalPayments = totalPayments;
            this.completedPayments = completedPayments;
            this.cancelledPayments = cancelledPayments;
            this.totalRevenue = totalRevenue;
            this.avgPaymentAmount = avgPaymentAmount;
        }

        public int getTotalPayments() { return totalPayments; }
        public int getCompletedPayments() { return completedPayments; }
        public int getCancelledPayments() { return cancelledPayments; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAvgPaymentAmount() { return avgPaymentAmount; }

        @Override
        public String toString() {
            return String.format("Total Payments: %d | Revenue: %.2f | Avg: %.2f",
                    totalPayments, totalRevenue, avgPaymentAmount);
        }
    }

    // --- Lấy thống kê thanh toán từ DB ---
    public PaymentStats getPaymentStats() {
        String sql = """
            SELECT 
                COUNT(*) AS totalPayments,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedPayments,
                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelledPayments,
                SUM(amount) AS totalRevenue,
                AVG(amount) AS avgPaymentAmount
            FROM payments
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new PaymentStats(
                    rs.getInt("totalPayments"),
                    rs.getInt("completedPayments"),
                    rs.getInt("cancelledPayments"),
                    rs.getDouble("totalRevenue"),
                    rs.getDouble("avgPaymentAmount")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving payment statistics: " + e.getMessage());
        }

        return new PaymentStats(0, 0, 0, 0.0, 0.0);
    }

    // --- Các phương thức CRUD khác ---
    public Payment getPaymentById(int paymentId) {
        String query = "SELECT * FROM payments WHERE payment_id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, paymentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createPaymentFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Payment> getPaymentsByOrderId(int orderId) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payments WHERE order_id = ? ORDER BY created_at DESC";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                payments.add(createPaymentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting payments by order ID: " + e.getMessage());
        }
        return payments;
    }

    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payments WHERE status = ? ORDER BY created_at DESC";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, status.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                payments.add(createPaymentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting payments by status: " + e.getMessage());
        }
        return payments;
    }

    // Get all payments
    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payments ORDER BY created_at DESC";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                payments.add(createPaymentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all payments: " + e.getMessage());
        }
        return payments;
    }

    public boolean updatePaymentStatus(int paymentId, Payment.PaymentStatus status) {
        String query = "UPDATE payments SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE payment_id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, status.toString());
            pstmt.setInt(2, paymentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;
        }
    }

    public boolean refundPayment(int paymentId) {
        String query = "UPDATE payments SET status = 'REFUNDED', updated_at = CURRENT_TIMESTAMP WHERE payment_id = ? AND status = 'COMPLETED'";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, paymentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error refunding payment: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePayment(int paymentId) {
        String query = "DELETE FROM payments WHERE payment_id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, paymentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting payment: " + e.getMessage());
            return false;
        }
    }

    // Helper: convert ResultSet → Payment object
    private Payment createPaymentFromResultSet(ResultSet rs) throws SQLException {
        int paymentId = rs.getInt("payment_id");
        int orderId = rs.getInt("order_id");
        String methodStr = rs.getString("payment_method");
        double amount = rs.getDouble("amount");
        String statusStr = rs.getString("status");
        String ref = rs.getString("transaction_reference");

        Payment.PaymentMethod method = Payment.PaymentMethod.valueOf(methodStr);
        Payment.PaymentStatus status = Payment.PaymentStatus.valueOf(statusStr);

        Payment payment = new Payment(paymentId, orderId, method, amount);
        payment.setTransactionReference(ref);
        payment.setStatus(status);
        return payment;
    }
}
