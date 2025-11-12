package coffeeshop.app;

import java.util.Scanner;

import coffeeshop.db.DatabaseConnection;
import coffeeshop.ui.CustomerView;
import coffeeshop.ui.ManagementView;


public class CoffeeShopApp {
    private Scanner scanner;
    private CustomerView customerView;
    private ManagementView managementView;
    private DatabaseConnection databaseConnection;
    
    public CoffeeShopApp() {
        this.scanner = new Scanner(System.in);
        this.customerView = new CustomerView();
        this.managementView = new ManagementView();
    }
    
    public static void main(String[] args) {
        CoffeeShopApp app = new CoffeeShopApp();
        app.start();
    }
    
    public void start() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("WELCOME TO COFFEE SHOP MANAGEMENT SYSTEM");
        System.out.println("=".repeat(50));
        
        if (!initializeDatabase()) {
            System.out.println("Failed to initialize database. Exiting application.");
            return;
        }
        
        while (true) {
            showMainMenu();
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    startCustomerMode();
                    break;
                case 2:
                    startManagementMode();
                    break;
                case 3:
                    showAbout();
                    break;
                case 4:
                    showSystemInfo();
                    break;
                case 5:
                    exitApplication();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    

    private void showMainMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("           MAIN MENU");
        System.out.println("=".repeat(40));
        System.out.println("1. Customer Mode - Place Orders");
        System.out.println("2. Management Mode - Admin Panel");
        System.out.println("3. About");
        System.out.println("4. System Information");
        System.out.println("5. Exit");
        System.out.println("-".repeat(40));
        System.out.print("Please select an option (1-5): ");
    }
    
    private boolean initializeDatabase() {
        try {
            System.out.println("\nInitializing database...");
            
            databaseConnection = DatabaseConnection.getInstance();
            
            DatabaseConnection.createTables();
            DatabaseConnection.insertSampleData();
            
            System.out.println("Database initialized successfully!");
            return true;
            
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void startCustomerMode() {
        try {
            System.out.println("\nSwitching to Customer Mode...");
            customerView.start();
        } catch (Exception e) {
            System.err.println("Error in customer mode: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void startManagementMode() {
        try {
            System.out.println("\nSwitching to Management Mode...");
            managementView.start();
        } catch (Exception e) {
            System.err.println("Error in management mode: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private void showAbout() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    ABOUT COFFEE SHOP APP");
        System.out.println("=".repeat(60));
        System.out.println("\nApplication: Coffee Shop Management System");
        System.out.println("Version: 1.0.0");
        System.out.println("Language: Java");
        System.out.println("Database: MySQL");
        System.out.println("\nFeatures:");
        System.out.println(" Customer ordering system with menu selection");
        System.out.println(" Payment processing (Cash, Card, Mobile, Loyalty Points)");
        System.out.println(" Table management (Dine-in and Take-away options)");
        System.out.println(" Inventory management with low stock alerts");
        System.out.println(" Order tracking and status management");
        System.out.println(" Customer loyalty points system");
        System.out.println(" Management dashboard with analytics");
        System.out.println(" Reports and statistics");
        System.out.println("\nOOP Principles Implemented:");
        System.out.println("  Encapsulation - Private fields with getters/setters");
        System.out.println("  Inheritance - MenuItem base class with Coffee subclass");
        System.out.println("  Abstraction - Abstract methods and interfaces");
        System.out.println("  Polymorphism - Method overriding and interface implementation");
        System.out.println("  Composition - Complex objects containing other objects");
        System.out.println("  Singleton Pattern - Database connection management");
        System.out.println("  Data Access Object (DAO) Pattern - Database operations");
        System.out.println("\nDeveloped following strict OOP methodology without frameworks.");
        System.out.println("=".repeat(60));
    }
    

    private void showSystemInfo() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("              SYSTEM INFORMATION");
        System.out.println("=".repeat(50));
        
        try {
            System.out.println("\nJava Environment:");
            System.out.println("  Java Version: " + System.getProperty("java.version"));
            System.out.println("  Java Vendor: " + System.getProperty("java.vendor"));
            System.out.println("  Operating System: " + System.getProperty("os.name"));
            System.out.println("  OS Version: " + System.getProperty("os.version"));
            System.out.println("  Architecture: " + System.getProperty("os.arch"));
            
            // Runtime runtime = Runtime.getRuntime();
            // long maxMemory = runtime.maxMemory();
            // long totalMemory = runtime.totalMemory();
            // long freeMemory = runtime.freeMemory();
            // long usedMemory = totalMemory - freeMemory;
            
            // System.out.println("\nMemory Usage:");
            // System.out.printf("  Max Memory: %.2f MB%n", maxMemory / (1024.0 * 1024.0));
            // System.out.printf("  Total Memory: %.2f MB%n", totalMemory / (1024.0 * 1024.0));
            // System.out.printf("  Used Memory: %.2f MB%n", usedMemory / (1024.0 * 1024.0));
            // System.out.printf("  Free Memory: %.2f MB%n", freeMemory / (1024.0 * 1024.0));
            
            System.out.println("\nDatabase Status:");
            if (databaseConnection != null) {
                System.out.println(" Connection: Active");
                System.out.println(" Database Type: MySQL");
                System.out.println(" Database Name: coffee_shop");
                System.out.println(" Host: localhost:3306");
            } else {
                System.out.println(" Connection: Not initialized");
            }
            
            System.out.println("\nApplication Components:");
            System.out.println("  Customer View: " + (customerView != null ? "Loaded" : "Not loaded"));
            System.out.println("  Management View: " + (managementView != null ? "Loaded" : "Not loaded"));
            
            System.out.println("\nSystem Time: " + java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("Error retrieving system information: " + e.getMessage());
        }
        
        System.out.println("=".repeat(50));
    }
    

    private void exitApplication() {
        System.out.println("\nShutting down Coffee Shop Management System...");
        
        try {
            if (customerView != null) {
                customerView.cleanup();
            }
            
            if (managementView != null) {
                managementView.cleanup();
            }
            
            if (databaseConnection != null) {
                DatabaseConnection.closeConnection();
            }
            
            if (scanner != null) {
                scanner.close();
            }
            
            System.out.println("Cleanup completed successfully.");
            
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("    Thank you for using Coffee Shop Management System!");
        System.out.println("                    Goodbye!");
        System.out.println("=".repeat(50));
    }

    private int getIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private void displayHeader(String title, int width) {
        System.out.println("\n" + "=".repeat(width));
        
        int padding = (width - title.length()) / 2;
        String paddedTitle = " ".repeat(Math.max(0, padding)) + title;
        System.out.println(paddedTitle);
        
        System.out.println("=".repeat(width));
    }
    
    private void displayMessage(String message) {
        System.out.println("\n[INFO] " + message);
    }
    
    // private void displayError(String error) {
    //     System.err.println("\n[ERROR] " + error);
    // }
    

    private void displaySuccess(String success) {
        System.out.println("\n[SUCCESS] " + success);
    }
    
    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }
    
    public CustomerView getCustomerView() {
        return customerView;
    }

    public ManagementView getManagementView() {
        return managementView;
    }
}