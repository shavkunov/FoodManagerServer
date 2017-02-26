package ru.spbau.shavkunov.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;

public class changeRecipeHandler implements HttpHandler {
    Connection connection = null;
    RecipeInformation data;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody();
             ObjectInputStream input = new ObjectInputStream(inputStream)) {

            data = new RecipeInformation(input);
            data.setRecipeID(input.readInt());
            new deleteRecipeHandler().deleteRecipe(data);
            new insertRecipeHandler().insertRecipe(data);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

            System.out.println("Changed user recipe");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
