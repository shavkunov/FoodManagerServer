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
    RecipeInformation data;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {

            data = new RecipeInformation(input);
            connection = Server.getConnection();
            connection.setAutoCommit(false);
            int recipeID = insertMainInformation();
            data.setRecipeID(recipeID);
            insertUserRecipeRelation();
            insertRecipeCategories();
            ArrayList<Integer> ingredientIDs = insertRecipeIngredients();
            insertRecipeIngredientRelation(ingredientIDs);
            ArrayList<Integer> stepIDs = insertRecipeSteps();
            insertRecipeImageStepRelation(stepIDs);
            connection.setAutoCommit(true);

            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

            System.out.println("Inserted user recipe");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int insertMainInformation() throws SQLException {
        Statement stmt = connection.createStatement();
        String insertRecipeQuery = "INSERT INTO Recipe(name, description) " +
                                   "VALUES (" + data.getRecipeName() + ", '" + data.getRecipeDescription() + "')";

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

    private void insertRecipeCategories() throws SQLException {
        Statement stmt = connection.createStatement();
        for (int categoryID : data.getCategoryIDs()) {
            String insertCategoryQuery = "INSERT INTO Recipe_to_category (recipe_ID, category_ID) "
                                       + "VALUES (" + data.getRecipeID() + ", " + categoryID + ")";

            stmt.executeUpdate(insertCategoryQuery);
        }

        stmt.close();
    }

    private ArrayList<Integer> insertRecipeIngredients() {
        ArrayList<Integer> ids = new ArrayList<>();

        try {
            for (Ingredient ing : data.getIngredients()) {
                String insertIngredientQuery = "INSERT INTO Ingredient (ID, name) " +
                                               "VALUES (?, ?)";

                PreparedStatement preparedStatement = connection.prepareStatement(insertIngredientQuery);
                preparedStatement.setInt(1, data.getRecipeID());
                preparedStatement.setString(2, ing.getName());
                ids.add(preparedStatement.executeUpdate());
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    private void insertRecipeIngredientRelation(ArrayList<Integer> ingredientIDs) {
        try {
            Statement stmt = connection.createStatement();
            // ingredients.size() == ingredientIDs.size()
            for (int i = 0; i < data.getIngredients().size(); i++) {
                double quantity = data.getIngredients().get(i).getQuantity();
                int measureOrdinal = data.getIngredients().get(i).getMeasure().ordinal();
                String insertRelationQuery = "INSERT INTO Ingredient_to_recipe " +
                        "(Ingredient_ID, recipe_ID, measure, quantity) VALUES " +
                        "(" + ingredientIDs.get(i) + ", " + data.getRecipeID() +
                        ", " + measureOrdinal + ", " + quantity + ")";

                stmt.executeUpdate(insertRelationQuery);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Integer> insertRecipeSteps() throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        for (String description : data.getStepDescriptions()) {
            String insertStep = "INSERT INTO Step(recipe_ID, description) VALUES  (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertStep);
            preparedStatement.setInt(1, data.getRecipeID());
            preparedStatement.setString(2, description);
            ids.add(preparedStatement.executeUpdate());
            preparedStatement.close();
        }

        return ids;
    }

    private void insertRecipeImageStepRelation(ArrayList<Integer> ids) {
        try {
            for (int i = 0; i < ids.size(); i++) {
                String insertRelation = "INSERT INTO Image(entity_type, entity_ID, link) " +
                        "VALUES (?, ?, ?)";

                ByteArrayInputStream bs = data.getTransformedImages().get(i);
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
