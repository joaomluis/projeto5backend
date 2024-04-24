package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.CategoryUsageDto;
import aor.paj.dto.UserRegistrationDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class DashboardBean {

    @EJB
    UserDao userDao;
    @EJB
    TaskDao taskDao;
    @EJB
    CategoryDao categoryDao;

public DashboardBean(){
        userDao = new UserDao();
        taskDao = new TaskDao();
        categoryDao = new CategoryDao();
    }

    public JsonObject getUsersDataAsJson(String token) {
        JsonObject taskCounts = null;

        try {
            UserEntity userEntity = userDao.findUserByToken(token);

            if (userEntity != null) {
                List<UserRegistrationDto> userRegistrationList = countUsersByRegistrationDate();
                JsonArrayBuilder userRegistrationArrayBuilder = Json.createArrayBuilder();

                for (UserRegistrationDto userRegistration : userRegistrationList) {
                    userRegistrationArrayBuilder.add(Json.createObjectBuilder()
                            .add("registrationDate", userRegistration.getRegistrationDate().toString())
                            .add("count", userRegistration.getUserCount()));
                }

                JsonArray userRegistrationArray = userRegistrationArrayBuilder.build();


                taskCounts = Json.createObjectBuilder()
                        .add("totalUsers", countUsers())
                        .add("activeUsers", countActiveUsers())
                        .add("inactiveUsers", countInactiveUsers())
                        .add("userRegistration", userRegistrationArray)
                        .build();
            }

        } catch (NullPointerException e) {
            System.out.println("Caught a NullPointerException: " + e.getMessage());
        }

        return taskCounts;
    }

    private int countUsers() {
        return userDao.countUsers();
    }

    private int countActiveUsers() {
        return userDao.countActiveUsers();
    }

    private int countInactiveUsers() {
        return userDao.countInactiveUsers();
    }

    public JsonObject getTasksDataAsJson(String token) {
        JsonObject taskCounts = null;

        try {
            UserEntity userEntity = userDao.findUserByToken(token);

            if (userEntity != null) {
                List<CategoryUsageDto> categoryUsageList = findCategoriesOrderedByUsage();
                JsonArrayBuilder categoryUsageArrayBuilder = Json.createArrayBuilder();

                for (CategoryUsageDto categoryUsage : categoryUsageList) {
                    categoryUsageArrayBuilder.add(Json.createObjectBuilder()
                            .add("title", categoryUsage.getCategoryName())
                            .add("count", categoryUsage.getTaskCount()));
                }

                JsonArray categoryUsageArray = categoryUsageArrayBuilder.build();

                taskCounts = Json.createObjectBuilder()
                        .add("toDoTasksQuantity", countTasksByState("toDo"))
                        .add("doingTasksQuantity", countTasksByState("doing"))
                        .add("doneTasksQuantity", countTasksByState("done"))
                        .add("totalTasks", countTasks())
                        .add("avgCompletionTime", getAverageCompletionTime())
                        .add("avgTaskPerUser", getAvgTaskPerUser())
                        .add("categoryUsage", categoryUsageArray)
                        .build();
            }

        } catch (NullPointerException e) {
            System.out.println("Caught a NullPointerException: " + e.getMessage());
        }

        return taskCounts;
    }

    private int countTasksByState(String state) {
        return taskDao.countTasksByState(state);
    }

    private int countTasks() {
        return taskDao.countTasks();
    }

    public double getAverageCompletionTime() {
        List<TaskEntity> tasks = taskDao.findAllCompletedTasks();
        return tasks.stream()
                .filter(task -> task.getConclusionDate() != null)
                .mapToLong(task -> ChronoUnit.DAYS.between(task.getInitialDate(), task.getConclusionDate()))
                .average()
                .orElse(0);
    }

    private List<CategoryUsageDto> findCategoriesOrderedByUsage(){
        List<Object[]> results = taskDao.findCategoriesOrderedByUsage();
        List<CategoryUsageDto> categoryUsageList = new ArrayList<>();
        for (Object[] result : results) {
            CategoryEntity category = (CategoryEntity) result[0];
            Long count = (Long) result[1];
            categoryUsageList.add(new CategoryUsageDto(category.getTitle(), count));
        }
        return categoryUsageList;
    }

    private List<UserRegistrationDto> countUsersByRegistrationDate() {
        List<Object[]> results = userDao.countUsersByRegistrationDate();
        List<UserRegistrationDto> userRegistrationList = new ArrayList<>();
        for (Object[] result : results) {
            LocalDate registrationDate = (LocalDate) result[0];
            int count = ((Long) result[1]).intValue();
            userRegistrationList.add(new UserRegistrationDto(registrationDate, count));
        }
        return userRegistrationList;
    }

    private double getAvgTaskPerUser() {
        return taskDao.getAvgTaskPerUser();
    }
}
