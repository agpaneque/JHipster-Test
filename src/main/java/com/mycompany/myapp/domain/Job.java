package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Job.
 */
@Table("job")
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column("job_title")
    private String jobTitle;

    @Column("job_description")
    private String jobDescription;

    @Column("job_hours")
    private Long jobHours;

    @JsonIgnoreProperties(value = { "jobs", "department" }, allowSetters = true)
    @Transient
    private Employee employee;

    @Column("employee_id")
    private Long employeeId;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job id(Long id) {
        this.id = id;
        return this;
    }

    public String getJobTitle() {
        return this.jobTitle;
    }

    public Job jobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobDescription() {
        return this.jobDescription;
    }

    public Job jobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
        return this;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Long getJobHours() {
        return this.jobHours;
    }

    public Job jobHours(Long jobHours) {
        this.jobHours = jobHours;
        return this;
    }

    public void setJobHours(Long jobHours) {
        this.jobHours = jobHours;
    }

    public Employee getEmployee() {
        return this.employee;
    }

    public Job employee(Employee employee) {
        this.setEmployee(employee);
        this.employeeId = employee != null ? employee.getId() : null;
        return this;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        this.employeeId = employee != null ? employee.getId() : null;
    }

    public Long getEmployeeId() {
        return this.employeeId;
    }

    public void setEmployeeId(Long employee) {
        this.employeeId = employee;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Job)) {
            return false;
        }
        return id != null && id.equals(((Job) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Job{" +
            "id=" + getId() +
            ", jobTitle='" + getJobTitle() + "'" +
            ", jobDescription='" + getJobDescription() + "'" +
            ", jobHours=" + getJobHours() +
            "}";
    }
}
