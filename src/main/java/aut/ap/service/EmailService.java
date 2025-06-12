package aut.ap.service;

import aut.ap.framework.*;
import aut.ap.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmailService {
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

    //send email

    //view email
    // view all : switch case with commands
    // view unread
    // view sent
    // read by code
    // show replies : format similar to unread
    //reply


    //forward
//    public static Email forward(Email originalMail){
//        Email newMail = new Email(originalMail.get)
//    }

    //persist
    public static Email persist(User sender, String code, String subject, String body, LocalDate date) {
        Email e = new Email(sender, code, subject, body, date);
        SingletonSessionFactory.get()
                .inTransaction(session -> {
                    session.persist(e);
                });
        return e;
    }

    //standardize username
    public static void standardizeUsername(String email) {
        if (!email.endsWith("@milou.com"))
            email = email + "@milou.com";
    }
}
