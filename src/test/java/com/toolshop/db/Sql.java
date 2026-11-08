package com.toolshop.db;

/**
 * SQL for the database tier, written against the migrations of the open-source Toolshop project
 * (sprint5/API/database/migrations). Not executed against the public demo, which has no DB access.
 * The Playwright project keeps an identical copy so both suites check the same things.
 */
public final class Sql {
    public static final String USER_BY_EMAIL = "SELECT id, first_name, email, password, role, enabled, failed_login_attempts FROM users WHERE email = ?";
    public static final String INVOICE_BY_ID = "SELECT id, invoice_number, total, subtotal, status, user_id FROM invoices WHERE id = ?";
    public static final String INVOICE_ITEMS = "SELECT unit_price, quantity, product_id FROM invoice_items WHERE invoice_id = ?";
    public static final String CART_ITEMS = "SELECT product_id, quantity FROM cart_items WHERE cart_id = ?";
    public static final String FAVORITE = "SELECT id FROM favorites WHERE user_id = ? AND product_id = ?";
    public static final String CONTACT_REQUEST = "SELECT subject, message, status, user_id FROM contact_requests WHERE id = ?";
    public static final String BRAND_BY_ID = "SELECT id, name, slug FROM brands WHERE id = ?";

    // Data integrity: every query below should return no rows.
    public static final String PRODUCTS_WITHOUT_RELATIONS = """
            SELECT p.id FROM products p
            LEFT JOIN brands b ON b.id = p.brand_id
            LEFT JOIN categories c ON c.id = p.category_id
            LEFT JOIN product_images i ON i.id = p.product_image_id
            WHERE b.id IS NULL OR c.id IS NULL OR i.id IS NULL""";
    public static final String ORPHAN_INVOICE_ITEMS = """
            SELECT ii.id FROM invoice_items ii
            LEFT JOIN invoices inv ON inv.id = ii.invoice_id
            WHERE inv.id IS NULL""";
    public static final String ORPHAN_CART_ITEMS = """
            SELECT ci.id FROM cart_items ci
            LEFT JOIN carts c ON c.id = ci.cart_id
            WHERE c.id IS NULL""";
    public static final String DUPLICATE_EMAILS = "SELECT email FROM users GROUP BY email HAVING COUNT(*) > 1";
    public static final String DUPLICATE_BRAND_SLUGS = "SELECT slug FROM brands GROUP BY slug HAVING COUNT(*) > 1";
    public static final String DUPLICATE_CATEGORY_SLUGS = "SELECT slug FROM categories GROUP BY slug HAVING COUNT(*) > 1";
    public static final String DUPLICATE_FAVORITES = "SELECT user_id, product_id FROM favorites GROUP BY user_id, product_id HAVING COUNT(*) > 1";
    public static final String NEGATIVE_PRICES = "SELECT id FROM products WHERE price < 0";
    public static final String NEGATIVE_STOCK = "SELECT id FROM products WHERE stock < 0";
    public static final String UNKNOWN_ROLES = "SELECT id FROM users WHERE role NOT IN ('user', 'admin')";
    public static final String CATEGORIES_WITH_MISSING_PARENT = """
            SELECT c.id FROM categories c
            LEFT JOIN categories p ON p.id = c.parent_id
            WHERE c.parent_id IS NOT NULL AND p.id IS NULL""";

    private Sql() {
    }
}
