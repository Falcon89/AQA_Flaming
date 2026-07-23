package com.homeassignment.ui.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UiConfig {

    public static final String BASE_URL = "https://demoqa.com";
    public static final String PRACTICE_FORM_URL = BASE_URL + "/automation-practice-form";
    public static final String WEB_TABLES_URL = BASE_URL + "/webtables";

    public static final int DEFAULT_TIMEOUT_MS = 15_000;
    public static final boolean HEADLESS = Boolean.parseBoolean(
            System.getProperty("ui.headless", "true"));

    public static final String SCREENSHOTS_DIR = "target/screenshots";
    public static final String UPLOAD_FILE = "upload/sample-resume.txt";
}
