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

/**
 * Created by Mikhail Shavkunov
 */
public class getCategoryByIDHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            int categoryID = input.readInt();
            String categoryQuery = "SELECT name FROM Category WHERE ID = " + categoryID;

            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(categoryQuery);
            String categoryName = "";
            if (rs.next()) {
                categoryName = rs.getString("name");
            }
            stmt.close();

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
            output.writeObject(categoryName);
            output.flush();
            output.close();

            System.out.println("Sent category name");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
