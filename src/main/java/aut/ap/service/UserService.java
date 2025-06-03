package aut.ap.service;

import aut.ap.framework.SingletonSessionFactory;
import aut.ap.model.User;

import java.util.List;
import java.util.Scanner;

public class UserService {

    public static User persist(String firstName, String lastName, String email, String password) {
        User u = new User(firstName, lastName, email, password);
        SingletonSessionFactory.get()
                .inTransaction(session -> {
                    session.persist(u);
                });
        return u;
    }

    //fix this to get specific user
//    public static List<User> getAll() {
//        return SingletonSessionFactory.get()
//                .fromTransaction(session ->
//                        session.createNativeQuery("select * from users", User.class)
//                                .getResultList());
//    }

    public static void login(Scanner scn) {
        System.out.println("\n--- Login ---");
        System.out.print("Email: ");
        String email = scn.nextLine();

        System.out.print("Password: ");
        String password = scn.nextLine();

        try {
            User user = SingletonSessionFactory.get()
                    .fromTransaction(session ->
                            session.createNativeQuery("select * from users" + "where password = :given password", User.class)
                                    .setParameter("given_password", password)
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
}
