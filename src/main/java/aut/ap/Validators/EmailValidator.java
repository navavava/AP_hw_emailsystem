package aut.ap.Validators;

import aut.ap.model.Email;
import aut.ap.model.UserEmails;

public class EmailValidator {
    public static void validate(Email email) throws Exception {
        if (!UserValidator.validate(email.getSender()) || email.getSubject().isEmpty())
            throw new Exception("Error!: Invalid email.");
    }


}
