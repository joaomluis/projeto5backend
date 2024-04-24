package aor.paj.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UserRegistrationDto implements Serializable {
    private LocalDateTime registrationDate;
    private int userCount;


    public UserRegistrationDto(LocalDateTime registrationDate, int userCount) {
        this.registrationDate = registrationDate;
        this.userCount = userCount;
    }

    public UserRegistrationDto() {
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }
}