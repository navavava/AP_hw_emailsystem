package aut.ap.model;

import jakarta.persistence.*;


import java.time.LocalDate;

@Entity
@Table(name = "emails")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic(optional = false)
    @Column(unique = true)
    private String code;

    @Basic(optional = false)
    private String subject;

    @Basic(optional = false)
    private String body;

    @JoinColumn(name = "parent_id")
    @ManyToOne
    private Email parentEmail;

    @Basic(optional = false)
    private LocalDate date;

    @JoinColumn(name = "user_id")
    @ManyToOne(optional = false)
    private User sender;

    public Email() {
    }

    public Email(User sender, String code, String subject, String body, LocalDate date, Email parentEmail) {
        this.sender = sender;
        this.code = code;
        this.subject = subject;
        this.body = body;
        this.date = date;
        this.parentEmail = parentEmail;

    }

    @Override
    public String toString() {
        return "From: " + sender.getEmail() +
                "\nDate: " + date +
                "\nSubject: " + subject +
                "\n\n" + body;
    }

    public Integer getId() {
        return id;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Email getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(Email parentEmail) {
        this.parentEmail = parentEmail;
    }
}
