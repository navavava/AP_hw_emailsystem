package aut.ap.service;

import aut.ap.framework.SingletonSessionFactory;
import aut.ap.model.User;

public class UserService {

    public static User persist(String firstName, String lastName, String email, String password) {
        User u = new User(firstName, lastName, email, password);
        SingletonSessionFactory.get()
                .inTransaction(session -> {
                    session.persist(u);
                });
        return u;
    }

}
