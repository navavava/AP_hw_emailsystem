package aut.ap.service;

import aut.ap.framework.SingletonSessionFactory;
import aut.ap.model.User;
import jakarta.transaction.Transaction;
import org.hibernate.Session;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class UserService {

    public static User findByUsername(String username) throws Exception {
        User result = SingletonSessionFactory.get()
                .fromTransaction(session -> session.createNativeQuery(
                                "select * from users" +
                                        " where username = :username", User.class
                        )
                        .setParameter("username", username)
                        .getSingleResultOrNull());
        if (result == null)
            throw new Exception("Error: This user doesn't exist.");
        return result;
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
        String email = EmailService.standardizeUsername(scn.nextLine());

        System.out.print("Password: ");
        String password = scn.nextLine();

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
            boolean whileCon = true;

            while (whileCon) {
                System.out.println("\n---List of commands---\n[V]iew, [S]end, [R]eply, [F]orward, Go [B]ack.");
                String command = scn.nextLine().trim().toLowerCase();

                switch (command) {
                    case "v":
                        EmailService.view(user, scn);
                        break;
                    case "s":
                        EmailService.send(user, scn);
                        break;
                    case "r":
                        EmailService.reply(user, scn);
                        break;
                    case "f":
                        EmailService.forward(user, scn);
                        break;
                    case "b":
                        whileCon = false;
                        break;
                    default:
                        System.out.println("Invalid command, try again.");
                }
            }
        }
    }

    public static void signup(Scanner scn) {
        System.out.println("\n--- Sign Up ---");

        System.out.print("Full name: ");
        String fullName = scn.nextLine();
        String firstName = "";
        String lastName = "";
        try {
            firstName = fullName.split(" ")[0];
            lastName = fullName.split(" ")[1];
        } catch (Exception e) {
            System.out.println("Error! Invalid name format. Separate your first and last name with a space.");
            return;
        }

        System.out.print("Email: ");
        String email = EmailService.standardizeUsername(scn.nextLine());

        System.out.print("Password: ");
        String password = scn.nextLine();

        while (password.length() < 8) {
            System.out.println("Error: Password must be at least 8 characters");
            System.out.println("Password: ");
            password = scn.nextLine();
        }
        User user = null;
        try {
            user = findByUsername(email);
        } catch (Exception _) {
        }
        if (user != null) {
            System.out.println("Error: an account with this email already exists!");
            return;
        } else {
            persist(firstName, lastName, email, password);
            System.out.println("Your new account is created. \nGo ahead and login!");
        }
    }
}
