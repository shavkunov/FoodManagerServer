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

public class GetUserSettingsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            String userID = (String) input.readObject();
            String query = "SELECT user_settings FROM user_settings WHERE user_ID = '" + userID + "'";
            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            String userSettings = "";
            if (rs.next()) {
                userSettings = rs.getString("user_settings");
            }

            stmt.close();
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
            output.writeObject(userSettings);
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
