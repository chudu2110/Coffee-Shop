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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Customer Account Management Interface
 * Provides GUI for customer account management and order history
 */
public class CustomerAccountSwing extends JFrame {
    private CustomerDAO customerDAO;
    private OrderDAO orderDAO;
    private PaymentDAO paymentDAO;
    private Customer currentCustomer;
    
    public CustomerAccountSwing() {
        super("Customer Account Management");
        initializeDAOs();
        setupUI();
    }
    
    private void initializeDAOs() {
        this.customerDAO = new CustomerDAO();
        this.orderDAO = new OrderDAO();
        this.paymentDAO = new PaymentDAO();
    }
    
    private void setupUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
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
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(34, 139, 34));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("👤 CUSTOMER ACCOUNT MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Quản lý tài khoản và lịch sử đơn hàng");
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
        
        // Login/Register Tab
        tabbedPane.addTab("Login/Register", createLoginPanel());
        
        // Account Info Tab
        tabbedPane.addTab("Account Info", createAccountInfoPanel());
        
        // Order History Tab
        tabbedPane.addTab("Order History", createOrderHistoryPanel());
        
        // Payment History Tab
        tabbedPane.addTab("Payment History", createPaymentHistoryPanel());
        
        return tabbedPane;
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(new Color(248, 248, 248));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Customer Authentication");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Login Section
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(new TitledBorder("Login"));
        loginPanel.setBackground(Color.WHITE);
        
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(20);
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(34, 139, 34));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBorderPainted(false);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        loginPanel.add(loginBtn, gbc);
        
        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter email address", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                Customer customer = customerDAO.getCustomerByEmail(email);
                if (customer != null) {
                    currentCustomer = customer;
                    JOptionPane.showMessageDialog(this, "Login successful! Welcome " + customer.getName(), 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    updateAccountInfo();
                    updateOrderHistory();
                    updatePaymentHistory();
                } else {
                    JOptionPane.showMessageDialog(this, "Customer not found with email: " + email, 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Register Section
        JPanel registerPanel = new JPanel(new GridBagLayout());
        registerPanel.setBorder(new TitledBorder("Register New Account"));
        registerPanel.setBackground(Color.WHITE);
        
        JLabel regNameLabel = new JLabel("Name:");
        JTextField regNameField = new JTextField(20);
        JLabel regEmailLabel = new JLabel("Email:");
        JTextField regEmailField = new JTextField(20);
        JLabel regPhoneLabel = new JLabel("Phone:");
        JTextField regPhoneField = new JTextField(20);
        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(new Color(70, 130, 180));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBorderPainted(false);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 0;
        registerPanel.add(regNameLabel, gbc);
        gbc.gridx = 1;
        registerPanel.add(regNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        registerPanel.add(regEmailLabel, gbc);
        gbc.gridx = 1;
        registerPanel.add(regEmailField, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        registerPanel.add(regPhoneLabel, gbc);
        gbc.gridx = 1;
        registerPanel.add(regPhoneField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        registerPanel.add(registerBtn, gbc);
        
        registerBtn.addActionListener(e -> {
            String name = regNameField.getText().trim();
            String email = regEmailField.getText().trim();
            String phone = regPhoneField.getText().trim();
            
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                Customer newCustomer = new Customer(0, name, email, phone);
                int customerId = customerDAO.createCustomer(newCustomer);
                if (customerId > 0) {
                    newCustomer = customerDAO.getCustomerById(customerId);
                    currentCustomer = newCustomer;
                    JOptionPane.showMessageDialog(this, "Registration successful! Welcome " + name, 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    updateAccountInfo();
                    regNameField.setText("");
                    regEmailField.setText("");
                    regPhoneField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Add panels to main panel
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(loginPanel, gbc);
        gbc.gridx = 1;
        panel.add(registerPanel, gbc);
        
        return panel;
    }
    
    private JPanel createAccountInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextArea accountInfo = new JTextArea(15, 50);
        accountInfo.setEditable(false);
        accountInfo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        accountInfo.setBackground(new Color(248, 248, 248));
        accountInfo.setBorder(new TitledBorder("Account Information"));
        
        JScrollPane scrollPane = new JScrollPane(accountInfo);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> updateAccountInfo());
        
        JButton updateBtn = new JButton("Update Info");
        updateBtn.addActionListener(e -> showUpdateDialog());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(updateBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Store reference for updates
        this.accountInfoArea = accountInfo;
        
        return panel;
    }
    
    private JPanel createOrderHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Order ID", "Date", "Service Type", "Table", "Total", "Status", "Items"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable ordersTable = new JTable(model);
        ordersTable.setRowHeight(25);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(new TitledBorder("Order History"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> updateOrderHistory());
        
        JButton detailsBtn = new JButton("View Details");
        detailsBtn.addActionListener(e -> showOrderDetails(ordersTable));
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(detailsBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Store reference for updates
        this.ordersTableModel = model;
        this.ordersTable = ordersTable;
        
        return panel;
    }
    
    private JPanel createPaymentHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Payment ID", "Order ID", "Method", "Amount", "Status", "Date", "Reference"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable paymentsTable = new JTable(model);
        paymentsTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(paymentsTable);
        scrollPane.setBorder(new TitledBorder("Payment History"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> updatePaymentHistory());
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Store reference for updates
        this.paymentsTableModel = model;
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(34, 139, 34));
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel footerLabel = new JLabel("© 2024 Coffee Shop Management System - Customer Account");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        footer.add(footerLabel);
        return footer;
    }
    
    // Helper fields for updates
    private JTextArea accountInfoArea;
    private DefaultTableModel ordersTableModel;
    private JTable ordersTable;
    private DefaultTableModel paymentsTableModel;
    
    private void updateAccountInfo() {
        if (currentCustomer == null) {
            accountInfoArea.setText("Please login first to view account information.");
            return;
        }
        
        StringBuilder info = new StringBuilder();
        info.append("=== CUSTOMER ACCOUNT INFORMATION ===\n\n");
        info.append("Customer ID: ").append(currentCustomer.getCustomerId()).append("\n");
        info.append("Name: ").append(currentCustomer.getName()).append("\n");
        info.append("Email: ").append(currentCustomer.getEmail()).append("\n");
        info.append("Phone: ").append(currentCustomer.getPhoneNumber()).append("\n");
        info.append("Registration Date: ").append(currentCustomer.getRegistrationDate()).append("\n");
        info.append("Loyalty Points: ").append(currentCustomer.getLoyaltyPoints()).append("\n");
        info.append("Total Orders: ").append(currentCustomer.getTotalOrders()).append("\n");
        info.append("Total Spent: ").append(String.format("%.0fđ", currentCustomer.getTotalSpent())).append("\n\n");
        
        // Recent orders summary
        try {
            List<Order> recentOrders = orderDAO.getOrdersByCustomerId(currentCustomer.getCustomerId());
            info.append("=== RECENT ORDERS SUMMARY ===\n");
            info.append("Total Orders: ").append(recentOrders.size()).append("\n");
            
            if (!recentOrders.isEmpty()) {
                info.append("\nLast 5 Orders:\n");
                info.append(String.format("%-8s %-12s %-15s %-8s %-12s%n", "Order ID", "Date", "Service Type", "Total", "Status"));
                info.append("-".repeat(60)).append("\n");
                
                int count = Math.min(5, recentOrders.size());
                for (int i = 0; i < count; i++) {
                    Order order = recentOrders.get(i);
                    info.append(String.format("%-8d %-12s %-15s %-8.0f %-12s%n",
                        order.getOrderId(),
                        order.getOrderTime().toString().substring(0, 10),
                        order.getServiceType(),
                        order.getTotalAmount(),
                        order.getStatus()));
                }
            }
        } catch (Exception e) {
            info.append("Error loading order history: ").append(e.getMessage());
        }
        
        accountInfoArea.setText(info.toString());
    }
    
    private void updateOrderHistory() {
        if (currentCustomer == null) {
            ordersTableModel.setRowCount(0);
            return;
        }
        
        ordersTableModel.setRowCount(0);
        try {
            List<Order> orders = orderDAO.getOrdersByCustomerId(currentCustomer.getCustomerId());
            for (Order order : orders) {
                String tableInfo = (order.getTableNumber() > 0) ? String.valueOf(order.getTableNumber()) : "N/A";
                String itemsCount = "N/A"; // Would need to get from order items
                
                ordersTableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getOrderTime().toString().substring(0, 16),
                    order.getServiceType(),
                    tableInfo,
                    String.format("%.0fđ", order.getTotalAmount()),
                    order.getStatus(),
                    itemsCount
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading order history: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updatePaymentHistory() {
        if (currentCustomer == null) {
            paymentsTableModel.setRowCount(0);
            return;
        }
        
        paymentsTableModel.setRowCount(0);
        try {
            List<Order> orders = orderDAO.getOrdersByCustomerId(currentCustomer.getCustomerId());
            for (Order order : orders) {
                List<Payment> payments = paymentDAO.getPaymentsByOrderId(order.getOrderId());
                for (Payment payment : payments) {
                    String paymentDate = "N/A";
                    if (payment.getPaymentTime() != null) {
                        paymentDate = payment.getPaymentTime().toString().substring(0, 16);
                    }
                    
                    paymentsTableModel.addRow(new Object[]{
                        payment.getPaymentId(),
                        payment.getOrderId(),
                        payment.getPaymentMethod(),
                        String.format("%.0fđ", payment.getAmount()),
                        payment.getStatus(),
                        paymentDate,
                        payment.getTransactionReference() != null ? payment.getTransactionReference() : "N/A"
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading payment history: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showUpdateDialog() {
        if (currentCustomer == null) {
            JOptionPane.showMessageDialog(this, "Please login first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JPanel updatePanel = new JPanel(new GridBagLayout());
        updatePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField(currentCustomer.getName(), 20);
        JLabel phoneLabel = new JLabel("Phone:");
        JTextField phoneField = new JTextField(currentCustomer.getPhoneNumber(), 20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        updatePanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        updatePanel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        updatePanel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        updatePanel.add(phoneField, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, updatePanel, "Update Account Information", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            String newPhone = phoneField.getText().trim();
            
            if (newName.isEmpty() || newPhone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                currentCustomer.setName(newName);
                currentCustomer.setPhoneNumber(newPhone);
                
                if (customerDAO.updateCustomer(currentCustomer)) {
                    JOptionPane.showMessageDialog(this, "Account updated successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    updateAccountInfo();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update account", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showOrderDetails(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int orderId = (Integer) table.getValueAt(selectedRow, 0);
            Order order = orderDAO.getOrderById(orderId);
            
            if (order != null) {
                StringBuilder details = new StringBuilder();
                details.append("=== ORDER DETAILS ===\n\n");
                details.append("Order ID: ").append(order.getOrderId()).append("\n");
                details.append("Customer ID: ").append(order.getCustomerId()).append("\n");
                details.append("Service Type: ").append(order.getServiceType()).append("\n");
                details.append("Table Number: ").append(order.getTableNumber()).append("\n");
                details.append("Subtotal: ").append(String.format("%.0fđ", order.getSubtotal())).append("\n");
                details.append("Tax: ").append(String.format("%.0fđ", order.getTax())).append("\n");
                details.append("Discount: ").append(String.format("%.0fđ", order.getDiscount())).append("\n");
                details.append("Total Amount: ").append(String.format("%.0fđ", order.getTotalAmount())).append("\n");
                details.append("Status: ").append(order.getStatus()).append("\n");
                details.append("Order Time: ").append(order.getOrderTime()).append("\n");
                if (order.getCompletionTime() != null) {
                    details.append("Completion Time: ").append(order.getCompletionTime()).append("\n");
                }
                if (order.getSpecialInstructions() != null && !order.getSpecialInstructions().isEmpty()) {
                    details.append("Special Instructions: ").append(order.getSpecialInstructions()).append("\n");
                }
                
                JOptionPane.showMessageDialog(this, details.toString(), "Order Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading order details: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
