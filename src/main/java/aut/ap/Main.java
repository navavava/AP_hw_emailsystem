package aut.ap;

import aut.ap.framework.SingletonSessionFactory;
import aut.ap.service.UserService;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scn = new Scanner(System.in);

        System.out.println("Email System - (L)ogin or (S)ignup?");
        String choice = scn.nextLine().trim().toLowerCase();

        if (choice.equals("l") || choice.equals("login"))
            UserService.login(scn);
        else if (choice.equals("s") || choice.equals("sign up"))
            UserService.signup(scn);
        else
            System.out.println("Invalid command, closing program . . .");

        SingletonSessionFactory.close();
    }
}