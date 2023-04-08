package com.example.mealer_project.data.handlers;

import android.util.Log;

import com.example.mealer_project.app.App;
import com.example.mealer_project.data.entity_models.ComplaintEntityModel;
import com.example.mealer_project.data.models.inbox.AdminInbox;
import com.example.mealer_project.data.models.inbox.Complaint;
import com.example.mealer_project.ui.core.StatefulView;
import com.example.mealer_project.ui.screens.AdminScreen;
import com.example.mealer_project.utils.Preconditions;
import com.example.mealer_project.utils.Response;
import com.example.mealer_project.utils.Result;

import java.util.List;

/**
 * Class to handle operations related to Admin's Inbox
 */
public class InboxHandler {

     private StatefulView uiScreen;
     private AdminScreen adminScreen;

     private enum dbOperations {
          ADD_COMPLAINT
     }

     /**
      * Checks if current user has access to admin only resources
      * @return Response object indicating success or failure (with error message if any)
      */
     private Response userHasAccess() {
          if (App.userIsAdmin()) {
               return new Response(true);
          } else {
               return new Response(false, "Access denied. User is not an admin.");
          }
     }

     /**
      * Get App's admin inbox if current user has access
      * @return Result object - if operation successful, contains AdminInbox, else String containing error message
      */
     public Result<AdminInbox, String> getAdminInbox() {
          // check if current user does not have access (user is not an admin)
          if (userHasAccess().isError()) {
               return new Result<>(null, userHasAccess().getErrorMessage());
          }
          // attempt to get Admin's inbox
          try {
               return new Result<>(App.getAdminInbox(), null);
          } catch (IllegalAccessException e) {
               return new Result<>(null, "Could not retrieve admin inbox. Access denied.");
          }
     }

     /**
      * Method to update App's AdminInbox
      * Retrieves all complaints from the database and creates a new AdminInbox
      * Stores the inbox by setting AppInstance's adminInbox to this new inbox
      * @return Response object indicating success or failure
      */
     public Response updateAdminInbox(AdminScreen inboxView) {
          // check if current user does not have access (user is not an admin)
          if (userHasAccess().isError()) {
               return userHasAccess();
          }

          // set inbox view
          this.adminScreen = inboxView;


          // make async call to fetch all complaints from database
          App.getPrimaryDatabase().INBOX.getAllComplaints(this);

          // return response once request to get all complaints has been submitted
          return new Response(true, "retrieving complaints for admin");
     }

     /**
      * Handles the response from async call made to obtain all complaints from database in updateAdminInbox method
      * Creates AdminInbox using the list of complaints and updates App's admin inbox
      * @param complaints list of complaints retrieved from database
      */
     public void createNewAdminInbox(List<Complaint> complaints){
          Log.e("complaint", "called 3");
          // validate complaints
          if (Preconditions.isNotEmptyList(complaints)) {
               // instantiate a new admin inbox by providing it list of complaints
               // set App's new admin inbox
               try {
                    App.setAdminInbox(new AdminInbox(complaints));
               } catch (NullPointerException e) {
                    Log.e("createNewAdminInbox", "one of the complaints is null: " + e.getMessage());

                    // guard-clause - make sure we have a valid instance of admin screen
                    if (adminScreen == null) {
                         Log.e("createNewAdminInbox", "adminScreen has not been instantiated yet, is null");
                    } else {
                         adminScreen.dbOperationFailureHandler(AdminScreen.dbOperations.GET_COMPLAINTS, "Failed to load complaints");
                    }

               } catch (Exception e) {
                    Log.e("createNewAdminInbox", "an exception occurred while creating Admin Inbox: " + e.getMessage());
                    // guard-clause - make sure we have a valid instance of admin screen
                    if (adminScreen == null) {
                         Log.e("createNewAdminInbox", "adminScreen has not been instantiated yet, is null");
                    } else {
                         adminScreen.dbOperationFailureHandler(AdminScreen.dbOperations.GET_COMPLAINTS, "Failed to load complaints");
                    }
               }

               // call method in inboxView to update inbox so admin can see all complaints
               adminScreen.dbOperationSuccessHandler(AdminScreen.dbOperations.GET_COMPLAINTS, "Complaints loaded!");
          } else {
               // display error in inbox view
               adminScreen.dbOperationFailureHandler(AdminScreen.dbOperations.GET_COMPLAINTS, "No complaints available for admin inbox");
          }
     }

     /**
      * Handle any error if it happens during retrieval of complaints from database
      * @param message error message
      */
     public void errorGettingComplaints(String message) {
          // display error on inbox view
          // inboxView.failedToLoadComplaints("Error getting complaints from database");
          Log.d("errorGettingComplaints", message );
     }

     /**
      * Add new complaint to the App's admin inbox and 'Complaint' collection on database
      * @param complaintEntityModel unvalidated complaint data
      * @return Response indicating error, if current user doesn't have access
      */
     public Response addNewComplaint(ComplaintEntityModel complaintEntityModel, StatefulView uiScreen) {
          // check if user has access
//          if (userHasAccess().isError()) {
//               return userHasAccess();
//          }

          Complaint complaint = null;
          // set UI Screen so error and success can be notified
          this.uiScreen = uiScreen;

          try {
               // try to create a Complaint object using unvalidated complaint data
               complaint = new Complaint(complaintEntityModel);
               // if data validation successful, initiate process to add complaint to database
               // once complaint has been added to database, it will be added locally to AdminInbox by successAddingComplaint
               App.getPrimaryDatabase().INBOX.addComplaint(complaint, this);
          } catch (Exception e) {
               errorAddingComplaint(e.getMessage());
          }

          // make the async call to add complaint to database

          return new Response(false, "method not implemented yet");
     }

     /**
      * Method to let UI know async operation to add complaint to database completed
      */
     public void successAddingComplaint(Complaint complaint) {
          // once complaint has been added to database (it will have an id)
          try {
               if (App.getAppInstance().userIsAdmin()) {
                    App.getAdminInbox().addComplaint(complaint);
                    // let ui know adding complaint has completed
                    // inboxView.successAddingComplaint();
               } else {
                    // let ui know of success
                    this.uiScreen.dbOperationSuccessHandler(dbOperations.ADD_COMPLAINT, "complaint added successfully");
               }
          } catch (Exception e) {
               errorAddingComplaint("complaint added on database, but failed to add to AdminInbox" + e.getMessage());
          }
     }

     /**
      * Handle any error if it happens during adding complaint to the database
      * @param message error message
      */
     public void errorAddingComplaint(String message) {
          // display error on inbox view
          // inboxView.handleError("Failed to add complaint to the database");
          Log.d("errorAddingComplaint", message );
          if (uiScreen != null) {
               this.uiScreen.dbOperationFailureHandler(dbOperations.ADD_COMPLAINT, message);
          }
     }

     /**
      * Remove a complaint from App's AdminInbox and 'Complaint' collection on database
      * @param complaintId id of the complaint to be removed
      * @return Response indicating error, if current user doesn't have access
      */
     public Response removeComplaint(String complaintId) {
          // check if user has access
          if (userHasAccess().isError()) {
               return userHasAccess();
          }

          try {
               // remove complaint from App's AdminInbox
               App.getAdminInbox().removeComplaint(complaintId);

               // remove complaint from firebase
               App.getPrimaryDatabase().INBOX.removeComplaint(complaintId, this);

          } catch (Exception e) {
               return new Response(false, e.getMessage());
          }

          return new Response(false, "method not implemented yet");
     }

     /**
      * Method to let UI know async operation to remove complaint from database completed
      */
     public void successRemovingComplaint() {
          // let ui know adding complaint has completed
          // inboxView.successRemovingComplaint();
     }

     /**
      * Handle any error if it happens during adding complaint to the database
      * @param message error message
      */
     public void errorRemovingComplaint(String message) {
          // display error on inbox view
          // inboxView.handleError("Failed to remove complaint from the database");
          Log.d("errorRemovingComplaint", message );
     }
}
