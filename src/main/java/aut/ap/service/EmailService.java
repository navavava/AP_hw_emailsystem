package aut.ap.service;

import aut.ap.framework.*;
import aut.ap.model.*;

import java.time.LocalDateTime;
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

    //change readFlag
    public static void setFlagAsRead(int emailId, int userId) {
        SingletonSessionFactory.get()
                .inTransaction(session -> session.createNativeQuery(
                                "update user_emails" +
                                        " set read_flag = true" +
                                        " where email_id = :emailId and user_id = :userId")
                        .setParameter("email_id", emailId)
                        .setParameter("user_id", userId));
    }

    public static void shortview(Email email) throws Exception {
        if (email == null) {
            throw new Exception("Email not found!");
        }
        System.out.println("+ " + email.getSender().getEmail() + " - " + email.getSubject() + " (" + email.getCode() + ")");
    }

    //check if user is part of email
    public static boolean checkUserAccess(User user, String emailCode) {
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
                            .setParameter("emailCode", emailCode)
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
            //TODO: error handling
            recipients.add(UserService.findByUsername(standardizeUsername(scn.nextLine())));
        }
        System.out.println("Subject: ");
        String subject = scn.nextLine();
        System.out.println("Body: (press ctrl + D to finish)");
        scn.useDelimiter("\\Z");
        String body = scn.next();
        String code = generateUniqueCode();
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
        System.out.println("Body: (press ctrl + D to finish)");
        scn.useDelimiter("\\Z");
        String body = scn.next();
        String childCode = generateUniqueCode();
        Email parent = findEmailByCode(parentCode);
        List<User> recipients = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select * from users as u" +
                                        " join user_emails ue on ue.user_id = " +
                                        " where code = :code",
                                User.class)
                        .setParameter("code", parentCode)
                        .getResultList());
        setFlagAsRead(parent.getId(), user.getId());
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
            //TODO: error handling
            recipients.add(UserService.findByUsername(standardizeUsername(scn.nextLine())));
        }
        String childCode = generateUniqueCode();
        Email parent = findEmailByCode(parentCode);

        setFlagAsRead(parent.getId(), user.getId());
        persist(user, childCode, "[Fw] " + parent.getSubject(), parent.getBody(), LocalDate.now(), parent, recipients);
    }

    public static void view(Scanner scn, User user) {
        System.out.println("[A]ll emails, [U]nread emails, [S]ent emails, Read by [C]ode, View [R]eplies by code: \n");
        String command = scn.nextLine();
        switch (command) {
            case "All emails":
                viewAll(user);
                break;
            case "Unread emails":
                viewUnreadEmails(user);
                break;
            case "Sent emails":
                viewSentEmails(user);
                break;
            case "Read by Code":
                System.out.println("Code:\n");
                String readCode = scn.nextLine();
                readByCode(user, readCode);
                break;
            case "View Replies by Code":
                System.out.println("Code:\n");
                String replyCode = scn.nextLine();
                Email e = findEmailByCode(replyCode);
                viewReplies(e);
            default:
                System.out.println("Error: Invalid command!");
                break;
        }

    }

    public static void viewAll(User user) {
        List<Email> unreadEmails = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select * from emails as e" +
                                        "join user_emails as ue on ue.email_id = e.id" +
                                        "where ue.user_id = :userId or (e.id = :userId )",
                                Email.class)
                        .setParameter("userId", user.getId())
                        .getResultList());
        System.out.println("Unread Emails:\n\n" + unreadEmails.size() + " unread emails:\n");
        for (Email e : unreadEmails) {
            try {
                shortview(e);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void viewUnreadEmails(User user) {
        List<Email> emails = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select * from emails as e" +
                                        "join user_emails as ue on ue.email_id = e.id" +
                                        "where read_flag = false and user_id = :userId",
                                Email.class)
                        .setParameter("userId", user.getId())
                        .getResultList());
        System.out.println("ALL Emails:\n\n" + emails.size() + " emails:\n");
        for (Email e : emails) {
            try {
                shortview(e);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void viewSentEmails(User user) {
        List<Email> sentEmails = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select * from emails " +
                                        "where user_id = :userId",
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

    // add user interaction
    public static void viewReplies(Email email) {
        List<Email> replies = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select * from emails " +
                                        "where parent_id = :parentId",
                                Email.class)
                        .setParameter("parent_id", email.getId())
                        .getResultList());
        System.out.println("Replies:\n" + replies.size() + " replies for this email:\n");
        for (Email e : replies) {
            try {
                shortview(e);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    // add user interaction
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
                                "select emails " +
                                        "where code = :code",
                                Email.class)
                        .setParameter("code", code)
                        .getSingleResultOrNull());
    }

    //persist
    public static Email persist(User sender, String code, String subject, String body, LocalDate date, Email
            parentEmail, List<User> recipients) {
        Email e = new Email(sender, code, subject, body, date, parentEmail);
        SingletonSessionFactory.get()
                .inTransaction(session -> {
                    session.persist(e);
                    for (User receiver : recipients) {
                        UserEmails userEmail = new UserEmails();
                        userEmail.setEmail(e);
                        userEmail.setReceiver(receiver);
                        session.persist(userEmail);
                    }
                });

        return e;
    }

    //standardize username
    public static String standardizeUsername(String email) {
        if (!email.endsWith("@milou.com"))
            email = email + "@milou.com";
        return email;
    }
}



