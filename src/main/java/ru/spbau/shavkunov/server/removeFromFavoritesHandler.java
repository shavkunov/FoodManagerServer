package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.Statement;

public class removeFromFavoritesHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            int recipeID = input.readInt();
            removeRecipeFromFavorites(recipeID);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

            System.out.println("removed recipe = " + recipeID + " from favorites");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeRecipeFromFavorites(int recipeID) throws Exception {
        String removeQuery = "DELETE FROM Favorites WHERE recipe_ID = " + recipeID;

        Connection connection = Server.getConnection();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(removeQuery);
        stmt.close();
    }
}
