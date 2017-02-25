package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.spbau.mit.foodmanager.Ingredient;
import ru.spbau.mit.foodmanager.Measure;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.sql.*;
import java.util.ArrayList;

public class getRecipeIngredientsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {

            int recipeID = input.readInt();
            Connection connection = Server.getConnection();
            Statement stmt = connection.createStatement();
            String ingredientsQuery = "SELECT name, measure, quantity " +
                    "FROM Ingredient_to_recipe AS itr " +
                    "INNER JOIN Ingredient AS ing ON " +
                    "itr.ingredient_ID = ing.ID " +
                    "WHERE itr.recipe_ID = " + recipeID;

            ResultSet ingredients = stmt.executeQuery(ingredientsQuery);
            ArrayList<Ingredient> recipeIngredients = new ArrayList<>();

            while (ingredients.next()) {
                String name = ingredients.getString("name");
                Measure measure = Measure.values()[ingredients.getInt("measure")];
                double quantity = ingredients.getDouble("quantity");

                recipeIngredients.add(new Ingredient(name, measure, quantity));
            }

            stmt.close();
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
            output.writeObject(recipeIngredients);
            output.flush();
            output.close();

            System.out.println("Sent Ingredients of recipeID = " + recipeID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
