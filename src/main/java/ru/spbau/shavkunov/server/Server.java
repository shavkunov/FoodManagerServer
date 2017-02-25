package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Server {
    public static final int port = 48800; // free random port;
    public static final String LOCAL_IP = "192.168.211.199"; // my local IP address
    public static final String PUBLIC_IP = "217.118.78.126"; // my public IP address
    private static final String getRecipeCommand = "/getRecipe";
    private static final String getRecipeCategoriesCommand = "/getRecipeCategories";
    private static final String getRecipeIngredientsCommand = "/getRecipeIngredients";

    public Server() {}

    public void start() {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(getRecipeCommand, new getRecipeHandler());
            server.createContext(getRecipeCategoriesCommand, new getRecipeCategoriesHangler());
            server.createContext(getRecipeIngredientsCommand, new getRecipeIngredientsHandler());
            // TODO : others contexts
            server.setExecutor(null);
            server.start();
            System.out.println("Server started.");
        } catch (Exception e) {
            System.out.println("Exception while creating Server on port " + port + ": " + e.getMessage());
        }
    }
}
