package szte.beadando.balatonilatnivalok.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import szte.beadando.balatonilatnivalok.asynk_tasks.DownloadImageTask;
import szte.beadando.balatonilatnivalok.R;
import szte.beadando.balatonilatnivalok.models.SightOfBalaton;
import szte.beadando.balatonilatnivalok.utils.NotificationSender;

public class MainPageActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSION = 123456789;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private List<SightOfBalaton> sightsOfBalatonList = new ArrayList<>();

    private SightOfBalaton actualSight;
    private NotificationSender notificationSender;
    private Context context;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Bundle bundleExtras = getIntent().getExtras();
        int secretKey = bundleExtras.getInt("SECRET_KEY", 0);

        if (secretKey != 123456789 || firebaseUser == null) {
            finish();
        }

        notificationSender = new NotificationSender(this);
        context = this;
        animation = AnimationUtils.loadAnimation(context, R.anim.bounce);

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("SightsOfBalaton");

        downloadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_page_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout: {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginAndRegistrationActivity.class);
                startActivity(intent);

                Toast.makeText(MainPageActivity.this, getString(R.string.successfull_logout), Toast.LENGTH_LONG).show();

                finish();
                return true;
            }
            case R.id.refreshSight: {
                setRandomSight();
                return true;
            }
            case R.id.addSight: {
                Intent intent = new Intent(this, AddNewSightActivity.class);
                startActivity(intent);

                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setRandomSight() {
        Random rand = new Random();
        SightOfBalaton sightOfBalaton = sightsOfBalatonList.get(rand.nextInt(sightsOfBalatonList.size()));
        actualSight = sightOfBalaton;

        ImageView sightImage = findViewById(R.id.sightImage);
        sightImage.setImageDrawable(null);
        new DownloadImageTask(MainPageActivity.this, sightImage).execute(sightOfBalaton.getImageLink());

        ImageButton voteButton = findViewById(R.id.voteButton);
        int id;
        if (actualSight.getVotedUsersId().contains(firebaseUser.getUid())) {
            id = getResources().getIdentifier("szte.beadando.balatonilatnivalok:drawable/filled_like", null, null);
        } else {
            id = getResources().getIdentifier("szte.beadando.balatonilatnivalok:drawable/outlined_like", null, null);
        }
        voteButton.setImageResource(id);

        ImageButton deleteButton = findViewById(R.id.deleteButton);
        if (actualSight.getAuthorId().equals(firebaseUser.getUid())) {
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }

        TextView nameOfSightText = findViewById(R.id.nameOfSightText);
        TextView locationOfSightText = findViewById(R.id.locationOfSightText);
        TextView ratingOfSight = findViewById(R.id.ratingOfSight);
        TextView descriptionOfSightText = findViewById(R.id.descriptionOfSightText);
        nameOfSightText.setText(sightOfBalaton.getName());
        locationOfSightText.setText(sightOfBalaton.getLocation());
        ratingOfSight.setText(Integer.toString(sightOfBalaton.getVotes()));
        descriptionOfSightText.setText(sightOfBalaton.getDescription());
    }

    private void downloadData() {
        sightsOfBalatonList.clear();

        collectionReference.limit(100).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                SightOfBalaton sightOfBalaton = queryDocumentSnapshot.toObject(SightOfBalaton.class);
                sightOfBalaton.setId(queryDocumentSnapshot.getId());

                sightsOfBalatonList.add(sightOfBalaton);
            }

            setRandomSight();
        });
    }

    public void vote(View view) {
        ImageButton voteButton = findViewById(R.id.voteButton);
        if (!actualSight.getVotedUsersId().contains(firebaseUser.getUid())) {
            List<String> updatedVotedUserIds = actualSight.getVotedUsersId();
            updatedVotedUserIds.add(firebaseUser.getUid());

            DocumentReference documentReference = collectionReference.document(actualSight.getId());
            documentReference.update("votes", actualSight.getVotes() + 1).addOnCompleteListener(this, task -> {
                actualSight.setVotes(actualSight.getVotes() + 1);

                TextView voteText = findViewById(R.id.ratingOfSight);
                voteText.setText(Integer.toString(actualSight.getVotes()));
                voteText.startAnimation(animation);
            });
            documentReference.update("votedUsersId", updatedVotedUserIds).addOnCompleteListener(this, task -> {
                actualSight.setVotedUsersId(updatedVotedUserIds);
            });

            int id = getResources().getIdentifier("szte.beadando.balatonilatnivalok:drawable/filled_like", null, null);
            voteButton.setImageResource(id);
        } else {
            List<String> updatedVotedUserIds = actualSight.getVotedUsersId();
            updatedVotedUserIds.remove(firebaseUser.getUid());

            DocumentReference documentReference = collectionReference.document(actualSight.getId());
            documentReference.update("votes", actualSight.getVotes() - 1).addOnCompleteListener(this, task -> {
                actualSight.setVotes(actualSight.getVotes() - 1);

                TextView voteText = findViewById(R.id.ratingOfSight);
                voteText.setText(Integer.toString(actualSight.getVotes()));
                voteText.startAnimation(animation);
            });
            documentReference.update("votedUsersId", updatedVotedUserIds).addOnCompleteListener(this, task -> {
                actualSight.setVotedUsersId(updatedVotedUserIds);
            });

            int id = getResources().getIdentifier("szte.beadando.balatonilatnivalok:drawable/outlined_like", null, null);
            voteButton.setImageResource(id);
        }
    }

    public void delete(View view) {
        sightsOfBalatonList.remove(actualSight);

        DocumentReference documentReference = collectionReference.document(actualSight.getId());
        documentReference.delete();

        notificationSender.sendNotificationMessage(getString(R.string.sight_deleted_successfully) + " " + actualSight.getName());
        Toast.makeText(MainPageActivity.this, getString(R.string.sight_deleted_successfully) + " " + actualSight.getName(), Toast.LENGTH_LONG).show();
        setRandomSight();
    }

    public void checkStoragePermission(View view) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                return;
            }
        }

        ImageView sightImage = findViewById(R.id.sightImage);
        BitmapDrawable drawable = (BitmapDrawable) sightImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        downloadSightImage(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImageView sightImage = findViewById(R.id.sightImage);
                BitmapDrawable drawable = (BitmapDrawable) sightImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                downloadSightImage(bitmap);
            } else {
                Toast.makeText(MainPageActivity.this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void downloadSightImage(Bitmap finalBitmap) {
        ContentValues values = contentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/sights_of_balaton");
        values.put(MediaStore.Images.Media.IS_PENDING, true);

        Uri uri = null;
        if (MediaStore.Images.Media.EXTERNAL_CONTENT_URI != null) {
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }

        if (uri != null) {
            try {
                saveImageToStream(finalBitmap, context.getContentResolver().openOutputStream(uri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            values.put(MediaStore.Images.Media.IS_PENDING, false);
            context.getContentResolver().update(uri, values, null, null);
        } else {
            File directory = new File(Environment.getExternalStorageDirectory().toString() + "/sights_of_balaton");

            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = actualSight.getName() + ".png";
            File file = new File(directory, fileName);
            try {
                saveImageToStream(finalBitmap, new FileOutputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (file.getAbsolutePath() != null) {
                values = contentValues();
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        }

        Toast.makeText(MainPageActivity.this, getString(R.string.image_succesfully_downloaded), Toast.LENGTH_LONG).show();
    }

    private ContentValues contentValues() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        return values;
    }

    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        downloadData();
    }
}