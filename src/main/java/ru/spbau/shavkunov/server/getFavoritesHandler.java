package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.spbau.mit.foodmanager.Recipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class getFavoritesHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            String userID = (String) input.readObject();
            String favoritesQuery = "SELECT recipe_ID FROM Favorites WHERE user_ID = '" + userID + "'";

            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(favoritesQuery);
            ArrayList<Recipe> favorites = new ArrayList<>();
            while (rs.next()) {
                favorites.add(getRecipeHandler.getRecipe(rs.getInt("recipe_ID")));
            }
            stmt.close();

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
            output.writeObject(favorites);
            output.flush();
            output.close();

            System.out.println("Sent user favorites");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
