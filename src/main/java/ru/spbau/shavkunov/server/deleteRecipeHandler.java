package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class deleteRecipeHandler implements HttpHandler {
    Connection connection = null;
    RecipeInformation data;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {

            data = new RecipeInformation(input);
            data.setRecipeID(input.readInt());
            deleteRecipe(data);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

            System.out.println("Deleted user recipe");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteRecipe(RecipeInformation data) throws Exception {
        connection = Server.getConnection();
        connection.setAutoCommit(false);
        deleteRecipeMainInformation();
        deleteUserRecipeRelation();
        deleteRecipeCategories();
        deleteRecipeIngredients();
        deleteIngredientToRecipeRelation();
        deleteRecipeSteps();
        ArrayList<Integer> stepIDs = getRecipeStepIDs();
        deleteRecipeImageStepRelation(stepIDs);
        setNotLikeHandler.setNotLike(data.getRecipeID(), data.getUserID());
        removeFromFavoritesHandler.removeRecipeFromFavorites(data.getRecipeID());
        connection.setAutoCommit(true);
    }

    private void deleteRecipeMainInformation() throws SQLException {
        String deleteQuery = "DELETE FROM Recipe WHERE ID = " + data.getRecipeID();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteQuery);
        stmt.close();
    }

    private void deleteUserRecipeRelation() throws SQLException {
        String deleteQuery = "DELETE FROM User_to_recipe WHERE recipe_ID = " + data.getRecipeID();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteQuery);
        stmt.close();
    }

    private void deleteRecipeSteps() throws SQLException {
        String deleteStepsQuery = "DELETE FROM Step WHERE recipe_ID = " + data.getRecipeID();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteStepsQuery);
        stmt.close();
    }

    private void deleteRecipeImageStepRelation(ArrayList<Integer> ids) throws SQLException {
        String deleteImagesQuery = "DELETE FROM Image WHERE entity_type = 0 AND entity_ID IN (";
        for (int i = 0; i < ids.size() - 1; i++) {
            deleteImagesQuery += ids.get(i) + ", ";
        }
        deleteImagesQuery += ids.get(ids.size() - 1) + ")";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteImagesQuery);
        stmt.close();
    }

    private void deleteIngredientToRecipeRelation() throws SQLException {
        String deleteQuery = "DELETE FROM Ingredient_to_recipe WHERE recipe_ID = " + data.getRecipeID();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteQuery);
        stmt.close();
    }

    private void deleteRecipeIngredientsFromIngredient(ArrayList<Integer> ids) throws SQLException {
        String deleteQuery = "DELETE FROM Ingredient WHERE ID IN (";
        for (int i = 0; i < ids.size() - 1; i++) {
            deleteQuery += ids.get(i) + ", ";
        }
        deleteQuery += ids.get(ids.size() - 1) + ")";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteQuery);
        stmt.close();
    }

    private void deleteRecipeIngredients() throws SQLException {
        ArrayList<Integer> ids = getRecipeIngredientIDs();
        deleteIngredientToRecipeRelation();
        deleteRecipeIngredientsFromIngredient(ids);
    }

    private void deleteRecipeCategories() throws SQLException {
        Statement stmt = connection.createStatement();
        String deletePreviousCategoriesQuery = "DELETE FROM Recipe_to_category " +
                "WHERE recipe_ID = " + data.getRecipeID();

        stmt.executeUpdate(deletePreviousCategoriesQuery);
    }

    private ArrayList<Integer> getRecipeStepIDs() throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();
        String getStepsQuery = "SELECT ID FROM Step WHERE recipe_ID = " + data.getRecipeID();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(getStepsQuery);
        while (rs.next()) {
            ids.add(rs.getInt("ID"));
        }

        return ids;
    }

    private ArrayList<Integer> getRecipeIngredientIDs() throws SQLException {
        String selectIngredientQuery = "SELECT Ingredient_ID FROM Ingredient_to_recipe " +
                "WHERE recipe_ID = " + data.getRecipeID();

        ArrayList<Integer> ids = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(selectIngredientQuery);
        while (rs.next()) {
            ids.add(rs.getInt("Ingredient_ID"));
        }
        return ids;
    }
}
