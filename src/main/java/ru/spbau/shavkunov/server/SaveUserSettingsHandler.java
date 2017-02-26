package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.Statement;

public class SaveUserSettingsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            String userID = (String) input.readObject();
            String settings = (String) input.readObject();
            String addSettingsQuery = "INSERT INTO user_settings (user_ID, user_settings) VALUES ('" +
                    userID + "', '" + settings + "')";

            String deleteSettingsQuery = "DELETE FROM user_settings WHERE user_ID = '" + userID + "'";
            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(deleteSettingsQuery);
            stmt.executeUpdate(addSettingsQuery);
            stmt.close();
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

            System.out.println("Saved user settings");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
