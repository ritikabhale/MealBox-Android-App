package com.example.mealer_project.ui.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.mealer_project.R;
import com.example.mealer_project.ui.core.UIScreen;

public class IntroScreen extends UIScreen {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen);
    }

    /**
     * this method describes the behaviour for the sign up button
     *
     * it leads the user to the sign up screen when the button is pressed
     *
     * @param view
     *  the intro screen display
     */
    public void OnSetSignUp(View view) {

        // Variable Declaration
        Intent intent = new Intent(getApplicationContext(), SignupScreen.class); //where SignUp.class is the sign up activity
        startActivity (intent);
    }

    /**
     * this method describes the behaviour for the log in button
     *
     * it leads the user to the login screen when the button is pressed
     *
     * @param view
     *  the intro screen display
     */
    public void OnSetLogIn(View view) {

        // Variable Declaration
        Intent intent = new Intent(getApplicationContext(), LoginScreen.class); //where LogIn.class is the login activity
        startActivity (intent);
    }

}