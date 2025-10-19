import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order class representing customer orders
 * Manages order items, payment, and order status
 */
public class Order {
    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, READY, COMPLETED, CANCELLED
    }
    
    public enum ServiceType {
        DINE_IN, TAKEAWAY
    }
    
    private int orderId;
    private int customerId;
    private List<OrderItem> orderItems;
    private OrderStatus status;
    private ServiceType serviceType;
    private LocalDateTime orderTime;
    private LocalDateTime completionTime;
    private double subtotal;
    private double tax;
    private double discount;
    private double totalAmount;
    private int tableNumber; // -1 for takeaway
    private String specialInstructions;
    
    // Constructor
    public Order(int orderId, int customerId, ServiceType serviceType) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.orderItems = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderTime = LocalDateTime.now();
        this.tableNumber = -1;
        this.specialInstructions = "";
        this.tax = 0.0;
        this.discount = 0.0;
    }

    public Order() {
    this.orderItems = new ArrayList<>();
    this.status = OrderStatus.PENDING;
    this.orderTime = LocalDateTime.now();
    this.tableNumber = -1;
    this.specialInstructions = "";
    this.tax = 0.0;
    this.discount = 0.0;
}
    
    // Getters
    public int getOrderId() {
        return orderId;
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public List<OrderItem> getOrderItems() {
        return new ArrayList<>(orderItems);
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public ServiceType getServiceType() {
        return serviceType;
    }
    
    public LocalDateTime getOrderTime() {
        return orderTime;
    }
    
    public LocalDateTime getCompletionTime() {
        return completionTime;
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    public double getTax() {
        return tax;
    }
    
    public double getDiscount() {
        return discount;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public int getTableNumber() {
        return tableNumber;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    // Setters
    public void setStatus(OrderStatus status) {
        this.status = status;
        if (status == OrderStatus.COMPLETED) {
            this.completionTime = LocalDateTime.now();
        }
    }
    
    public void setTableNumber(int tableNumber) {
        if (serviceType == ServiceType.DINE_IN && tableNumber > 0) {
            this.tableNumber = tableNumber;
        }
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions != null ? specialInstructions : "";
    }
    
    public void setDiscount(double discount) {
        if (discount >= 0) {
            this.discount = discount;
            calculateTotal();
        }
    }
    public void setOrderId(int orderId) {
    this.orderId = orderId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }
    public void setOrderItems(List<OrderItem> items) { this.orderItems = items; }



    
    
    // Methods
    public void addItem(MenuItem menuItem, int quantity) {
        if (menuItem != null && quantity > 0) {
            // Check if item already exists in order
            for (OrderItem item : orderItems) {
                if (item.getMenuItem().getId() == menuItem.getId()) {
                    item.setQuantity(item.getQuantity() + quantity);
                    calculateTotal();
                    return;
                }
            }
            // Add new item
            orderItems.add(new OrderItem(menuItem, quantity));
            calculateTotal();
        }
    }
    
    public void removeItem(int menuItemId) {
        orderItems.removeIf(item -> item.getMenuItem().getId() == menuItemId);
        calculateTotal();
    }
    
    public void updateItemQuantity(int menuItemId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(menuItemId);
            return;
        }
        
        for (OrderItem item : orderItems) {
            if (item.getMenuItem().getId() == menuItemId) {
                item.setQuantity(newQuantity);
                calculateTotal();
                return;
            }
        }
    }
    
    public void clearOrder() {
        orderItems.clear();
        calculateTotal();
    }
    
    private void calculateTotal() {
        subtotal = orderItems.stream()
                .mapToDouble(item -> item.getMenuItem().calculatePrice() * item.getQuantity())
                .sum();
        
        // Calculate tax (10% VAT)
        tax = subtotal * 0.10;
        
        // Calculate total
        totalAmount = subtotal + tax - discount;
        
        // Ensure total is not negative
        if (totalAmount < 0) {
            totalAmount = 0;
        }
    }
    
    public boolean isEmpty() {
        return orderItems.isEmpty();
    }
    
    public int getTotalItems() {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Order #%d (Customer ID: %d)\n", orderId, customerId));
        sb.append(String.format("Status: %s | Service: %s\n", status, serviceType));
        sb.append(String.format("Order Time: %s\n", orderTime));
        
        if (serviceType == ServiceType.DINE_IN && tableNumber > 0) {
            sb.append(String.format("Table: %d\n", tableNumber));
        }
        
        sb.append("\nItems:\n");
        for (OrderItem item : orderItems) {
            double line = item.getMenuItem().calculatePrice() * item.getQuantity();
            long vnd = Math.round(line);
            sb.append(String.format("- %s x%d = %dđ\n", 
                    item.getMenuItem().getName(), 
                    item.getQuantity(), 
                    vnd));
        }
        long subtotalVnd = Math.round(subtotal);
        long taxVnd = Math.round(tax);
        long discountVnd = Math.round(discount);
        long totalVnd = Math.round(totalAmount);
        sb.append(String.format("\nTạm tính: %dđ\n", subtotalVnd));
        sb.append(String.format("Thuế (VAT 10%): %dđ\n", taxVnd));
        if (discount > 0) {
            sb.append(String.format("Giảm giá: -%dđ\n", discountVnd));
        }
        sb.append(String.format("Tổng cộng: %dđ\n", totalVnd));
        
        if (!specialInstructions.isEmpty()) {
            sb.append(String.format("Special Instructions: %s\n", specialInstructions));
        }
        
        return sb.toString();
    }
}