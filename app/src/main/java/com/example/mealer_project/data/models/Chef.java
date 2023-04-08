package com.example.mealer_project.data.models;

import com.example.mealer_project.data.entity_models.UserEntityModel;
import com.example.mealer_project.data.models.meals.Meals;
import java.util.Date;

/**
 * This class instantiates an instance of Chef for Mealer App
 * Child Class of User
 */
public class Chef extends User {
    private String description;
    private String voidCheque;
    private double chefRatingSum; // sum of ratings
    private int numOfRatings; // number of ratings
    private int numOfOrdersSold;
    private boolean isSuspended;
    private Date suspensionDate;
    // storing Chef's meals in an instance of Meals class which provides methods to work with a collection of meals
    // variable is public for accessibility, but also final
    public final Meals MEALS;
    public final Orders ORDERS;

    /**
     * Create a single instance of chef
     * @param firstName First name of the chef
     * @param lastName Last name of the chef
     * @param email email of the chef
     * @param password password for the chef
     * @param address address of the chef
     * @param role Role of the chef
     * @param description Description of the chef
     * @param voidCheque Chequing information of chef
     * Menu of a chef is stored in a HashMap
     */
    public Chef(String firstName, String lastName, String email, String password, Address address,
                UserRoles role, String description, String voidCheque, int numberOfMealsSold, double chefRatingSum, int numOfRatings)  throws IllegalArgumentException {
        // instantiate Admins data members
        super(firstName, lastName, email, password, address, role);
        this.setDescription(description);
        this.setVoidCheque(voidCheque);
        this.setNumOfOrdersSold(numberOfMealsSold);
        this.setChefRatingSum(chefRatingSum);
        this.setNumOfRatings(numOfRatings);
        // instantiate a meals object where Chef's meals will be stored
        this.MEALS = new Meals();
        this.isSuspended = false;
        this.suspensionDate = null;
        this.ORDERS = new Orders();
    }

    public Chef(UserEntityModel userData, Address address, String description, String voidCheque) throws IllegalArgumentException {
        // instantiate Admins data members
        super(userData, address);
        this.setDescription(description);
        this.setVoidCheque(voidCheque);
        //this.setNumOfOrdersSold(0);
        this.setChefRatingSum(0);
        this.setNumOfRatings(0);
        // instantiate a meals object where Chef's meals will be stored
        this.MEALS = new Meals();
        this.isSuspended = false;
        this.suspensionDate = null;
        this.ORDERS = new Orders();
    }

    /**
     * Get a short description of the chef
     * @return String representing chef's description
     */
    public String getDescription() { return description; }

    /**
     * Set the chef's description
     * @param description String representing the chef's description
     */
    public void setDescription(String description) throws IllegalArgumentException {
        // validate description
        if (description.length() > 0)
            this.description = description;
        else
            throw new IllegalArgumentException("Please enter a description");
    }

    /**
     * Get chequing information about chef
     * @return String representing void cheque of chef
     */
    public String getVoidCheque() {
        return voidCheque;
    }

    /**
     * Set the chef's void cheque
     * @param voidCheque String representing void cheque of chef
     */
    public void setVoidCheque(String voidCheque) {
        // validate void cheque
        this.voidCheque = voidCheque;
    }

    /**
     * Get the average rating of a chef
     * @return Integer representing chef's overall rating
     */
    public double getChefRatingSum() { return chefRatingSum; }

    /**
     * Set the chef's rating sum
     * @param chefRatingSum integer representing the chef's rating
     */
    public void setChefRatingSum(double chefRatingSum) {

        this.chefRatingSum = chefRatingSum;
    }

    /**
     * Add to the chef's rating sum
     * @param chefRating integer representing the chef's rating
     */
    public void addToChefRatingSum(double chefRating) {

        this.chefRatingSum += chefRating;
        this.numOfRatings ++;
    }

    /**
     * Get the number of ratings done for a chef
     * @return Integer representing number of ratings
     */
    public double getNumOfRatings() { return numOfRatings; }

    /**
     * Set the chef's number of ratings
     * @param numOfRatings integer representing the chef's number of ratings
     */
    public void setNumOfRatings(int numOfRatings) {
        this.numOfRatings = numOfRatings;
    }

    public double getChefRating(){
        return chefRatingSum/numOfRatings;
    }
    /**
     * Get the total number of orders sold by a chef
     * @return Integer representing chef's total sales
     */
    public int getNumOfOrdersSold() { return ORDERS.getCompletedOrders().size(); }

    /**
     * Set the chef's total orders sold
     * @param numOfOrdersSold integer representing the chef's total sales
     */
    public void setNumOfOrdersSold(int numOfOrdersSold) {
        this.numOfOrdersSold = numOfOrdersSold;
    }

    /**
     * Get a true/false whether chef is banned
     * @return boolean suspended or not
     */
    public boolean getIsSuspended() { return isSuspended; }

    /**
     * Set the boolean for suspended or not
     * @param isSuspended suspended or not
     */
    public void setIsSuspended(boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

    /**
     * Get the end of suspension date
     * @return Date of end of suspension
     */
    public Date getSuspensionDate() { return suspensionDate; }

    /**
     * Set the boolean for suspended or not
     * @param suspensionDate date of end of suspension
     */
    public void setSuspensionDate(Date suspensionDate) {
        this.suspensionDate = suspensionDate;
    }

}
