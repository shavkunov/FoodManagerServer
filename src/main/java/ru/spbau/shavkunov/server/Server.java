package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Server {
    public static final int port = 48800; // free random port;
    public static final String LOCAL_IP = "192.168.211.199"; // my local IP address
    public static final String PUBLIC_IP = "217.118.78.126"; // my public IP address
    private static final String getRecipeCommand = "/getRecipe";
    private static final String getRecipeCategoriesCommand = "/getRecipeCategories";
    private static final String getRecipeIngredientsCommand = "/getRecipeIngredients";
    private static final String getRecipeStepsCommand = "/getRecipeSteps";
    private static final String getRecipesByFilterCommand = "/getRecipesByFilter";
    private static final String getUserSettingsCommand = "/getUserSettings";
    private static final String getUserLikeCommand = "/getUserLike";
    private static final String getRecipeLikesCommand = "/getRecipeLikes";
    private static final String getFavoritesCommand = "/getFavorites";
    private static final String getRecipesOfCategoryCommand = "/getRecipesOfCategory";
    private static final String getCategoryByIDCommand = "/getCategoryByID";
    private static final String getCategoriesListCommand = "/getCategoriesList";
    private static final String setUserLikeCommand = "/setLike";
    private static final String setUserNotLikeCommand = "/setNotLike";
    private static final String addToFavoritesCommand = "/addToFavorites";
    private static final String removeFromFavoritesCommand = "/removeFromFavorites";
    private static final String saveUserSettingsCommand = "/saveUserSettings";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + Main.databaseName);
        }

        return connection;
    }

    public Server() {}

    public void start() {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(getRecipeCommand, new getRecipeHandler());
            server.createContext(getRecipeCategoriesCommand, new getRecipeCategoriesHangler());
            server.createContext(getRecipeIngredientsCommand, new getRecipeIngredientsHandler());
            server.createContext(getRecipeStepsCommand, new getRecipeStepsHandler());
            server.createContext(getRecipesByFilterCommand, new getRecipesByFilterHandler());
            server.createContext(getUserSettingsCommand, new getUserSettingsHandler());
            server.createContext(getUserLikeCommand, new getUserLikeHandler());
            server.createContext(getRecipeLikesCommand, new getRecipeLikesHandler());
            server.createContext(setUserLikeCommand, new setLikeHandler());
            server.createContext(setUserNotLikeCommand, new setNotLikeHandler());
            server.createContext(addToFavoritesCommand, new addToFavoritesHandler());
            server.createContext(removeFromFavoritesCommand, new removeFromFavoritesHandler());
            server.createContext(getFavoritesCommand, new getFavoritesHandler());
            server.createContext(getRecipesOfCategoryCommand, new getRecipesOfCategoryHandler());
            server.createContext(getCategoryByIDCommand, new getCategoryByIDHandler());
            server.createContext(getCategoriesListCommand, new getCategoryListHandler());
            server.createContext(saveUserSettingsCommand, new saveUserSettingsHandler());


            // TODO : others contexts
            server.setExecutor(null);
            server.start();
            System.out.println("Server started.");
        } catch (Exception e) {
            System.out.println("Exception while creating Server on port " + port + ": " + e.getMessage());
        }
    }
}
