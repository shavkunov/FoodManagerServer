package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.Statement;

public class setNotLikeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            int recipeID = input.readInt();
            String userID = (String) input.readObject();
            String removeLikeQuery = "DELETE FROM Likes WHERE user_ID = '" + userID + "' AND recipe_ID = " + recipeID;
            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(removeLikeQuery);
            stmt.close();
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

            System.out.println("Removed user like");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
