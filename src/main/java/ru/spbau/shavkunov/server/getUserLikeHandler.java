package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class getUserLikeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            int recipeID = input.readInt();
            String userID = (String) input.readObject();
            String likesQuery = "SELECT COUNT(*) AS total FROM Likes WHERE recipe_ID = " +
                    recipeID + " AND user_ID = '" + userID + "'";

            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.databaseName);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(likesQuery);
            int like = 0;
            if (rs.next())
                like = rs.getInt("total");
            stmt.close();

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
            output.writeInt(like);
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
