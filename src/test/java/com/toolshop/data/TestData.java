package com.toolshop.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Fake but valid-format data. Never real personal data. */
public final class TestData {
    public static final String CONTACT_MESSAGE =
            "This is an automated QA message for the public practice site, please ignore it.";
    public static final String COUNTRY = "NL";
    public static final String POSTCODE = "1011AB";
    public static final String HOUSE_NUMBER = "42";

    private TestData() {
    }

    /** Unique suffix so parallel tests on the shared demo never collide. */
    public static String uniqueId() {
        return System.currentTimeMillis() + "" + ThreadLocalRandom.current().nextInt(100_000);
    }

    public record NewUser(String firstName, String lastName, String email, String password) {
        public NewUser withEmail(String value) {
            return new NewUser(firstName, lastName, value, password);
        }

        public NewUser withPassword(String value) {
            return new NewUser(firstName, lastName, email, value);
        }

        public NewUser withFirstName(String value) {
            return new NewUser(value, lastName, email, password);
        }

        /** Body for POST /users/register. */
        public Map<String, Object> payload() {
            Map<String, Object> address = new LinkedHashMap<>();
            address.put("street", "Test Street");
            address.put("house_number", HOUSE_NUMBER);
            address.put("city", "Amsterdam");
            address.put("state", "Noord-Holland");
            address.put("country", COUNTRY);
            address.put("postal_code", POSTCODE);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("first_name", firstName);
            body.put("last_name", lastName);
            body.put("dob", "1990-01-15");
            body.put("phone", "0612345678");
            body.put("email", email);
            body.put("password", password);
            body.put("address", address);
            return body;
        }
    }

    public static NewUser newUser() {
        String id = uniqueId();
        return new NewUser("Test", "User", "qa." + id + "@example.com", "Qa!" + id + "xZ");
    }

    /** Parses "$14.15" / "14.15" into a number. */
    public static double parsePrice(String text) {
        return Double.parseDouble(text.replaceAll("[^0-9.]", ""));
    }
}
