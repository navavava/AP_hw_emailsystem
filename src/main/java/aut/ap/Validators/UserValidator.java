package aut.ap.Validators;

import aut.ap.model.User;

public class UserValidator {
    public static boolean validate(User user) {
        return (!user.getFirstName().isEmpty()) && (!user.getLastName().isEmpty()) && (!user.getEmail().isEmpty()) && (user.getPassword().length() >= 8);
    }
}
