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

public class GetCategoryListHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            String typeOfCategoryView = (String) input.readObject();
            String categoryQuery = "";
            if (typeOfCategoryView.equals("category_dish")) {
                categoryQuery = "SELECT * FROM Category WHERE is_category_dish = 1";
            } else {
                categoryQuery = "SELECT * FROM Category WHERE is_national_kitchen = 1";
            }

            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet category = stmt.executeQuery(categoryQuery);
            ArrayList<Integer> ids = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            while (category.next()) {
                ids.add(category.getInt("ID"));
                names.add(category.getString("name"));
            }
            stmt.close();

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
            output.writeObject(ids);
            output.writeObject(names);
            output.flush();
            output.close();

            System.out.println("Sent list of categories");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
