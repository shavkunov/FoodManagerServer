package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChangeRecipeHandler implements HttpHandler {
    Connection connection = null;
    RecipeInformation data;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {

            data = new RecipeInformation(input);
            data.setRecipeID(input.readInt());
            connection = Server.getConnection();
            connection.setAutoCommit(false);
            changeRecipeMainInformation();
            changeRecipeCategories();
            changeRecipeIngredients();
            changeRecipeSteps();
            connection.setAutoCommit(true);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

            System.out.println("Changed user recipe");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeRecipeSteps() throws SQLException {
        DeleteRecipeHandler deleteHandler = new DeleteRecipeHandler();
        InsertRecipeHandler insertHandler = new InsertRecipeHandler();
        ArrayList<Integer> recipeIDs = deleteHandler.getRecipeStepIDs(data);
        deleteHandler.deleteRecipeSteps(data);
        deleteHandler.deleteRecipeImageStepRelation(recipeIDs);
        ArrayList<Integer> stepIDs = insertHandler.insertRecipeSteps(data);
        insertHandler.insertRecipeImageStepRelation(stepIDs, data);
    }

    private void changeRecipeIngredients() throws SQLException {
        DeleteRecipeHandler deleteHandler = new DeleteRecipeHandler();
        InsertRecipeHandler insertHandler = new InsertRecipeHandler();
        deleteHandler.deleteRecipeIngredients(data);
        ArrayList<Integer> newIds = insertHandler.insertRecipeIngredients(data);
        insertHandler.insertRecipeIngredientRelation(newIds, data);
    }

    private void changeRecipeMainInformation() {
        String updateQuery = "UPDATE Recipe SET name = ?, description = ? WHERE ID = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(updateQuery);
            stmt.setString(1, data.getRecipeName());
            stmt.setString(2, data.getRecipeDescription());
            stmt.setInt(3, data.getRecipeID());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void changeRecipeCategories() throws SQLException {
        DeleteRecipeHandler deleteHandler = new DeleteRecipeHandler();
        InsertRecipeHandler insertHandler = new InsertRecipeHandler();
        deleteHandler.deleteRecipeCategories(data);
        insertHandler.insertRecipeCategories(data);
    }
}