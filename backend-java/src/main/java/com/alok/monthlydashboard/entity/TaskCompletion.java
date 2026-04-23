package com.alok.monthlydashboard.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "task_completions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_task_occurrence", columnNames = {"task_id", "occurrence_date"})
        }
)
public class TaskCompletion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "occurrence_date", nullable = false)
    private LocalDate occurrenceDate;

    @Column(name = "completion_date", nullable = false)
    private LocalDate completionDate;

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public LocalDate getOccurrenceDate() {
        return occurrenceDate;
    }

    public void setOccurrenceDate(LocalDate occurrenceDate) {
        this.occurrenceDate = occurrenceDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }
}