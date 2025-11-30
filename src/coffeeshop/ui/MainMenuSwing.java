package coffeeshop.ui;

import coffeeshop.db.DatabaseConnection;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
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
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;


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
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(88, 57, 39),
                    0, getHeight(), new Color(222, 206, 170)
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
        titleLabel.setForeground(new Color(245, 235, 224));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Chọn chế độ hoạt động");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(245, 235, 224, 200));
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
        center.setBorder(new EmptyBorder(80, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        
        // Shared button color (vibe coffee)
        Color btnColorCustomer = new Color(121, 85, 72);
        Color btnColorAccount = new Color(186, 140, 99);
        Color btnColorMgmt = new Color(93, 64, 55);
        Color btnColorAbout = new Color(198, 120, 63);
        // Customer Mode Button
        JButton customerBtn = createModeButton(
            "KHÁCH HÀNG", 
            "Đặt hàng, xem menu, quản lý tài khoản",
            btnColorCustomer, 
            e -> openCustomerMode()
        );
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 1; gbc.weighty = 1;
        center.add(customerBtn, gbc);
        
        // Customer Account Management Button
        JButton accountBtn = createModeButton(
            "QUẢN LÝ TÀI KHOẢN", 
            "Đăng nhập, đăng ký, xem lịch sử đơn hàng",
            btnColorAccount, 
            e -> openCustomerAccountMode()
        );
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1;
        center.add(accountBtn, gbc);
        
        // Management Mode Button
        JButton managementBtn = createModeButton(
            "QUẢN LÝ", 
            "Quản lý đơn hàng, báo cáo, thống kê",
            btnColorMgmt, 
            e -> openManagementMode()
        );
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1; gbc.weighty = 1;
        center.add(managementBtn, gbc);
        
        // About Button (thay thế vị trí của "Thông tin hệ thống")
        JButton aboutBtn = createModeButton(
            "GIỚI THIỆU", 
            "Thông tin về ứng dụng và nhóm phát triển",
            btnColorAbout, 
            e -> showAbout()
        );
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 1; gbc.weighty = 1;
        center.add(aboutBtn, gbc);
        
        return center;
    }
    
    private JButton createModeButton(String title, String description, Color color, ActionListener action) {
        JButton button = new JButton() {
            private float anim = 0f;
            private javax.swing.Timer timer;

            private java.awt.Color blend(java.awt.Color c, float a) {
                int r = (int) Math.min(255, c.getRed() * (1 - a) + 255 * a);
                int g = (int) Math.min(255, c.getGreen() * (1 - a) + 255 * a);
                int b = (int) Math.min(255, c.getBlue() * (1 - a) + 255 * a);
                return new java.awt.Color(r, g, b);
            }

            @Override
            protected void paintComponent(Graphics g) {
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

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
        
        button.setPreferredSize(new Dimension(230, 85));
        button.setBorder(new EmptyBorder(12, 16, 12, 16));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        
        // Button content
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel descLabel = new JLabel("<html><div style='text-align: center;'>" + description + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(255, 255, 255, 220));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setBorder(new javax.swing.border.EmptyBorder(8, 0, 12, 0));

        JPanel titleHolder = new JPanel(new java.awt.GridBagLayout());
        titleHolder.setOpaque(false);
        titleHolder.add(titleLabel, new java.awt.GridBagConstraints());

        content.add(titleHolder, BorderLayout.CENTER);
        content.add(descLabel, BorderLayout.SOUTH);

        button.setLayout(new BorderLayout());
        button.add(content, BorderLayout.CENTER);
        
        return button;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(20, 20, 30, 20));
        
        JLabel footerLabel = new JLabel("© 2025 Coffee Shop Management System - Nhóm 8 OOP");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(245, 235, 224, 200));
        
        footer.add(footerLabel);
        footer.setOpaque(false);
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
        JDialog dialog = new JDialog(this, "Giới thiệu", false);
        dialog.setModalityType(Dialog.ModalityType.MODELESS);
        dialog.setSize(600, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.addWindowFocusListener(new WindowFocusListener() {
            @Override public void windowGainedFocus(WindowEvent e) {}
            @Override public void windowLostFocus(WindowEvent e) {
                dialog.dispose();
            }
        });

        JPanel main = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(88, 57, 39), 0, getHeight(), new Color(222, 206, 170));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        main.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setOpaque(false);
        JLabel title = new JLabel("Coffee Shop Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(245, 235, 224));
        header.add(title);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(16, 24, 24, 24));

        String contentText = """
            Nền tảng quản lý quán cà phê tập trung vào hiệu quả vận hành: Order, thanh toán và chăm sóc khách hàng.

            Phát triển với Java Swing và OOP để đảm bảo ổn định, dễ bảo trì và mở rộng. Hỗ trợ cơ sở dữ liệu SQLite/MySQL, mang lại trải nghiệm quản lý trực quan, nhanh và chính xác.
        """;

        JTextArea contentArea = new JTextArea(contentText);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setForeground(new Color(250, 245, 235));
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        contentArea.setBorder(new EmptyBorder(6, 6, 6, 6));
        contentArea.setFocusable(false);
        contentArea.getCaret().setVisible(false);
        contentArea.getCaret().setSelectionVisible(false);
        contentArea.setCaretColor(new Color(0,0,0,0));
        center.add(contentArea, BorderLayout.NORTH);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        JLabel footText = new JLabel("Phát triển bởi Nhóm 8 OOP • Phiên bản 1.0.0");
        footText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footText.setForeground(new Color(255, 255, 255, 230));
        footer.add(footText);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(main);
        dialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenuSwing().setVisible(true);
        });
    }
}
