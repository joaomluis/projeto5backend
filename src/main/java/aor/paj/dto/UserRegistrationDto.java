package aor.paj.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


public class UserRegistrationDto implements Serializable {
    private LocalDate registrationDate;
    private int userCount;


    public UserRegistrationDto(LocalDate registrationDate, int userCount) {
        this.registrationDate = registrationDate;
        this.userCount = userCount;
    }

    public UserRegistrationDto() {
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }
}