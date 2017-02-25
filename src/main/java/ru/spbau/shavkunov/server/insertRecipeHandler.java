package ru.spbau.shavkunov.server;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.cloudinary.json.JSONObject;
import ru.spbau.mit.foodmanager.Ingredient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

public class insertRecipeHandler implements HttpHandler {
    Connection connection = null;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {

            String recipeName = (String) input.readObject();
            String recipeDescription = (String) input.readObject();
            ArrayList<Integer> categoryIDs = (ArrayList<Integer>) input.readObject();
            String userID = (String) input.readObject();
            ArrayList<Ingredient> ingredients = (ArrayList<Ingredient>) input.readObject();
            ArrayList<String> stepDescriptions = (ArrayList<String>) input.readObject();
            ArrayList<ByteArrayInputStream> transformedImages = (ArrayList<ByteArrayInputStream>) input.readObject();

            connection = Server.getConnection();
            connection.setAutoCommit(false);
            int recipeID = insertMainInformation(recipeName, recipeDescription);
            insertUserRecipeRelation(userID, recipeID);
            insertRecipeCategories(categoryIDs, recipeID);
            ArrayList<Integer> ingredientIDs = insertRecipeIngredients(ingredients, recipeID);
            insertRecipeIngredientRelation(ingredientIDs, ingredients, recipeID);
            ArrayList<Integer> stepIDs = insertRecipeSteps(stepDescriptions, recipeID);
            insertRecipeImageStepRelation(stepIDs, transformedImages);
            connection.setAutoCommit(true);

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

            System.out.println("Inserted user recipe");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int insertMainInformation(String recipeName, String recipeDescription) throws SQLException {
        Statement stmt = connection.createStatement();
        String insertRecipeQuery = "INSERT INTO Recipe(name, description) " +
                                   "VALUES (" + recipeName + ", '" + recipeDescription + "')";

        int res = stmt.executeUpdate(insertRecipeQuery);
        stmt.close();

        return res;
    }

    private void insertUserRecipeRelation(String userID, int recipeID) throws SQLException {
        String deleteQuery = "DELETE FROM User_to_recipe WHERE recipe_ID = " + recipeID;
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteQuery);
        String insertQuery = "INSERT INTO User_to_recipe VALUES (" + recipeID + ", '" + userID + "')";
        stmt.executeUpdate(insertQuery);
        stmt.close();
    }

    private void insertRecipeCategories(ArrayList<Integer> categoryIDs, int recipeID) throws SQLException {
        Statement stmt = connection.createStatement();
        for (int categoryID : categoryIDs) {
            String insertCategoryQuery = "INSERT INTO Recipe_to_category (recipe_ID, category_ID) "
                                       + "VALUES (" + recipeID + ", " + categoryID + ")";

            stmt.executeUpdate(insertCategoryQuery);
        }

        stmt.close();
    }

    private ArrayList<Integer> insertRecipeIngredients(ArrayList<Ingredient> ingredients, int recipeID) {
        ArrayList<Integer> ids = new ArrayList<>();

        try {
            for (Ingredient ing : ingredients) {
                String insertIngredientQuery = "INSERT INTO Ingredient (ID, name) " +
                                               "VALUES (?, ?)";

                PreparedStatement preparedStatement = connection.prepareStatement(insertIngredientQuery);
                preparedStatement.setInt(1, recipeID);
                preparedStatement.setString(2, ing.getName());
                ids.add(preparedStatement.executeUpdate());
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    private void insertRecipeIngredientRelation(ArrayList<Integer> ingredientIDs, ArrayList<Ingredient> ingredients, int recipeID) {
        try {
            Statement stmt = connection.createStatement();
            // ingredients.size() == ingredientIDs.size()
            for (int i = 0; i < ingredients.size(); i++) {
                double quantity = ingredients.get(i).getQuantity();
                int measureOrdinal = ingredients.get(i).getMeasure().ordinal();
                String insertRelationQuery = "INSERT INTO Ingredient_to_recipe " +
                        "(Ingredient_ID, recipe_ID, measure, quantity) VALUES " +
                        "(" + ingredientIDs.get(i) + ", " + recipeID +
                        ", " + measureOrdinal + ", " + quantity + ")";

                stmt.executeUpdate(insertRelationQuery);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Integer> insertRecipeSteps(ArrayList<String> descriptions, int recipeID) throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        for (String description : descriptions) {
            String insertStep = "INSERT INTO Step(recipe_ID, description) VALUES  (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertStep);
            preparedStatement.setInt(1, recipeID);
            preparedStatement.setString(2, description);
            ids.add(preparedStatement.executeUpdate());
            preparedStatement.close();
        }

        return ids;
    }

    private void insertRecipeImageStepRelation(ArrayList<Integer> ids, ArrayList<ByteArrayInputStream> transformedImages) {
        try {
            for (int i = 0; i < ids.size(); i++) {
                String insertRelation = "INSERT INTO Image(entity_type, entity_ID, link) " +
                        "VALUES (?, ?, ?)";

                ByteArrayInputStream bs = transformedImages.get(i);
                String link = uploadImage(bs);
                PreparedStatement preparedStatement = connection.prepareStatement(insertRelation);
                preparedStatement.setInt(1, 0);
                preparedStatement.setInt(2, ids.get(i));
                preparedStatement.setString(3, link);
                preparedStatement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String uploadImage(InputStream imageIn) throws Exception {
        Cloudinary cloudinary = new Cloudinary(Server.CLOUDINARY_URL);
        Map result = cloudinary.uploader().upload(imageIn, ObjectUtils.emptyMap());
        JSONObject jsonObject = new JSONObject(result);

        return jsonObject.getString("url");
    }
}
