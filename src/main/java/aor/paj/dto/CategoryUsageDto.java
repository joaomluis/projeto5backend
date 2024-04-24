package aor.paj.dto;

import java.io.Serializable;

public class CategoryUsageDto implements Serializable {
    private String categoryName;
    private Long taskCount;

    public CategoryUsageDto(String categoryName, Long taskCount) {
        this.categoryName = categoryName;
        this.taskCount = taskCount;
    }

    public CategoryUsageDto() {
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Long taskCount) {
        this.taskCount = taskCount;
    }
}