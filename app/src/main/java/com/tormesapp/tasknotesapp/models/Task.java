package com.tormesapp.tasknotesapp.models;


public class Task {
    private int id;
    private String title;
    private String description;
    private String  isCompleted;
    private String fk_usuario;

    public String getFk_usuario() {
        return fk_usuario;
    }

    public void setFk_usuario(String fk_usuario) {
        this.fk_usuario = fk_usuario;
    }

    public Task() {
    }

// Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String  isCompleted() {
        return isCompleted;
    }

    public String  setCompleted(String  completed) {
        isCompleted = completed;
        return completed;
    }
}
