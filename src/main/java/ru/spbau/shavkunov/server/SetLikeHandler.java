package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.Statement;

public class SetLikeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            int recipeID = input.readInt();
            String userID = (String) input.readObject();
            String setLikeQuery = "INSERT INTO Likes(user_ID, recipe_ID) VALUES ('" +  userID + "', " + recipeID + ")";
            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(setLikeQuery);
            stmt.close();
            connection.commit();
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

            System.out.println("Set user like");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
