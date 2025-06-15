package aut.ap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_emails")
public class UserEmails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name = "email_id")
    @OneToMany
    private Email email;

    @JoinColumn(name = "user_id")
    @OneToMany
    private User receiver;

    @Column(name = "read_flag", columnDefinition = "boolean default false")
    private boolean readFlag;

    public UserEmails() {
    }

    public UserEmails(Email email, User receiver) {
        this.email = email;
        this.receiver = receiver;
        readFlag = false;
    }

    public Integer getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public void setReadFlag(boolean readFlag) {
        this.readFlag = readFlag;
    }

    public boolean getReadFlag(boolean readFlag) {
        return readFlag;
    }
}
