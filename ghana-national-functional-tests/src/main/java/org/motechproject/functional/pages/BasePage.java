package org.motechproject.functional.pages;

import org.motechproject.functional.util.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class BasePage<T> {
    protected JavascriptExecutor javascriptExecutor = new JavascriptExecutor();
    protected PlatformSpecificExecutor platformSpecificExecutor = new PlatformSpecificExecutor();
    protected HtmlTableParser htmlTableParser = new HtmlTableParser();
    protected DateSelector dateSelector = new DateSelector();
    protected ElementPoller elementPoller = new ElementPoller();

    protected WebDriver driver;

    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    protected T enter(WebElement webElement, String value) {
        webElement.clear();
        webElement.sendKeys(value);
        return (T) this;
    }

    public void waitForSuccessfulCompletion() {
    }
}
