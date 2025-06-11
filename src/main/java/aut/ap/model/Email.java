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

//    @Basic(optional = false)
//    @Column(name = "user_id")
//    private Integer userId;

    @Basic(optional = false)
    private String subject;

    @Basic(optional = false)
    private String body;

    @Column(name = "parent_id")
    private Integer parentId;

    @Basic(optional = false)
    private LocalDate date;

    @Basic(optional = false)
    private User sender;

    public Email() {
    }

    public Email(User sender, String code, String subject, String body, Integer parentId, LocalDate date) {
        this.sender = sender;
        this.code = code;
        this.subject = subject;
        this.body = body;
        this.parentId = parentId;
        this.date = date;
    }

    @Override
    public String toString() {
        return "Email code: " + code +
                "From: " + sender.getEmail() +
                "Date: " + date +
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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
}
