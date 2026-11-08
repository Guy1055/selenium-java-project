package com.toolshop.core;

/** Target environment; every value can be overridden by an environment variable. */
public final class Env {
    public static final String BASE_URL = get("BASE_URL", "https://practicesoftwaretesting.com");
    public static final String API_URL = get("API_URL", "https://api.practicesoftwaretesting.com");

    /** Admin account the site publishes for practice; public, so not a secret. */
    public static final String ADMIN_EMAIL = get("ADMIN_EMAIL", "admin@practicesoftwaretesting.com");
    public static final String ADMIN_PASSWORD = get("ADMIN_PASSWORD", "welcome01");

    /** JDBC URL for the database tier, e.g. jdbc:mariadb://localhost:3306/toolshop?user=user&password=root. */
    public static final String DB_URL = System.getenv("DB_URL");

    /** Regex for the storefront home page on whichever environment is under test. */
    public static final String HOME_URL = java.util.regex.Pattern.quote(BASE_URL) + "/?$";

    public static final String HTTPS_PROXY = System.getenv("HTTPS_PROXY");
    public static final boolean HEADLESS = System.getenv("CI") != null || System.getenv("HEADLESS") != null;
    public static final boolean UPDATE_SNAPSHOTS = System.getenv("UPDATE_SNAPSHOTS") != null;

    private Env() {
    }

    private static String get(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
