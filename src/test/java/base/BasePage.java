package base;

import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.LogHelper;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Base class for all Page Objects.
 * Provides 30+ reusable DOM interaction methods with built-in:
 * - Explicit waits (visibility, clickability, presence)
 * - Automatic retry for StaleElement and ClickIntercepted exceptions
 * - Allure step logging
 * - JavaScript execution helpers
 */
public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 500;

    public BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        PageFactory.initElements(driver, this);
    }

    // ═══════════════════════ WAIT METHODS ═══════════════════════

    protected WebElement waitForVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement waitForVisible(WebElement element, int timeoutSeconds) {
        return createWait(timeoutSeconds).until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForVisible(By locator, int timeoutSeconds) {
        return createWait(timeoutSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected boolean waitForInvisible(WebElement element) {
        return wait.until(ExpectedConditions.invisibilityOf(element));
    }

    protected boolean waitForInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitForUrlContains(String urlPart) {
        wait.until(ExpectedConditions.urlContains(urlPart));
    }

    protected void waitForTitleContains(String titlePart) {
        wait.until(ExpectedConditions.titleContains(titlePart));
    }

    // ═══════════════════════ RETRY LOGIC ═══════════════════════

    /**
     * Retries a void operation up to MAX_RETRIES times on transient failures.
     */
    private void retryOperation(Runnable operation, String operationName) {
        retrySupplier(() -> {
            operation.run();
            return null;
        }, operationName);
    }

    /**
     * Retries an operation that returns a value up to MAX_RETRIES times.
     */
    @SuppressWarnings("SameParameterValue")
    private <T> void retrySupplier(Supplier<T> operation, String operationName) {
        for (int attempt = 1; true; attempt++) {
            try {
                operation.get();
                return;
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                LogHelper.warn(operationName + " failed: " + e.getClass().getSimpleName()
                        + " — retry " + attempt + "/" + MAX_RETRIES);
                if (attempt == MAX_RETRIES) {
                    throw new RuntimeException("Failed: " + operationName
                            + " after " + MAX_RETRIES + " retries", e);
                }
                sleep();
            }
        }
    }

    // ═══════════════════════ CLICK & INPUT ═══════════════════════

    @Step("Click on element")
    protected void click(WebElement element) {
        LogHelper.info("Clicking on element: " + element);
        retryOperation(() -> waitForClickable(element).click(), "click");
    }

    @Step("Click on locator: {locator}")
    protected void click(By locator) {
        LogHelper.info("Clicking on locator: " + locator);
        retryOperation(() -> waitForClickable(locator).click(), "click");
    }

    @Step("Set text: {text}")
    protected void setText(WebElement element, String text) {
        LogHelper.info("Setting text: " + text);
        retryOperation(() -> {
            waitForVisible(element).clear();
            element.sendKeys(text);
        }, "setText");
    }

    @Step("Set text: {text} to locator: {locator}")
    protected void setText(By locator, String text) {
        LogHelper.info("Setting text: " + text + " to locator: " + locator);
        retryOperation(() -> {
            WebElement el = waitForVisible(locator);
            el.clear();
            el.sendKeys(text);
        }, "setText");
    }

    @Step("Clear text in element")
    protected void clearText(WebElement element) {
        LogHelper.info("Clearing text in element");
        waitForVisible(element).clear();
    }

    protected void pressKey(WebElement element, Keys key) {
        LogHelper.info("Pressing key: " + key.name());
        element.sendKeys(key);
    }

    protected void pressEnter(WebElement element) {
        pressKey(element, Keys.ENTER);
    }

    protected void pressTab(WebElement element) {
        pressKey(element, Keys.TAB);
    }

    // ═══════════════════════ CHECKBOX & DROPDOWN ═══════════════════════

    @Step("Set checkbox to: {checked}")
    protected void setCheckbox(WebElement checkbox) {
        LogHelper.info("Setting checkbox to: " + true);
        waitForClickable(checkbox);
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
    }

    @Step("Select dropdown by text: {text}")
    protected void selectByText(WebElement dropdown, String text) {
        LogHelper.info("Selecting dropdown by text: " + text);
        new Select(waitForVisible(dropdown)).selectByVisibleText(text);
    }

    @Step("Select dropdown by value: {value}")
    protected void selectByValue(WebElement dropdown, String value) {
        LogHelper.info("Selecting dropdown by value: " + value);
        new Select(waitForVisible(dropdown)).selectByValue(value);
    }

    @Step("Select dropdown by index: {index}")
    protected void selectByIndex(WebElement dropdown, int index) {
        LogHelper.info("Selecting dropdown by index: " + index);
        new Select(waitForVisible(dropdown)).selectByIndex(index);
    }

    protected String getSelectedText(WebElement dropdown) {
        return new Select(waitForVisible(dropdown)).getFirstSelectedOption().getText();
    }

    // ═══════════════════════ GETTERS ═══════════════════════

    protected String getText(WebElement element) {
        return waitForVisible(element).getText().trim();
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText().trim();
    }

    protected String getAttribute(WebElement element) {
        return waitForVisible(element).getAttribute("type");
    }

    protected String getCssValue(WebElement element, String property) {
        return element.getCssValue(property);
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ═══════════════════════ STATE CHECKS ═══════════════════════

    protected boolean isDisplayed(WebElement element) {
        try {
            return waitForVisible(element).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isDisplayed(By locator) {
        try {
            return waitForVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isEnabled(WebElement element) {
        try {
            return waitForVisible(element).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isSelected(WebElement element) {
        return element.isSelected();
    }

    // ═══════════════════════ MOUSE ACTIONS ═══════════════════════

    protected void hoverElement(WebElement element) {
        LogHelper.info("Hovering over element");
        new Actions(driver).moveToElement(waitForVisible(element)).perform();
    }

    protected void doubleClick(WebElement element) {
        LogHelper.info("Double clicking element");
        new Actions(driver).doubleClick(waitForClickable(element)).perform();
    }

    protected void rightClick(WebElement element) {
        LogHelper.info("Right clicking element");
        new Actions(driver).contextClick(waitForClickable(element)).perform();
    }

    protected void dragAndDrop(WebElement source, WebElement target) {
        LogHelper.info("Dragging and dropping element");
        new Actions(driver).dragAndDrop(source, target).perform();
    }

    // ═══════════════════════ JAVASCRIPT ═══════════════════════

    protected void scrollToElement(WebElement element) {
        LogHelper.info("Scrolling to element");
        executeJs("arguments[0].scrollIntoView(true);", element);
    }

    @Step("Click on element using JS")
    protected void clickByJs(WebElement element) {
        LogHelper.info("Clicking element via JS");
        executeJs("arguments[0].click();", element);
    }

    @Step("Set text: {text} using JS")
    protected void setTextByJs(WebElement element, String text) {
        LogHelper.info("Setting text via JS: " + text);
        executeJs("arguments[0].value=arguments[1];", element, text);
    }

    protected void executeJs(String script, Object... args) {
        ((JavascriptExecutor) driver).executeScript(script, args);
    }

    // ═══════════════════════ ALERTS ═══════════════════════

    @Step("Accept alert")
    protected void acceptAlert() {
        LogHelper.info("Accepting alert");
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    }

    @Step("Dismiss alert")
    protected void dismissAlert() {
        LogHelper.info("Dismissing alert");
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().dismiss();
    }

    protected String getAlertText() {
        wait.until(ExpectedConditions.alertIsPresent());
        return driver.switchTo().alert().getText();
    }

    // ═══════════════════════ FRAMES ═══════════════════════

    protected void switchToFrame(WebElement frame) {
        LogHelper.info("Switching to frame");
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
    }

    protected void switchToFrame(int index) {
        LogHelper.info("Switching to frame by index: " + index);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(index));
    }

    protected void switchToFrame(String nameOrId) {
        LogHelper.info("Switching to frame by name/id: " + nameOrId);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(nameOrId));
    }

    protected void switchToDefaultContent() {
        LogHelper.info("Switching to default content");
        driver.switchTo().defaultContent();
    }

    // ═══════════════════════ WINDOWS ═══════════════════════

    protected String getWindowHandle() {
        return driver.getWindowHandle();
    }

    protected void switchToWindow(String handle) {
        LogHelper.info("Switching to window: " + handle);
        driver.switchTo().window(handle);
    }

    protected void switchToNewWindow() {
        LogHelper.info("Switching to new window");
        String currentHandle = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(currentHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
    }

    // ═══════════════════════ ELEMENTS ═══════════════════════

    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    protected int countElements(By locator) {
        return driver.findElements(locator).size();
    }

    // ═══════════════════════ NAVIGATION ═══════════════════════

    @Step("Navigate to: {url}")
    protected void navigateTo(String url) {
        LogHelper.info("Navigating to URL: " + url);
        driver.get(url);
    }

    protected void refreshPage() {
        LogHelper.info("Refreshing page");
        driver.navigate().refresh();
    }

    protected void goBack() {
        LogHelper.info("Navigating back");
        driver.navigate().back();
    }

    protected void goForward() {
        LogHelper.info("Navigating forward");
        driver.navigate().forward();
    }

    @Step("Wait for page load to complete")
    public void waitForPageLoad() {
        LogHelper.info("Waiting for page load to complete");
        wait.until(webDriver -> Objects.equals(((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState"), "complete"));
    }

    // ═══════════════════════ UTILITY ═══════════════════════

    private WebDriverWait createWait(int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    private void sleep() {
        try {
            Thread.sleep(BasePage.RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
