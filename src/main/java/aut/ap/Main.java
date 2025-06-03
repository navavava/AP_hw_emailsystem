package aut.ap;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;

public class Main {
    private static SessionFactory sessionFactory;

    public static void main(String[] args) {
        setUpSessionFactory();
        Session session = sessionFactory.openSession();
//        try {
//        } catch (Exception e) {
//            System.out.println("Exception in database: " + e.getMessage());
//        }
        session.close();
        closeSessionFactory();
    }

    private static void setUpSessionFactory() {
        sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
    }

    private static void closeSessionFactory() {
        sessionFactory.close();
    }
}


