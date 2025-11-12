package coffeeshop.dao;

import coffeeshop.db.DatabaseConnection;
import coffeeshop.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Ingredient operations
 * Handles all database operations related to ingredient management (MySQL version)
 */
public class IngredientDAO {
    private DatabaseConnection dbConnection;
    
    public IngredientDAO() {
        this.dbConnection = new DatabaseConnection(); // Không còn getInstance()
    }
    
    // --- Create a new ingredient ---
    public int createIngredient(Ingredient ingredient) {
        String query = "INSERT INTO ingredients (name, current_stock, minimum_stock, maximum_stock, " +
                      "unit, cost_per_unit, supplier, expiration_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, ingredient.getName());
            pstmt.setDouble(2, ingredient.getCurrentStock());
            pstmt.setDouble(3, ingredient.getMinimumStock());
            pstmt.setDouble(4, ingredient.getMaximumStock());
            pstmt.setString(5, ingredient.getUnit().toString());
            pstmt.setDouble(6, ingredient.getCostPerUnit());
            pstmt.setString(7, ingredient.getSupplier());
            if (ingredient.getExpirationDate() != null)
                pstmt.setDate(8, Date.valueOf(ingredient.getExpirationDate()));
            else
                pstmt.setNull(8, Types.DATE);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating ingredient: " + e.getMessage());
        }
        
        return -1;
    }
    
    // --- Get ingredient by ID ---
    public Ingredient getIngredientById(int ingredientId) {
        String query = "SELECT * FROM ingredients WHERE ingredient_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, ingredientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createIngredientFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting ingredient by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    // --- Get ingredient by name ---
    public Ingredient getIngredientByName(String name) {
        String query = "SELECT * FROM ingredients WHERE name = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createIngredientFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting ingredient by name: " + e.getMessage());
        }
        
        return null;
    }
    
    // --- Get all ingredients ---
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting all ingredients: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // --- Get low stock ingredients ---
    public List<Ingredient> getLowStockIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE current_stock <= minimum_stock ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting low stock ingredients: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // --- Get out of stock ingredients ---
    public List<Ingredient> getOutOfStockIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE current_stock = 0 ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting out of stock ingredients: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // --- Get expired ingredients ---
    public List<Ingredient> getExpiredIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE expiration_date < CURDATE() ORDER BY expiration_date";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting expired ingredients: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // --- Get ingredients expiring soon ---
    public List<Ingredient> getIngredientExpiringSoon(int days) {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE expiration_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) ORDER BY expiration_date";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, days);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = createIngredientFromResultSet(rs);
                    if (ingredient != null) {
                        ingredients.add(ingredient);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting ingredients expiring soon: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // --- Get ingredients by supplier ---
    public List<Ingredient> getIngredientsBySupplier(String supplier) {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredients WHERE supplier LIKE ? ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + supplier + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = createIngredientFromResultSet(rs);
                    if (ingredient != null) {
                        ingredients.add(ingredient);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting ingredients by supplier: " + e.getMessage());
        }
        
        return ingredients;
    }
    
    // --- Update ingredient stock ---
    public boolean updateIngredientStock(int ingredientId, double newStock) {
        String query = "UPDATE ingredients SET current_stock = ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, newStock);
            pstmt.setInt(2, ingredientId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating ingredient stock: " + e.getMessage());
            return false;
        }
    }

    public void updateIngredient(Ingredient ingredient) throws SQLException {
    String sql = "UPDATE ingredients SET name = ?, unit = ?, current_stock = ? WHERE ingredient_id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, ingredient.getName());
        stmt.setString(2, ingredient.getUnit().toString());
        stmt.setDouble(3, ingredient.getCurrentStock());
        stmt.setInt(4, ingredient.getIngredientId());
        stmt.executeUpdate();
    }
}

    public static class IngredientStats {
        private int totalIngredients;
        private double totalStock;
        private int lowStockCount;
        private int outOfStockCount;

        public IngredientStats(int totalIngredients, double totalStock, int lowStockCount, int outOfStockCount) {
            this.totalIngredients = totalIngredients;
            this.totalStock = totalStock;
            this.lowStockCount = lowStockCount;
            this.outOfStockCount = outOfStockCount;
        }

        public int getTotalIngredients() { return totalIngredients; }
        public double getTotalStock() { return totalStock; }
        public int getLowStockCount() { return lowStockCount; }
        public int getOutOfStockCount() { return outOfStockCount; }
    }

    // --- Hàm lấy thống kê từ database ---
    public IngredientStats getIngredientStats() {
        String sql = """
            SELECT
                COUNT(*) AS totalIngredients,
                SUM(current_stock) AS totalStock,
                SUM(CASE WHEN current_stock <= 5 AND current_stock > 0 THEN 1 ELSE 0 END) AS lowStockCount,
                SUM(CASE WHEN current_stock = 0 THEN 1 ELSE 0 END) AS outOfStockCount
            FROM ingredients
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new IngredientStats(
                    rs.getInt("totalIngredients"),
                    rs.getDouble("totalStock"),
                    rs.getInt("lowStockCount"),
                    rs.getInt("outOfStockCount")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching ingredient statistics: " + e.getMessage());
        }
        return new IngredientStats(0, 0.0, 0, 0); // Giá trị mặc định nếu lỗi
    }

    
    // --- Add stock ---
    public boolean addStock(int ingredientId, double quantity) {
        String query = "UPDATE ingredients SET current_stock = current_stock + ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, quantity);
            pstmt.setInt(2, ingredientId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding stock: " + e.getMessage());
            return false;
        }
    }
    
    // --- Remove stock ---
    public boolean removeStock(int ingredientId, double quantity) {
        Ingredient ingredient = getIngredientById(ingredientId);
        if (ingredient == null || ingredient.getCurrentStock() < quantity) {
            return false;
        }
        
        String query = "UPDATE ingredients SET current_stock = current_stock - ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, quantity);
            pstmt.setInt(2, ingredientId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing stock: " + e.getMessage());
            return false;
        }
    }
    
    // --- Update cost per unit ---
    public boolean updateCostPerUnit(int ingredientId, double costPerUnit) {
        String query = "UPDATE ingredients SET cost_per_unit = ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDouble(1, costPerUnit);
            pstmt.setInt(2, ingredientId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating cost per unit: " + e.getMessage());
            return false;
        }
    }
    
    // --- Update expiration date ---
    public boolean updateExpirationDate(int ingredientId, LocalDate expirationDate) {
        String query = "UPDATE ingredients SET expiration_date = ?, updated_at = CURRENT_TIMESTAMP WHERE ingredient_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            if (expirationDate != null)
                pstmt.setDate(1, Date.valueOf(expirationDate));
            else
                pstmt.setNull(1, Types.DATE);
            pstmt.setInt(2, ingredientId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating expiration date: " + e.getMessage());
            return false;
        }
    }

    
    
    // --- Delete ingredient ---
    public boolean deleteIngredient(int ingredientId) {
        String query = "DELETE FROM ingredients WHERE ingredient_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, ingredientId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting ingredient: " + e.getMessage());
            return false;
        }
    }
    
    // --- Helper: convert ResultSet -> Ingredient object ---
    private Ingredient createIngredientFromResultSet(ResultSet rs) throws SQLException {
        int ingredientId = rs.getInt("ingredient_id");
        String name = rs.getString("name");
        double currentStock = rs.getDouble("current_stock");
        double minimumStock = rs.getDouble("minimum_stock");
        double maximumStock = rs.getDouble("maximum_stock");
        String unitStr = rs.getString("unit");
        double costPerUnit = rs.getDouble("cost_per_unit");
        String supplier = rs.getString("supplier");
        Date expirationDate = rs.getDate("expiration_date");
        
        Ingredient.Unit unit = Ingredient.Unit.valueOf(unitStr);
        Ingredient ingredient = new Ingredient(ingredientId, name, "", unit, minimumStock, costPerUnit);
        ingredient.addStock(currentStock);
        ingredient.setMaximumStock(maximumStock);
        ingredient.setSupplier(supplier);
        if (expirationDate != null)
            ingredient.setExpirationDate(expirationDate.toLocalDate());
        return ingredient;
    }
}
