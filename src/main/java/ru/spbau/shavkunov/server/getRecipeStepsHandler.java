package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class getRecipeStepsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {

            int recipeID = input.readInt();
            String stepsQuery = "SELECT * FROM Step INNER JOIN Image ON " +
                    "Step.ID = Image.entity_ID " +
                    "WHERE Step.recipe_ID = " + recipeID + " AND Image.entity_type = 0";

            try {
                Connection connection = Server.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet steps = stmt.executeQuery(stepsQuery);
                ArrayList<ArrayList<String>> stepsData = new ArrayList<>();

                while (steps.next()) {
                    String stepDescription = steps.getString("description");
                    String imageURL = steps.getString("link");
                    stepsData.add(new ArrayList<>(Arrays.asList(stepDescription, imageURL)));
                }

                stmt.close();
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                ObjectOutputStream output = new ObjectOutputStream(httpExchange.getResponseBody());
                output.writeObject(stepsData);
                output.flush();
                output.close();

                System.out.println("Sent recipe steps");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
