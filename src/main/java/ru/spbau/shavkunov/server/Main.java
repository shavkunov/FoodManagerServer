package ru.spbau.shavkunov.server;

public class Main {
    public static String databaseName = "content.db";
    public static void main(String[] args) {
        new Server().start();
    }
}