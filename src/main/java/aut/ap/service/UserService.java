package aut.ap.service;

import aut.ap.framework.SingletonSessionFactory;
import aut.ap.model.User;
import jakarta.transaction.Transaction;
import org.hibernate.Session;

import java.util.List;
import java.util.Scanner;

public class UserService {

    public static User persist(String firstName, String lastName, int age, String email, String password) {
        User u = new User(firstName, lastName, age, email, password);
        SingletonSessionFactory.get()
                .inTransaction(session -> {
                    session.persist(u);
                });
        return u;
    }

    public static void login(Scanner scn) {
        System.out.println("\n--- Login ---");
        System.out.print("Email: ");
        String email = scn.nextLine();

        System.out.print("Password: ");
        String password = scn.nextLine();

        try {
            User user = SingletonSessionFactory.get()
                    .fromTransaction(session ->
                            session.createNativeQuery("select * from users" +
                                            " where username = :given_email", User.class)
                                    .setParameter("given_email", email)
                                    .getSingleResultOrNull());

            if (user == null || !user.getPassword().equals(password)) {
                System.out.println("Error: Invalid credentials!");
            } else {
                System.out.println("\nWelcome, " + user.getFirstName() + " " + user.getLastName() + "!");
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
        }
    }

    public static void signup(Scanner scn) {
        System.out.println("\n--- Sign Up ---");

        System.out.print("First Name: ");
        String firstName = scn.nextLine();

        System.out.print("Last Name: ");
        String lastName = scn.nextLine();

        System.out.print("Age: ");
        int age = Integer.parseInt(scn.nextLine());

        System.out.print("Email: ");
        String email = scn.nextLine();

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
                                            " where username = :given_email", User.class)
                                    .setParameter("given_email", email)
                                    .getSingleResultOrNull());
            if (user != null) {
                System.out.println("Error: an account with this email already exists!");
                return;
            }
            persist(firstName, lastName, age, email, password);
            System.out.println("Registered successfully!");
        } catch (Exception e) {
            System.out.println("Error during sign up: " + e.getMessage());
        }
    }
}
