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
import java.util.ArrayList;

public class getRecipeCategoriesHangler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {

            int recipeID = input.readInt();

            String categoriesQuery = "SELECT category_ID FROM Recipe_to_category WHERE recipe_ID = " + recipeID;
            System.out.println(categoriesQuery);
            try {
                Connection connection = Server.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet categories = stmt.executeQuery(categoriesQuery);
                ArrayList<Integer> ids = new ArrayList<>();
                while (categories.next()) {
                    ids.add(categories.getInt("category_ID"));
                }

                stmt.close();
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(httpExchange.getResponseBody())
                ) {
                    objectOutputStream.writeObject(ids);
                    objectOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
