package pages;

import base.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DataHelper;

/**
 * Page Object for the CRM Login page.
 * Encapsulates all locators and business logic for login interactions.
 * Uses a fluent pattern — all action methods return {@code this} for chaining.
 */
public class LoginPage extends BasePage {

    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    @FindBy(id = "remember")
    private WebElement rememberMeCheckbox;

    @FindBy(css = "div.alert-danger")
    private WebElement errorMessage;

    private final By dashboardHeading = By.xpath("//span[normalize-space()='Dashboard']");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    // ═══════════════════════ ACTIONS ═══════════════════════

    @Step("Open Login page")
    public void open() {
        navigateTo(DataHelper.get("urls.login"));
    }

    @Step("Enter email: {email}")
    public void enterEmail(String email) {
        setText(emailInput, email);
    }

    @Step("Enter password")
    public void enterPassword(String password) {
        setText(passwordInput, password);
    }

    @Step("Click Login button")
    public void clickLogin() {
        click(loginButton);
    }

    @Step("Tick Remember Me checkbox")
    public LoginPage tickRememberMe() {
        setCheckbox(rememberMeCheckbox);
        return this;
    }

    @Step("Login with email: {email}")
    public void performLogin(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    // ═══════════════════════ ASSERTIONS / QUERIES ═══════════════════════

    @Step("Get error message text")
    public String getErrorMessage() {
        return getText(errorMessage);
    }

    @Step("Check if error message is displayed")
    public boolean isErrorMessageDisplayed() {
        return isDisplayed(errorMessage);
    }

    @Step("Check if Dashboard is displayed")
    public boolean isDashboardDisplayed() {
        return isDisplayed(dashboardHeading);
    }

    @Step("Check if password field is masked")
    public boolean isPasswordMasked() {
        return "password".equals(getAttribute(passwordInput));
    }

    @Step("Check if Login button is displayed")
    public boolean isLoginButtonDisplayed() {
        return isDisplayed(loginButton);
    }
}
