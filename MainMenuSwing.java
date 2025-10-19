import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Main Menu Interface for Coffee Shop Management System
 * Provides beautiful GUI for selecting different modes
 */
public class MainMenuSwing extends JFrame {
    
    public MainMenuSwing() {
        super("Coffee Shop Management System");
        initializeDatabase();
        setupUI();
    }
    
    private void initializeDatabase() {
        try {
            DatabaseConnection.getInstance();
            DatabaseConnection.createTables();
            DatabaseConnection.insertSampleData();
            System.out.println("Database initialized successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Database initialization failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Use default look and feel
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(139, 69, 19), // Dark brown
                    0, getHeight(), new Color(210, 180, 140) // Light brown
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center content
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("COFFEE SHOP MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Chọn chế độ hoạt động");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        header.add(titlePanel);
        return header;
    }
    
    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        
        // Customer Mode Button
        JButton customerBtn = createModeButton(
            "KHÁCH HÀNG", 
            "Đặt hàng, xem menu, quản lý tài khoản",
            new Color(34, 139, 34), // Forest Green
            e -> openCustomerMode()
        );
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        center.add(customerBtn, gbc);
        
        // Customer Account Management Button
        JButton accountBtn = createModeButton(
            "QUẢN LÝ TÀI KHOẢN", 
            "Đăng nhập, đăng ký, xem lịch sử đơn hàng",
            new Color(50, 205, 50), // Lime Green
            e -> openCustomerAccountMode()
        );
        gbc.gridx = 1; gbc.gridy = 0;
        center.add(accountBtn, gbc);
        
        // Management Mode Button
        JButton managementBtn = createModeButton(
            "QUẢN LÝ", 
            "Quản lý đơn hàng, báo cáo, thống kê",
            new Color(70, 130, 180), // Steel Blue
            e -> openManagementMode()
        );
        gbc.gridx = 0; gbc.gridy = 1;
        center.add(managementBtn, gbc);
        
        // System Info Button
        JButton systemBtn = createModeButton(
            "THÔNG TIN HỆ THỐNG", 
            "Xem thông tin database, trạng thái hệ thống",
            new Color(128, 0, 128), // Purple
            e -> showSystemInfo()
        );
        gbc.gridx = 1; gbc.gridy = 1;
        center.add(systemBtn, gbc);
        
        // About Button
        JButton aboutBtn = createModeButton(
            "GIỚI THIỆU", 
            "Thông tin về ứng dụng và nhóm phát triển",
            new Color(255, 140, 0), // Dark Orange
            e -> showAbout()
        );
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        center.add(aboutBtn, gbc);
        
        return center;
    }
    
    private JButton createModeButton(String title, String description, Color color, ActionListener action) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(color.brighter());
                } else {
                    g2d.setColor(color);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setPreferredSize(new Dimension(300, 120));
        button.setBorder(new EmptyBorder(15, 20, 15, 20));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        
        // Button content
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel descLabel = new JLabel("<html><div style='text-align: center;'>" + description + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(255, 255, 255, 220));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        content.add(titleLabel, BorderLayout.CENTER);
        content.add(descLabel, BorderLayout.SOUTH);
        
        button.setLayout(new BorderLayout());
        button.add(content, BorderLayout.CENTER);
        
        return button;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(20, 20, 30, 20));
        
        JLabel footerLabel = new JLabel("© 2024 Coffee Shop Management System - Nhóm 6 OOP");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(255, 255, 255, 180));
        
        footer.add(footerLabel);
        return footer;
    }
    
    private void openCustomerMode() {
        SwingUtilities.invokeLater(() -> {
            new CoffeeShopSwingApp().setVisible(true);
            this.dispose();
        });
    }
    
    private void openCustomerAccountMode() {
        SwingUtilities.invokeLater(() -> {
            new CustomerAccountSwing().setVisible(true);
            this.dispose();
        });
    }
    
    private void openManagementMode() {
        SwingUtilities.invokeLater(() -> {
            ManagementSwingApp managementApp = new ManagementSwingApp();
            managementApp.setVisible(true);
            this.dispose();
        });
    }
    
    private void showSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== THÔNG TIN HỆ THỐNG ===\n\n");
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("OS: ").append(System.getProperty("os.name")).append("\n");
        info.append("Database: MySQL\n");
        info.append("Framework: Java Swing\n\n");
        
        // Memory info
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        info.append("=== MEMORY USAGE ===\n");
        info.append(String.format("Max Memory: %.2f MB%n", maxMemory / (1024.0 * 1024.0)));
        info.append(String.format("Used Memory: %.2f MB%n", usedMemory / (1024.0 * 1024.0)));
        info.append(String.format("Free Memory: %.2f MB%n", freeMemory / (1024.0 * 1024.0)));
        
        JOptionPane.showMessageDialog(this, info.toString(), "System Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAbout() {
        String about = """
            === GIỚI THIỆU ỨNG DỤNG ===
            
            Coffee Shop Management System
            Phiên bản: 1.0.0
            Nhóm phát triển: Nhóm 6 OOP
            
            === TÍNH NĂNG CHÍNH ===
            Quản lý menu cà phê đa dạng
            Đặt hàng và thanh toán
            Quản lý bàn và khách hàng
            Báo cáo và thống kê
            Hệ thống loyalty points
            
            === CÔNG NGHỆ ===
            Java Swing (GUI)
            MySQL Database
            OOP Design Patterns
            
            Cảm ơn bạn đã sử dụng ứng dụng!
            """;
        
        JOptionPane.showMessageDialog(this, about, "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenuSwing().setVisible(true);
        });
    }
}
