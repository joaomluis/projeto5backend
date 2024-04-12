package aor.paj.service;

import aor.paj.bean.TaskBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.*;

import aor.paj.entity.UserEntity;
import aor.paj.utils.EncryptHelper;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;
import java.util.List;

@Path("/users")
public class UserService {

    @Inject
    UserBean userBean;

    @Inject
    TaskBean taskBean;

    @Inject
    EncryptHelper encryptHelper;



    @POST
    @Path("/addUserDB")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addUserToDB(User user) {

        Response response;

        boolean isFieldEmpty = userBean.isAnyFieldEmpty(user);
        boolean isEmailValid = userBean.isEmailValid(user.getEmail());
        boolean isUsernameAvailable = userBean.isUsernameAvailable(user.getUsername());
        boolean isImageValid = userBean.isImageUrlValid(user.getImgURL());
        boolean isPhoneValid = userBean.isPhoneNumberValid(user.getPhoneNumber());


        if (isFieldEmpty) {
            response = Response.status(422).entity("There's an empty field. ALl fields must be filled in").build();

        } else if (!isEmailValid) {
            response = Response.status(422).entity("Invalid email").build();

        } else if (!isUsernameAvailable) {
            response = Response.status(Response.Status.CONFLICT).entity("Username already in use").build(); //status code 409

        } else if (!isImageValid) {
            response = Response.status(422).entity("Image URL invalid").build(); //400

        } else if (!isPhoneValid) {
            response = Response.status(422).entity("Invalid phone number").build();

        } else if (userBean.register(user)) {
            response = Response.status(Response.Status.CREATED).entity("User registered successfully").build(); //status code 201

        } else {
            response = Response.status(Response.Status.BAD_REQUEST).entity("Something went wrong").build(); //status code 400

        }

        return response;
    }
    @PUT
    @Path("/updateUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newUpdateUser (@HeaderParam("token") String token, User updatedUser) {
        Response response;

        User user = userBean.getUserByToken(token);

        boolean isEmailValid = userBean.isEmailValidToUpdate(updatedUser.getEmail(), user.getUsername());
        boolean isImageValid = userBean.isImageUrlValid(updatedUser.getImgURL());
        boolean isPhoneValid = userBean.isPhoneNumberValid(updatedUser.getPhoneNumber());



        if (user != null) {

            if (!isEmailValid) {
                response = Response.status(422).entity("Invalid email").build();

            } else if (!isImageValid) {
                response = Response.status(422).entity("Image URL invalid").build();

            } else if (!isPhoneValid) {
                response = Response.status(422).entity("Invalid phone number").build();

            } else if (userBean.updateOwnUser(token, updatedUser) != null) {


                JsonObject jsonResponse = Json.createObjectBuilder()
                        .add("firstName", updatedUser.getFirstName())
                        .add("imgURL", updatedUser.getImgURL())
                        .build();

                response = Response.status(Response.Status.OK).entity(jsonResponse).build();

            } else {
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to update user").build();
            }

        } else {
            response = Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized access").build();
        }

        return response;
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUserByToken(@HeaderParam("token") String token) {
        if (token != null) {

            User user = userBean.getUserByToken(token);

            if (user != null) {

                return Response.ok(user).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } else {

            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllUsers(@HeaderParam("token") String token) {
        List<User> allUsers = userBean.getAllUsers();
        User userRequest = userBean.getUserByToken(token);

        if (userRequest == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized access").build();
        } else if (allUsers != null && !allUsers.isEmpty()) {
            return Response.ok(allUsers).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("No users found").build();
        }
    }

    @GET
    @Path("/activeUsers")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getActiveUsers(@HeaderParam("token") String token) {

        User userRequest = userBean.getUserByToken(token);
        if (userRequest != null && (userRequest.getTypeOfUser().equals("product_owner") || userRequest.getTypeOfUser().equals("scrum_master"))) {
            List<User> activeUsers = userBean.getActiveUsers();

            if (activeUsers != null && !activeUsers.isEmpty()) {
                return Response.ok(activeUsers).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("No users found").build();
            }
        }else{
            return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized access").build();
        }

    }

    @GET
    @Path("/inactiveUsers")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getInactiveUsers(@HeaderParam("token") String token) {

        User userRequest = userBean.getUserByToken(token);
        if (userRequest != null && (userRequest.getTypeOfUser().equals("product_owner"))) {
            List<User> inactiveUsers = userBean.getInactiveUsers();

            if (inactiveUsers != null && !inactiveUsers.isEmpty()) {
                return Response.ok(inactiveUsers).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("No users found").build();
            }
        }else{
            return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized access").build();
        }

    }



    @PUT
    @Path("/restoreUser/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response restoreUser(@HeaderParam("token") String token, @PathParam("username") String username) {
        User user = userBean.getUserByToken(token);
        if (user != null && (user.getTypeOfUser().equals("product_owner"))) {
            boolean restored = userBean.restoreUser(username);
            if (restored) {
                return Response.status(Response.Status.OK).entity("User restored successfully").build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to restore user").build();
        }
        return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").build();
    }

    @PUT
    @Path("/deleteUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("token") String token, @HeaderParam("username") String username) {
        User user = userBean.getUserByToken(token);
        if (user != null && (user.getTypeOfUser().equals("product_owner"))) {
            boolean deleted = userBean.removeUser(username);
            if (deleted) {
                return Response.status(Response.Status.OK).entity("User deleted successfully").build();
            }

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Forbidden").build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Forbidden").build();
    }

    @DELETE
    @Path("/removeUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(@HeaderParam("token") String token, @HeaderParam("username") String username) {
        User user = userBean.getUserByToken(token);
        if (user != null && (user.getTypeOfUser().equals("product_owner"))) {
            boolean deleted = userBean.deletePermanentlyUser(username);
            if (deleted) {
                return Response.status(Response.Status.OK).entity("User deleted successfully").build();
            }

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Forbidden").build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("User with this token is not found").build();
    }


    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginDto user) {

        User loggedUser = userBean.loginDB(user);
        if (loggedUser != null) {
            // Criar um objeto JSON contendo apenas o token
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("firstName", loggedUser.getFirstName())
                    .add("username", loggedUser.getUsername())
                    .add("typeOfUser", loggedUser.getTypeOfUser())
                    .add("imgURL", loggedUser.getImgURL())
                    .add("token", loggedUser.getToken())
                    .build();
            // Retornar a resposta com o token
            return Response.status(200).entity(jsonResponse).build();

        } else {
            return Response.status(403).entity("{\"error\": \"Something went wrong\"}").build();
        }
    }





    @PUT
    @Path("/updateProfile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("token") String token, User updatedUser) {
        User user = userBean.getUserByToken(token);

        if (user != null) {
            if (updatedUser.getEmail() != null) {
                if (!userBean.isEmailValid(updatedUser.getEmail())) {
                    return Response.status(422).entity("Invalid email").build();
                } else if(!userBean.emailAvailable(updatedUser.getEmail())) {
                    return Response.status(422).entity("Email allready exists").build();
                }else {
                    user.setEmail(updatedUser.getEmail());
                }
            }
            if (updatedUser.getFirstName() != null) {
                user.setFirstName(updatedUser.getFirstName());
            }
            if (updatedUser.getLastName() != null) {
                user.setLastName(updatedUser.getLastName());
            }
            if (updatedUser.getPhoneNumber() != null) {
                if (!userBean.isPhoneNumberValid(updatedUser.getPhoneNumber())) {
                    return Response.status(422).entity("Invalid phone number").build();
                } else {
                    user.setPhoneNumber(updatedUser.getPhoneNumber());
                }
            }
            if (updatedUser.getImgURL() != null) {
                if (!userBean.isImageUrlValid(updatedUser.getImgURL())) {
                    return Response.status(422).entity("Image URL invalid").build();
                } else {
                    user.setImgURL(updatedUser.getImgURL());
                }
            }

            if(updatedUser.getPassword() != null){
                String plainPassword = updatedUser.getPassword();
                String hashedPassword = encryptHelper.encryptPassword(plainPassword);
                user.setPassword(hashedPassword);
            }


            boolean updatedUSer = userBean.updateUser(token, user);
            if (updatedUSer) {
                return Response.status(Response.Status.OK).entity(user).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to update user").build();
            }
        }else{
            return Response.status(401).entity("Invalid credentials").build();
        }
    }

    @PUT
    @Path("/updatePassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(@HeaderParam("token") String token, PasswordUpdateDto passwordUpdateDto) {
        User user = userBean.getUserByToken(token);

        String currentPassword = passwordUpdateDto.getCurrentPassword();
        String newPassword = passwordUpdateDto.getNewPassword();
        String confirmPassword = passwordUpdateDto.getConfirmPassword();

        if (user != null) {
            if (newPassword.equals(confirmPassword)) {
                if (encryptHelper.checkPassword(currentPassword, user.getPassword())) {


                    boolean updatedUserPassword = userBean.updatePassword(token, newPassword);
                    if (updatedUserPassword) {
                        return Response.status(Response.Status.OK).entity(user).build();
                    } else {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to update password").build();
                    }
                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid current password").build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Passwords don't match").build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }
    }

    @PUT
    @Path("/updateProfilePO")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserByPO(@HeaderParam("token") String token, @HeaderParam("username") String username, User updatedUser) {
        User userRequest = userBean.getUserByToken(token);
        User beModified = userBean.getUserByUsername(username);


        if (userRequest != null && (userRequest.getTypeOfUser()).equals("product_owner")) {
            if (updatedUser.getEmail() != null ) {
                if (!userBean.isEmailValidToUpdate(updatedUser.getEmail(), username)) {
                    return Response.status(422).entity("Invalid email").build();
                }else {
                    beModified.setEmail(updatedUser.getEmail());
                }
            }
            if (updatedUser.getFirstName() != null) {
                beModified.setFirstName(updatedUser.getFirstName());
            }
            if (updatedUser.getLastName() != null) {
                beModified.setLastName(updatedUser.getLastName());
            }
            if (updatedUser.getPhoneNumber() != null) {
                if (!userBean.isPhoneNumberValid(updatedUser.getPhoneNumber())) {
                    return Response.status(422).entity("Invalid phone number").build();
                } else {
                    beModified.setPhoneNumber(updatedUser.getPhoneNumber());
                }
            }
            if (updatedUser.getImgURL() != null) {
                if (!userBean.isImageUrlValid(updatedUser.getImgURL())) {
                    return Response.status(422).entity("Image URL invalid").build();
                } else {
                    beModified.setImgURL(updatedUser.getImgURL());
                }
            }

            if(updatedUser.getTypeOfUser() != null){
                beModified.setTypeOfUser(updatedUser.getTypeOfUser());
            }

            boolean updatedUSer = userBean.updateUserByPO(token, username, beModified);
            if (updatedUSer) {
                return Response.status(200).entity(beModified).build();
            } else {
                return Response.status(406).entity("Failed to update user").build();
            }
        }else{
            return Response.status(401).entity("Invalid credentials").build();
        }
    }

    @PUT
    @Path("/addUserByPO")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserByPO(@HeaderParam("token") String token, User user) {
        User userRequest = userBean.getUserByToken(token);
        Response response;

        String dummyPassword = userBean.generateNewToken();
        user.setPassword(dummyPassword);

        if(userRequest!= null && userRequest.getTypeOfUser().equals("product_owner")) {


            boolean isFieldEmpty = userBean.isAnyFieldEmpty(user);
            boolean isEmailValid = userBean.isEmailValid(user.getEmail());
            boolean isUsernameAvailable = userBean.isUsernameAvailable(user.getUsername());
            boolean isImageValid = userBean.isImageUrlValid(user.getImgURL());
            boolean isPhoneValid = userBean.isPhoneNumberValid(user.getPhoneNumber());


            if (isFieldEmpty) {
                response = Response.status(422).entity("There's an empty field. ALl fields must be filled in").build();

            } else if (!isEmailValid) {
                response = Response.status(422).entity("Invalid email").build();

            } else if (!isUsernameAvailable) {
                response = Response.status(Response.Status.CONFLICT).entity("Username already in use").build(); //status code 409

            } else if (!isImageValid) {
                response = Response.status(422).entity("Image URL invalid").build();

            } else if (!isPhoneValid) {
                response = Response.status(422).entity("Invalid phone number").build();

            } else if (user.getTypeOfUser().isEmpty() || user.getTypeOfUser() == null) {
                response = Response.status(422).entity("Select a role to create an user").build();

            } else if (userBean.registerByPO(token,user)) {
                response = Response.status(Response.Status.CREATED).entity("User registered successfully").build();

            } else {
                response = Response.status(Response.Status.BAD_REQUEST).entity("Something went wrong").build();

            }
        }else{
            response = Response.status(Response.Status.UNAUTHORIZED).entity("You don´t have permission").build();
        }

        return response;
    }


    @PUT
    @Path("/{username}/updateUserRole")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserRole(@HeaderParam("token") String token, @PathParam("username") String username, String newRole) {
        Response response;

        JsonObject jsonObject = Json.createReader(new StringReader(newRole)).readObject();
        String newRoleConverted = jsonObject.getString("typeOfUser");

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else if (!userBean.getUserByToken(token).getTypeOfUser().equals("product_owner")) {
            response = Response.status(403).entity("You dont have permission to do that").build();

        } else if (!newRoleConverted.equals("developer") && !newRoleConverted.equals("scrum_master") && !newRoleConverted.equals("product_owner")) {
            response = Response.status(403).entity("Invalid role").build();

        } else if (userBean.updateUserRole(username, newRoleConverted)) {
            response = Response.status(200).entity("User role updated").build();

        } else {
            response = Response.status(401).entity("Role couldnt be updated").build();
        }

        return response;
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logoutValidate(@HeaderParam("token") String token){
        User userRequest=userBean.getUserByToken(token);

        if (userRequest==null){
            return Response.status(401).entity("Failed").build();
        }

        userBean.logoutUser(token);

        return Response.status(200).entity(" Logout successful").build();
    }

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("username")String username){
        UserDetails userRequested=userBean.getUserDetails(username);
        if (userRequested==null) return Response.status(400).entity("Failed").build();
        return Response.status(200).entity(userRequested).build();
    }


    @POST
    @Path("/recoverPassword/{email}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recoverPassword(@PathParam("email") String email) {
        User userRequestingMail = userBean.getUserByEmail(email);

        if (userRequestingMail != null) {

            userBean.recoverPassword(email);



            return Response.status(200).entity("Email sent").build();
        } else {
            return Response.status(400).entity("Email not found").build();
        }

    }

    @PUT
    @Path("/resetPassword/{confirmationToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(@PathParam("confirmationToken") String confirmationToken, String newPassword) {
        User user = userBean.getUserByConfirmationToken(confirmationToken);

        JsonObject jsonObject = Json.createReader(new StringReader(newPassword)).readObject();
        String newPasswordConverted = jsonObject.getString("newPassword");
        String confirmPasswordConverted = jsonObject.getString("confirmPassword");

        if (user != null) {
            if (newPasswordConverted.equals(confirmPasswordConverted)) {
                boolean updatedUserPassword = userBean.resetPassword(confirmationToken, confirmPasswordConverted);
                if (updatedUserPassword) {
                    return Response.status(Response.Status.OK).entity("Password updated").build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to update password").build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Passwords don't match").build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }
    }

    @PUT
    @Path("/confirmAccount/{confirmationToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmAccount(@PathParam("confirmationToken") String confirmationToken, String newPassword) {
        User user = userBean.getUserByConfirmationToken(confirmationToken);

        JsonObject jsonObject = Json.createReader(new StringReader(newPassword)).readObject();
        String newPasswordConverted = jsonObject.getString("newPassword");
        String confirmPasswordConverted = jsonObject.getString("confirmPassword");


        if (user != null) {
            if (!confirmationToken.equals(user.getConfirmationToken())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid token").build();
            }
                if (newPasswordConverted.equals(confirmPasswordConverted)) {
                    boolean updatedUserPassword = userBean.confirmAccount(confirmationToken, confirmPasswordConverted);
                    if (updatedUserPassword) {
                        return Response.status(Response.Status.OK).entity("Account registered successfully").build();
                    } else {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to confirm account").build();
                    }
                } else {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Passwords don't match").build();
                }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }

    }

}