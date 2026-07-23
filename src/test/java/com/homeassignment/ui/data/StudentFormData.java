package com.homeassignment.ui.data;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StudentFormData {
    String firstName;
    String lastName;
    String email;
    String gender;
    String mobile;
    int birthDay;
    String birthMonth;
    String birthYear;
    String subject;
    String hobby;
    String currentAddress;
    String state;
    String city;
}
