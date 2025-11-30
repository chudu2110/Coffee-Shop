package coffeeshop.ui;

import coffeeshop.dao.*;
import coffeeshop.model.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
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
    private final Color coffeeDark = new Color(88, 57, 39);
    private final Color coffeeLight = new Color(222, 206, 170);
    private final Color coffeeText = new Color(245, 235, 224);
    private final Color panelBg = new Color(255, 253, 250);
    
    private boolean authenticated = false;

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
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Authentication
        if (!authenticate()) {
            authenticated = false;
            dispose();
            return;
        }
        authenticated = true;
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, coffeeDark, 0, getHeight(), coffeeLight);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        
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

    public boolean isAuthenticated() {
        return authenticated;
    }
    
    private boolean authenticate() {
        final boolean[] ok = {false};
        JDialog dialog = new JDialog(this, "Authentication", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(520, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel authPanel = new JPanel(new GridBagLayout());
        authPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        authPanel.setBackground(new Color(255, 253, 250));

        JLabel titleLabel = new JLabel("ADMIN AUTHENTICATION");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(new Color(85, 45, 25));

        JLabel passwordLabel = new JLabel("Enter Management Password:");
        JPasswordField passwordField = new JPasswordField(24);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new java.awt.Dimension(280, 30));

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(186, 50, 50));
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton okBtn = createStaticCoffeeButton("OK", new Color(93, 64, 55));
        JButton cancelBtn = createStaticCoffeeButton("Cancel", new Color(121, 85, 72));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        authPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        authPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        authPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        authPanel.add(errorLabel, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        buttons.setOpaque(false);
        buttons.add(okBtn);
        buttons.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        authPanel.add(buttons, gbc);

        okBtn.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            if (password.equals("admin123")) {
                ok[0] = true;
                dialog.dispose();
            } else {
                errorLabel.setText("Access denied!");
            }
        });

        cancelBtn.addActionListener(e -> { ok[0] = false; dialog.dispose(); });

        dialog.setContentPane(authPanel);
        dialog.getRootPane().setDefaultButton(okBtn);
        dialog.addWindowListener(new java.awt.event.WindowAdapter(){
            @Override public void windowOpened(java.awt.event.WindowEvent e){
                passwordField.requestFocusInWindow();
            }
        });
        dialog.setVisible(true);
        return ok[0];
    }

    private JButton createStaticCoffeeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, coffeeDark, 0, getHeight(), coffeeLight);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("MANAGEMENT DASHBOARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(coffeeText);
        
        JLabel subtitleLabel = new JLabel("Quản lý hệ thống quán cà phê");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(245, 235, 224, 200));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        header.add(titlePanel, BorderLayout.WEST);
        
        // Back button
        JButton backBtn = createStaticCoffeeButton("← Back to Main Menu", new Color(93, 64, 55));
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
        javax.swing.UIManager.put("TabbedPane.contentAreaColor", panelBg);
        javax.swing.UIManager.put("TabbedPane.background", panelBg);
        javax.swing.UIManager.put("TabbedPane.darkShadow", panelBg);
        javax.swing.UIManager.put("TabbedPane.light", panelBg);
        javax.swing.UIManager.put("TabbedPane.highlight", panelBg);
        tabbedPane.setOpaque(false);
        
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
        panel.setBackground(panelBg);
        
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
        scrollPane.getViewport().setBackground(panelBg);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        JButton refreshBtn = createStaticCoffeeButton("Refresh", new Color(121, 85, 72));
        refreshBtn.addActionListener(e -> loadMenuItems(model));
        
        JButton addBtn = createStaticCoffeeButton("Add Item", new Color(186, 140, 99));
        addBtn.addActionListener(e -> showAddMenuItemDialog());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(panelBg);
        
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
        scrollPane.getViewport().setBackground(panelBg);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        JButton refreshBtn = createStaticCoffeeButton("Refresh", new Color(121, 85, 72));
        refreshBtn.addActionListener(e -> loadOrders(model));
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(panelBg);
        
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
        scrollPane.getViewport().setBackground(panelBg);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        JButton refreshBtn = createStaticCoffeeButton("Refresh", new Color(121, 85, 72));
        refreshBtn.addActionListener(e -> loadPayments(model));
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(panelBg);
        
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
        scrollPane.getViewport().setBackground(panelBg);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        JButton refreshBtn = createStaticCoffeeButton("Refresh", new Color(121, 85, 72));
        refreshBtn.addActionListener(e -> loadCustomers(model));
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTablesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(panelBg);
        
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
        scrollPane.getViewport().setBackground(panelBg);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        JButton refreshBtn = createStaticCoffeeButton("Refresh", new Color(121, 85, 72));
        refreshBtn.addActionListener(e -> loadTables(model));
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(panelBg);
        
        JTextArea reportsArea = new JTextArea(20, 50);
        reportsArea.setEditable(false);
        reportsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportsArea.setBackground(new Color(248, 248, 248));
        
        loadReports(reportsArea);
        
        JScrollPane scrollPane = new JScrollPane(reportsArea);
        scrollPane.setBorder(new TitledBorder("Reports & Analytics"));
        scrollPane.getViewport().setBackground(panelBg);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        JButton refreshBtn = createStaticCoffeeButton("Refresh Reports", new Color(121, 85, 72));
        refreshBtn.addActionListener(e -> loadReports(reportsArea));
        
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JButton createCoffeeButton(String text, Color color) {
        JButton button = new JButton(text) {
            private float anim = 0f;
            private javax.swing.Timer timer;

            private java.awt.Color blend(java.awt.Color c, float a) {
                int r = (int) Math.min(255, c.getRed() * (1 - a) + 255 * a);
                int g = (int) Math.min(255, c.getGreen() * (1 - a) + 255 * a);
                int b = (int) Math.min(255, c.getBlue() * (1 - a) + 255 * a);
                return new java.awt.Color(r, g, b);
            }

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                if (timer == null) {
                    timer = new javax.swing.Timer(16, e -> {
                        float target = getModel().isRollover() ? 1f : 0f;
                        float step = 0.12f;
                        if (anim < target) anim = Math.min(target, anim + step);
                        else anim = Math.max(target, anim - step);
                        repaint();
                    });
                    timer.start();
                }

                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                boolean press = getModel().isPressed();

                float lift = press ? anim * 0.4f : anim;
                g2.translate(0, -2 * lift);
                g2.scale(1 + 0.01f * lift, 1 + 0.01f * lift);

                java.awt.Color fill = blend(color, anim * 0.15f);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w, h, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, coffeeDark, 0, getHeight(), coffeeLight);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel footerLabel = new JLabel("© 2025 Coffee Shop Management System - Admin Panel");
        footerLabel.setForeground(new Color(245, 235, 224));
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
