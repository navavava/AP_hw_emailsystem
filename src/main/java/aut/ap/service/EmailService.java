package aut.ap.service;

import aut.ap.Validators.EmailValidator;
import aut.ap.framework.*;
import aut.ap.model.*;

import java.util.Random;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EmailService {

    private static String generateUniqueCode() {
        String chars = "abcdefghijklmnopqrstuvwxyz123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    public static void setFlagAsRead(int emailId, int userId) {
        SingletonSessionFactory.get()
                .inTransaction(session -> session.createNativeQuery(
                                "update user_emails" +
                                        " set read_flag = true" +
                                        " where email_id = :emailId and user_id = :userId")
                        .setParameter("emailId", emailId)
                        .setParameter("userId", userId)
                        .executeUpdate());
    }

    public static void shortview(Email email) {
        try {
            EmailValidator.validate(email);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("+ " + email.getSender().getEmail() + " - " + email.getSubject() + " (" + email.getCode() + ")");
    }

    //check if user is part of email
    public static boolean checkUserAccess(User user, String emailCode) {
        Email e = findEmailByCode(emailCode);
        return SingletonSessionFactory.get()
                .fromTransaction(session -> {
                    Boolean isSender = session.createNativeQuery(
                                    "select count(*) > 0 from emails " +
                                            "where code = :emailCode and user_id = :userId",
                                    Boolean.class)
                            .setParameter("emailCode", emailCode)
                            .setParameter("userId", user.getId())
                            .getSingleResult();

                    Boolean isReceiver = session.createNativeQuery(
                                    "select count(*) > 0 from user_emails " +
                                            "where email_id = :emailCode and user_id = :userId",
                                    Boolean.class)
                            .setParameter("emailCode", e.getId())
                            .setParameter("userId", user.getId())
                            .getSingleResult();
                    return isSender || isReceiver;
                });
    }

    public static void send(User sender, Scanner scn) {
        System.out.println("How many recipients does your email have?");
        int recNum = scn.nextInt();
        scn.nextLine();
        ArrayList<User> recipients = new ArrayList<>();
        System.out.println("Recipient(s):\n");
        for (int i = 0; i < recNum; i++) {
            try {
                recipients.add(UserService.findByUsername(standardizeUsername(scn.nextLine())));
            } catch (Exception e) {
                System.out.println(e.getMessage() + " Try again.");
                i--;
            }
        }
        System.out.println("Subject: ");
        String subject = scn.nextLine();
        System.out.println("Body: (type \"EOF\" to finish)");
        StringBuilder bodyBuilder = new StringBuilder();
        boolean isFirstLine = true;

        while (scn.hasNextLine()) {
            String line = scn.nextLine();
            if (line.equals("EOF")) {
                break;
            }
            if (isFirstLine) {
                bodyBuilder.append(line);
                isFirstLine = false;
            } else {
                bodyBuilder.append("\n").append(line);
            }
        }
        String body = bodyBuilder.toString();

        String code = generateUniqueCode();
        try {
            EmailValidator.validate(new Email(sender, code, subject, body, LocalDate.now(), null));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        persist(sender, code, subject, body, LocalDate.now(), null, recipients);
    }

    public static void reply(User user, Scanner scn) {
        System.out.println("Code: ");
        String parentCode = scn.nextLine();
        boolean access = checkUserAccess(user, parentCode);
        if (!access) {
            System.out.println("Error: You cant read this email.");
            return;
        }
        System.out.println("Body: (type \"EOF\" to finish)");
        StringBuilder bodyBuilder = new StringBuilder();
        boolean isFirstLine = true;

        while (scn.hasNextLine()) {
            String line = scn.nextLine();
            if (line.equals("EOF")) {
                break;
            }
            if (isFirstLine) {
                bodyBuilder.append(line);
                isFirstLine = false;
            } else {
                bodyBuilder.append("\n").append(line);
            }
        }
        String body = bodyBuilder.toString();

        String childCode = generateUniqueCode();
        Email parent = findEmailByCode(parentCode);
        List<User> recipients = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select u.id, u.first_name, u.last_name, u.username, u.password from users as u" +
                                        " join user_emails ue on ue.user_id = u.id" +
                                        " where email_id = :emailId and u.id != :userId",
                                User.class)
                        .setParameter("emailId", parent.getId())
                        .setParameter("userId", user.getId())
                        .getResultList());
        setFlagAsRead(parent.getId(), user.getId());
        recipients.add(parent.getSender());
        persist(user, childCode, "[Re] " + parent.getSubject(), body, LocalDate.now(), parent, recipients);
    }

    public static void forward(User user, Scanner scn) {
        System.out.println("Code: ");
        String parentCode = scn.nextLine();
        boolean access = checkUserAccess(user, parentCode);
        if (!access) {
            System.out.println("Error: You cant read this email.");
            return;
        }
        System.out.println("How many recipients does your email have?");
        int recNum = scn.nextInt();
        scn.nextLine();
        ArrayList<User> recipients = new ArrayList<>();
        System.out.println("Recipient(s):\n");
        for (int i = 0; i < recNum; i++) {
            try {
                recipients.add(UserService.findByUsername(standardizeUsername(scn.nextLine())));
            } catch (Exception e) {
                System.out.println(e.getMessage() + " Try again.");
                i--;
            }
        }
        String childCode = generateUniqueCode();
        Email parent = findEmailByCode(parentCode);

        setFlagAsRead(parent.getId(), user.getId());
        persist(user, childCode, "[Fw] " + parent.getSubject(), parent.getBody(), LocalDate.now(), parent, recipients);
    }

    public static void view(User user, Scanner scn) {
        System.out.println("\n---List of commands---\n[A]ll emails, [U]nread emails, [S]ent emails, Read by [C]ode, View [R]eplies by code: ");
        String command = scn.nextLine().trim().toLowerCase();
        switch (command) {
            case "a":
                viewAll(user);
                break;
            case "u":
                viewUnreadEmails(user);
                break;
            case "s":
                viewSentEmails(user);
                break;
            case "c":
                System.out.println("Code:");
                String readCode = scn.nextLine();
                readByCode(user, readCode);
                break;
            case "r":
                System.out.println("Code:");
                String replyCode = scn.nextLine();
                if (checkUserAccess(user, replyCode)) {
                    Email e = findEmailByCode(replyCode);
                    viewReplies(e);
                } else
                    System.out.println("Error: You cant read this email.");
                break;
            default:
                System.out.println("Error: Invalid command!");
                break;
        }

    }

    public static void viewAll(User user) {
        List<Email> AllEmails = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select e.id, e.user_id, e.code, e.subject, e.body, e.parent_id, e.date from emails as e" +
                                        " join user_emails as ue on ue.email_id = e.id" +
                                        " where ue.user_id = :userId or e.user_id = :userId ",
                                Email.class)
                        .setParameter("userId", user.getId())
                        .getResultList());
        if (!AllEmails.isEmpty()) {
            System.out.println("All Emails:\n\n" + AllEmails.size() + " emails:\n");
            for (Email e : AllEmails) {
                shortview(e);
            }
        } else
            System.out.println("You currently have no emails.");
    }

    public static void viewUnreadEmails(User user) {
        List<Email> unreadEmails = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select e.id, e.user_id, e.code, e.subject, e.body, e.parent_id, e.date from emails as e" + //probably something wrong with this too bc of join
                                        " join user_emails as ue on ue.email_id = e.id" +
                                        " where read_flag = false and ue.user_id = :userId",
                                Email.class)
                        .setParameter("userId", user.getId())
                        .getResultList());
        if (!unreadEmails.isEmpty()) {
            System.out.println("Unread Emails:\n\n" + unreadEmails.size() + " unread emails:\n");
            for (Email e : unreadEmails)
                shortview(e);
        } else
            System.out.println("No unread emails.");
    }

    public static void viewSentEmails(User user) {
        List<Email> sentEmails = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select * from emails" +
                                        " where user_id = :userId",
                                Email.class)
                        .setParameter("userId", user.getId())
                        .getResultList());
        System.out.println("Sent Emails:\n" + sentEmails.size() + " sent emails:\n");
        for (Email e : sentEmails) {
            try {
                shortview(e);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void viewReplies(Email email) {
        List<Email> replies = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select * from emails" +
                                        " where parent_id = :parentId",
                                Email.class)
                        .setParameter("parentId", email.getId())
                        .getResultList());
        if (!replies.isEmpty()) {
            System.out.println("Replies:\n" + replies.size() + " replies for this email:\n");
            for (Email e : replies) {
                shortview(e);
            }
        } else
            System.out.println("No replies for this email.");
    }

    public static void readByCode(User user, String code) {
        boolean access = checkUserAccess(user, code);
        if (!access) {
            System.out.println("Error: You cant read this email.");
            return;
        }
        Email e = findEmailByCode(code);
        try {
            System.out.println(e.toString());
            setFlagAsRead(e.getId(), user.getId());
        } catch (Exception ex) {
            System.out.println("Email not found.");
        }
    }

    public static Email findEmailByCode(String code) {
        return SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select * from emails " +
                                        "where code = :code",
                                Email.class)
                        .setParameter("code", code)
                        .getSingleResultOrNull());
    }

    public static Email persist(User sender, String code, String subject, String body, LocalDate date, Email
            parentEmail, List<User> recipients) {
        Email e = new Email(sender, code, subject, body, date, parentEmail);
        SingletonSessionFactory.get()
                .inTransaction(session -> {
                    session.persist(e);
                    for (User receiver : recipients) {
                        UserEmails recipient = new UserEmails(e, receiver);
                        session.persist(recipient);
                    }
                });
        return e;
    }

    public static String standardizeUsername(String email) {
        if (!email.endsWith("@milou.com"))
            email = email + "@milou.com";
        return email;
    }
}



