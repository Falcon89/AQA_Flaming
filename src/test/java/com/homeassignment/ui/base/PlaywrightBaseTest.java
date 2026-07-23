package com.homeassignment.ui.base;

import com.homeassignment.ui.config.UiConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class PlaywrightBaseTest {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeEach
    void setUpBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(UiConfig.HEADLESS)
                .setArgs(java.util.List.of("--disable-dev-shm-usage")));

        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 900)
                .setIgnoreHTTPSErrors(true));
        context.setDefaultTimeout(UiConfig.DEFAULT_TIMEOUT_MS);

        page = context.newPage();
        ScreenshotOnFailureExtension.setPage(page);
    }

    @AfterEach
    void tearDownBrowser() {
        ScreenshotOnFailureExtension.clear();
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
