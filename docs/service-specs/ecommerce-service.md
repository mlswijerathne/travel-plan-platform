# E-Commerce Service Specification

> **Service Name:** ecommerce-service
> **Port:** 8091
> **Package:** `com.travelplan.ecommerce`
> **Database Schema:** `ecommerce`
> **Version:** 1.0.0-SNAPSHOT

---

## 1. Service Overview

### Responsibility

The E-Commerce Service manages a simplified online marketplace for travel-related products and souvenirs. It provides product catalog management, shopping cart functionality, and order creation (MVP scope — no payment gateway integration; university project constraint).

### Key Features

- Product catalog management (CRUD) by admin
- Product browsing and search by category and keyword
- Shopping cart operations (add, update, remove items)
- Order creation from cart with shipping address
- Order status tracking
- Stock quantity management
- Order history for tourists

### Dependencies

| Dependency | Purpose |
|---|---|
| `common-lib` | Shared DTOs, JWT filter, Security config, exception handling |
| Supabase Auth | JWT-based authentication |
| Eureka Discovery Server | Service registration and discovery |
| PostgreSQL (Supabase) | Persistent storage |
| Flyway | Database migration management |

---

## 2. API Endpoints

### 2.1 Create Product

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/products` |
| **Description** | Creates a new product listing. Admin-only. |
| **Auth** | Bearer JWT — Role: `ADMIN` |

**Request Body:**

```json
{
  "name": "Ceylon Tea Gift Box",
  "description": "Premium collection of 6 Sri Lankan tea varieties: black, green, white, oolong, herbal, and spiced chai.",
  "category": "SOUVENIRS",
  "price": 35.00,
  "stockQuantity": 100,
  "images": [
    "https://storage.example.com/products/tea-box-1.jpg",
    "https://storage.example.com/products/tea-box-2.jpg"
  ]
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `name` | String | Yes | `@NotBlank`, max 255 chars |
| `description` | String | No | Free text |
| `category` | String | No | Max 100 chars |
| `price` | BigDecimal | Yes | Min 0.01 |
| `stockQuantity` | Integer | No | Min 0, default: 0 |
| `images` | String[] | No | Image URL array |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "name": "Ceylon Tea Gift Box",
    "description": "Premium collection of 6 Sri Lankan tea varieties...",
    "category": "SOUVENIRS",
    "price": 35.00,
    "stockQuantity": 100,
    "images": ["https://storage.example.com/products/tea-box-1.jpg"],
    "isActive": true,
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:00Z"
  }
}
```

---

### 2.2 Get Product by ID

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/products/{id}` |
| **Description** | Retrieves product details. |
| **Auth** | Bearer JWT |

---

### 2.3 Browse Products

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/products` |
| **Description** | Search and filter products with pagination. |
| **Auth** | Bearer JWT |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `category` | String | No | Filter by category |
| `query` | String | No | Free-text search (name, description) |
| `minPrice` | BigDecimal | No | Minimum price |
| `maxPrice` | BigDecimal | No | Maximum price |
| `inStock` | Boolean | No | Only in-stock products |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 10) |
| `sort` | String | No | Sort field (e.g., `price,asc`) |

---

### 2.4 Update Product

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/products/{id}` |
| **Description** | Updates product details and stock. |
| **Auth** | Bearer JWT — Role: `ADMIN` |

---

### 2.5 Delete Product (Soft Delete)

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/products/{id}` |
| **Description** | Soft-deletes a product. |
| **Auth** | Bearer JWT — Role: `ADMIN` |

**Response:** `204 No Content`

---

### 2.6 Create Order

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/orders` |
| **Description** | Creates an order from selected products. Decrements stock quantities. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Request Body:**

```json
{
  "shippingAddress": "42 Temple Road, Colombo 07, Sri Lanka",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 5,
      "quantity": 1
    }
  ]
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `shippingAddress` | String | No | Free text |
| `items` | Array | Yes | `@NotEmpty`, min 1 item |
| `items[].productId` | Long | Yes | Must reference active product |
| `items[].quantity` | Integer | Yes | Min 1 |

**Response:** `201 Created`

```json
{
  "data": {
    "id": 1,
    "touristId": "tourist-uuid",
    "status": "PENDING",
    "totalAmount": 105.00,
    "shippingAddress": "42 Temple Road, Colombo 07, Sri Lanka",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Ceylon Tea Gift Box",
        "quantity": 2,
        "unitPrice": 35.00,
        "subtotal": 70.00
      },
      {
        "id": 2,
        "productId": 5,
        "productName": "Handwoven Batik Scarf",
        "quantity": 1,
        "unitPrice": 35.00,
        "subtotal": 35.00
      }
    ],
    "createdAt": "2026-02-24T10:00:00Z",
    "updatedAt": "2026-02-24T10:00:00Z"
  }
}
```

---

### 2.7 Get Order by ID

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/orders/{id}` |
| **Description** | Retrieves order details. Tourist can only access own orders. |
| **Auth** | Bearer JWT |

---

### 2.8 Get Tourist Orders

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/orders` |
| **Description** | Lists the authenticated tourist's order history. |
| **Auth** | Bearer JWT — Role: `TOURIST` |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `status` | String | No | Filter: `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED` |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 10) |

---

### 2.9 Update Order Status

| Field | Value |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/orders/{id}/status` |
| **Description** | Updates order status (admin operation). |
| **Auth** | Bearer JWT — Role: `ADMIN` |

**Request Body:**

```json
{
  "status": "SHIPPED"
}
```

---

## 3. Data Model Schema

### 3.1 `products` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `name` | `VARCHAR(255)` | No | — | Product name |
| `description` | `TEXT` | Yes | — | Product description |
| `category` | `VARCHAR(100)` | Yes | — | Product category |
| `price` | `DECIMAL(10,2)` | No | — | Unit price (USD) |
| `stock_quantity` | `INT` | Yes | `0` | Available stock |
| `images` | `TEXT[]` | Yes | — | Image URL array |
| `is_active` | `BOOLEAN` | Yes | `true` | Soft-delete flag |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last modification |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_products_category` | `category` | Category-based filtering |
| `idx_products_is_active` | `is_active` | Active product queries |

### 3.2 `orders` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `tourist_id` | `VARCHAR(255)` | No | — | Supabase UID |
| `status` | `VARCHAR(50)` | No | `PENDING` | Order status |
| `total_amount` | `DECIMAL(12,2)` | No | — | Order total |
| `shipping_address` | `TEXT` | Yes | — | Delivery address |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Order placed |
| `updated_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Last status change |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_orders_tourist_id` | `tourist_id` | Tourist order history |

### 3.3 `order_items` Table

| Column | Type | Nullable | Default | Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto-increment | Primary key |
| `order_id` | `BIGINT` | No | — | FK → `orders.id` (cascade delete) |
| `product_id` | `BIGINT` | No | — | FK → `products.id` |
| `quantity` | `INT` | No | — | Quantity ordered |
| `unit_price` | `DECIMAL(10,2)` | No | — | Price at time of order |
| `subtotal` | `DECIMAL(10,2)` | No | — | `unitPrice × quantity` |
| `created_at` | `TIMESTAMPTZ` | Yes | `CURRENT_TIMESTAMP` | Record creation |

**Indexes:**

| Index | Column(s) | Purpose |
|---|---|---|
| `idx_order_items_order_id` | `order_id` | Items per order |

### Relationships

```
products ──────────────── (referenced by) order_items
orders (1) ──── (1..*) order_items
```

---

## 4. User Input Requirements

### Product Creation

| Field | Validation Rules |
|---|---|
| `name` | Required. 1–255 characters. |
| `price` | Required. Decimal > 0. |
| `category` | Optional. One of: `SOUVENIRS`, `CLOTHING`, `FOOD`, `ACCESSORIES`, `BOOKS`, `CRAFTS`. |
| `stockQuantity` | Optional. Integer >= 0. |

### Order Creation

| Field | Validation Rules |
|---|---|
| `items` | Required. At least 1 item. |
| `items[].productId` | Required. Must reference an active product. |
| `items[].quantity` | Required. Integer >= 1. Must not exceed available stock. |

### Business Constraints

- Stock is decremented atomically when an order is placed.
- If any product has insufficient stock, the entire order fails.
- `unit_price` is captured at order time (price snapshot), so future price changes don't affect existing orders.
- No payment processing (MVP constraint) — orders are created in `PENDING` status.
- Only admin can transition order status (`PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED`).

---

## 5. Inter-Service Communication

### Inbound

| Consumer | Endpoint | Purpose |
|---|---|---|
| Frontend | `/api/products/**`, `/api/orders/**` (via gateway) | Tourist browsing and ordering |

### Outbound

None. The E-Commerce Service is a standalone, leaf service.

### Communication Method

- **Gateway Routes:** `/api/products/**` and `/api/orders/**` → `lb://ecommerce-service`
- No Feign clients or SQS integration in MVP.

---

## 6. Security Considerations

### Authentication

- **Method:** Supabase JWT (HS256)
- **Public Endpoints:** `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`

### Role-Based Access Control

| Role | Permissions |
|---|---|
| `TOURIST` | Browse products; create, view own orders |
| `ADMIN` | Full CRUD on products; manage all orders and status transitions |

---

## 7. Error Handling Standard

### Common Error Codes

| HTTP Status | Condition |
|---|---|
| `400` | Invalid input, insufficient stock, empty order |
| `401` | Missing or invalid JWT |
| `403` | Non-admin attempting product CRUD or status update |
| `404` | Product or order not found |
| `409` | Stock conflict (concurrent order depleted stock) |
| `500` | Internal server error |

---

## 8. Example Request & Response

### Browse Products by Category

**Request:**

```bash
curl -X GET "http://localhost:8091/api/products?category=SOUVENIRS&inStock=true&page=0&size=5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

**Response:** `200 OK`

```json
{
  "data": [
    {
      "id": 1,
      "name": "Ceylon Tea Gift Box",
      "description": "Premium collection of 6 Sri Lankan tea varieties.",
      "category": "SOUVENIRS",
      "price": 35.00,
      "stockQuantity": 98,
      "images": ["https://storage.example.com/products/tea-box-1.jpg"],
      "isActive": true
    },
    {
      "id": 3,
      "name": "Wooden Elephant Carving",
      "description": "Hand-carved traditional Sri Lankan elephant figurine.",
      "category": "SOUVENIRS",
      "price": 45.00,
      "stockQuantity": 25,
      "images": ["https://storage.example.com/products/elephant.jpg"],
      "isActive": true
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 5,
    "totalItems": 2,
    "totalPages": 1
  }
}
```

### Place an Order

**Request:**

```bash
curl -X POST http://localhost:8091/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "shippingAddress": "42 Temple Road, Colombo 07, Sri Lanka",
    "items": [
      { "productId": 1, "quantity": 2 },
      { "productId": 3, "quantity": 1 }
    ]
  }'
```

**Response:** `201 Created`

```json
{
  "data": {
    "id": 10,
    "touristId": "tourist-uuid",
    "status": "PENDING",
    "totalAmount": 115.00,
    "shippingAddress": "42 Temple Road, Colombo 07, Sri Lanka",
    "items": [
      { "id": 1, "productId": 1, "quantity": 2, "unitPrice": 35.00, "subtotal": 70.00 },
      { "id": 2, "productId": 3, "quantity": 1, "unitPrice": 45.00, "subtotal": 45.00 }
    ],
    "createdAt": "2026-02-24T14:00:00Z"
  }
}
```
