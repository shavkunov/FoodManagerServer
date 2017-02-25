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

public class getRandomDishOfCategoryHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {
            int categoryID = input.readInt();
            String getRandomRecipeQuery = "SELECT recipe_ID FROM Recipe_to_category " +
                    "WHERE category_ID = " + categoryID +
                    " ORDER BY RANDOM() LIMIT 1";
            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(getRandomRecipeQuery);

            int recipeID = 0;
            if (rs.next()) {
                recipeID = rs.getInt("recipe_ID");
            }
            stmt.close();
            Recipe recipe = getRecipeHandler.getRecipe(recipeID);

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
            output.writeObject(recipe);
            output.flush();
            output.close();

            System.out.println("Sent user favorites");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
