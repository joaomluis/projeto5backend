package aor.paj.dto;

import java.io.Serializable;
import java.time.LocalDate;


public class TasksDateCompletionDto implements Serializable {
    private LocalDate completionDate;
    private int count;


    public TasksDateCompletionDto(LocalDate completionDate, int count) {
        this.completionDate = completionDate;
        this.count = count;
    }

    public TasksDateCompletionDto() {
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}