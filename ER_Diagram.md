# ER Diagram - Coffee Shop Management System

## Entity Relationship Diagram

```mermaid
erDiagram
    CUSTOMERS {
        int customer_id PK
        varchar name
        varchar email UK
        varchar phone_number
        decimal loyalty_points
        timestamp registration_date
        timestamp created_at
        timestamp updated_at
    }

    TABLES {
        int table_number PK
        int capacity
        varchar status
        int current_customer_id FK
        timestamp occupied_since
        timestamp reserved_until
        text notes
        timestamp created_at
        timestamp updated_at
    }

    MENU_ITEMS {
        int id PK
        varchar name UK
        text description
        decimal base_price
        varchar category
        varchar item_type
        varchar coffee_type
        boolean is_available
        timestamp created_at
        timestamp updated_at
    }

    ORDERS {
        int order_id PK
        int customer_id FK
        varchar status
        varchar service_type
        int table_number FK
        decimal subtotal
        decimal tax
        decimal discount
        decimal total_amount
        text special_instructions
        timestamp order_time
        timestamp completion_time
        timestamp created_at
        timestamp updated_at
    }

    ORDER_ITEMS {
        int order_item_id PK
        int order_id FK
        int menu_item_id FK
        int quantity
        decimal unit_price
        decimal total_price
        text customizations
        varchar size
        boolean is_hot
        timestamp created_at
    }

    PAYMENTS {
        int payment_id PK
        int order_id FK
        varchar payment_method
        varchar status
        decimal amount
        decimal amount_paid
        decimal change_given
        varchar transaction_reference
        varchar card_last_four_digits
        text failure_reason
        timestamp payment_time
        timestamp created_at
        timestamp updated_at
    }

    INGREDIENTS {
        int ingredient_id PK
        varchar name
        text description
        varchar unit
        decimal current_stock
        decimal minimum_stock
        decimal maximum_stock
        decimal cost_per_unit
        date expiration_date
        varchar supplier
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }

    MENU_ITEM_INGREDIENTS {
        int menu_item_id PK,FK
        int ingredient_id PK,FK
        decimal quantity_required
    }

    %% Relationships
    CUSTOMERS ||--o{ ORDERS : "places"
    CUSTOMERS ||--o{ TABLES : "occupies"
    
    TABLES ||--o{ ORDERS : "serves"
    
    ORDERS ||--o{ ORDER_ITEMS : "contains"
    ORDERS ||--o{ PAYMENTS : "paid_by"
    
    MENU_ITEMS ||--o{ ORDER_ITEMS : "ordered_as"
    MENU_ITEMS ||--o{ MENU_ITEM_INGREDIENTS : "requires"
    
    INGREDIENTS ||--o{ MENU_ITEM_INGREDIENTS : "used_in"
```

## Database Schema Overview

### Core Entities

1. **CUSTOMERS** - Thông tin khách hàng và điểm tích lũy
2. **TABLES** - Quản lý bàn và trạng thái sử dụng
3. **MENU_ITEMS** - Danh sách món ăn và đồ uống
4. **ORDERS** - Đơn hàng của khách hàng
5. **ORDER_ITEMS** - Chi tiết từng món trong đơn hàng
6. **PAYMENTS** - Thông tin thanh toán
7. **INGREDIENTS** - Nguyên liệu và kho hàng
8. **MENU_ITEM_INGREDIENTS** - Quan hệ nhiều-nhiều giữa món ăn và nguyên liệu

### Key Relationships

- **One-to-Many**: Một khách hàng có thể có nhiều đơn hàng
- **One-to-Many**: Một bàn có thể phục vụ nhiều đơn hàng
- **One-to-Many**: Một đơn hàng có nhiều món ăn
- **One-to-Many**: Một đơn hàng có thể có nhiều thanh toán
- **Many-to-Many**: Một món ăn cần nhiều nguyên liệu, một nguyên liệu có thể dùng cho nhiều món

### Business Rules

1. **Khách hàng**: Email phải duy nhất
2. **Bàn**: Mỗi bàn có thể có một khách hàng hiện tại
3. **Đơn hàng**: Phải có khách hàng và loại dịch vụ
4. **Thanh toán**: Mỗi đơn hàng có thể có nhiều phương thức thanh toán
5. **Nguyên liệu**: Theo dõi tồn kho tối thiểu và tối đa

### Views Available

1. **table_status_view** - Trạng thái hiện tại của các bàn
2. **order_summary_view** - Tóm tắt đơn hàng với thông tin khách hàng
3. **low_stock_ingredients_view** - Nguyên liệu sắp hết hàng
4. **menu_items_with_ingredients_view** - Món ăn với yêu cầu nguyên liệu

## MySQL Specific Features

- **AUTO_INCREMENT** cho primary keys
- **ENGINE=InnoDB** cho foreign key constraints
- **Triggers** để tự động cập nhật timestamp
- **Indexes** để tối ưu hiệu suất truy vấn
- **Views** để đơn giản hóa các truy vấn phức tạp

## Usage Notes

- Database được thiết kế cho MySQL với hỗ trợ SQLite
- Tất cả foreign keys có constraints để đảm bảo tính toàn vẹn dữ liệu
- Timestamps tự động cập nhật khi có thay đổi
- Có sẵn dữ liệu mẫu để test và demo
