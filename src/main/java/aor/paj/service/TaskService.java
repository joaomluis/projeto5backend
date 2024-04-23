package aor.paj.service;

import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dao.CategoryDao;
import aor.paj.dto.Category;
import aor.paj.dto.Task;
import aor.paj.dto.User;
import aor.paj.entity.TaskEntity;
import aor.paj.websocket.TasksWebsocket;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Path("/tasks")
public class TaskService {

    @Inject
    TaskBean taskBean;

    @Inject
    UserBean userBean;

    @Inject
    CategoryDao categoryDao;

    @Inject
    TasksWebsocket tasksWebsocket;

    //getter das tasks

    @POST
    @Path("/createTask")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addTask(@HeaderParam("token") String token, @HeaderParam("categoryId") String categoryId, Task task) {

        Response response;

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (categoryId == null || categoryDao.findCategoryById(Long.parseLong(categoryId)) == null) {
            response = Response.status(422).entity("Invalid category").build();

        } else if (!taskBean.isTaskTitleAvailable(task) || task.getTitle().isEmpty()) {
            response = Response.status(422).entity("Title's not valid").build();

        } else if (task.getInitialDate().isAfter(task.getEndDate())) {
            response = Response.status(422).entity("Initial date cannot be after the end date").build();

        } else if (task.getPriority() != 100 && task.getPriority() != 200 && task.getPriority() != 300) {
            response = Response.status(422).entity("Priority can only be 100, 200 or 300").build();

        } else if (!task.getState().equals("toDo") && !task.getState().equals("doing") && !task.getState().equals("done")) {
            response = Response.status(422).entity("State can only be toDo, doing or done").build();

        } else if (taskBean.addTask(token,task, categoryId)) {
            response = Response.status(200).entity("A new task is created").build();
            tasksWebsocket.notifyTasksUpdated();

        } else {
            response = Response.status(400).entity("Failed to update task").build();
        }

        return response;
    }

    @PUT
    @Path("/updateTask")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTask(@HeaderParam("token") String token, @HeaderParam("categoryId") String categoryId,
                               @HeaderParam("taskId") String taskId, Task task) {

        Response response;

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (categoryId == null || categoryId.isEmpty()) {
            response = Response.status(422).entity("Select a category").build();

        } else if (categoryDao.findCategoryById(Long.parseLong(categoryId)) == null) {
            response = Response.status(422).entity("Invalid category").build();

        } else if (!taskBean.isTaskTitleAvailableToUpdate(token, taskId)) {
            response = Response.status(422).entity("Title not available").build();

        } else if (task.getInitialDate().isAfter(task.getEndDate())) {
            response = Response.status(422).entity("Initial date cannot be after the end date").build();

        } else if (task.getPriority() != 100 && task.getPriority() != 200 && task.getPriority() != 300) {
           response = Response.status(422).entity("Priority can only be 100, 200 or 300").build();

        } else if (!task.getState().equals("toDo") && !task.getState().equals("doing") && !task.getState().equals("done")) {
            response = Response.status(422).entity("State can only be toDo, doing or done").build();

        } else if (taskBean.updateTask(token, taskId, task, categoryId)) {
            response = Response.status(200).entity("Task updated sucessfully").build();
            tasksWebsocket.notifyTasksUpdated();

        } else
            response = Response.status(400).entity("Failed to update task").build();

        return response;
    }



    @PUT
    @Path("/{taskId}/updateCategory")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTaskCategory(@HeaderParam("token") String token, @PathParam("taskId") String taskId, Category category) {
        Response response;
        User requestingUser = userBean.getUserByToken(token);

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (!requestingUser.getTypeOfUser().equals("Product Owner")) {
            response = Response.status(409).entity("You dont have permissions to edit that").build();

        } else if (taskBean.updateTaskCategory(token, taskId, category)) {
            response = Response.status(200).entity("Task category changed successfully").build();
            tasksWebsocket.notifyTasksUpdated();

        } else {
            response = Response.status(400).entity("Task category update failed").build();
        }
        return response;
    }

    @PUT
    @Path("/{taskId}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTaskStatus(@HeaderParam("token") String token, @PathParam("taskId") String taskId, @HeaderParam("newState") String state) {
        Response response;

        //JsonObject jsonObject = Json.createReader(new StringReader(state)).readObject();
        //String newStatusConverted = jsonObject.getString("state");

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (!state.equals("toDo") && !state.equals("doing") && !state.equals("done")) {
            response = Response.status(422).entity("State can only be toDo, doing or done").build();

        } else if (taskBean.updateTaskState(token, taskId, state)) {
            response = Response.status(200).entity("Task state updated successfully").build();

            tasksWebsocket.notifyTasksUpdated();

            // send to task websocket data so that other users can refresh the screen
            // no servidor criar um websocket para as tasks
            // nesta class e metodo ter acesso ao websocket para enviar a mensagem
            // enviar a mensagem para o websocket
            // no cliente fazer update a store que da trigger ao refresh do ecra

        } else
            response = Response.status(400).entity("Failed to update task state").build();

        return response;
    }

    /**
     * Editar atributo isActive da task (soft delete), permite ao scrum master só colocar
     * para falso, enquanto ao owner pode mudar para false ou true, o developer não deixa
     * fazer nada
     * @param token
     * @param taskId
     * @return
     */
    @PUT
    @Path("/{taskId}/softDelete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response softDeleteTask(@HeaderParam("token") String token, @PathParam("taskId") String taskId) {
        Response response;


        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if(!userBean.getUserByToken(token).getTypeOfUser().equals("product_owner") && !userBean.getUserByToken(token).getTypeOfUser().equals("scrum_master")) {
            response = Response.status(409).entity("You dont have permissions to edit that").build();


        } else if (taskBean.updateTaskActiveState(token, taskId)) {
            response = Response.status(200).entity("Task active state updated successfully").build();
            tasksWebsocket.notifyTasksUpdated();

        } else
            response = Response.status(400).entity("Failed to update task active state").build();

        return response;
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasks(@HeaderParam("token") String token, @QueryParam("username") String username, @QueryParam("category") String category) {
        List<Task> tasks;

        User user = userBean.getUserByToken(token);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build();
        }

        if (category != null && !category.isEmpty() && username != null && !username.isEmpty()) {
            tasks = taskBean.getTasksByUserAndCategory(token, username, category);
            return Response.ok(tasks).build();
        } else if (username != null && !username.isEmpty()) {
            tasks = taskBean.getTasksByUser(token, username);
            return Response.ok(tasks).build();
        } else if (category != null && !category.isEmpty()) {
            tasks = taskBean.getTasksByCategory(token, category);
            return Response.ok(tasks).build();
        } else {
            tasks = taskBean.getActiveTasks(token);
            return Response.ok(tasks).build();
        }
    }

    @GET
    @Path("/getInactiveTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSoftDeletedTasks(@HeaderParam("token") String token) {
        Response response;

        ArrayList<Task> softDeletedTasks = taskBean.getSoftDeletedTasks();

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (!userBean.getUserByToken(token).getTypeOfUser().equals("scrum_master") && !userBean.getUserByToken(token).getTypeOfUser().equals("product_owner")) {
            response = Response.status(403).entity("You dont have permissions to do that").build();

        } else if (softDeletedTasks != null) {
            response = Response.status(200).entity(softDeletedTasks).build();

        } else {
            response = Response.status(400).entity("Failed to retrieve tasks").build();
        }


        return response;
    }

    @DELETE
    @Path("/deleteTasksByUsername/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllTasksByUsername(@HeaderParam("token"
    ) String token, @PathParam("username") String username) {
        Response response;

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (!userBean.getUserByToken(token).getTypeOfUser().equals("product_owner")) {
            response = Response.status(403).entity("You dont have permissions to do that").build();

        } else if (taskBean.deleteTasksByUsername(username))  {
            response = Response.status(200).entity("Tasks deleted successfully").build();
            tasksWebsocket.notifyTasksUpdated();

        } else {
            response = Response.status(400).entity("Failed to execute order").build();
        }

        return response;
    }

    @DELETE
    @Path("/{id}/hardDeleteTask")
    @Produces(MediaType.APPLICATION_JSON)
    public Response hardDeleteTask(@HeaderParam("token") String token, @PathParam("id") String id) {
        Response response;

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (!userBean.getUserByToken(token).getTypeOfUser().equals("product_owner")) {
            response = Response.status(403).entity("You dont have permissions to do that").build();

        } else if (taskBean.hardDeleteTask(token, id))  {
            response = Response.status(200).entity("Task permanently deleted").build();
            tasksWebsocket.notifyTasksUpdated();

        } else {
            response = Response.status(400).entity("Failed to execute order").build();
        }

        return response;
    }


    //Método para filtrar tasks por categoria
    @GET
    @Path("/getFilterTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserTasksByCategory(@HeaderParam("token") String token, @QueryParam("username") String username,@QueryParam("category") long category) {
         User user = userBean.getUserByToken(token);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build();
        }

        if (!user.getTypeOfUser().equals("product_owner") && !user.getTypeOfUser().equals("scrum_master")) {
            return Response.status(Response.Status.FORBIDDEN).entity("You don't have permission to access this resource").build();
        }

        List<Task> userTasksByCategory = new ArrayList<>();
        if(username != null || category != 0){

            userTasksByCategory = taskBean.getFilterTasks(token, username, category);
            return Response.ok(userTasksByCategory).build();


        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("No tasks found for this user or category").build();
        }

    }

    @GET
    @Path("/getAllTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasks(@HeaderParam("token") String token) {
        Response response;

        ArrayList<Task> tasks = taskBean.getAllTasks(token);

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (tasks != null) {
            response = Response.status(200).entity(tasks).build();

        } else {
            response = Response.status(400).entity("Failed to retrieve tasks").build();
        }

        return response;
    }

    @GET
    @Path("getActiveTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActiveTasks(@HeaderParam("token") String token) {
        Response response;

        ArrayList<Task> tasks = taskBean.getActiveTasks(token);

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (tasks != null) {
            response = Response.status(200).entity(tasks).build();

        } else {
            response = Response.status(400).entity("Failed to retrieve tasks").build();
        }

        return response;
    }

    @GET
    @Path("getTaskById/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskById(@HeaderParam("token") String token, @PathParam("id") String id) {
        Response response;

        Task task = taskBean.getTaskById(token, id);

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (task != null) {
            response = Response.status(200).entity(task).build();

        } else {
            response = Response.status(400).entity("Failed to retrieve task").build();
        }
        return response;
    }

    @GET
    @Path("statistics")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatistics(@HeaderParam("token") String token, @HeaderParam("username") String username) {
        Response response;

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else {
            JsonObject statistics = taskBean.getTaskDataAsJson(token, username);
            response = Response.status(200).entity(statistics).build();
        }

        return response;
    }


}

