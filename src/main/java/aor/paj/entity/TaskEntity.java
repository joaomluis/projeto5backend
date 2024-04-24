package aor.paj.entity;

import aor.paj.dto.Category;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name="task")
@NamedQuery(name="Task.findTaskById", query="SELECT a FROM TaskEntity a WHERE a.id = :id")
@NamedQuery(name="Task.findTaskByTitle", query="SELECT a FROM TaskEntity a WHERE a.title = :title")
@NamedQuery(name="Task.findTaskByUser", query="SELECT a FROM TaskEntity a WHERE a.owner = :owner AND a.isActive = true ORDER BY a.priority DESC, a.endDate ASC")
@NamedQuery(name = "Task.findActiveTasks", query = "SELECT a FROM TaskEntity a WHERE a.isActive = true")
@NamedQuery(name = "Task.findActiveTasksOrdered", query = "SELECT a FROM TaskEntity a WHERE a.isActive = true ORDER BY a.priority DESC, a.endDate ASC")
@NamedQuery(name = "Task.findSoftDeletedTasks", query = "SELECT a FROM TaskEntity a WHERE a.isActive = false")
@NamedQuery(name = "Task.findTasksByCategory", query = "SELECT t FROM TaskEntity t WHERE t.category = :category AND t.isActive = true ORDER BY t.priority DESC, t.endDate ASC")
@NamedQuery(name = "Task.findTaskByCategoryName", query =" SELECT t FROM TaskEntity t JOIN t.category c WHERE c.title = :categoryName")
@NamedQuery(name = "Task.findAllTasks", query = "SELECT t FROM TaskEntity t")
@NamedQuery(name = "Task.findFilterTasks", query = "SELECT t FROM TaskEntity t WHERE t.category.idCategory = :category AND t.owner.username=:username AND t.isActive = true ORDER BY t.priority DESC, t.endDate ASC")
@NamedQuery(name="Task.findTaskByUserNameFilter", query="SELECT a FROM TaskEntity a WHERE a.owner.username = :username AND a.isActive = true ORDER BY a.priority DESC, a.endDate ASC")
@NamedQuery(name = "Task.findTasksByCategoryFilter", query = "SELECT t FROM TaskEntity t WHERE t.category.idCategory = :category AND t.isActive = true ORDER BY t.priority DESC, t.endDate ASC")

//para user page
@NamedQuery(name = "Task.countTasksByUser", query = "SELECT COUNT(a) FROM TaskEntity a WHERE a.owner = :owner AND a.isActive = true")
@NamedQuery(name = "Task.countToDoTasksByUser", query = "SELECT COUNT(a) FROM TaskEntity a WHERE a.owner = :owner AND a.isActive = true AND a.state = 'toDo'")
@NamedQuery(name = "Task.countDoingTasksByUser", query = "SELECT COUNT(a) FROM TaskEntity a WHERE a.owner = :owner AND a.isActive = true AND a.state = 'doing'")
@NamedQuery(name = "Task.countDoneTasksByUser", query = "SELECT COUNT(a) FROM TaskEntity a WHERE a.owner = :owner AND a.isActive = true AND a.state = 'done'")

//para dashboard
@NamedQuery(name ="Task.countTaskByState", query = "SELECT COUNT(a) FROM TaskEntity a WHERE a.state = :state AND a.isActive = true")
@NamedQuery(name = "Task.countTasks", query = "SELECT COUNT(a) FROM TaskEntity a")
@NamedQuery(name = "Task.findCategoriesOrderedByUsage",
		query = "SELECT t.category, COUNT(t) FROM TaskEntity t GROUP BY t.category ORDER BY COUNT(t) DESC")
@NamedQuery(name= "Task.findAllCompletedTasks", query = "SELECT t FROM TaskEntity t WHERE t.state = 'done' AND t.isActive = true")
@NamedQuery(name = "Task.countTasksByCompletionDate",
		query = "SELECT t.conclusionDate, COUNT(t) FROM TaskEntity t WHERE t.conclusionDate IS NOT NULL GROUP BY t.conclusionDate")

public class TaskEntity implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column (name="id", nullable = false, unique = true, updatable = false)
	private long id;

	@Column (name="title", nullable = false, unique = true, updatable = true)
	private String title;

	@Column (name="description", nullable = true, unique = false, length = 65535, columnDefinition = "TEXT")
	private String description;

	@Column(name="initialDate", nullable = false, unique = false, updatable = true)
	private LocalDate initialDate;

	@Column(name="endDate", nullable = false, unique = false, updatable = true)
	private LocalDate endDate;

	@Column(name="conclusionDate", nullable = true, unique = false, updatable = true)
	private LocalDate conclusionDate;

	@Column(name="priority", nullable = false, unique = false, updatable = true)
	private int priority;

	@Column(name="state", nullable = false, unique = false, updatable = true)
	private String state;

	@Column(name="is_Active", nullable = false, unique = false, updatable = true)
	private boolean isActive;

	@ManyToOne
	@JoinColumn(name="category", nullable = false, unique = false, updatable = true)
	private CategoryEntity category;

	//Owning Side User - Activity
	@ManyToOne
	@JoinColumn(name="author", nullable = false, unique = false, updatable = true)
	private UserEntity owner;

	
	public TaskEntity() {
		
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public LocalDate getInitialDate() {
		return initialDate;
	}

	public void setInitialDate(LocalDate initialDate) {
		this.initialDate = initialDate;
	}

	public UserEntity getOwner() {
		return owner;
	}

	public void setOwner(UserEntity owner) {
		this.owner = owner;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public CategoryEntity getCategory() {
		return category;
	}

	public void setCategory(CategoryEntity category) {
		this.category = category;
	}

	public LocalDate getConclusionDate() {
		return conclusionDate;
	}

	public void setConclusionDate(LocalDate conclusionDate) {
		this.conclusionDate = conclusionDate;
	}
}
	
    