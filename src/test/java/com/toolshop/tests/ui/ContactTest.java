package com.toolshop.tests.ui;

import com.toolshop.core.BaseUiTest;
import com.toolshop.data.TestData;
import com.toolshop.pages.ContactPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContactTest extends BaseUiTest {
    private ContactPage openAsGuest() {
        ContactPage contact = new ContactPage(driver());
        contact.open();
        contact.fillGuest("Test", "User", "qa.contact@example.com");
        return contact;
    }

    private Path tempFile(String suffix, String content) throws IOException {
        Path file = Files.createTempFile("attachment", suffix);
        Files.writeString(file, content);
        afterTest(() -> file.toFile().delete());
        return file;
    }

    @DataProvider
    public Object[][] subjects() {
        return new Object[][]{{"customer-service"}, {"webmaster"}, {"return"}, {"payments"}, {"warranty"}, {"status-of-order"}};
    }

    @Test(description = "CON-01: a guest can send a message for each subject", dataProvider = "subjects")
    public void guestSends(String subject) {
        ContactPage contact = openAsGuest();
        contact.fillMessage(subject, TestData.CONTACT_MESSAGE);

        contact.click(ContactPage.SUBMIT);

        expect().visible(ContactPage.CONFIRMATION);
    }

    @Test(description = "CON-02: an empty form shows every required message")
    public void required() {
        ContactPage contact = new ContactPage(driver());
        contact.open();

        contact.click(ContactPage.SUBMIT);

        expect().containsText(ContactPage.error("first-name"), "First name is required");
        expect().containsText(ContactPage.error("last-name"), "Last name is required");
        expect().containsText(ContactPage.error("email"), "Email is required");
        expect().containsText(ContactPage.error("subject"), "Subject is required");
        expect().containsText(ContactPage.error("message"), "Message is required");
    }

    @Test(description = "CON-03: a message under 50 characters is rejected")
    public void shortMessage() {
        ContactPage contact = openAsGuest();
        contact.fillMessage("webmaster", "Too short.");

        contact.click(ContactPage.SUBMIT);

        expect().containsText(ContactPage.error("message"), "minimal 50");
        expect().hidden(ContactPage.CONFIRMATION);
    }

    @Test(description = "CON-04: an invalid email is rejected")
    public void invalidEmail() {
        ContactPage contact = new ContactPage(driver());
        contact.open();
        contact.fillGuest("Test", "User", "not-an-email");
        contact.fillMessage("webmaster", TestData.CONTACT_MESSAGE);

        contact.click(ContactPage.SUBMIT);

        expect().visible(ContactPage.error("email"));
        expect().hidden(ContactPage.CONFIRMATION);
    }

    @DataProvider
    public Object[][] badAttachments() {
        return new Object[][]{{".pdf", "x"}, {".txt", "x"}};
    }

    @Test(description = "CON-05: a wrong type or non-empty attachment is rejected", dataProvider = "badAttachments")
    public void badAttachment(String suffix, String content) throws IOException {
        ContactPage contact = new ContactPage(driver());
        contact.open();

        contact.attach(tempFile(suffix, content));
        contact.click(ContactPage.SUBMIT);

        expect().visible(ContactPage.error("attachment"));
    }

    @Test(description = "CON-06: an empty text attachment is accepted")
    public void emptyAttachment() throws IOException {
        ContactPage contact = openAsGuest();
        contact.fillMessage("webmaster", TestData.CONTACT_MESSAGE);
        contact.attach(tempFile(".txt", ""));

        contact.click(ContactPage.SUBMIT);

        expect().visible(ContactPage.CONFIRMATION);
    }

    @Test(description = "CON-07: a logged-in customer does not retype name and email")
    public void loggedIn() {
        signedInCustomer();
        ContactPage contact = new ContactPage(driver());
        contact.open();

        expect().hidden(ContactPage.FIRST_NAME);
        expect().hidden(ContactPage.EMAIL);
        contact.fillMessage("customer-service", TestData.CONTACT_MESSAGE);
        contact.click(ContactPage.SUBMIT);

        expect().visible(ContactPage.CONFIRMATION);
    }
}
