package com.homeassignment.ui.tests;

import com.homeassignment.ui.base.PlaywrightBaseTest;
import com.homeassignment.ui.data.WebTableRecord;
import com.homeassignment.ui.pages.WebTablesPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Epic("DemoQA UI")
@Feature("Web Tables")
@DisplayName("Option B: Web Tables")
class WebTablesTest extends PlaywrightBaseTest {

    @Test
    @Story("Add a new record")
    @DisplayName("Add a new record")
    void shouldAddNewRecord() {
        WebTableRecord record = uniqueRecord("Add");
        WebTablesPage tables = new WebTablesPage(page).open();

        tables.addRecord(record);

        assertThat(tables.isRecordVisible(record.getEmail())).isTrue();
        assertThat(tables.readRecordByEmail(record.getEmail()))
                .usingRecursiveComparison()
                .isEqualTo(record);
    }

    @Test
    @Story("Edit existing record")
    @DisplayName("Edit existing record")
    void shouldEditExistingRecord() {
        WebTableRecord original = uniqueRecord("Edit");
        WebTableRecord updated = WebTableRecord.builder()
                .firstName(original.getFirstName())
                .lastName("Updated")
                .email(original.getEmail())
                .age("31")
                .salary("6100")
                .department("Automation")
                .build();

        WebTablesPage tables = new WebTablesPage(page).open();
        tables.addRecord(original);

        tables.editRecordByEmail(original.getEmail(), updated);

        assertThat(tables.readRecordByEmail(original.getEmail()))
                .usingRecursiveComparison()
                .isEqualTo(updated);
    }

    @Test
    @Story("Delete record")
    @DisplayName("Delete record")
    void shouldDeleteRecord() {
        WebTableRecord record = uniqueRecord("Delete");
        WebTablesPage tables = new WebTablesPage(page).open();
        tables.addRecord(record);

        tables.deleteRecordByEmail(record.getEmail());

        assertThat(tables.isRecordVisible(record.getEmail())).isFalse();
    }

    @Test
    @Story("Search functionality")
    @DisplayName("Search functionality")
    void shouldSearchRecords() {
        WebTablesPage tables = new WebTablesPage(page).open();

        tables.search("Cierra");

        assertThat(tables.isRecordVisible("cierra@example.com")).isTrue();
        assertThat(tables.visibleColumnValues(0)).containsOnly("Cierra");
        assertThat(tables.isRecordVisible("alden@example.com")).isFalse();

        tables.clearSearch();
        assertThat(tables.isRecordVisible("alden@example.com")).isTrue();
        assertThat(tables.isRecordVisible("kierra@example.com")).isTrue();
    }

    @Test
    @Story("Sorting validation")
    @DisplayName("Sorting validation")
    void shouldSortByFirstName() {
        WebTablesPage tables = new WebTablesPage(page).open();

        assertThat(tables.columnHeaders())
                .contains("First Name", "Last Name", "Age", "Email", "Salary", "Department", "Action");

        List<String> original = tables.visibleColumnValues(0);
        assertThat(original).contains("Cierra", "Alden", "Kierra");

        List<String> expectedAsc = original.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        List<String> expectedDesc = new java.util.ArrayList<>(expectedAsc);
        java.util.Collections.reverse(expectedDesc);

        // First click → ascending
        tables.sortByColumn("First Name");
        assertThat(tables.visibleColumnValues(0))
                .as("First Name ascending after header click")
                .containsExactlyElementsOf(expectedAsc);

        // Second click → descending
        tables.sortByColumn("First Name");
        assertThat(tables.visibleColumnValues(0))
                .as("First Name descending after second header click")
                .containsExactlyElementsOf(expectedDesc);
    }

    private static WebTableRecord uniqueRecord(String prefix) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return WebTableRecord.builder()
                .firstName(prefix)
                .lastName("User" + id)
                .email(prefix.toLowerCase() + "." + id + "@example.com")
                .age("28")
                .salary("4800")
                .department("QA")
                .build();
    }
}
