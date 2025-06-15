package aut.ap.service;

import aut.ap.framework.SingletonSessionFactory;
import aut.ap.model.User;
import jakarta.transaction.Transaction;
import org.hibernate.Session;

import java.util.List;
import java.util.Scanner;

public class UserService {

    public static User findByUsername(String username) {
        return SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select * from users" +
                                        " where username = :username", User.class
                        )
                        .setParameter("username", username)
                        .getSingleResultOrNull());
    }

    public static User persist(String firstName, String lastName, String email, String password) {
        User u = new User(firstName, lastName, email, password);
        SingletonSessionFactory.get()
                .inTransaction(session -> session.persist(u));
        return u;
    }

    public static void login(Scanner scn) {
        System.out.println("\n--- Login ---");
        System.out.print("Email: ");
        String email = scn.nextLine();
        EmailService.standardizeUsername(email);

        System.out.print("Password: ");
        String password = scn.nextLine();

        try {
            User user = SingletonSessionFactory.get()
                    .fromTransaction(session ->
                            session.createNativeQuery("select * from users" +
                                            " where username = :email", User.class)
                                    .setParameter("email", email)
                                    .getSingleResultOrNull());

            if (user == null || !user.getPassword().equals(password)) {
                System.out.println("Error: Invalid credentials! Try again please.");
            } else {
                System.out.println("\nWelcome back, " + user.getFirstName() + " " + user.getLastName() + "!");
                //TODO:view unread emails method and send,view,reply,forward
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
        }
    }

    public static void signup(Scanner scn) {
        System.out.println("\n--- Sign Up ---");

        System.out.print("Full name: ");
        String fullName = scn.nextLine();

        String firstName = fullName.split(" ")[0];
        String lastName = fullName.split(" ")[1];

        System.out.print("Email: ");
        String email = scn.nextLine();
        EmailService.standardizeUsername(email);

        System.out.print("Password: ");
        String password = scn.nextLine();

        while (password.length() < 8) {
            System.out.println("Error: Password must be at least 8 characters");
            System.out.println("Password: ");
            password = scn.nextLine();
        }

        try {
            User user = SingletonSessionFactory.get()
                    .fromTransaction(session ->
                            session.createNativeQuery("select * from users" +
                                            " where username = :email", User.class)
                                    .setParameter("email", email)
                                    .getSingleResultOrNull());
            if (user != null) {
                System.out.println("Error: an account with this email already exists!");
                return;
                //TODO: how to make this not end after the error
            }
            persist(firstName, lastName, email, password);
            System.out.println("Your new account is created. \nGo ahead and login!");
        } catch (Exception e) {
            System.out.println("Error during sign up: " + e.getMessage());
        }
    }
}
