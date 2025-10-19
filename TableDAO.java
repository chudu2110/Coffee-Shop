import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {
    private DatabaseConnection dbConnection;

    public TableDAO() {
        this.dbConnection = new DatabaseConnection();
    }

    // Create a new table
    public int createTable(Table table) {
        String query = "INSERT INTO tables (table_number, capacity, status, location) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, table.getTableNumber());
            pstmt.setInt(2, table.getCapacity());
            pstmt.setString(3, table.getStatus().toString());
            pstmt.setString(4, table.getNotes());
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
        return -1;
    }

    // Get table by ID
    public Table getTableById(int tableId) {
        String query = "SELECT * FROM tables WHERE table_id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return createTableFromResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Error getting table by ID: " + e.getMessage());
        }
        return null;
    }

    // Get all tables
    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT * FROM tables ORDER BY table_number";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Table table = createTableFromResultSet(rs);
                if (table != null) tables.add(table);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all tables: " + e.getMessage());
        }
        return tables;
    }

    public List<Table> getAvailableTables() {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT * FROM tables WHERE status = 'AVAILABLE' ORDER BY table_number ASC";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Table table = createTableFromResultSet(rs);
                tables.add(table);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving available tables: " + e.getMessage());
        }

        return tables;
    }

    // --- Lấy danh sách bàn theo trạng thái ---
    public List<Table> getTablesByStatus(Table.TableStatus status) {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT * FROM tables WHERE status = ? ORDER BY table_number ASC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Table table = createTableFromResultSet(rs);
                tables.add(table);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving tables by status: " + e.getMessage());
        }

        return tables;
    }

    // Update table status
    public boolean updateTableStatus(int tableId, Table.TableStatus status) {
        String query = "UPDATE tables SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE table_id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, status.toString());
            pstmt.setInt(2, tableId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating table status: " + e.getMessage());
            return false;
        }
    }

    // Delete table
    public boolean deleteTable(int tableId) {
        String query = "DELETE FROM tables WHERE table_id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, tableId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting table: " + e.getMessage());
            return false;
        }
    }

    public static class TableStats {
        private int totalTables;
        private int availableTables;
        private int occupiedTables;
        private int reservedTables;
        private int outOfServiceTables;

        public TableStats(int totalTables, int availableTables, int occupiedTables, int outOfServiceTables) {
        this.totalTables = totalTables;
        this.availableTables = availableTables;
        this.occupiedTables = occupiedTables;
        this.outOfServiceTables = outOfServiceTables;
    }

        public int getTotalTables() { return totalTables; }
        public int getAvailableTables() { return availableTables; }
        public int getOccupiedTables() { return occupiedTables; }
        public int getReservedTables() { return reservedTables; }
        public int getOutOfServiceTables() { return outOfServiceTables; }

    }

    public TableStats getTableStats() {
    String sql = """
        SELECT 
            COUNT(*) AS totalTables,
            SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) AS availableTables,
            SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupiedTables,
            SUM(CASE WHEN status = 'OUT_OF_SERVICE' THEN 1 ELSE 0 END) AS outOfServiceTables
        FROM tables
    """;

    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        if (rs.next()) {
            return new TableStats(
                rs.getInt("totalTables"),
                rs.getInt("availableTables"),
                rs.getInt("occupiedTables"),
                rs.getInt("outOfServiceTables")
            );
        }

    } catch (SQLException e) {
        System.err.println("Error retrieving table statistics: " + e.getMessage());
    }

    return new TableStats(0, 0, 0, 0);
}


    private int getCount(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
    // Helper: convert ResultSet -> Table object
    private Table createTableFromResultSet(ResultSet rs) throws SQLException {
        int tableId = rs.getInt("table_id");
        int tableNumber = rs.getInt("table_number");
        int capacity = rs.getInt("capacity");
        String statusStr = rs.getString("status");
        String location = rs.getString("location");

        Table.TableStatus status = Table.TableStatus.valueOf(statusStr);
        Table table = new Table(tableNumber, capacity);
        table.setNotes(location);
        return table;
    }
}
