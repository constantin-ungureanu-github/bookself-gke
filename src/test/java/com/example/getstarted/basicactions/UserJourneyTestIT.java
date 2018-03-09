package com.example.getstarted.basicactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.datastore.Batch;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.extern.slf4j.Slf4j;

@RunWith(JUnit4.class)
@Slf4j
public class UserJourneyTestIT {

    private static final String TITLE = "mytitle";
    private static final String AUTHOR = "myauthor";
    private static final String PUBLISHED_DATE = "1984-02-27";
    private static final String DESCRIPTION = "mydescription";

    private static DriverService service;
    private WebDriver driver;

    @BeforeClass
    public static void setupClass() throws Exception {
        service = ChromeDriverService.createDefaultService();
        service.start();
        log.info("Started.");
    }

    @AfterClass
    public static void tearDownClass() {
        service.stop();

        final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        final Batch batch = datastore.newBatch();
        final StructuredQuery<Key> query = Query.newKeyQueryBuilder().setKind("Book5").build();
        for (final QueryResults<Key> keys = datastore.run(query); keys.hasNext();) {
            batch.delete(keys.next());
        }
        batch.submit();
    }

    @Before
    public void setup() {
        driver = new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome());
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    private WebElement checkLandingPage() throws Exception {
        final WebElement button = driver.findElement(By.cssSelector("a.btn"));
        assertEquals("Add book", button.getText());

        final WebElement heading = driver.findElement(By.cssSelector("body>.container h3"));
        assertEquals("Books", heading.getText());

        final WebElement list = driver.findElement(By.cssSelector("body>.container p"));
        assertEquals("No books found", list.getText());

        final WebElement loginButton = driver.findElement(By.linkText("Login"));
        assertTrue(null != loginButton);

        return button;
    }

    private void checkAddBookPage() throws Exception {
        final List<WebElement> inputContainers = driver.findElements(By.cssSelector("form .form-group"));
        assertTrue("Should have more than 5 inputs", inputContainers.size() > 5);
        assertEquals("First input should be Title", "Title", inputContainers.get(0).findElement(By.tagName("label")).getText());
        assertEquals("Second input should be Author", "Author", inputContainers.get(1).findElement(By.tagName("label")).getText());
        assertEquals("Third input should be Date Published", "Date Published", inputContainers.get(2).findElement(By.tagName("label")).getText());
        assertEquals("Fourth input should be Description", "Description", inputContainers.get(3).findElement(By.tagName("label")).getText());

        for (final Iterator<WebElement> iter = inputContainers.listIterator(5); iter.hasNext();) {
            final WebElement el = iter.next();
            assertTrue(el.getAttribute("class").indexOf("hidden") >= 0);
        }
    }

    private void submitForm(final String title, final String author, final String datePublished, final String description) throws Exception {
        driver.findElement(By.cssSelector("[name=title]")).sendKeys(title);
        driver.findElement(By.cssSelector("[name=author]")).sendKeys(author);
        driver.findElement(By.cssSelector("[name=publishedDate]")).sendKeys(datePublished);
        driver.findElement(By.cssSelector("[name=description]")).sendKeys(description);
        driver.findElement(By.cssSelector("button[type=submit]")).submit();
    }

    private void checkReadPage(final String title, final String author, final String datePublished, final String description) throws Exception {
        final WebElement heading = driver.findElement(By.cssSelector("h3"));
        assertEquals("Book", heading.getText());

        final List<WebElement> buttons = driver.findElements(By.cssSelector("a.btn"));
        assertEquals(2, buttons.size());
        assertEquals("Edit book", buttons.get(0).getText());
        assertEquals("Delete book", buttons.get(1).getText());
        assertTrue(driver.findElement(By.cssSelector("img.book-image")).getAttribute("src").indexOf("placekitten") > 0);
        assertTrue("Should show title", driver.findElement(By.cssSelector(".book-title")).getText().startsWith(title));
        assertEquals("Should show author", "By " + author, driver.findElement(By.cssSelector(".book-author")).getText());
        assertEquals("Should show description", description, driver.findElement(By.cssSelector(".book-description")).getText());
    }

    @Test
    public void userJourney() throws Exception {
        driver.get("http://localhost:8080");

        try {
            final WebElement button = checkLandingPage();

            button.click();

            (new WebDriverWait(driver, 10)).until((Function<? super WebDriver, Boolean>) ExpectedConditions.urlMatches(".*/create$"));

            checkAddBookPage();

            submitForm(TITLE, AUTHOR, PUBLISHED_DATE, DESCRIPTION);
            (new WebDriverWait(driver, 10)).until((Function<? super WebDriver, Boolean>) ExpectedConditions.urlMatches(".*/read\\?id=[0-9]+$"));

            checkReadPage(TITLE, AUTHOR, PUBLISHED_DATE, DESCRIPTION);

            driver.findElement(By.linkText("Login")).click();
            (new WebDriverWait(driver, 10)).until((Function<? super WebDriver, Boolean>) ExpectedConditions.urlMatches("https://accounts.google.com"));

        } catch (final Exception e) {
            System.err.println(driver.getPageSource());
            throw e;
        }
    }
}
