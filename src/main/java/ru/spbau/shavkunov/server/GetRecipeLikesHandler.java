package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class GetRecipeLikesHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            int recipeID = input.readInt();
            String likesQuery = "SELECT COUNT(*) AS total FROM Likes WHERE recipe_ID = " + recipeID;
            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(likesQuery);
            int likes = 0;
            if (rs.next()) {
                likes = rs.getInt("total");
            }

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
            output.writeInt(likes);
            output.flush();
            output.close();

            System.out.println("Sent recipe likes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
