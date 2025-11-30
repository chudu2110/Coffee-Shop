package coffeeshop.ui;

import coffeeshop.dao.CustomerDAO;
import coffeeshop.db.DatabaseConnection;
import coffeeshop.model.Customer;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
        
        // Center content: Đăng nhập/Đăng ký với vai trò
        JPanel centerPanel = createAuthCenterPanel();
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
    
    private JPanel createAuthCenterPanel() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(40, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.BOTH;

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(255, 253, 250));
        card.setBorder(new javax.swing.border.CompoundBorder(
            new javax.swing.border.LineBorder(new Color(200, 180, 150), 1, true),
            new EmptyBorder(24, 24, 24, 24)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JLabel cardTitle = new JLabel("Xác thực và Vai trò");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        cardTitle.setForeground(new Color(85, 45, 25));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        card.add(cardTitle, c);

        JLabel roleLabel = new JLabel("Vai trò:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JRadioButton rbCustomer = new JRadioButton("Khách hàng", true);
        JRadioButton rbAdmin = new JRadioButton("Admin");
        rbCustomer.setOpaque(false);
        rbAdmin.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        group.add(rbCustomer);
        group.add(rbAdmin);

        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0;
        card.add(roleLabel, c);
        JPanel roleBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        roleBtns.setOpaque(false);
        roleBtns.add(rbCustomer);
        roleBtns.add(rbAdmin);
        c.gridx = 1;
        card.add(roleBtns, c);

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(22);
        JLabel nameLabel = new JLabel("Họ tên:");
        JTextField nameField = new JTextField(22);
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        JTextField phoneField = new JTextField(22);
        JLabel adminPassLabel = new JLabel("Mật khẩu quản lý:");
        JPasswordField adminPassField = new JPasswordField(22);

        c.gridy = 2; c.gridx = 0;
        card.add(emailLabel, c);
        c.gridx = 1;
        card.add(emailField, c);
        c.gridy = 3; c.gridx = 0;
        card.add(nameLabel, c);
        c.gridx = 1;
        card.add(nameField, c);
        c.gridy = 4; c.gridx = 0;
        card.add(phoneLabel, c);
        c.gridx = 1;
        card.add(phoneField, c);
        c.gridy = 5; c.gridx = 0;
        card.add(adminPassLabel, c);
        c.gridx = 1;
        card.add(adminPassField, c);

        JButton actionBtn = createSimpleButton("Đăng nhập", new Color(93, 64, 55));
        JButton toggleBtn = createSimpleButton("Chuyển sang Đăng ký", new Color(186, 140, 99));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(toggleBtn);
        actions.add(actionBtn);
        c.gridy = 6; c.gridx = 0; c.gridwidth = 2;
        card.add(actions, c);

        final boolean[] registerMode = {false};
        Runnable updateMode = () -> {
            boolean isAdmin = rbAdmin.isSelected();
            boolean isRegister = registerMode[0];
            emailLabel.setVisible(!isAdmin);
            emailField.setVisible(!isAdmin);
            nameLabel.setVisible(!isAdmin && isRegister);
            nameField.setVisible(!isAdmin && isRegister);
            phoneLabel.setVisible(!isAdmin && isRegister);
            phoneField.setVisible(!isAdmin && isRegister);
            adminPassLabel.setVisible(isAdmin);
            adminPassField.setVisible(isAdmin);
            actionBtn.setText(isAdmin ? "Vào quản lý" : (isRegister ? "Đăng ký" : "Đăng nhập"));
            toggleBtn.setVisible(!isAdmin);
            toggleBtn.setText(isRegister ? "Chuyển sang Đăng nhập" : "Chuyển sang Đăng ký");
            card.revalidate();
            card.repaint();
        };
        toggleBtn.addActionListener(e -> { registerMode[0] = !registerMode[0]; updateMode.run(); });
        rbAdmin.addActionListener(e -> updateMode.run());
        rbCustomer.addActionListener(e -> updateMode.run());
        updateMode.run();

        actionBtn.addActionListener(e -> {
            if (rbAdmin.isSelected()) {
                String pass = new String(adminPassField.getPassword());
                if (pass.equals("admin123")) {
                    SwingUtilities.invokeLater(() -> {
                        ManagementSwingApp app = new ManagementSwingApp(true);
                        app.setVisible(true);
                        dispose();
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "Mật khẩu không đúng", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }
            CustomerDAO cdao = new CustomerDAO();
            boolean isRegister = registerMode[0];
            if (isRegister) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng điền đủ thông tin", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Customer newCustomer = new Customer(0, name, email, phone);
                    int id = cdao.createCustomer(newCustomer);
                    if (id > 0) {
                        Customer full = cdao.getCustomerById(id);
                        openCustomerOrder(full);
                    } else {
                        JOptionPane.showMessageDialog(this, "Đăng ký thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi đăng ký: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                String email = emailField.getText().trim();
                if (email.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập email", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Customer customer = cdao.getCustomerByEmail(email);
                    if (customer != null) {
                        openCustomerOrder(customer);
                    } else {
                        JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng với email này", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi đăng nhập: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1;
        center.add(card, gbc);
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

    private JButton createSimpleButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void openCustomerOrder(Customer customer) {
        SwingUtilities.invokeLater(() -> {
            CoffeeShopSwingApp app = new CoffeeShopSwingApp(customer);
            app.setVisible(true);
            dispose();
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
