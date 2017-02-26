package ru.spbau.shavkunov.server;

import ru.spbau.mit.foodmanager.Ingredient;

import java.io.ObjectInputStream;
import java.util.ArrayList;

public class RecipeInformation {
    private String recipeName;
    private String recipeDescription;
    private ArrayList<Integer> categoryIDs;
    private String userID;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<String> stepDescriptions;
    private ArrayList<String> images;
    private int recipeID;

    public RecipeInformation(ObjectInputStream input) throws Exception {
        recipeName = (String) input.readObject();
        recipeDescription = (String) input.readObject();
        categoryIDs = (ArrayList<Integer>) input.readObject();
        userID = (String) input.readObject();
        ingredients = (ArrayList<Ingredient>) input.readObject();
        stepDescriptions = (ArrayList<String>) input.readObject();
        images = (ArrayList<String>) input.readObject();
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public void setRecipeDescription(String recipeDescription) {
        this.recipeDescription = recipeDescription;
    }

    public void setCategoryIDs(ArrayList<Integer> categoryIDs) {
        this.categoryIDs = categoryIDs;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setStepDescriptions(ArrayList<String> stepDescriptions) {
        this.stepDescriptions = stepDescriptions;
    }

    public void setRecipeID(int recipeID) {
        this.recipeID = recipeID;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public String getRecipeDescription() {
        return recipeDescription;
    }

    public ArrayList<Integer> getCategoryIDs() {
        return categoryIDs;
    }

    public String getUserID() {
        return userID;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public ArrayList<String> getStepDescriptions() {
        return stepDescriptions;
    }

    public ArrayList<String> getTransformedImages() {
        return images;
    }

    public int getRecipeID() {
        return recipeID;
    }
}
