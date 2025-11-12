package coffeeshop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import coffeeshop.dao.CustomerDAO;
import coffeeshop.dao.MenuItemDAO;
import coffeeshop.dao.OrderDAO;
import coffeeshop.dao.PaymentDAO;
import coffeeshop.db.DatabaseConnection;
import coffeeshop.model.Coffee;
import coffeeshop.model.Customer;
import coffeeshop.model.MenuItem;
import coffeeshop.model.Order;
import coffeeshop.model.OrderItem;
import coffeeshop.model.Payment;

public class CoffeeShopSwingApp extends JFrame {
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final Order currentOrder = new Order(0, 1, Order.ServiceType.TAKEAWAY);

    private final DefaultListModel<MenuItem> menuModel = new DefaultListModel<>();
    private final JList<MenuItem> menuList = new JList<>(menuModel);
    private final JComboBox<String> categoryCombo = new JComboBox<>();
    private final JTextArea orderArea = new JTextArea(14, 30);
    private final JLabel totalLabel = new JLabel();
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));

    private String toVND(double amount) {
        long vnd = Math.round(amount);
        return vnd + "đ";
    }

    public CoffeeShopSwingApp() {
        super("Coffee Shop - Nhóm 8 OOP");
        DatabaseConnection.getInstance().createTables();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        UIManager.put("Panel.background", new Color(250, 247, 242));
        UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
		setContentPane(root);
		setJMenuBar(createMenuBar());

        JLabel title = new JLabel("Ứng dụng quản lý quán cà phê");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(85, 45, 25));
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));
        root.add(center, BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout(10, 10));
        left.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(200, 180, 150), 2, true), "Danh mục món", 
                        TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(85, 45, 25)),
				new EmptyBorder(8, 8, 8, 8)
        ));

        JPanel catPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        catPanel.add(new JLabel("Danh mục:"));
        categoryCombo.addItem("Tất cả");
		for (String c : menuItemDAO.getAllCategories())
			categoryCombo.addItem(c);

        categoryCombo.addActionListener(e -> loadMenu());
        catPanel.add(categoryCombo);
        left.add(catPanel, BorderLayout.NORTH);

        menuList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        menuList.setSelectionBackground(new Color(210, 170, 120));
        menuList.setSelectionForeground(Color.WHITE);
        menuList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MenuItem) {
                    MenuItem mi = (MenuItem) value;
                    setText(mi.getName() + " — " + toVND(mi.getPrice()));
                }
                return c;
            }
        });
        left.add(new JScrollPane(menuList), BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controls.add(new JLabel("Số lượng:"));
        controls.add(qtySpinner);

        JButton addBtn = createStyledButton("Thêm vào giỏ", new Color(100, 150, 100));
        addBtn.addActionListener(e -> onAdd());
        JButton clearBtn = createStyledButton("Xóa giỏ", new Color(200, 100, 100));
        clearBtn.addActionListener(e -> { currentOrder.clearOrder(); updateOrderArea(); });
        JButton checkoutBtn = createStyledButton("Thanh toán", new Color(85, 130, 180));
        checkoutBtn.addActionListener(e -> onCheckout());
		JButton historyBtn = createStyledButton("Lịch sử đơn", new Color(140, 120, 160));
		historyBtn.addActionListener(e -> showOrderHistory());

        controls.add(addBtn);
        controls.add(clearBtn);
		controls.add(checkoutBtn);
		controls.add(historyBtn);
        left.add(controls, BorderLayout.SOUTH);

        center.add(left);

        JPanel right = new JPanel(new BorderLayout(10, 10));
        right.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(200, 180, 150), 2, true), "Đơn hàng hiện tại", 
                        TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), new Color(85, 45, 25)),
				new EmptyBorder(8, 8, 8, 8)
        ));

        orderArea.setEditable(false);
        orderArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        orderArea.setBackground(new Color(255, 253, 250));
        right.add(new JScrollPane(orderArea), BorderLayout.CENTER);

        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(new Color(100, 70, 40));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
        right.add(totalLabel, BorderLayout.SOUTH);

        center.add(right);

		JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
		JButton bottomCheckout = createStyledButton("Thanh toán", new Color(85, 130, 180));
		bottomCheckout.addActionListener(e -> onCheckout());
		JButton bottomHistory = createStyledButton("Lịch sử đơn", new Color(140, 120, 160));
		bottomHistory.addActionListener(e -> showOrderHistory());
		bottomBar.add(bottomHistory);
		bottomBar.add(bottomCheckout);
		root.add(bottomBar, BorderLayout.SOUTH);

        loadMenu();
        updateOrderArea();
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadMenu() {
        menuModel.clear();
        String sel = (String) categoryCombo.getSelectedItem();
        List<MenuItem> items = (sel == null || sel.equals("Tất cả"))
                ? menuItemDAO.getAvailableMenuItems()
                : menuItemDAO.getMenuItemsByCategory(sel);
        for (MenuItem mi : items) menuModel.addElement(mi);
    }

    private void onAdd() {
        MenuItem selected = menuList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn món");
            return;
        }
		int qty = (Integer) qtySpinner.getValue();
		if (selected instanceof Coffee) {
			Coffee configured = showCoffeeOptionsAndConfigure((Coffee) selected);
			if (configured == null) return; 
			currentOrder.addItem(configured, qty);
		} else {
			currentOrder.addItem(selected, qty);
		}
		updateOrderArea();
    }

	private Coffee showCoffeeOptionsAndConfigure(Coffee base) {
		JDialog dialog = new JDialog(this, "Tùy chọn đồ uống", true);
		dialog.setLayout(new BorderLayout(10, 10));
		dialog.getContentPane().setBackground(new Color(250, 247, 242));
		((JComponent) dialog.getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

		// Size selection
		JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sizePanel.setOpaque(false);
		sizePanel.add(new JLabel("Kích cỡ:"));
		JComboBox<String> sizeCombo = new JComboBox<>(new String[] { "Nhỏ", "Vừa", "Lớn" });
		sizeCombo.setSelectedIndex(1);
		sizePanel.add(sizeCombo);

		JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempPanel.setOpaque(false);
		JRadioButton hotBtn = new JRadioButton("Nóng", true);
		JRadioButton coldBtn = new JRadioButton("Lạnh");
		ButtonGroup tempGroup = new ButtonGroup();
		tempGroup.add(hotBtn);
		tempGroup.add(coldBtn);
		tempPanel.add(new JLabel("Nhiệt độ:"));
		tempPanel.add(hotBtn);
		tempPanel.add(coldBtn);

		JPanel custPanel = new JPanel(new GridLayout(0, 2, 8, 8));
		custPanel.setOpaque(false);
		custPanel.setBorder(new TitledBorder(new LineBorder(new Color(200, 180, 150), 1, true), "Thêm lựa chọn"));
		JCheckBox extraShot = new JCheckBox("Thêm shot espresso (+5.000đ)");
		JCheckBox caramel = new JCheckBox("Sốt caramel (+5.000đ)");
		JCheckBox vanilla = new JCheckBox("Siro vanilla (+5.000đ)");
		JCheckBox whipped = new JCheckBox("Kem tươi (+5.000đ)");
		extraShot.setOpaque(false);
		caramel.setOpaque(false);
		vanilla.setOpaque(false);
		whipped.setOpaque(false);
		custPanel.add(extraShot);
		custPanel.add(caramel);
		custPanel.add(vanilla);
		custPanel.add(whipped);

		JPanel center = new JPanel();
		center.setOpaque(false);
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(sizePanel);
		center.add(tempPanel);
		center.add(Box.createVerticalStrut(6));
		center.add(custPanel);
		dialog.add(center, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		actions.setOpaque(false);
		JButton ok = new JButton("Xong");
		JButton cancel = new JButton("Hủy");
		actions.add(cancel);
		actions.add(ok);
		dialog.add(actions, BorderLayout.SOUTH);

		final Coffee[] result = new Coffee[1];
		ok.addActionListener((ActionEvent e) -> {
			Coffee.Size size = Coffee.Size.MEDIUM;
			int idx = sizeCombo.getSelectedIndex();
			if (idx == 0) size = Coffee.Size.SMALL; else if (idx == 2) size = Coffee.Size.LARGE;
			Coffee configured = new Coffee(base.getId(), base.getName(), base.getDescription(), base.getBasePrice(), base.getCoffeeType(), size, hotBtn.isSelected());
			if (extraShot.isSelected()) configured.addCustomization("Extra Shot");
			if (caramel.isSelected()) configured.addCustomization("Caramel");
			if (vanilla.isSelected()) configured.addCustomization("Vanilla");
			if (whipped.isSelected()) configured.addCustomization("Whipped Cream");
			result[0] = configured;
			dialog.dispose();
		});
		cancel.addActionListener((ActionEvent e) -> {
			result[0] = null;
			dialog.dispose();
		});

		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);

		return result[0];
	}

	private JMenuBar createMenuBar() {
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("Đơn hàng");
		JMenuItem miCheckout = new JMenuItem("Thanh toán");
		miCheckout.addActionListener(e -> onCheckout());
		JMenuItem miHistory = new JMenuItem("Lịch sử đơn");
		miHistory.addActionListener(e -> showOrderHistory());
		menu.add(miCheckout);
		menu.add(miHistory);
		bar.add(menu);
		return bar;
	}

    private void onCheckout() {
        if (currentOrder.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống");
            return;
        }
        OrderDAO orderDAO = new OrderDAO();
        int orderId = orderDAO.createOrder(currentOrder);
        if (orderId > 0) {
			double total = currentOrder.getTotalAmount();
			if (showPaymentDialog(orderId, total)) {
				JOptionPane.showMessageDialog(this,
					"Thanh toán thành công cho đơn #" + orderId +
					"\nSố tiền: " + toVND(total));
				// Cập nhật trạng thái đơn
				//orderDAO.createOrder(currentOrder) /*(orderId, Order.OrderStatus.COMPLETED)*/;
                // Trừ kho và cập nhật trạng thái món
                // boolean deducted = menuItemDAO.deductStockForOrder(currentOrder.getOrderItems());
                // if (!deducted) {
                //     System.err.println("Cảnh báo: Không thể trừ kho sau thanh toán");
                // }
                // Refresh menu nếu có món trở thành hết hàng
                loadMenu();
				currentOrder.clearOrder();
				updateOrderArea();
			} else {
				JOptionPane.showMessageDialog(this, "Thanh toán thất bại hoặc đã hủy.");
			}
        } else {
            JOptionPane.showMessageDialog(this, "Lưu đơn thất bại");
        }
    }

	private boolean showPaymentDialog(int orderId, double total) {
		JDialog dialog = new JDialog(this, "Thanh toán", true);
		dialog.setLayout(new BorderLayout(10, 10));
		((JComponent) dialog.getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

		JPanel top = new JPanel(new GridLayout(0, 1, 4, 4));
		top.add(new JLabel("Tổng cần thanh toán: " + toVND(total)));
		dialog.add(top, BorderLayout.NORTH);

		JPanel center = new JPanel(new GridLayout(0, 2, 8, 8));
		center.add(new JLabel("Phương thức:"));
		JComboBox<String> method = new JComboBox<>(new String[] { "Tiền mặt", "Thẻ", "Ví điện tử" });
		center.add(method);

		center.add(new JLabel("Tiền khách đưa (nếu tiền mặt):"));
		JTextField cashField = new JTextField();
		center.add(cashField);

		dialog.add(center, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton cancel = new JButton("Hủy");
		JButton pay = new JButton("Thanh toán");
		actions.add(cancel);
		actions.add(pay);
		dialog.add(actions, BorderLayout.SOUTH);

		final boolean[] success = new boolean[] { false };
		pay.addActionListener((ActionEvent e) -> {
			Payment.PaymentMethod pm = Payment.PaymentMethod.CASH;
			String sel = (String) method.getSelectedItem();
			if ("Thẻ".equals(sel)) pm = Payment.PaymentMethod.CREDIT_CARD;
			else if ("Ví điện tử".equals(sel)) pm = Payment.PaymentMethod.MOBILE_PAYMENT;

			Payment payment = new Payment(0, orderId, pm, total);
			boolean processed = false;
			if (pm == Payment.PaymentMethod.CASH) {
				try {
					double cash = Double.parseDouble(cashField.getText().trim());
					processed = payment.processCashPayment(cash);
					if (processed) {
						JOptionPane.showMessageDialog(dialog, "Tiền thừa: " + toVND(payment.getChangeGiven()));
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(dialog, "Số tiền khách đưa không hợp lệ");
					return;
				}
			} else if (pm == Payment.PaymentMethod.CREDIT_CARD) {
				processed = payment.processCardPayment("4111111111111111", "12/30", "123");
			} else {
				processed = payment.processMobilePayment("MOMO_" + System.currentTimeMillis());
			}

			if (processed) {
				PaymentDAO pdao = new PaymentDAO();
				pdao.createPayment(payment);
				success[0] = true;
				dialog.dispose();
			} else {
				JOptionPane.showMessageDialog(dialog, "Xử lý thanh toán thất bại");
			}
		});

		cancel.addActionListener((ActionEvent e) -> dialog.dispose());

		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		return success[0];
	}

	private void showOrderHistory() {
		JDialog dialog = new JDialog(this, "Lịch sử đơn hàng", true);
		dialog.setLayout(new BorderLayout(10, 10));
		((JComponent) dialog.getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

		String[] cols = { "Mã đơn", "Khách", "Loại", "Bàn", "Tổng", "Trạng thái", "Thời gian" };
		javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
			@Override public boolean isCellEditable(int r, int c) { return false; }
		};
		JTable table = new JTable(model);
		OrderDAO dao = new OrderDAO();
		CustomerDAO cdao = new CustomerDAO();
		for (Order o : dao.getAllOrders()) {
			Customer c = cdao.getCustomerById(o.getCustomerId());
			model.addRow(new Object[] {
				o.getOrderId(),
				c != null ? c.getName() : "N/A",
				o.getServiceType(),
				o.getTableNumber() > 0 ? o.getTableNumber() : "-",
				toVND(o.getTotalAmount()),
				o.getStatus(),
				o.getOrderTime().toString()
			});
		}
		dialog.add(new JScrollPane(table), BorderLayout.CENTER);

		JButton close = new JButton("Đóng");
		JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		south.add(close);
		dialog.add(south, BorderLayout.SOUTH);
		close.addActionListener(e -> dialog.dispose());

		dialog.setSize(720, 420);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

    private void updateOrderArea() {
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : currentOrder.getOrderItems()) {
            double total = item.getMenuItem().calculatePrice() * item.getQuantity();
            sb.append(String.format("%-20s x%-2d %s\n", 
                    item.getMenuItem().getName(), 
                    item.getQuantity(), 
                    toVND(total)));
        }
        sb.append("\nTạm tính: ").append(toVND(currentOrder.getSubtotal()));
        sb.append("\nThuế: ").append(toVND(currentOrder.getTax()));
        sb.append("\nTổng: ").append(toVND(currentOrder.getTotalAmount()));
        orderArea.setText(sb.toString());
        totalLabel.setText("Tổng cộng: " + toVND(currentOrder.getTotalAmount()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CoffeeShopSwingApp().setVisible(true));
    }
}
