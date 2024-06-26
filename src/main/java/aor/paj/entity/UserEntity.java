package aor.paj.entity;

import jakarta.persistence.*;

import java.io.Serializable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="user")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.findUserByEmail", query = "SELECT u FROM UserEntity u WHERE u.email = :email")
@NamedQuery(name = "User.findUserByToken", query = "SELECT DISTINCT u FROM UserEntity u WHERE u.token = :token")
@NamedQuery(name = "User.findAllUsers", query = "SELECT u FROM UserEntity u " )
@NamedQuery(name="User.findUserByName", query = "SELECT u FROM UserEntity u WHERE u.firstName=:name OR u.lastName = :name")
@NamedQuery(name="User.findUserByConfirmationToken", query = "SELECT u FROM UserEntity u WHERE u.confirmationToken = :confirmationToken")

//para dashboard
@NamedQuery(name="User.countUsers", query = "SELECT COUNT(u) FROM UserEntity u")
@NamedQuery(name="User.countConfirmedUsers", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.isConfirmed = true")
@NamedQuery(name="User.countUnconfirmedUsers", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.isConfirmed = false")
@NamedQuery(name="User.countActiveUsers", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.isActive = true")
@NamedQuery(name="User.countInactiveUsers", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.isActive = false")
@NamedQuery(name = "User.countUsersByRegistrationDate",
		query = "SELECT u.registrationDate, COUNT(u) FROM UserEntity u GROUP BY u.registrationDate ORDER BY u.registrationDate ASC")



public class UserEntity implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="username", nullable=false, unique = false, updatable = true)
	private String username;

	@Column(name="email", nullable=false, unique = true, updatable = true)
	private String email;

	//user's name
	@Column(name="firstName", nullable=false, unique = false, updatable = true)
	private String firstName;

	@Column(name="lastName", nullable=false, unique = false, updatable = true)
	private String lastName;

	@Column(name="phoneNumber", nullable=false, unique = false, updatable = true)
	private String phoneNumber;

	@Column(name="token", nullable=true, unique = true, updatable = true)
	private String token;

	@Column(name="tokenValidity", nullable=true, unique = false, updatable = true)
	private LocalDateTime tokenValidity;

	@Column(name="tokenRefreshTime", nullable=true, unique = false, updatable = true)
	private int tokenRefreshTime;

	@Column(name="password", nullable=false, unique = false, updatable = true)
	private String password;

	@Column(name="imgURL", nullable=false, unique = false, updatable = true)
	private String imgURL;
	@Column(name="isActive", nullable=false, unique = false, updatable = true)
	private boolean isActive;

	@Column(name="typeOfUser", nullable=false, unique = false, updatable = true)
	private String typeOfUser;

	@Column(name="isConfirmed", nullable=false, unique = false, updatable = true)
	private boolean isConfirmed;

	@Column (name="confirmationToken", nullable=true, unique = false, updatable = true)
	private String confirmationToken;

	@Column (name="confirmationTokenDate", nullable=true, unique = false, updatable = true)
	private LocalDateTime confirmationTokenDate;

	@Column (name="registrationDate", nullable=false, unique = false, updatable = true)
	private LocalDate registrationDate;


	@OneToMany(mappedBy = "owner")
	private Set<TaskEntity> tasks;


	//default empty constructor
	public UserEntity() {}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}

	public Set<TaskEntity> getTasks() {
		return tasks;
	}

	public void setTasks(Set<TaskEntity> tasks) {
		this.tasks = tasks;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getImgURL() {
		return imgURL;
	}

	public void setImgURL(String imgURL) {
		this.imgURL = imgURL;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getTypeOfUser() {
		return typeOfUser;
	}

	public void setTypeOfUser(String typeOfUser) {
		this.typeOfUser = typeOfUser;
	}

	public boolean isConfirmed() {
		return isConfirmed;
	}

	public void setConfirmed(boolean confirmed) {
		isConfirmed = confirmed;
	}

	public String getConfirmationToken() {
		return confirmationToken;
	}

	public void setConfirmationToken(String confirmationToken) {
		this.confirmationToken = confirmationToken;
	}

	public LocalDateTime getConfirmationTokenDate() {
		return confirmationTokenDate;
	}

	public void setConfirmationTokenDate(LocalDateTime confirmationTokenDate) {
		this.confirmationTokenDate = confirmationTokenDate;
	}

	public LocalDate getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(LocalDate registrationDate) {
		this.registrationDate = registrationDate;
	}

	public LocalDateTime getTokenValidity() {
		return tokenValidity;
	}

	public void setTokenValidity(LocalDateTime tokenValidity) {
		this.tokenValidity = tokenValidity;
	}

	public int getTokenRefreshTime() {
		return tokenRefreshTime;
	}

	public void setTokenRefreshTime(int tokenRefreshTime) {
		this.tokenRefreshTime = tokenRefreshTime;
	}
}
