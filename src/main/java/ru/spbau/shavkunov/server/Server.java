package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;

import static ru.spbau.shavkunov.server.Commands.*;

public class Server {
    public static final int port = 48800; // free random port;
    public static final String LOCAL_IP = "192.168.211.199"; // my local IP address
    public static final String PUBLIC_IP = "217.118.78.126"; // my public IP address
    public static final String CLOUDINARY_URL = "cloudinary://285162791646134:yGqzM1FdReQ8uPa1taEUZihoNgI@dxc952wrd";

    private static Connection connection = null;

    public static Connection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");

        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + Main.databaseName);
        }

        return connection;
    }

    public Server() {}

    public void start() {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(getRecipeCommand, new GetRecipeHandler());
            server.createContext(getRecipeCategoriesCommand, new GetRecipeCategoriesHandler());
            server.createContext(getRecipeIngredientsCommand, new GetRecipeIngredientsHandler());
            server.createContext(getRecipeStepsCommand, new GetRecipeStepsHandler());
            server.createContext(getRecipesByFilterCommand, new GetRecipesByFilterHandler());
            server.createContext(getUserSettingsCommand, new GetUserSettingsHandler());
            server.createContext(getUserLikeCommand, new GetUserLikeHandler());
            server.createContext(getRecipeLikesCommand, new GetRecipeLikesHandler());
            server.createContext(setUserLikeCommand, new SetLikeHandler());
            server.createContext(setUserNotLikeCommand, new SetNotLikeHandler());
            server.createContext(addToFavoritesCommand, new AddToFavoritesHandler());
            server.createContext(removeFromFavoritesCommand, new RemoveFromFavoritesHandler());
            server.createContext(getFavoritesCommand, new GetFavoritesHandler());
            server.createContext(getRecipesOfCategoryCommand, new GetRecipesOfCategoryHandler());
            server.createContext(getCategoryByIDCommand, new GetCategoryByIDHandler());
            server.createContext(getCategoriesListCommand, new GetCategoryListHandler());
            server.createContext(saveUserSettingsCommand, new SaveUserSettingsHandler());
            server.createContext(getRandomDishCommand, new GetRandomDishOfCategoryHandler());
            server.createContext(isUserOwnRecipeCommand, new UserOwnRecipeHandler());
            server.createContext(insertRecipeCommand, new InsertRecipeHandler());
            server.createContext(deleteRecipeCommand, new DeleteRecipeHandler());
            server.createContext(changeRecipeCommand, new ChangeRecipeHandler());

            server.setExecutor(null);
            server.start();
            System.out.println("Server started.");
        } catch (Exception e) {
            System.out.println("Exception while creating Server on port " + port + ": " + e.getMessage());
        }
    }
}
