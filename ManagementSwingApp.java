import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Management Interface for Coffee Shop Management System
 * Provides GUI for management operations
 */
public class ManagementSwingApp extends JFrame {
    private MenuItemDAO menuItemDAO;
    private CustomerDAO customerDAO;
    private OrderDAO orderDAO;
    private PaymentDAO paymentDAO;
    private TableDAO tableDAO;
    
    public ManagementSwingApp() {
        super("Coffee Shop Management System - Admin Panel");
        initializeDAOs();
        setupUI();
    }
    
    private void initializeDAOs() {
        this.menuItemDAO = new MenuItemDAO();
        this.customerDAO = new CustomerDAO();
        this.orderDAO = new OrderDAO();
        this.paymentDAO = new PaymentDAO();
        this.tableDAO = new TableDAO();
    }
    
    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Authentication
        if (!authenticate()) {
            JOptionPane.showMessageDialog(this, "Access denied!", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center with tabs
        JTabbedPane tabbedPane = createTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private boolean authenticate() {
        JPanel authPanel = new JPanel(new GridBagLayout());
        authPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("ADMIN AUTHENTICATION");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel passwordLabel = new JLabel("Enter Management Password:");
        JPasswordField passwordField = new JPasswordField(20);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        authPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1; gbc.gridy = 1;
        authPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        authPanel.add(passwordField, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, authPanel, "Authentication", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            return password.equals("admin123");
        }
        
        return false;
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(70, 130, 180));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("MANAGEMENT DASHBOARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Quản lý hệ thống quán cà phê");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        header.add(titlePanel, BorderLayout.WEST);
        
        // Back button
        JButton backBtn = new JButton("← Back to Main Menu");
        backBtn.setBackground(new Color(220, 20, 60));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            new MainMenuSwing().setVisible(true);
            dispose();
        });
        
        header.add(backBtn, BorderLayout.EAST);
        
        return header;
    }
    
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Menu Management Tab
        tabbedPane.addTab("Menu Management", createMenuManagementPanel());
        
        // Orders Tab
        tabbedPane.addTab("Orders", createOrdersPanel());
        
        // Payments Tab
        tabbedPane.addTab("Payments", createPaymentsPanel());
        
        // Customers Tab
        tabbedPane.addTab("Customers", createCustomersPanel());
        
        // Tables Tab
        tabbedPane.addTab("Tables", createTablesPanel());
        
        // Reports Tab
        tabbedPane.addTab("Reports", createReportsPanel());
        
        return tabbedPane;
    }
    
    private JPanel createMenuManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Menu items table
        String[] columns = {"ID", "Name", "Description", "Price", "Category", "Type", "Available"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable menuTable = new JTable(model);
        menuTable.setRowHeight(25);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Load menu items
        loadMenuItems(model);
        
        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setBorder(new TitledBorder("Menu Items"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadMenuItems(model));
        
        JButton addBtn = new JButton("Add Item");
        addBtn.addActionListener(e -> showAddMenuItemDialog());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Order ID", "Customer", "Service Type", "Table", "Total", "Status", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable ordersTable = new JTable(model);
        ordersTable.setRowHeight(25);
        
        loadOrders(model);
        
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(new TitledBorder("All Orders"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadOrders(model));
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Payment ID", "Order ID", "Method", "Amount", "Status", "Date", "Customer"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable paymentsTable = new JTable(model);
        paymentsTable.setRowHeight(25);
        
        loadPayments(model);
        
        JScrollPane scrollPane = new JScrollPane(paymentsTable);
        scrollPane.setBorder(new TitledBorder("All Payments (Invoices)"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadPayments(model));
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"ID", "Name", "Email", "Phone", "Loyalty Points", "Registration Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable customersTable = new JTable(model);
        customersTable.setRowHeight(25);
        
        loadCustomers(model);
        
        JScrollPane scrollPane = new JScrollPane(customersTable);
        scrollPane.setBorder(new TitledBorder("Customers"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadCustomers(model));
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTablesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Table #", "Capacity", "Status", "Customer ID", "Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tablesTable = new JTable(model);
        tablesTable.setRowHeight(25);
        
        loadTables(model);
        
        JScrollPane scrollPane = new JScrollPane(tablesTable);
        scrollPane.setBorder(new TitledBorder("Table Management"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadTables(model));
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextArea reportsArea = new JTextArea(20, 50);
        reportsArea.setEditable(false);
        reportsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportsArea.setBackground(new Color(248, 248, 248));
        
        loadReports(reportsArea);
        
        JScrollPane scrollPane = new JScrollPane(reportsArea);
        scrollPane.setBorder(new TitledBorder("Reports & Analytics"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh Reports");
        refreshBtn.addActionListener(e -> loadReports(reportsArea));
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(70, 130, 180));
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel footerLabel = new JLabel("© 2024 Coffee Shop Management System - Admin Panel");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        footer.add(footerLabel);
        return footer;
    }
    
    private void loadMenuItems(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<MenuItem> items = menuItemDAO.getAllMenuItems();
            for (MenuItem item : items) {
                model.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getDescription(),
                    String.format("%.0fđ", item.getBasePrice()),
                    item.getCategory(),
                    item.getItemType(),
                    item.isAvailable() ? "Yes" : "No"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading menu items: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadOrders(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<Order> orders = orderDAO.getAllOrders();
            for (Order order : orders) {
                Customer customer = customerDAO.getCustomerById(order.getCustomerId());
                String customerName = (customer != null) ? customer.getName() : "Unknown";
                String tableInfo = (order.getTableNumber() > 0) ? String.valueOf(order.getTableNumber()) : "N/A";
                
                model.addRow(new Object[]{
                    order.getOrderId(),
                    customerName,
                    order.getServiceType(),
                    tableInfo,
                    String.format("%.0fđ", order.getTotalAmount()),
                    order.getStatus(),
                    order.getOrderTime().toString().substring(0, 16)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadPayments(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<Payment> payments = paymentDAO.getAllPayments();
            
            for (Payment payment : payments) {
                Order order = orderDAO.getOrderById(payment.getOrderId());
                String customerName = "Unknown";
                if (order != null) {
                    Customer customer = customerDAO.getCustomerById(order.getCustomerId());
                    if (customer != null) {
                        customerName = customer.getName();
                    }
                }
                
                String paymentDate = "N/A";
                if (payment.getPaymentTime() != null) {
                    paymentDate = payment.getPaymentTime().toString().substring(0, 16);
                }
                
                model.addRow(new Object[]{
                    payment.getPaymentId(),
                    payment.getOrderId(),
                    payment.getPaymentMethod(),
                    String.format("%.0fđ", payment.getAmount()),
                    payment.getStatus(),
                    paymentDate,
                    customerName
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading payments: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadCustomers(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<Customer> customers = customerDAO.getAllCustomers();
            for (Customer customer : customers) {
                model.addRow(new Object[]{
                    customer.getCustomerId(),
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhoneNumber(),
                    customer.getLoyaltyPoints(),
                    customer.getRegistrationDate().toString().substring(0, 10)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTables(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<Table> tables = tableDAO.getAllTables();
            for (Table table : tables) {
                model.addRow(new Object[]{
                    table.getTableNumber(),
                    table.getCapacity(),
                    table.getStatus(),
                    table.getCurrentCustomerId() > 0 ? String.valueOf(table.getCurrentCustomerId()) : "N/A",
                    table.getNotes() != null ? table.getNotes() : ""
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading tables: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadReports(JTextArea reportsArea) {
        StringBuilder report = new StringBuilder();
        
        try {
            // Order Statistics
            OrderDAO.OrderStats orderStats = orderDAO.getOrderStats();
            report.append("=== ORDER STATISTICS ===\n");
            report.append("Total Orders: ").append(orderStats.getTotalOrders()).append("\n");
            report.append("Pending Orders: ").append(orderStats.getPendingOrders()).append("\n");
            report.append("Completed Orders: ").append(orderStats.getCompletedOrders()).append("\n");
            report.append("Cancelled Orders: ").append(orderStats.getCancelledOrders()).append("\n");
            report.append(String.format("Average Order Value: %.0fđ\n", orderStats.getAvgOrderValue()));
            report.append(String.format("Total Revenue: %.0fđ\n\n", orderStats.getTotalRevenue()));
            
            // Payment Statistics
            PaymentDAO.PaymentStats paymentStats = paymentDAO.getPaymentStats();
            report.append("=== PAYMENT STATISTICS ===\n");
            report.append("Total Payments: ").append(paymentStats.getTotalPayments()).append("\n");
            report.append("Completed Payments: ").append(paymentStats.getCompletedPayments()).append("\n");
            report.append("Cancelled Payments: ").append(paymentStats.getCancelledPayments()).append("\n");
            report.append(String.format("Total Revenue: %.0fđ\n", paymentStats.getTotalRevenue()));
            report.append(String.format("Average Payment Amount: %.0fđ\n\n", paymentStats.getAvgPaymentAmount()));
            
            // Table Statistics
            TableDAO.TableStats tableStats = tableDAO.getTableStats();
            report.append("=== TABLE STATISTICS ===\n");
            report.append("Available Tables: ").append(tableStats.getAvailableTables()).append("\n");
            report.append("Occupied Tables: ").append(tableStats.getOccupiedTables()).append("\n");
            report.append("Reserved Tables: ").append(tableStats.getReservedTables()).append("\n");
            report.append("Out of Service: ").append(tableStats.getOutOfServiceTables()).append("\n\n");
            
            // Menu Statistics
            report.append("=== MENU STATISTICS ===\n");
            report.append("Total Menu Items: ").append(menuItemDAO.getMenuItemCount()).append("\n");
            report.append("Available Items: ").append(menuItemDAO.getAvailableMenuItemCount()).append("\n");
            
        } catch (Exception e) {
            report.append("Error generating reports: ").append(e.getMessage());
        }
        
        reportsArea.setText(report.toString());
    }
    
    private void showAddMenuItemDialog() {
        JOptionPane.showMessageDialog(this, "Add Menu Item feature will be implemented in the next version.", 
            "Feature Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
}
