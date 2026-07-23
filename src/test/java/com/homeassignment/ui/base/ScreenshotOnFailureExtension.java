package com.homeassignment.ui.base;

import com.homeassignment.ui.config.UiConfig;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Captures a screenshot after a failed/aborted test and before {@code @AfterEach}
 * closes the browser ({@link AfterTestExecutionCallback} runs before AfterEach).
 */
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    static void setPage(Page page) {
        PAGE.set(page);
    }

    static void clear() {
        PAGE.remove();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        boolean failed = context.getExecutionException().isPresent();
        if (!failed) {
            return;
        }

        Page page = PAGE.get();
        if (page == null || page.isClosed()) {
            return;
        }

        try {
            Path dir = Paths.get(UiConfig.SCREENSHOTS_DIR);
            Files.createDirectories(dir);

            String safeName = context.getDisplayName().replaceAll("[^a-zA-Z0-9._-]", "_");
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path file = dir.resolve(safeName + "_" + stamp + ".png");

            byte[] bytes = page.screenshot(new Page.ScreenshotOptions().setPath(file).setFullPage(true));
            Allure.addAttachment(
                    "Failure screenshot",
                    "image/png",
                    new java.io.ByteArrayInputStream(bytes),
                    ".png"
            );
        } catch (Exception ignored) {
            // Best-effort; do not mask the original failure.
        }
    }
}
