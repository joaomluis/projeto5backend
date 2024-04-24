package aor.paj.bean;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.TaskDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.LoginDto;
import aor.paj.dto.User;
import aor.paj.dto.UserDetails;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.TaskEntity;
import aor.paj.entity.UserEntity;
import aor.paj.utils.EncryptHelper;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static aor.paj.controller.EmailSender.sendPasswordResetEmail;
import static aor.paj.controller.EmailSender.sendVerificationEmail;

@Singleton
public class UserBean implements Serializable {

    private static final Logger logger = LogManager.getLogger();

    @EJB
    UserDao userDao;

    @EJB
    TaskDao taskDao;

    @EJB
    CategoryDao categoryDao;

    @EJB
    EncryptHelper encryptHelper;

    public UserBean(){
    }


    public User loginDB(LoginDto user){
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());
        user.setPassword(encryptHelper.encryptPassword(user.getPassword()));
        if (userEntity != null && userEntity.getIsActive() && userEntity.isConfirmed()){
            if (userEntity.getPassword().equals(user.getPassword())){
                String token = generateNewToken();
                userEntity.setToken(token);
                User loggedInUser = (convertUserEntityToUserLogged(userEntity));

                logger.info("User " + user.getUsername() + " logged in");

                return loggedInUser;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
            List<User> users = new ArrayList<>();
            List<UserEntity> userEntities = userDao.findAllUsers();

            if(userEntities != null){
                for(UserEntity userEntity : userEntities){
                    User user = convertUserEntityToDto(userEntity);
                    users.add(user);
                }
            }

            return users;
    }

    public List<User> getActiveUsers(){

        List<User> users = new ArrayList<>();
        List<UserEntity> userEntities = userDao.findAllUsers();

        if(userEntities != null) {
            for (UserEntity userEntity : userEntities) {
                if (userEntity.getIsActive() && !userEntity.getUsername().equals("admin") && !userEntity.getUsername().equals("deletedUser")) {
                    User user = convertUserEntityToDto(userEntity);
                    users.add(user);
                }
            }
        }

        return users;

    }


    public List<User> getInactiveUsers(){

        List<User> users = new ArrayList<>();
        List<UserEntity> userEntities = userDao.findAllUsers();

        if(userEntities != null) {
            for (UserEntity userEntity : userEntities) {
                if (!userEntity.getIsActive() && !userEntity.getUsername().equals("admin") && !userEntity.getUsername().equals("deletedUser")) {
                    User user = convertUserEntityToDto(userEntity);
                    users.add(user);
                }
            }
        }

        return users;

    }

    /**
     *
     * @param token
     * @return return is null if user is not found or token not found
     */
    public boolean updateUserByPO(String token, String username, User updatedUser) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity == null) {
            return false;
        }

        if (updatedUser.getEmail() != null ) {
            userEntity.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getFirstName() != null) {
            userEntity.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            userEntity.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getPhoneNumber() != null) {
            userEntity.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if (updatedUser.getImgURL() != null) {
            userEntity.setImgURL(updatedUser.getImgURL());
        }
        if (updatedUser.getPassword() != null){
            userEntity.setPassword(updatedUser.getPassword());
        }
        if(updatedUser.getTypeOfUser() != null){
            userEntity.setTypeOfUser(updatedUser.getTypeOfUser());

        }
        return userDao.update(userEntity);

    }


    public boolean updateUser(String token, User updatedUser) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity == null) {
            return false;
        }

        if (updatedUser.getEmail() != null ) {
            userEntity.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getFirstName() != null) {
            userEntity.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            userEntity.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getPhoneNumber() != null) {
            userEntity.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if (updatedUser.getImgURL() != null) {
            userEntity.setImgURL(updatedUser.getImgURL());
        }
        if (updatedUser.getPassword() != null){
            userEntity.setPassword(updatedUser.getPassword());
    }
            return userDao.update(userEntity);

    }

    public User updateOwnUser(String token, User updatedUser) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity == null) {
            return null;
        }

        if (updatedUser.getEmail() != null ) {
            userEntity.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getFirstName() != null) {
            userEntity.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            userEntity.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getPhoneNumber() != null) {
            userEntity.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if (updatedUser.getImgURL() != null) {
            userEntity.setImgURL(updatedUser.getImgURL());
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {


                String plainPassword = updatedUser.getPassword();
                String hashedPassword = encryptHelper.encryptPassword(plainPassword);
                updatedUser.setPassword(hashedPassword);



            userEntity.setPassword(updatedUser.getPassword());
        }

        User u = convertUserEntityToDto(userEntity);
        return u;

    }

    public boolean updatePassword (String token, String newPassword){
        UserEntity userEntity = userDao.findUserByToken(token);
        if (userEntity == null) {
            return false;
        }
        userEntity.setPassword(encryptHelper.encryptPassword(newPassword));
        return userDao.update(userEntity);
    }


    /**
     * Update ao role do user, só disponivel para users do tipo product owner
     * @param username
     * @param newRole
     * @return
     */
    public boolean updateUserRole(String username, String newRole) {
        boolean status;

        UserEntity userEntity = userDao.findUserByUsername(username);

        if(userEntity != null) {
            userEntity.setTypeOfUser(newRole);
            userDao.update(userEntity);
            status = true;
        } else {
            status = false;
        }


        return status;
    }


    public User getUserByToken(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        User u = null;
        u = convertUserEntityToDto(userEntity);
        return u;
    }
    public User getUserByUsername(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        User u = null;
        u = convertUserEntityToDto(userEntity);
        return u;
    }

    public User getUserByEmail(String email) {
        UserEntity userEntity = userDao.findUserByEmail(email);
        User u = null;
        u = convertUserEntityToDto(userEntity);
        return u;
    }

    public User getUserByConfirmationToken(String token) {
        UserEntity userEntity = userDao.findUserByConfirmationToken(token);
        User u = null;
        u = convertUserEntityToDto(userEntity);
        return u;
    }


    public String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public User convertUserEntityToDto(UserEntity userEntity) {
        if(userEntity != null) {
            User userDto = new User();
            userDto.setUsername(userEntity.getUsername());
            userDto.setPassword(userEntity.getPassword());
            userDto.setEmail(userEntity.getEmail());
            userDto.setPhoneNumber(userEntity.getPhoneNumber());
            userDto.setImgURL(userEntity.getImgURL());
            userDto.setFirstName(userEntity.getFirstName());
            userDto.setLastName(userEntity.getLastName());
            userDto.setTypeOfUser(userEntity.getTypeOfUser());
            userDto.setActive(userEntity.getIsActive());
            userDto.setConfirmed(userEntity.isConfirmed());
            userDto.setConfirmationToken(userEntity.getConfirmationToken());
            userDto.setConfirmationTokenDate(userEntity.getConfirmationTokenDate());
            return userDto;
        }
        return null;
    }

    private User convertUserEntityToUserLogged (UserEntity userEntity){
        if(userEntity != null) {
            User userDto = new User();
            userDto.setFirstName(userEntity.getFirstName());
            userDto.setUsername(userEntity.getUsername());
            userDto.setToken(userEntity.getToken());
            userDto.setTypeOfUser(userEntity.getTypeOfUser());
            userDto.setImgURL(userEntity.getImgURL());

            return userDto;
        }
        return null;
    }

    public User convertUserEntityToDtoForTask(UserEntity userEntity) {
        if(userEntity != null) {
            User userDto = new User();
            userDto.setUsername(userEntity.getUsername());

            return userDto;
        }
        return null;
    }


    public UserEntity convertUserDtotoUserEntity(User user){
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(user.getPassword());
        userEntity.setToken(user.getToken());
        userEntity.setEmail(user.getEmail());
        userEntity.setPhoneNumber(user.getPhoneNumber());
        userEntity.setImgURL(user.getImgURL());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setIsActive(true);
        userEntity.setTypeOfUser(user.getTypeOfUser());
        userEntity.setConfirmed(user.isConfirmed());
        userEntity.setConfirmationToken(user.getConfirmationToken());
        userEntity.setConfirmationTokenDate(user.getConfirmationTokenDate());
        userEntity.setRegistrationDate(user.getRegistrationDate());

        return userEntity;
    }


    public boolean register(User user){
        UserEntity u= userDao.findUserByUsername(user.getUsername());

        if (u==null){
            user.setPassword(encryptHelper.encryptPassword(user.getPassword()));
            user.setRegistrationDate(LocalDate.now());
            userDao.persist(convertUserDtotoUserEntity(user));
            return true;
        }else
            return false;
    }

    public boolean registerByPO(String token,User user){
        UserEntity userEntityPO = userDao.findUserByToken(token);

        if(userEntityPO != null && userEntityPO.getTypeOfUser().equals("product_owner")) {

            UserEntity u = userDao.findUserByUsername(user.getUsername());

            if (u == null) {

                String confirmationToken = generateNewToken();
                user.setConfirmationToken(confirmationToken);
                user.setConfirmationTokenDate(LocalDateTime.now().plusDays(1));

                user.setPassword(encryptHelper.encryptPassword(user.getPassword()));
                user.setRegistrationDate(LocalDate.now());
                userDao.persist(convertUserDtotoUserEntity(user));

                sendVerificationEmail(user.getEmail(),
                        user.getUsername(),
                        "http://localhost:3000/auth/define-password/verify/" + confirmationToken);

                return true;
            } else
                return false;
        }else{
            return false;
        }
    }

    public boolean isAnyFieldEmpty(User user) {
        boolean status = false;

        if (user.getUsername().isEmpty() ||
                user.getPassword().isEmpty() ||
                user.getEmail().isEmpty() ||
                user.getFirstName().isEmpty() ||
                user.getLastName().isEmpty() ||
                user.getPhoneNumber().isEmpty() ||
                user.getImgURL().isEmpty()) {
            status = true;
        }
        return status;
    }

    private boolean isEmailFormatValid(String email) {

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public boolean isEmailValid(String email) {
        if (!isEmailFormatValid(email)) {
            return false;
        }

        UserEntity userEntity = userDao.findUserByEmail(email);

        return userEntity == null;
    }

    public boolean isEmailValidToUpdate(String email, String username) {
        if (!isEmailFormatValid(email)) {
            return false;
        }

        UserEntity userEntity = userDao.findUserByEmail(email);

        if (userEntity == null || userEntity.getUsername().equals(username)) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isUsernameAvailable(String username) {

        UserEntity userEntity = userDao.findUserByUsername(username);

        return userEntity == null;
    }

    public boolean isImageUrlValid(String url) {
        boolean status = true;

        if (url == null) {
            status = false;
        }

        try {
            BufferedImage img = ImageIO.read(new URL(url));
            if (img == null) {
                status = false;
            }
        } catch (IOException e) {
            status = false;
        }

        return status;
    }


    public boolean isPhoneNumberValid(String phone) {
        boolean status = true;
        int i = 0;

        while (status && i < phone.length() - 1) {
            if (phone.length() == 9) {
                for (; i < phone.length(); i++) {
                    if (!Character.isDigit(phone.charAt(i))) {
                        status = false;
                    }
                }
            } else {
                status = false;
            }
        }
        return status;
    }

    public boolean emailAvailable (String email){
        UserEntity userEntity = userDao.findUserByEmail(email);

        return userEntity == null;
    }

    public boolean removeUser(String username){
        UserEntity userEntity = userDao.findUserByUsername(username);
        boolean wasRemoved=false;
        if (userEntity != null) {
            userEntity.setIsActive(false);
            wasRemoved =  userDao.update(userEntity);
        }
        return wasRemoved;
    }
    public boolean restoreUser(String username){
        UserEntity userEntity = userDao.findUserByUsername(username);
        boolean wasRemoved=false;
        if (userEntity != null) {
            userEntity.setIsActive(true);
            wasRemoved =  userDao.update(userEntity);
        }
        return wasRemoved;
    }

    public boolean deletePermanentlyUser(String username){
        UserEntity userEntity = userDao.findUserByUsername(username);
        ArrayList<TaskEntity> tasks = taskDao.findTasksByUser(userEntity);
        ArrayList<CategoryEntity> categories = categoryDao.findCategoriesByUser(userEntity);

        boolean wasRemoved=false;
        if (userEntity != null && !userEntity.getUsername().equals("deletedUser") && !userEntity.getUsername().equals("admin")) {

            if (tasks != null) {
                for (TaskEntity task : tasks) {
                    task.setOwner(userDao.findUserByUsername("deletedUser"));
                }
            }

            if (categories != null) {
                for (CategoryEntity category : categories) {
                    category.setOwner(userDao.findUserByUsername("deletedUser"));
                }
            }

            userEntity.setIsActive(false);
            userDao.remove(userEntity);
            wasRemoved = true;
        }
        return wasRemoved;
    }
    public boolean logoutUser(String token){
        UserEntity userEntity = userDao.findUserByToken(token);
        boolean wasRemovedToken = false;
        if(userEntity != null){
            wasRemovedToken = userDao.removedToken(userEntity);
        }

        return wasRemovedToken;
    }





    //Método em que o output é o objeto UserDetails que tem todos os atributos iguais ao User menos a pass
    public UserDetails getUserDetails(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if(userEntity != null){
        return new UserDetails(
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getImgURL(),
                userEntity.getPhoneNumber(),
                userEntity.getTypeOfUser()
        );
    }
    return null;
    }


    //Recebe uma string e vê se é um número de telefone válido
    public boolean isValidPhoneNumber(String phoneNumber){
        boolean valideNumber=false;
        try {

            String cleanedPhoneNumber = phoneNumber.replaceAll("[^\\d]", "");

            if (cleanedPhoneNumber.length() == 9 || cleanedPhoneNumber.length() == 10) {
                valideNumber=true;
            } else {
                valideNumber= false;
            }
        } catch (NumberFormatException e) {
            valideNumber=false;
        }
        return valideNumber;
    }

    //verifica se um URL é válido
    public boolean isValidUrl(String urlString) {
        try {

            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {

            return false;
        }
    }

    //verifica se um email é válido
    public boolean isValidEmail(String email) {
        boolean isValid = false;
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
            isValid = true;
        } catch (AddressException e) {
        }
        return isValid;
    }


    public void createDefaultUsersIfNotExistent() {
        UserEntity userEntity = userDao.findUserByUsername("admin");
        if (userEntity == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setEmail("admin@admin.com");
            admin.setFirstName("admin");
            admin.setLastName("admin");
            admin.setPhoneNumber("123456789");
            admin.setImgURL("https://t4.ftcdn.net/jpg/04/75/00/99/240_F_475009987_zwsk4c77x3cTpcI3W1C1LU4pOSyPKaqi.jpg");
            admin.setTypeOfUser("product_owner");
            admin.setConfirmed(true);
            admin.setRegistrationDate(LocalDate.now());


            register(admin);
        }

        UserEntity userEntity2 = userDao.findUserByUsername("deletedUser");
        if (userEntity2 == null) {
            User deletedUser = new User();
            deletedUser.setUsername("deletedUser");
            deletedUser.setPassword("123");
            deletedUser.setEmail("deleted@user.com");
            deletedUser.setFirstName("Deleted");
            deletedUser.setLastName("User");
            deletedUser.setPhoneNumber("123456789");
            deletedUser.setImgURL("https://www.iconpacks.net/icons/1/free-remove-user-icon-303-thumb.png");
            deletedUser.setTypeOfUser("developer");
            deletedUser.setActive(false);
            deletedUser.setConfirmed(true);
            deletedUser.setRegistrationDate(LocalDate.now());

            register(deletedUser);
        }

        UserEntity userEntity3 = userDao.findUserByUsername("Celso_dev");
        if (userEntity3 == null) {
            User celso = new User();
            celso.setUsername("Celso_dev");
            celso.setPassword("123");
            celso.setEmail("celso@dev.com");
            celso.setFirstName("Celso");
            celso.setLastName("Developer");
            celso.setPhoneNumber("123456789");
            celso.setImgURL("https://i.seadn.io/gae/2hDpuTi-0AMKvoZJGd-yKWvK4tKdQr_kLIpB_qSeMau2TNGCNidAosMEvrEXFO9G6tmlFlPQplpwiqirgrIPWnCKMvElaYgI-HiVvXc?auto=format&dpr=1&w=1000");
            celso.setTypeOfUser("developer");
            celso.setActive(true);
            celso.setConfirmed(true);
            celso.setRegistrationDate(LocalDate.now());

            register(celso);
        }

        UserEntity userEntity4 = userDao.findUserByUsername("Celso_sm");
        if (userEntity4 == null) {
            User celsoSm = new User();
            celsoSm.setUsername("Celso_sm");
            celsoSm.setPassword("123");
            celsoSm.setEmail("celsoSm@sm.com");
            celsoSm.setFirstName("Celso");
            celsoSm.setLastName("SM");
            celsoSm.setPhoneNumber("123456789");
            celsoSm.setImgURL("https://i.seadn.io/gae/2hDpuTi-0AMKvoZJGd-yKWvK4tKdQr_kLIpB_qSeMau2TNGCNidAosMEvrEXFO9G6tmlFlPQplpwiqirgrIPWnCKMvElaYgI-HiVvXc?auto=format&dpr=1&w=1000");
            celsoSm.setTypeOfUser("scrum_master");
            celsoSm.setActive(true);
            celsoSm.setConfirmed(true);
            celsoSm.setRegistrationDate(LocalDate.now());


            register(celsoSm);
        }

        UserEntity userEntity5 = userDao.findUserByUsername("Celso_po");
        if (userEntity5 == null) {
            User celsoPo = new User();
            celsoPo.setUsername("Celso_po");
            celsoPo.setPassword("123");
            celsoPo.setEmail("celso@po.com");
            celsoPo.setFirstName("Celso");
            celsoPo.setLastName("PO");
            celsoPo.setPhoneNumber("123456789");
            celsoPo.setImgURL("https://i.seadn.io/gae/2hDpuTi-0AMKvoZJGd-yKWvK4tKdQr_kLIpB_qSeMau2TNGCNidAosMEvrEXFO9G6tmlFlPQplpwiqirgrIPWnCKMvElaYgI-HiVvXc?auto=format&dpr=1&w=1000");
            celsoPo.setTypeOfUser("product_owner");
            celsoPo.setActive(true);
            celsoPo.setConfirmed(true);
            celsoPo.setRegistrationDate(LocalDate.now());

            register(celsoPo);
        }

        UserEntity userEntity6 = userDao.findUserByUsername("Celsinho");
        if (userEntity6 == null) {
            User celsinho = new User();
            celsinho.setUsername("Celsinho");
            celsinho.setPassword("123");
            celsinho.setEmail("celsinho@agileup.com");
            celsinho.setFirstName("Celsinho");
            celsinho.setLastName("Mendes");
            celsinho.setPhoneNumber("123456789");
            celsinho.setImgURL("https://i.seadn.io/gae/2hDpuTi-0AMKvoZJGd-yKWvK4tKdQr_kLIpB_qSeMau2TNGCNidAosMEvrEXFO9G6tmlFlPQplpwiqirgrIPWnCKMvElaYgI-HiVvXc?auto=format&dpr=1&w=1000");
            celsinho.setTypeOfUser("developer");
            celsinho.setActive(true);
            celsinho.setConfirmed(true);
            celsinho.setRegistrationDate(LocalDate.now());

            register(celsinho);
        }

        UserEntity userEntity7 = userDao.findUserByUsername("Celsao");
        if (userEntity7 == null) {
            User celsoPo = new User();
            celsoPo.setUsername("Celsao");
            celsoPo.setPassword("123");
            celsoPo.setEmail("celsao@po.com");
            celsoPo.setFirstName("Celsão");
            celsoPo.setLastName("Oliveira");
            celsoPo.setPhoneNumber("123456789");
            celsoPo.setImgURL("https://i.seadn.io/gae/2hDpuTi-0AMKvoZJGd-yKWvK4tKdQr_kLIpB_qSeMau2TNGCNidAosMEvrEXFO9G6tmlFlPQplpwiqirgrIPWnCKMvElaYgI-HiVvXc?auto=format&dpr=1&w=1000");
            celsoPo.setTypeOfUser("product_owner");
            celsoPo.setActive(true);
            celsoPo.setConfirmed(true);
            celsoPo.setRegistrationDate(LocalDate.now());

            register(celsoPo);
        }

        UserEntity userEntity8 = userDao.findUserByUsername("Celsito");
        if (userEntity8 == null) {
            User celsoPo = new User();
            celsoPo.setUsername("Celsito");
            celsoPo.setPassword("123");
            celsoPo.setEmail("celsito@agileup.com");
            celsoPo.setFirstName("Celsito");
            celsoPo.setLastName("Castro");
            celsoPo.setPhoneNumber("123456789");
            celsoPo.setImgURL("https://i.seadn.io/gae/2hDpuTi-0AMKvoZJGd-yKWvK4tKdQr_kLIpB_qSeMau2TNGCNidAosMEvrEXFO9G6tmlFlPQplpwiqirgrIPWnCKMvElaYgI-HiVvXc?auto=format&dpr=1&w=1000");
            celsoPo.setTypeOfUser("developer");
            celsoPo.setActive(true);
            celsoPo.setConfirmed(true);
            celsoPo.setRegistrationDate(LocalDate.now());

            register(celsoPo);


        }

        UserEntity userEntity9 = userDao.findUserByUsername("Celsozito");
        if (userEntity9 == null) {
            User celsoPo = new User();
            celsoPo.setUsername("Celsozito");
            celsoPo.setPassword("123");
            celsoPo.setEmail("celsozito@agileup.com");
            celsoPo.setFirstName("Celsozito");
            celsoPo.setLastName("Anastácio");
            celsoPo.setPhoneNumber("123456789");
            celsoPo.setImgURL("https://i.seadn.io/gae/2hDpuTi-0AMKvoZJGd-yKWvK4tKdQr_kLIpB_qSeMau2TNGCNidAosMEvrEXFO9G6tmlFlPQplpwiqirgrIPWnCKMvElaYgI-HiVvXc?auto=format&dpr=1&w=1000");
            celsoPo.setTypeOfUser("developer");
            celsoPo.setActive(true);
            celsoPo.setConfirmed(true);
            celsoPo.setRegistrationDate(LocalDate.now());

            register(celsoPo);
        }

    }

    public boolean recoverPassword (String email){
        UserEntity userEntity = userDao.findUserByEmail(email);
        if (userEntity == null) {
            return false;
        }

        String token = generateNewToken();
        userEntity.setConfirmationToken(token);
        userEntity.setConfirmationTokenDate(LocalDateTime.now().plusMinutes(20));
        userDao.update(userEntity);

        sendPasswordResetEmail (email,
                userEntity.getUsername(),
                "http://localhost:3000/auth/define-password/recover/" + token);
        return true;
    }

    public boolean resetPassword (String token, String newPassword){
        UserEntity userEntity = userDao.findUserByConfirmationToken(token);
        if (userEntity == null) {
            return false;
        } else if (LocalDateTime.now().isAfter(userEntity.getConfirmationTokenDate())) {
            return false;
        }
        userEntity.setPassword(encryptHelper.encryptPassword(newPassword));
        userEntity.setConfirmationToken(null);
        userEntity.setConfirmationTokenDate(null);
        return userDao.update(userEntity);
    }

    public boolean confirmAccount(String confirmationToken, String confirmPasswordConverted) {

        UserEntity userEntity = userDao.findUserByConfirmationToken(confirmationToken);
        if (userEntity == null) {
            return false;
        } else if (LocalDateTime.now().isAfter(userEntity.getConfirmationTokenDate())) {
            return false;
        }
        userEntity.setConfirmed(true);
        userEntity.setPassword(encryptHelper.encryptPassword(confirmPasswordConverted));
        userEntity.setConfirmationToken(null);
        userEntity.setConfirmationTokenDate(null);
        return userDao.update(userEntity);
    }

    public void resendVerificationEmail(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            String confirmationToken = generateNewToken();
            userEntity.setConfirmationToken(confirmationToken);
            userEntity.setConfirmationTokenDate(LocalDateTime.now().plusDays(1));
            userDao.update(userEntity);

            sendVerificationEmail(userEntity.getEmail(),
                    userEntity.getUsername(),
                    "http://localhost:3000/auth/define-password/verify/" + confirmationToken);
        }
    }
}

