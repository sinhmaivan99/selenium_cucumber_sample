package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigManager;
import utils.LogHelper;

import java.time.Duration;

/**
 * Thread-safe WebDriver factory.
 * Manages browser lifecycle using ThreadLocal for parallel execution support.
 * Replace the previous Strategy pattern (ChromeManager/EdgeManager/FirefoxManager)
 * with a simpler enum-based approach.
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> WAIT = new ThreadLocal<>();

    private DriverFactory() {
        // Utility class — prevent instantiation
    }

    /** Supported browser types. */
    public enum BrowserType {
        CHROME, FIREFOX, EDGE
    }

    /**
     * Returns the WebDriver for the current thread, initializing if necessary.
     */
    public static WebDriver getDriver() {
        if (DRIVER.get() == null) {
            initDriver();
        }
        return DRIVER.get();
    }

    /**
     * Returns the WebDriverWait for the current thread, initializing if necessary.
     */
    public static WebDriverWait getWait() {
        if (WAIT.get() == null) {
            initDriver();
        }
        return WAIT.get();
    }

    /**
     * Quits the WebDriver and cleans up ThreadLocal resources for the current thread.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
            WAIT.remove();
            LogHelper.info("Driver quit successfully");
        }
    }

    // ─── Private helpers ────────────────────────────────────────────────

    private static void initDriver() {
        if (DRIVER.get() != null) return;

        BrowserType browser = parseBrowser(ConfigManager.get("browser"));
        boolean headless = ConfigManager.getBoolean("headless");
        LogHelper.info("Initializing " + browser + " (headless=" + headless + ")");

        WebDriver driver = createBrowser(browser, headless);

        int implicitWait = ConfigManager.getInt("implicit.wait");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().window().maximize();

        DRIVER.set(driver);

        int explicitWait = ConfigManager.getInt("explicit.wait");
        WAIT.set(new WebDriverWait(driver, Duration.ofSeconds(explicitWait)));
    }

    private static WebDriver createBrowser(BrowserType type, boolean headless) {
        return switch (type) {
            case CHROME -> {
                ChromeOptions opts = new ChromeOptions();
                if (headless) opts.addArguments("--headless=new");
                opts.addArguments("--disable-notifications", "--remote-allow-origins=*");
                yield new ChromeDriver(opts);
            }
            case FIREFOX -> {
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("-headless");
                yield new FirefoxDriver(opts);
            }
            case EDGE -> {
                EdgeOptions opts = new EdgeOptions();
                if (headless) opts.addArguments("--headless=new");
                opts.addArguments("--disable-notifications", "--remote-allow-origins=*");
                yield new EdgeDriver(opts);
            }
        };
    }

    private static BrowserType parseBrowser(String name) {
        if (name == null || name.isBlank()) {
            LogHelper.warn("Browser not configured, defaulting to CHROME");
            return BrowserType.CHROME;
        }
        try {
            return BrowserType.valueOf(name.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            LogHelper.warn("Unknown browser '" + name + "', defaulting to CHROME");
            return BrowserType.CHROME;
        }
    }
}
