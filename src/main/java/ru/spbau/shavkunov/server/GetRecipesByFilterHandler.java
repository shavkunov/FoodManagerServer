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

public class GetRecipesByFilterHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            String filter = (String) input.readObject();
            String filterQuery = "SELECT * FROM Recipe WHERE name LIKE '" + filter + "%'";
            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet recipes = stmt.executeQuery(filterQuery);
            ArrayList<Recipe> res = new ArrayList<>();
            while (recipes.next()) {
                res.add(new Recipe(recipes.getInt("ID"),
                        recipes.getString("description"),
                        recipes.getString("name")));
            }

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
            output.writeObject(res);
            output.flush();
            output.close();

            System.out.println("Sent filter results");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}