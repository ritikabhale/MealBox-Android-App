package com.example.mealer_project.data.sources.actions;

import static com.example.mealer_project.data.sources.FirebaseCollections.CHEF_COLLECTION;
import static com.example.mealer_project.data.sources.FirebaseCollections.CHEF_ORDERS_COLLECTION;
import static com.example.mealer_project.data.sources.FirebaseCollections.CLIENT_COLLECTION;
import static com.example.mealer_project.data.sources.FirebaseCollections.CLIENT_ORDERS_COLLECTION;
import static com.example.mealer_project.data.sources.FirebaseCollections.ORDER_COLLECTION;
import static com.example.mealer_project.data.handlers.OrderHandler.dbOperations.*;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mealer_project.app.App;
import com.example.mealer_project.data.entity_models.AddressEntityModel;
import com.example.mealer_project.data.models.Address;
import com.example.mealer_project.data.models.Order;
import com.example.mealer_project.data.models.orders.ChefInfo;
import com.example.mealer_project.data.models.orders.ClientInfo;
import com.example.mealer_project.data.models.orders.MealInfo;
import com.example.mealer_project.utils.Preconditions;
import com.example.mealer_project.utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OrderActions {

    FirebaseFirestore database;

    public OrderActions(FirebaseFirestore database) {
        this.database = database;
    }

    /**
     * Adds order to firebase
     *
     * @param order order object to be added
     */
    public void addOrder(Order order) {

        if (Preconditions.isNotNull(order)) {

            Map<String, Object> databaseOrder = new HashMap<>();

            databaseOrder.put("clientInfo", order.getClientInfo());
            databaseOrder.put("chefInfo", order.getChefInfo());

            databaseOrder.put("date", order.getOrderDate());
            databaseOrder.put("isPending", order.getIsPending());
            databaseOrder.put("isRejected", order.getIsRejected());
            databaseOrder.put("isCompleted", order.getIsCompleted());
            databaseOrder.put("meals", order.getMeals());
            databaseOrder.put("isRated", order.isRated());
            databaseOrder.put("rating", order.getRating());
            databaseOrder.put("complaintSubmitted", order.isComplaintSubmitted());

            // Add order to Orders Collection
            database
                    .collection(ORDER_COLLECTION)
                    .add(databaseOrder)
                    .addOnSuccessListener(documentReference -> {

                        // if successful, update orderId
                        order.setOrderID(documentReference.getId());

                        // if successful, add orderId to specific chef's list of orders
                        database.collection(CHEF_COLLECTION)
                                        .document(order.getChefInfo().getChefId())
                                        .update(CHEF_ORDERS_COLLECTION, FieldValue.arrayUnion(order.getOrderID()))
                                        .addOnSuccessListener(aVoid -> Log.d("ChefOrdersSuccess", "DocumentSnapshot successfully updated!"))
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("ChefOrdersError", "Error updating document: " + e.getMessage());
                                            }
                                        });

                        // if successful, add orderId to specific client's list of orders
                        database.collection(CLIENT_COLLECTION)
                                .document(order.getClientInfo().getClientId())
                                .update(CLIENT_ORDERS_COLLECTION, FieldValue.arrayUnion(order.getOrderID()))
                                .addOnSuccessListener(aVoid -> Log.d("ClientOrdersSuccess", "DocumentSnapshot successfully updated!"))
                                .addOnFailureListener(e -> Log.e("ClientOrdersError", "Error updating document: " + e));

                        App.ORDER_HANDLER.handleActionSuccess(ADD_ORDER, order);
                    })
                    .addOnFailureListener(e -> App.ORDER_HANDLER.handleActionFailure(ADD_ORDER, "Failed to add order to database: " + e.getMessage()));
        } else {
            App.ORDER_HANDLER.handleActionFailure(ADD_ORDER, "Invalid order instance provided");
        }
    }


    /**
     * Removes order from firebase
     *
     * @param orderId orderId of object to be removed
     */
    public void removeOrder(String orderId){

        if (Preconditions.isNotNull(orderId)) {

            // retrieve order object from firebase
            DocumentReference docRef = database.collection(ORDER_COLLECTION).document(orderId);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        // if the order exists, get clientId and chefId
                        String clientId = (String) document.get("clientId");
                        String chefId = (String) document.get("chefId");

                        // delete orderId from specific chef's list of orders
                        database.collection(CHEF_COLLECTION)
                                        .document(chefId)
                                        .update(CHEF_ORDERS_COLLECTION, FieldValue.arrayRemove(orderId));

                        // delete orderId from specific client's list of orders
                        database.collection(CLIENT_COLLECTION)
                                .document(clientId)
                                .update(CLIENT_ORDERS_COLLECTION, FieldValue.arrayRemove(orderId));

                        //finally, delete order from Orders Collection
                        docRef
                        .delete()
                                .addOnSuccessListener(aVoid -> App.ORDER_HANDLER.handleActionSuccess(REMOVE_ORDER, orderId))
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        App.ORDER_HANDLER.handleActionFailure(REMOVE_ORDER, "Failed to remove order from database: " + e.getMessage());
                                    }
                                });


                    } else {
                        App.ORDER_HANDLER.handleActionFailure(REMOVE_ORDER, "No such document");
                    }
                } else {
                    App.ORDER_HANDLER.handleActionFailure(REMOVE_ORDER, "get failed with " + task.getException());
                }
            });
        }
    }

    public void getOrderById(String orderId){

        if (Preconditions.isNotNull(orderId)) {

            // retrieve order object from firebase
            database
                    .collection(ORDER_COLLECTION)
                    .document(orderId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                Order order = makeOrderFromFirebase(document);


                                App.ORDER_HANDLER.handleActionSuccess(GET_ORDER_BY_ID, order);

                            } else {
                                Log.d("RemoveOrder", "No such document");
                            }
                        } else {
                            Log.d("RemoveOrder", "get failed with ", task.getException());
                        }
                    });
        }

    }

    public void updateOrder(Order order){

        if (Preconditions.isNotNull(order)) {

            Log.e("order2", order.getOrderID());
            Log.e("order2", "p: " + order.getIsPending());

            database.collection(ORDER_COLLECTION)
                    .document(order.getOrderID())
                    .update("isPending", order.getIsPending(),
                                "isRejected", order.getIsRejected(),
                                "isCompleted", order.getIsCompleted())
                    .addOnSuccessListener(aVoid -> App.ORDER_HANDLER.handleActionSuccess(UPDATE_ORDER,order))
                    .addOnFailureListener(e -> App.ORDER_HANDLER.handleActionFailure(UPDATE_ORDER,"Error updating order in firebase"));
        }
    }

    public void updateComplaintStatus(Order order){
        if (Preconditions.isNotNull(order)) {

            database.collection(ORDER_COLLECTION)
                    .document(order.getOrderID())
                    .update("complaintSubmitted", order.isComplaintSubmitted())
                    .addOnSuccessListener(aVoid -> App.ORDER_HANDLER.handleActionSuccess(UPDATE_ORDER,order))
                    .addOnFailureListener(e -> App.ORDER_HANDLER.handleActionFailure(UPDATE_ORDER,"Error updating order in firebase"));
        }
    }

    public void loadChefOrders(String chefId){

        if (Preconditions.isNotNull(chefId)) {

            // retrieve chef object from firebase
            database.collection(CHEF_COLLECTION)
                    .document(chefId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                // retrieve list of orderIds from chef
                                ArrayList<String> orderIds = (ArrayList<String>) document.getData().get(CHEF_ORDERS_COLLECTION);

                                // if no orders
                                if (orderIds == null) {
                                    return;
                                }

                                // iterate through list
                                for (String orderId : orderIds) {

                                    // go to orders_collection and retrieve order using the given orderId in the list
                                    database.collection(ORDER_COLLECTION)
                                            .document(orderId)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {

                                                            //make order object from firebase
                                                            Order order = makeOrderFromFirebase(document);

                                                            //update orders of the logged in chef

                                                            App.getChef().ORDERS.addOrder(order);

                                                        } else {
                                                            Log.d("loadChefOrders", "Order not found given orderId stored in chef orders");
                                                        }
                                                    } else {
                                                        Log.d("loadChefOrders", "get failed with ", task.getException());
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Log.d("loadChefOrders", "Chef not found");
                            }
                        } else {
                            Log.d("loadChefOrders", "get failed with ", task.getException());
                        }
                    });
        }
    }

    public void loadClientOrders(String clientId){

        if (Preconditions.isNotNull(clientId)) {

            // retrieve client object from firebase
            database.collection(CLIENT_COLLECTION)
                    .document(clientId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                if (document.getData().get(CHEF_ORDERS_COLLECTION) != null) {

                                    // retrieve list of orderIds from client
                                    ArrayList<String> orderIds = (ArrayList<String>) document.getData().get(CHEF_ORDERS_COLLECTION);


                                    // iterate through list
                                    for (String orderId : orderIds) {

                                        // go to orders_collection and retrieve order using the given orderId in the list
                                        database.collection(ORDER_COLLECTION)
                                                .document(orderId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()) {

                                                                //make order object from firebase
                                                                Order order = makeOrderFromFirebase(document);

                                                                //update orders of the logged in client
                                                                App.getClient().ORDERS.addOrder(order);

                                                            } else {
                                                                Log.d("loadClientOrders", "Order not found given orderId stored in chef orders");
                                                            }
                                                        } else {
                                                            Log.d("loadClientOrders", "get failed with ", task.getException());
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Log.d("loadClientOrders", "Chef not found");
                                }
                            }
                        } else {
                            Log.d("loadClientOrders", "get failed with ", task.getException());
                        }
                    });
        }
    }

    public void updateChefRating(String orderId, String chefId, Double newRating){

        if (Preconditions.isNotNull(chefId)) {

            // retrieve client object from firebase
            database.collection(CHEF_COLLECTION)
                    .document(chefId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                Double ratingSum = ((Number) document.getData().get("ratingSum")).doubleValue();
                                int numOfRatings = ((Number) document.getData().get("numOfRatings")).intValue();
                                numOfRatings = numOfRatings + 1;

                                database.collection(CHEF_COLLECTION)
                                        .document(chefId)
                                        .update("ratingSum", ratingSum + newRating,
                                                "numOfRatings", numOfRatings)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                database.collection(ORDER_COLLECTION)
                                                        .document(orderId)
                                                        .update("rating", newRating,
                                                                "isRated", true)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                App.ORDER_HANDLER.handleUpdateChefRatingSuccess();

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                App.ORDER_HANDLER.handleUpdateChefRatingFailure("Failed to update chef's rating!");
                                                            }
                                                        });

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                App.ORDER_HANDLER.handleUpdateChefRatingFailure("Failed to update chef's rating!");
                                            }
                                        });

                            } else {
                                    Log.d("updateChefRating", "Chef not found");
                                }
                        } else {
                            Log.d("updateChefRating" , "get failed with ", task.getException());
                        }
                    });

        }

    }

    protected Order makeOrderFromFirebase(DocumentSnapshot document){

        if (document.getData() == null) {
            throw new NullPointerException("makeOrderFromFirebase: invalid document object");
        }

        Order newOrder = new Order();

        newOrder.setOrderID(document.getId());
        newOrder.setIsCompleted((Boolean)document.getData().get("isCompleted"));
        newOrder.setIsPending((Boolean)document.getData().get("isPending"));
        newOrder.setIsRejected((Boolean)document.getData().get("isRejected"));
        newOrder.setIsRated((Boolean)document.getData().get("isRated"));
        newOrder.setRating(((Number)document.getData().get("rating")).doubleValue());
        if (document.getData().get("complaintSubmitted") != null){
            newOrder.setComplaintSubmitted((Boolean)document.getData().get("complaintSubmitted"));
        }else{
            newOrder.setComplaintSubmitted((Boolean) false);
        }

        Map<String,Object> chefData = (Map<String, Object>) document.getData().get("chefInfo");
        Map<String,Object> clientData = (Map<String, Object>) document.getData().get("clientInfo");
        Map<String, String> chefAddressData = (Map<String, String>) chefData.get("chefAddress");

        AddressEntityModel addressEntityModel = new AddressEntityModel();
        addressEntityModel.setStreetAddress(chefAddressData.get("streetAddress"));
        addressEntityModel.setCity(chefAddressData.get("city"));
        addressEntityModel.setCountry(chefAddressData.get("country"));
        addressEntityModel.setPostalCode(chefAddressData.get("postalCode"));
        Address chefAddress = new Address(addressEntityModel);

        ChefInfo chefInfo = new ChefInfo((String) chefData.get("chefId"), (String) chefData.get("chefName"),
                (chefData.get("chefDescription") != null ? (String) chefData.get("chefDescription") : "no description available"),
                ((Number)chefData.get("chefRating")).intValue(), chefAddress);

        ClientInfo clientInfo = new ClientInfo((String) clientData.get("clientId"), (String) clientData.get("clientName"),
                (String) clientData.get("clientEmail"));

        newOrder.setChefInfo(chefInfo);
        newOrder.setClientInfo(clientInfo);

        try {
            Timestamp timestamp = (Timestamp) document.getData().get("date");
            Date date = timestamp.toDate();
            newOrder.setDate(date);

        } catch (Exception e) {
            newOrder.setDate(Utilities.getTodaysDate());
            Log.e("DATE", "makeOrderFromFirebase: Error parsing date");
        }

        Map<String,Object> mealsData = (Map<String, Object>) document.getData().get("meals");
        Map<String,MealInfo> meals = new HashMap<>();

        for (Map.Entry<String, Object> entry : mealsData.entrySet()) {
            String key = entry.getKey();
            Map<String,Object> value = (Map<String,Object>) entry.getValue();

            MealInfo mealInfo = new MealInfo((String) value.get("name"), ((Number)value.get("price")).doubleValue(), ((Number)value.get("quantity")).intValue());

            meals.put(key, mealInfo);
        }

        newOrder.setMeals(meals);

        return newOrder;
    }
}