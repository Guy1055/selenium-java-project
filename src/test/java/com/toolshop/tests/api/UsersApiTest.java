package com.toolshop.tests.api;

import com.toolshop.core.BaseApiTest;
import com.toolshop.core.Env;
import com.toolshop.core.KnownBug;
import com.toolshop.data.TestData;
import com.toolshop.data.TestData.NewUser;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.toolshop.api.ToolshopApi.expectStatus;

@Test(groups = "api")
public class UsersApiTest extends BaseApiTest {
    private static final String WRONG_PASSWORD = "Wrong!Password1";

    @Test(description = "API-USR-01: a new customer can register and log in", groups = {"api", "sanity"})
    public void registerAndLogin() {
        NewUser user = TestData.newUser();

        Response response = expectStatus(api.post("/users/register", user.payload()), 201);
        afterTest(() -> adminApi().delete("/users/" + response.jsonPath().getString("id")));

        Assert.assertEquals(response.jsonPath().getString("email"), user.email());
        Assert.assertNull(response.jsonPath().get("password"));
        Response login = expectStatus(api.post("/users/login", Map.of("email", user.email(), "password", user.password())), 200);
        Assert.assertNotNull(login.jsonPath().getString("access_token"));
        Assert.assertEquals(login.jsonPath().getString("token_type"), "bearer");
    }

    @Test(description = "API-USR-02: a duplicate email is rejected")
    public void duplicateEmail() {
        Customer customer = newCustomer();

        int status = api.post("/users/register", TestData.newUser().withEmail(customer.user().email()).payload()).statusCode();

        Assert.assertTrue(Set.of(409, 422).contains(status), "status was " + status);
    }

    @DataProvider
    public Object[][] invalidRegistrations() {
        return new Object[][]{
                {"weak password", TestData.newUser().withPassword("abc"), "password"},
                {"missing first name", TestData.newUser().withFirstName(""), "first_name"},
        };
    }

    @Test(description = "API-USR-03: registration rejects invalid data", dataProvider = "invalidRegistrations")
    public void invalidRegistration(String label, NewUser user, String field) {
        Response response = expectStatus(api.post("/users/register", user.payload()), 422);

        Assert.assertNotNull(response.jsonPath().get(field));
    }

    @Test(description = "API-USR-03b: registration rejects an invalid email")
    @KnownBug("BUG-06: the API accepts a malformed email that the UI form rejects")
    public void invalidEmail() {
        Response response = api.post("/users/register", TestData.newUser().withEmail("not-an-email").payload());
        if (response.statusCode() == 201) {
            afterTest(() -> adminApi().delete("/users/" + response.jsonPath().getString("id")));
        }

        Assert.assertEquals(response.statusCode(), 422);
    }

    @Test(description = "API-USR-04: a wrong password is unauthorized")
    public void wrongPassword() {
        Customer customer = newCustomer();

        expectStatus(api.post("/users/login", Map.of("email", customer.user().email(), "password", WRONG_PASSWORD)), 401);
    }

    @Test(description = "API-USR-05: repeated failures lock the account")
    public void lockout() {
        // Throwaway user only: running this against a shared demo account would lock it for everyone.
        Customer customer = newCustomer();
        int status = 0;
        for (int attempt = 0; attempt < 6 && status != 423; attempt++) {
            status = api.post("/users/login", Map.of("email", customer.user().email(), "password", WRONG_PASSWORD)).statusCode();
        }

        Assert.assertEquals(status, 423);
    }

    @Test(description = "API-USR-06: /users/me returns the logged-in customer")
    public void me() {
        Customer customer = newCustomer();

        Response me = customer.api().get("/users/me");

        Assert.assertEquals(me.jsonPath().getString("id"), customer.id());
        Assert.assertEquals(me.jsonPath().getString("email"), customer.user().email());
    }

    @Test(description = "API-USR-07: protected routes need a token")
    public void needsToken() {
        expectStatus(api.get("/users/me"), 401);
        expectStatus(api.get("/users"), 401);
    }

    @Test(description = "API-USR-08: an invalid token is unauthorized")
    public void invalidToken() {
        expectStatus(api.withToken("not.a.token").get("/users/me"), 401);
    }

    @Test(description = "API-USR-09: a customer cannot list all users")
    public void customerCannotList() {
        int status = newCustomer().api().get("/users").statusCode();

        Assert.assertTrue(Set.of(401, 403).contains(status), "status was " + status);
    }

    @Test(description = "API-USR-10: an admin can list and search users")
    public void adminListsUsers() {
        Customer customer = newCustomer();

        Assert.assertFalse(adminApi().get("/users").jsonPath().getList("data").isEmpty());
        List<String> found = adminApi().get("/users/search", Map.of("q", customer.user().email())).jsonPath().getList("data.id");
        Assert.assertTrue(found.contains(customer.id()));
    }

    @Test(description = "API-USR-11: an admin session reports the admin account")
    public void adminMe() {
        Assert.assertEquals(adminApi().get("/users/me").jsonPath().getString("email"), Env.ADMIN_EMAIL);
    }

    @Test(description = "API-USR-12: a customer can update their own record")
    public void updateOwnRecord() {
        Customer customer = newCustomer();
        Map<String, Object> details = new LinkedHashMap<>(customer.user().payload());
        details.remove("password");
        details.put("first_name", "Renamed");

        expectStatus(customer.api().put("/users/" + customer.id(), details), 200);
        Assert.assertEquals(customer.api().get("/users/" + customer.id()).jsonPath().getString("first_name"), "Renamed");

        expectStatus(customer.api().patch("/users/" + customer.id(), Map.of("first_name", "Patched")), 200);
        Assert.assertEquals(customer.api().get("/users/" + customer.id()).jsonPath().getString("first_name"), "Patched");
    }

    @Test(description = "API-USR-13: a customer cannot read another user")
    public void cannotReadOtherUser() {
        Customer customer = newCustomer();
        List<String> ids = adminApi().get("/users").jsonPath().getList("data.id");
        String other = ids.stream().filter(id -> !id.equals(customer.id())).findFirst().orElseThrow();

        expectStatus(customer.api().get("/users/" + other), 404);
    }

    @Test(description = "API-USR-14: a customer cannot delete users")
    public void customerCannotDelete() {
        Customer customer = newCustomer();

        int status = customer.api().delete("/users/" + customer.id()).statusCode();

        Assert.assertTrue(Set.of(401, 403).contains(status), "status was " + status);
    }

    @Test(description = "API-USR-15: an admin can delete a user without orders")
    public void adminDeletes() {
        String id = api.register(TestData.newUser());

        expectStatus(adminApi().delete("/users/" + id), 204);
        expectStatus(adminApi().get("/users/" + id), 404);
    }

    private Map<String, String> passwordChange(String current, String next) {
        return Map.of("current_password", current, "new_password", next, "new_password_confirmation", next);
    }

    @Test(description = "API-USR-16: changing the password works with the right current one")
    public void changePassword() {
        Customer customer = newCustomer();
        String next = customer.user().password() + "N1";

        expectStatus(customer.api().post("/users/change-password", passwordChange(customer.user().password(), next)), 200);

        expectStatus(api.post("/users/login", Map.of("email", customer.user().email(), "password", next)), 200);
    }

    @Test(description = "API-USR-17: changing the password fails with a wrong current one")
    public void changePasswordWrongCurrent() {
        Customer customer = newCustomer();

        int status = customer.api().post("/users/change-password",
                passwordChange("Not!TheCurrent1", customer.user().password() + "N1")).statusCode();

        Assert.assertTrue(status >= 400, "status was " + status);
    }

    @Test(description = "API-USR-18: forgot password accepts a registered email")
    public void forgotPassword() {
        // Resets the password, so it must only ever target a user the test created.
        expectStatus(api.post("/users/forgot-password", Map.of("email", newCustomer().user().email())), 200);
    }

    @Test(description = "API-USR-19: logout invalidates the token")
    public void logout() {
        Customer customer = newCustomer();

        expectStatus(customer.api().get("/users/logout"), 200);

        expectStatus(customer.api().get("/users/me"), 401);
    }

    @Test(description = "API-USR-20: a token can be refreshed")
    public void refresh() {
        Customer customer = newCustomer();

        String fresh = expectStatus(customer.api().get("/users/refresh"), 200).jsonPath().getString("access_token");

        expectStatus(api.withToken(fresh).get("/users/me"), 200);
    }

    @Test(description = "API-TOTP-01: setup returns a secret and an otpauth URL")
    public void totpSetup() {
        Response response = expectStatus(newCustomer().api().post("/totp/setup"), 200);

        Assert.assertNotNull(response.jsonPath().getString("secret"));
        Assert.assertTrue(response.jsonPath().getString("qrCodeUrl").contains("otpauth://totp/"));
    }

    @Test(description = "API-TOTP-02: a wrong code is rejected")
    public void totpWrongCode() {
        Customer customer = newCustomer();
        customer.api().post("/totp/setup");

        Response response = expectStatus(customer.api().post("/totp/verify",
                Map.of("totp", "000000", "access_token", customer.token())), 400);

        Assert.assertEquals(response.jsonPath().getString("error"), "Invalid TOTP");
    }

    @Test(description = "API-TOTP-03: setup needs a token")
    public void totpNeedsToken() {
        expectStatus(api.post("/totp/setup"), 401);
    }

    @DataProvider
    public Object[][] reports() {
        return new Object[][]{
                {"total-sales-per-country", "total_sales"},
                {"top10-purchased-products", "name"},
                {"top10-best-selling-categories", "category_name"},
                {"total-sales-of-years", "year"},
                {"customers-by-country", "amount"},
        };
    }

    @Test(description = "API-RPT-01: each report returns rows", dataProvider = "reports")
    public void report(String report, String key) {
        Response response = expectStatus(adminApi().get("/reports/" + report), 200);

        List<Map<String, Object>> rows = response.jsonPath().getList("$");
        if (!rows.isEmpty()) {
            Assert.assertTrue(rows.get(0).containsKey(key), rows.get(0).toString());
        }
    }

    @DataProvider
    public Object[][] yearReports() {
        return new Object[][]{{"average-sales-per-month"}, {"average-sales-per-week"}};
    }

    @Test(description = "API-RPT-02: yearly reports accept a year", dataProvider = "yearReports")
    public void yearReport(String report) {
        Response response = expectStatus(adminApi().get("/reports/" + report, Map.of("year", Year.now().getValue())), 200);

        Assert.assertNotNull(response.jsonPath().getList("$"));
    }

    @Test(description = "API-RPT-03: reports need an admin")
    public void reportsNeedAdmin() {
        expectStatus(api.get("/reports/customers-by-country"), 401);

        int status = newCustomer().api().get("/reports/customers-by-country").statusCode();
        Assert.assertTrue(Set.of(401, 403).contains(status), "status was " + status);
    }
}
