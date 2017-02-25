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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class getRecipeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream input = httpExchange.getRequestBody()) {
            ObjectInputStream reader = new ObjectInputStream(input);

            int recipeID = reader.readInt();
            String recipeQuery = "SELECT name, description FROM Recipe WHERE ID = " + recipeID;
            System.out.println("Sending recipe with recipeID = " + recipeID);
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.databaseName);
                Statement stmt = connection.createStatement();
                ResultSet mainData = stmt.executeQuery(recipeQuery);

                String recipeName = null;
                String recipeDescription = null;
                if (mainData.next()) {
                    recipeName = mainData.getString("name");
                    recipeDescription = mainData.getString("description");
                }
                stmt.close();

                Recipe recipe = new Recipe(recipeID, recipeDescription, recipeName);
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(httpExchange.getResponseBody())
                ) {
                    objectOutputStream.writeObject(recipe);
                    objectOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("Sent recipe");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
