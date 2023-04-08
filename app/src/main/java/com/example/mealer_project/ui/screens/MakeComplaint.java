package com.example.mealer_project.ui.screens;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mealer_project.R;
import com.example.mealer_project.app.App;
import com.example.mealer_project.data.entity_models.ComplaintEntityModel;
import com.example.mealer_project.data.models.Client;
import com.example.mealer_project.data.models.Order;
import com.example.mealer_project.ui.core.StatefulView;
import com.example.mealer_project.ui.core.UIScreen;
import com.example.mealer_project.utils.Utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MakeComplaint extends UIScreen implements StatefulView {
    Order orderData;
    Order trueOrder;

    ComplaintEntityModel complaint;
    EditText titleText;
    EditText descriptionText;
    TextView clientName;
    TextView chefName;
    TextView dateOfOrder;
    Button submitComplaint;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_complaint);

        //Variables for displaying order information
        clientName = findViewById(R.id.client_name);
        chefName = findViewById(R.id.chef_name);
        dateOfOrder = findViewById(R.id.meal_date);

        //Variables for the user writing complaints
        titleText = findViewById(R.id.title_of_complaint);
        descriptionText = findViewById(R.id.complaint_writer);

        //Buttons
        submitComplaint = findViewById(R.id.send_complaint_button);
        backButton = findViewById(R.id.backButtonComplaint);

        try {
            orderData = (Order) getIntent().getSerializableExtra("ORDER_DATA_ARG_KEY");
            updateUI();
        } catch (Exception e) {
            Log.e("MakeComplaint", String.valueOf(e));
            Toast.makeText(getApplicationContext(), "Unable to retrieve the meal info!", Toast.LENGTH_LONG).show();
        }

        submitComplaint.setOnClickListener(view -> sendComplaint());

        backButton.setOnClickListener(v -> finish());

    }

    //Creates a complaint
    private void sendComplaint(){
        String title = titleText.getText().toString();
        String description = descriptionText.getText().toString();
        String chefID = orderData.getChefInfo().getChefId();
        String clientID = orderData.getClientInfo().getClientId();
        Date date = Utilities.getTodaysDate();


        complaint = new ComplaintEntityModel(null, title, description, clientID, chefID, date);
        App.getInboxHandler().addNewComplaint(complaint, this);

        Toast.makeText(getApplicationContext(), "Complaint Sent!", Toast.LENGTH_LONG).show();
        orderData.setComplaintSubmitted(true);
        List<Order> orders = ((Client) App.getUser()).ORDERS.getCompletedOrders();
        for (int x = 0; x < orders.size(); x++){
            if (orders.get(x).getOrderID().equals(orderData.getOrderID())){
                trueOrder = orders.get(x);
                trueOrder.setComplaintSubmitted(true);
            }
        }
        App.getPrimaryDatabase().ORDERS.updateComplaintStatus(trueOrder);
        this.finish();
    }

    // UI Methods-----------------------------------------------------------------------------------------------
    //sets the values of the order information
    @Override
    public void updateUI(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        clientName.setText("Client Name: " + orderData.getClientInfo().getClientName());
        chefName.setText("Chef Name: " + orderData.getChefInfo().getChefName());
        dateOfOrder.setText("Date: " + dateFormat.format(orderData.getOrderDate()));
    }

    @Override
    public void showNextScreen() {

    }

    // Firebase Methods-----------------------------------------------------------------------------------------------
    /**
     * Method to handle success of a DB operation
     *
     * @param dbOperation
     * @param payload
     */
    @Override
    public void dbOperationSuccessHandler(Object dbOperation, Object payload) {
        App.getAppInstance().getCompletedOrdersScreen().notifyDataChanged();
        displaySuccessToast((String) payload);
    }

    /**
     * Method to handle failure of a DB operation
     *
     * @param dbOperation
     * @param payload
     */
    @Override
    public void dbOperationFailureHandler(Object dbOperation, Object payload) {
        displayErrorToast((String) payload);
    }
}