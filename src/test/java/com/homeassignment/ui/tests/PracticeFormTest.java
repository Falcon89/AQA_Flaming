package com.homeassignment.ui.tests;

import com.homeassignment.ui.base.PlaywrightBaseTest;
import com.homeassignment.ui.config.UiConfig;
import com.homeassignment.ui.data.StudentFormData;
import com.homeassignment.ui.pages.PracticeFormPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Epic("DemoQA UI")
@Feature("Practice Form")
@DisplayName("Option A: Student Registration Form")
class PracticeFormTest extends PlaywrightBaseTest {

    @Test
    @Story("Form submission")
    @DisplayName("Fill form, upload file, pick date, use dropdowns, verify success modal")
    void shouldSubmitStudentRegistrationForm() throws Exception {
        StudentFormData student = StudentFormData.builder()
                .firstName("Vasyl")
                .lastName("Kachala")
                .email("vasyl.kachala@example.com")
                .gender("Male")
                .mobile("0977777777")
                .birthDay(23)
                .birthMonth("October")
                .birthYear("1989")
                .subject("Maths")
                .hobby("Reading")
                .currentAddress("Lviv, EU")
                .state("NCR")
                .city("Delhi")
                .build();

        Path uploadFile = prepareUploadFile();

        PracticeFormPage formPage = new PracticeFormPage(page).open();

        // Fill the student registration form
        formPage.fillStudentDetails(student);

        // Select a date from the date picker
        formPage.selectDateOfBirth(student.getBirthDay(), student.getBirthMonth(), student.getBirthYear());

        // Subjects + hobby (form fields used before dropdowns/upload)
        formPage.selectSubject(student.getSubject());
        formPage.selectHobby(student.getHobby());

        // Upload a file
        formPage.uploadFile(uploadFile);

        // Choose from dropdowns (State / City)
        formPage.selectStateAndCity(student.getState(), student.getCity());

        // Submit and verify the success modal
        Map<String, String> modal = formPage.submit().readSuccessModalRows();

        assertThat(modal)
                .containsEntry("Student Name", student.getFirstName() + " " + student.getLastName())
                .containsEntry("Student Email", student.getEmail())
                .containsEntry("Gender", student.getGender())
                .containsEntry("Mobile", student.getMobile())
                .containsEntry("Date of Birth", "23 October,1989")
                .containsEntry("Subjects", student.getSubject())
                .containsEntry("Hobbies", student.getHobby())
                .containsEntry("Picture", uploadFile.getFileName().toString())
                .containsEntry("Address", student.getCurrentAddress())
                .containsEntry("State and City", student.getState() + " " + student.getCity());
    }

    private Path prepareUploadFile() throws Exception {
        Path target = Path.of("target", "test-upload", "sample-resume.txt");
        Files.createDirectories(target.getParent());

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(UiConfig.UPLOAD_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + UiConfig.UPLOAD_FILE);
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target.toAbsolutePath();
    }
}
