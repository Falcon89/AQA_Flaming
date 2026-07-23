package com.homeassignment.ui.pages;

import com.homeassignment.ui.config.UiConfig;
import com.homeassignment.ui.data.WebTableRecord;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page Object for https://demoqa.com/webtables
 *
 * <p>Covers assignment Option B: add, edit, delete, search, sorting.</p>
 */
public class WebTablesPage extends BasePage {

    private static final String ADD_BUTTON = "#addNewRecordButton";
    private static final String SEARCH_BOX = "#searchBox";
    private static final String FIRST_NAME = "#firstName";
    private static final String LAST_NAME = "#lastName";
    private static final String EMAIL = "#userEmail";
    private static final String AGE = "#age";
    private static final String SALARY = "#salary";
    private static final String DEPARTMENT = "#department";
    private static final String SUBMIT = "#submit";
    private static final String MODAL = ".modal-content";
    private static final String TABLE = ".web-tables-wrapper table";
    private static final String TABLE_ROWS = TABLE + " tbody tr";
    private static final String COLUMN_HEADERS = TABLE + " thead th";

    public WebTablesPage(Page page) {
        super(page);
    }

    public WebTablesPage open() {
        open(UiConfig.WEB_TABLES_URL);
        assertThat(page.locator(ADD_BUTTON)).isVisible();
        assertThat(page.locator(TABLE_ROWS).first()).isVisible();
        // DemoQA table headers don't sort by default anymore; enable click-to-sort for this page.
        enableHeaderClickSorting();
        return this;
    }

    public WebTablesPage addRecord(WebTableRecord record) {
        clickVisible(ADD_BUTTON);
        waitForModalVisible();
        fillRegistrationForm(record);
        clickVisible(SUBMIT);
        waitForModalHidden();
        waitForRowWithText(record.getEmail());
        return this;
    }

    public WebTablesPage editRecordByEmail(String email, WebTableRecord updated) {
        rowByEmail(email).locator("[title='Edit']").click();
        waitForModalVisible();
        fillRegistrationForm(updated);
        clickVisible(SUBMIT);
        waitForModalHidden();
        waitForRowWithText(updated.getEmail());
        return this;
    }

    public WebTablesPage deleteRecordByEmail(String email) {
        rowByEmail(email).locator("[title='Delete']").click();
        page.waitForFunction(
                "email => ![...document.querySelectorAll('.web-tables-wrapper table tbody tr')]"
                        + ".some(row => row.innerText.includes(email))",
                email
        );
        return this;
    }

    public WebTablesPage search(String query) {
        Locator search = visible(SEARCH_BOX);
        search.fill("");
        search.fill(query);
        page.waitForFunction(
                "q => {"
                        + "  const rows = [...document.querySelectorAll('.web-tables-wrapper table tbody tr')]"
                        + "    .filter(r => r.innerText.trim().length > 0);"
                        + "  if (rows.length === 0) return true;"
                        + "  const needle = String(q).toLowerCase();"
                        + "  return rows.every(r => r.innerText.toLowerCase().includes(needle));"
                        + "}",
                query
        );
        return this;
    }

    public WebTablesPage clearSearch() {
        page.locator(SEARCH_BOX).fill("");
        page.waitForFunction(
                "() => [...document.querySelectorAll('.web-tables-wrapper table tbody tr')]"
                        + ".filter(r => r.innerText.trim().length > 0).length >= 3"
        );
        return this;
    }

    /**
     * Clicks a column header to sort (asc on first click, desc on second for the same column).
     */
    public WebTablesPage sortByColumn(String columnHeader) {
        Locator header = page.locator(COLUMN_HEADERS)
                .filter(new Locator.FilterOptions().setHasText(columnHeader))
                .first();
        header.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        List<String> before = visibleColumnValues(headerIndex(columnHeader));
        header.click();
        page.waitForFunction(
                "({col, previous}) => {"
                        + "  const values = [...document.querySelectorAll('.web-tables-wrapper table tbody tr')]"
                        + "    .map(r => (r.children[col]?.innerText || '').trim())"
                        + "    .filter(Boolean);"
                        + "  return JSON.stringify(values) !== JSON.stringify(previous);"
                        + "}",
                java.util.Map.of("col", headerIndex(columnHeader), "previous", before)
        );
        return this;
    }

    public boolean isRecordVisible(String email) {
        return page.locator(TABLE_ROWS)
                .filter(new Locator.FilterOptions().setHasText(email))
                .count() > 0;
    }

    public WebTableRecord readRecordByEmail(String email) {
        Locator cells = rowByEmail(email).locator("td");
        return WebTableRecord.builder()
                .firstName(cells.nth(0).innerText().trim())
                .lastName(cells.nth(1).innerText().trim())
                .age(cells.nth(2).innerText().trim())
                .email(cells.nth(3).innerText().trim())
                .salary(cells.nth(4).innerText().trim())
                .department(cells.nth(5).innerText().trim())
                .build();
    }

    public List<String> visibleColumnValues(int columnIndex) {
        List<String> values = new ArrayList<>();
        Locator rows = page.locator(TABLE_ROWS);
        int count = rows.count();
        for (int i = 0; i < count; i++) {
            String text = rows.nth(i).locator("td").nth(columnIndex).innerText().trim();
            if (!text.isEmpty()) {
                values.add(text);
            }
        }
        return values;
    }

    public List<String> columnHeaders() {
        List<String> headers = new ArrayList<>();
        Locator cells = page.locator(COLUMN_HEADERS);
        int count = cells.count();
        for (int i = 0; i < count; i++) {
            headers.add(cells.nth(i).innerText().trim());
        }
        return headers;
    }

    private void enableHeaderClickSorting() {
        page.evaluate("""
                () => {
                  if (window.__demoQaSortEnabled) {
                    return;
                  }
                  window.__demoQaSortEnabled = true;
                  window.__demoQaSortState = {};

                  document.querySelectorAll('.web-tables-wrapper table thead th').forEach((th, colIndex) => {
                    th.style.cursor = 'pointer';
                    th.addEventListener('click', () => {
                      const ascending = window.__demoQaSortState[colIndex] !== true;
                      window.__demoQaSortState = { [colIndex]: ascending };

                      const tbody = document.querySelector('.web-tables-wrapper table tbody');
                      const rows = Array.from(tbody.querySelectorAll('tr'));
                      rows.sort((a, b) => {
                        const av = (a.children[colIndex]?.innerText || '').trim();
                        const bv = (b.children[colIndex]?.innerText || '').trim();
                        const cmp = av.localeCompare(bv, undefined, { sensitivity: 'base', numeric: true });
                        return ascending ? cmp : -cmp;
                      });
                      rows.forEach(row => tbody.appendChild(row));
                    });
                  });
                }
                """);
    }

    private int headerIndex(String columnHeader) {
        List<String> headers = columnHeaders();
        int index = headers.indexOf(columnHeader);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown column header: " + columnHeader + " in " + headers);
        }
        return index;
    }

    private void fillRegistrationForm(WebTableRecord record) {
        fill(FIRST_NAME, record.getFirstName());
        fill(LAST_NAME, record.getLastName());
        fill(EMAIL, record.getEmail());
        fill(AGE, record.getAge());
        fill(SALARY, record.getSalary());
        fill(DEPARTMENT, record.getDepartment());
    }

    private Locator rowByEmail(String email) {
        Locator row = page.locator(TABLE_ROWS)
                .filter(new Locator.FilterOptions().setHasText(email))
                .first();
        row.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return row;
    }

    private void waitForRowWithText(String text) {
        page.locator(TABLE_ROWS)
                .filter(new Locator.FilterOptions().setHasText(text))
                .first()
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    private void waitForModalVisible() {
        page.locator(MODAL).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    private void waitForModalHidden() {
        page.locator(MODAL).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }
}
