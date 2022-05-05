package szte.beadando.balatonilatnivalok.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import szte.beadando.balatonilatnivalok.R;
import szte.beadando.balatonilatnivalok.models.SightOfBalaton;
import szte.beadando.balatonilatnivalok.utils.NotificationSender;

public class AddNewSightActivity extends AppCompatActivity {

    private static final int SECRET_KEY = 123456789;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;

    private NotificationSender notificationSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_sight);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("SightsOfBalaton");

        notificationSender = new NotificationSender(this);
    }

    public void openMainPage(View view) {
        Intent intent = new Intent(this, MainPageActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);

        finish();
    }

    public void addNewSight(View view) {
        EditText nameOfSight = findViewById(R.id.nameOfSightText);
        EditText locationOfSightText = findViewById(R.id.locationOfSightText);
        EditText sightImageLinkText = findViewById(R.id.sightImageLinkText);
        EditText sightDescriptionText = findViewById(R.id.sightDescriptionText);

        collectionReference.add(new SightOfBalaton(
                nameOfSight.getText().toString(),
                locationOfSightText.getText().toString(),
                sightDescriptionText.getText().toString(),
                sightImageLinkText.getText().toString(),
                firebaseUser.getUid(),
                0
        ));

        notificationSender.sendNotificationMessage(getString(R.string.sight_added_successfully) + " " + nameOfSight.getText().toString());
        Toast.makeText(AddNewSightActivity.this, getString(R.string.sight_added_successfully) + " " + nameOfSight.getText().toString(), Toast.LENGTH_LONG).show();

        openMainPage(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.add_sight_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginAndRegistrationActivity.class);
            startActivity(intent);

            Toast.makeText(AddNewSightActivity.this, getString(R.string.successfull_logout), Toast.LENGTH_LONG).show();

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}