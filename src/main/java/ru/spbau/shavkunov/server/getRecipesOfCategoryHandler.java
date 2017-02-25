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

public class getRecipesOfCategoryHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            int categoryID = input.readInt();
            String categoryQuery = "SELECT recipe_ID FROM Recipe_to_category WHERE category_ID = " + categoryID;

            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(categoryQuery);
            ArrayList<Recipe> recipes = new ArrayList<>();
            while (rs.next()) {
                recipes.add(getRecipeHandler.getRecipe(rs.getInt("recipe_ID")));
            }
            stmt.close();

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
            output.writeObject(recipes);
            output.flush();
            output.close();

            System.out.println("Sent recipes of category");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
