package aut.ap;

import aut.ap.framework.SingletonSessionFactory;
import aut.ap.service.UserService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scn = new Scanner(System.in);

        while (true) {
            System.out.println("Email System - [L]ogin, [S]ignup or Exit?");
            String choice = scn.nextLine().trim().toLowerCase();

            if (choice.equals("l") || choice.equals("login"))
                UserService.login(scn);
            else if (choice.equals("s") || choice.equals("sign up"))
                UserService.signup(scn);
            else if (choice.equals("exit")) {
                System.out.println("Closing program . . .");
                SingletonSessionFactory.close();
                System.exit(0);
            } else
                System.out.println("Invalid command, try again.");
        }
    }
}
//problem with view unread method
// when i reply to an email that email appears in my unread folder
// invlaid command error after read replies by code command