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

public class InsertRecipeHandler implements HttpHandler {
    private Connection connection = null;
    private RecipeInformation data;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {

            data = new RecipeInformation(input);
            insertRecipe(data);
            connection.commit();
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

            System.out.println("Inserted user recipe");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertRecipe(RecipeInformation data) throws Exception {
        connection = Server.getConnection();
        connection.setAutoCommit(false);
        int recipeID = insertMainInformation();
        data.setRecipeID(recipeID);
        insertUserRecipeRelation();
        insertRecipeCategories(data);
        ArrayList<Integer> ingredientIDs = insertRecipeIngredients(data);
        insertRecipeIngredientRelation(ingredientIDs, data);
        ArrayList<Integer> stepIDs = insertRecipeSteps(data);
        insertRecipeImageStepRelation(stepIDs, data);
        connection.setAutoCommit(true);
    }

    private int insertMainInformation() throws SQLException {
        Statement stmt = connection.createStatement();
        String insertRecipeQuery = "INSERT INTO Recipe (name, description) " +
                                   "VALUES ('" + data.getRecipeName() + "', '" + data.getRecipeDescription() + "')";

        System.out.println(insertRecipeQuery);
        int res = stmt.executeUpdate(insertRecipeQuery);
        stmt.close();

        return res;
    }

    private void insertUserRecipeRelation() throws SQLException {
        String deleteQuery = "DELETE FROM User_to_recipe WHERE recipe_ID = " + data.getRecipeID();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(deleteQuery);
        String insertQuery = "INSERT INTO User_to_recipe VALUES (" + data.getRecipeID() + ", '" + data.getUserID() + "')";
        stmt.executeUpdate(insertQuery);
        stmt.close();
    }

    public void insertRecipeCategories(RecipeInformation recipe) throws SQLException {
        Statement stmt = connection.createStatement();
        for (int categoryID : recipe.getCategoryIDs()) {
            String insertCategoryQuery = "INSERT INTO Recipe_to_category (recipe_ID, category_ID) "
                                       + "VALUES (" + recipe.getRecipeID() + ", " + categoryID + ")";

            stmt.executeUpdate(insertCategoryQuery);
        }

        stmt.close();
    }

    public ArrayList<Integer> insertRecipeIngredients(RecipeInformation recipe) {
        ArrayList<Integer> ids = new ArrayList<>();

        try {
            for (Ingredient ing : recipe.getIngredients()) {
                String insertIngredientQuery = "INSERT INTO Ingredient (name) " +
                                               "VALUES (?)";

                PreparedStatement preparedStatement = connection.prepareStatement(insertIngredientQuery);
                preparedStatement.setString(1, ing.getName());
                ids.add(preparedStatement.executeUpdate());
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    public void insertRecipeIngredientRelation(ArrayList<Integer> ingredientIDs, RecipeInformation recipe) {
        try {
            Statement stmt = connection.createStatement();
            // ingredients.size() == ingredientIDs.size()
            for (int i = 0; i < recipe.getIngredients().size(); i++) {
                double quantity = recipe.getIngredients().get(i).getQuantity();
                int measureOrdinal = recipe.getIngredients().get(i).getMeasure().ordinal();
                int ingredientID = ingredientIDs.get(i);
                String insertRelationQuery = "INSERT INTO Ingredient_to_recipe " +
                        "(Ingredient_ID, recipe_ID, measure, quantity) VALUES " +
                        "(" + ingredientID + ", " + recipe.getRecipeID() +
                        ", " + measureOrdinal + ", " + quantity + ")";

                stmt.executeUpdate(insertRelationQuery);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> insertRecipeSteps(RecipeInformation recipe) throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        for (String description : recipe.getStepDescriptions()) {
            String insertStep = "INSERT INTO Step(recipe_ID, description) VALUES  (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertStep);
            preparedStatement.setInt(1, recipe.getRecipeID());
            preparedStatement.setString(2, description);
            ids.add(preparedStatement.executeUpdate());
            preparedStatement.close();
        }

        return ids;
    }

    public void insertRecipeImageStepRelation(ArrayList<Integer> ids, RecipeInformation recipe) {
        try {
            for (int i = 0; i < ids.size(); i++) {
                String insertRelation = "INSERT INTO Image(entity_type, entity_ID, link) " +
                        "VALUES (?, ?, ?)";

                ByteArrayInputStream bs = new ByteArrayInputStream(recipe.getTransformedImages().get(i));
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
