import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for MenuItem operations
 * Handles all database operations related to menu items
 */
public class MenuItemDAO {
    private final DatabaseConnection dbConnection;

    public MenuItemDAO() {
        this.dbConnection = new DatabaseConnection();
    }

    // Create a new menu item
    public boolean createMenuItem(MenuItem menuItem) {
        String query = "INSERT INTO menu_items (name, description, base_price, category, coffee_type, is_available) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, menuItem.getName());
            pstmt.setString(2, menuItem.getDescription());
            pstmt.setDouble(3, menuItem.getPrice());
            pstmt.setString(4, menuItem.getCategory());

            if (menuItem instanceof Coffee coffee) {
                pstmt.setString(5, coffee.getCoffeeType().toString());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }

            pstmt.setBoolean(6, menuItem.isAvailable());
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error creating menu item: " + e.getMessage());
            return false;
        }
    }

    // Get menu item by ID
    public MenuItem getMenuItemById(int id) {
        String query = "SELECT * FROM menu_items WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createMenuItemFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting menu item by ID: " + e.getMessage());
        }
        return null;
    }

    // Get all menu items
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM menu_items ORDER BY category, name";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                menuItems.add(createMenuItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all menu items: " + e.getMessage());
        }
        return menuItems;
    }

    // Get menu items by category
    public List<MenuItem> getMenuItemsByCategory(String category) {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM menu_items WHERE category = ? AND is_available = TRUE ORDER BY name";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                menuItems.add(createMenuItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting menu items by category: " + e.getMessage());
        }
        return menuItems;
    }

    // Get distinct categories
    public List<String> getAllCategories() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM menu_items ORDER BY category";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
        }
        return list;
    }

    // Get available menu items
    public List<MenuItem> getAvailableMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM menu_items WHERE is_available = TRUE ORDER BY category, name";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                menuItems.add(createMenuItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting available menu items: " + e.getMessage());
        }
        return menuItems;
    }

    // Update menu item
    public boolean updateMenuItem(MenuItem menuItem) {
        String query = "UPDATE menu_items SET name = ?, description = ?, base_price = ?, category = ?, is_available = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, menuItem.getName());
            pstmt.setString(2, menuItem.getDescription());
            pstmt.setDouble(3, menuItem.getPrice());
            pstmt.setString(4, menuItem.getCategory());
            pstmt.setBoolean(5, menuItem.isAvailable());
            pstmt.setInt(6, menuItem.getId());
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            return false;
        }
    }

    // Update availability
    public boolean updateMenuItemAvailability(int id, boolean isAvailable) {
        String query = "UPDATE menu_items SET is_available = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setBoolean(1, isAvailable);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating availability: " + e.getMessage());
            return false;
        }
    }

    // Delete menu item
    public boolean deleteMenuItem(int id) {
        String query = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
            return false;
        }
    }

    // Search by name
    public List<MenuItem> searchMenuItemsByName(String searchTerm) {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM menu_items WHERE name LIKE ? AND is_available = TRUE ORDER BY name";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                menuItems.add(createMenuItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching menu items: " + e.getMessage());
        }
        return menuItems;
    }

    // Helper: Convert ResultSet → MenuItem
    private MenuItem createMenuItemFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double basePrice = rs.getDouble("base_price");
        String category = rs.getString("category");
        String coffeeTypeStr = rs.getString("coffee_type");
        boolean isAvailable = rs.getBoolean("is_available");

        MenuItem menuItem;

        if ("Coffee".equalsIgnoreCase(category) && coffeeTypeStr != null) {
            try {
                Coffee.CoffeeType coffeeType = Coffee.CoffeeType.valueOf(coffeeTypeStr);
                menuItem = new Coffee(id, name, description, basePrice, coffeeType, Coffee.Size.MEDIUM, true);
            } catch (IllegalArgumentException e) {
                menuItem = new Coffee(id, name, description, basePrice, Coffee.CoffeeType.AMERICANO, Coffee.Size.MEDIUM, true);
            }
        } else {
            menuItem = new MenuItem(id, name, description, basePrice, category) {
                @Override
                public String getItemType() {
                    return category;
                }
            };
        }

        menuItem.setAvailable(isAvailable);
        return menuItem;
    }

    // Count menu items
    public int getMenuItemCount() {
        String query = "SELECT COUNT(*) AS count FROM menu_items";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("count") : 0;
        } catch (SQLException e) {
            System.err.println("Error getting menu item count: " + e.getMessage());
            return 0;
        }
    }

    // Count available items
    public int getAvailableMenuItemCount() {
        String query = "SELECT COUNT(*) AS count FROM menu_items WHERE is_available = TRUE";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("count") : 0;
        } catch (SQLException e) {
            System.err.println("Error getting available menu item count: " + e.getMessage());
            return 0;
        }
    }
}
