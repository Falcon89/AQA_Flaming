package com.homeassignment.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Shared helpers for DemoQA pages (ads overlays, waits, safe clicks).
 */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected void open(String url) {
        page.navigate(url);
        page.waitForLoadState();
        dismissBlockingOverlays();
    }

    /**
     * DemoQA often shows fixed banners/ads that intercept clicks.
     */
    protected void dismissBlockingOverlays() {
        page.evaluate("""
                () => {
                  document.querySelectorAll('#fixedban, .Advertisement, iframe[id^="google_ads"], #adplus-anchor')
                    .forEach(el => el.remove());
                  const footer = document.getElementById('footer');
                  if (footer) footer.style.display = 'none';
                }
                """);
    }

    protected Locator visible(String selector) {
        Locator locator = page.locator(selector);
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return locator;
    }

    protected void clickVisible(String selector) {
        Locator locator = visible(selector);
        locator.scrollIntoViewIfNeeded();
        try {
            locator.click(new Locator.ClickOptions().setTimeout(5_000));
        } catch (RuntimeException e) {
            // Fallback when an ad/footer still intercepts the pointer
            locator.evaluate("el => el.click()");
        }
    }

    protected void fill(String selector, String value) {
        Locator locator = visible(selector);
        locator.scrollIntoViewIfNeeded();
        locator.fill(value);
    }
}
