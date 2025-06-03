package aut.ap;

import aut.ap.framework.SingletonSessionFactory;

public class Main {
    public static void main(String[] args) {
        SingletonSessionFactory.get()
                .inTransaction(session -> {

                });
        SingletonSessionFactory.close();
    }
}