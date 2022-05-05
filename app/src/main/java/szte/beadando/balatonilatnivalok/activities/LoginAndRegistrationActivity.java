package szte.beadando.balatonilatnivalok.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import szte.beadando.balatonilatnivalok.R;

public class LoginAndRegistrationActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private static final int SECRET_KEY = 123456789;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_registration);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void registerUser(View view) {
        EditText emailText = findViewById(R.id.emailText);
        EditText passwordText = findViewById(R.id.passwordText);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {

            if (task.isSuccessful()) {
                Intent intent = new Intent(this, MainPageActivity.class);
                intent.putExtra("SECRET_KEY", SECRET_KEY);
                startActivity(intent);

                Toast.makeText(LoginAndRegistrationActivity.this, getString(R.string.successfull_registration) + " " + email, Toast.LENGTH_LONG).show();

                finish();
            } else {
                Toast.makeText(LoginAndRegistrationActivity.this, getString(R.string.unsuccessfull_registration) + " " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void login(View view) {
        EditText emailText = findViewById(R.id.emailText);
        EditText passwordText = findViewById(R.id.passwordText);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(this, MainPageActivity.class);
                intent.putExtra("SECRET_KEY", SECRET_KEY);
                startActivity(intent);

                Toast.makeText(LoginAndRegistrationActivity.this, getString(R.string.successfull_login) + " " + email, Toast.LENGTH_LONG).show();

                finish();
            } else {
                Toast.makeText(LoginAndRegistrationActivity.this, getString(R.string.unsuccessfull_login) + " " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
