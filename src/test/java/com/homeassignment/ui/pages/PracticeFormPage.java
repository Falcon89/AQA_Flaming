package com.homeassignment.ui.pages;

import com.homeassignment.ui.config.UiConfig;
import com.homeassignment.ui.data.StudentFormData;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page Object for https://demoqa.com/automation-practice-form
 *
 * <p>Covers assignment Option A: fill form, upload, date picker, dropdowns, submit + modal.</p>
 */
public class PracticeFormPage extends BasePage {

    private static final String FIRST_NAME = "#firstName";
    private static final String LAST_NAME = "#lastName";
    private static final String EMAIL = "#userEmail";
    private static final String MOBILE = "#userNumber";
    private static final String DOB_INPUT = "#dateOfBirthInput";
    private static final String SUBJECTS_INPUT = "#subjectsInput";
    private static final String UPLOAD = "#uploadPicture";
    private static final String ADDRESS = "#currentAddress";
    private static final String STATE = "#state";
    private static final String CITY = "#city";
    private static final String SUBMIT = "#submit";
    private static final String MODAL = ".modal-content";
    private static final String MODAL_TITLE = "#example-modal-sizes-title-lg";

    public PracticeFormPage(Page page) {
        super(page);
    }

    public PracticeFormPage open() {
        open(UiConfig.PRACTICE_FORM_URL);
        assertThat(page.locator(FIRST_NAME)).isVisible();
        return this;
    }

    public PracticeFormPage fillStudentDetails(StudentFormData data) {
        fill(FIRST_NAME, data.getFirstName());
        fill(LAST_NAME, data.getLastName());
        fill(EMAIL, data.getEmail());
        selectGender(data.getGender());
        fill(MOBILE, data.getMobile());
        fill(ADDRESS, data.getCurrentAddress());
        return this;
    }

    public PracticeFormPage selectDateOfBirth(int day, String month, String year) {
        clickVisible(DOB_INPUT);
        assertThat(page.locator(".react-datepicker")).isVisible();

        page.locator(".react-datepicker__month-select").selectOption(month);
        page.locator(".react-datepicker__year-select").selectOption(year);

        String dayClass = String.format(".react-datepicker__day--%03d", day);
        Locator dayCell = page.locator(dayClass + ":not(.react-datepicker__day--outside-month)").first();
        dayCell.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dayCell.click();

        assertThat(page.locator(".react-datepicker")).isHidden();
        return this;
    }

    public PracticeFormPage selectSubject(String subject) {
        Locator input = visible(SUBJECTS_INPUT);
        input.click();
        input.fill(subject);
        Locator option = page.locator(".subjects-auto-complete__option").first();
        option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        option.click();
        page.keyboard().press("Escape");
        return this;
    }

    public PracticeFormPage selectHobby(String hobby) {
        page.locator("#hobbiesWrapper label")
                .filter(new Locator.FilterOptions().setHasText(hobby))
                .click();
        return this;
    }

    public PracticeFormPage uploadFile(Path file) {
        page.locator(UPLOAD).setInputFiles(file);
        return this;
    }

    public PracticeFormPage selectStateAndCity(String state, String city) {
        dismissBlockingOverlays();

        clickVisible(STATE);
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(state)).click();
        assertThat(page.locator(STATE)).containsText(state);

        clickVisible(CITY);
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(city)).click();
        assertThat(page.locator(CITY)).containsText(city);
        return this;
    }

    public PracticeFormPage submit() {
        dismissBlockingOverlays();
        Locator submit = page.locator(SUBMIT);
        submit.scrollIntoViewIfNeeded();
        try {
            submit.click(new Locator.ClickOptions().setTimeout(5_000));
        } catch (RuntimeException e) {
            submit.evaluate("el => el.click()");
        }
        page.locator(MODAL).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertThat(page.locator(MODAL_TITLE)).hasText("Thanks for submitting the form");
        return this;
    }

    public Map<String, String> readSuccessModalRows() {
        assertThat(page.locator(MODAL)).isVisible();

        Map<String, String> rows = new LinkedHashMap<>();
        Locator tableRows = page.locator(".modal-body tbody tr");
        tableRows.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        int count = tableRows.count();
        for (int i = 0; i < count; i++) {
            Locator cells = tableRows.nth(i).locator("td");
            rows.put(cells.nth(0).innerText().trim(), cells.nth(1).innerText().trim());
        }
        return rows;
    }

    private void selectGender(String gender) {
        page.locator("#genterWrapper")
                .getByText(gender, new Locator.GetByTextOptions().setExact(true))
                .click();
    }
}
