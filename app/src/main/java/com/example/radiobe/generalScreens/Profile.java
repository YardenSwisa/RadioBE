package com.example.radiobe.generalScreens;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.radiobe.R;
import com.example.radiobe.database.CurrentUser;
import com.example.radiobe.database.RefreshUserName;
import com.example.radiobe.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class Profile extends AppCompatActivity implements RefreshUserName {
    private final String FIRST_NAME = "First Name ";
    private final String LAST_NAME = "Last name ";
    private final String DATE_OF_BIRTH = "Date of birth ";
    private final String PASSWORD = "Password ";
    private final int PICK_COVER_IMAGE = 71;
    private final int PICK_PROFILE_IMAGE = 72;
    private final long ONE_MEGABYTE = 2048 * 2048;
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri filePath;
    private ImageView editImageProfile;
    private ImageView editCoverImage;
    private ImageView coverImage;
    private ImageView editDetails;
    private ImageView editAboutMeButton;
    private EditText dialogAboutMeEditText;
    private CircleImageView profileImage;
    private TextView profileName;
    private TextView email;
    private TextView profileDescription;
    private TextView profileBirthDay;
    //dialog
    private EditText editDialogName;
    private EditText editDialogLastName;
    private EditText editPassword;
    private EditText editConfirmPassword;

    private View viewForAlert;
    private View viewForAlertAboutMe;
    private String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private DatePicker datePicker;
    private Button positiveButton;
    private Button negativeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupViews();

        CurrentUser.getInstance().registerUsernameObserver(this);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        setupInfo();

        editImageProfile.setOnClickListener((view -> {

            Toast.makeText(this, "The image MUST be Smaller then 4 MegaBytes(MB).", Toast.LENGTH_LONG).show();
            chooseImage(PICK_PROFILE_IMAGE);
        }));
        editCoverImage.setOnClickListener((view -> {
            Toast.makeText(this, "The image MUST be Smaller then 4 MegaBytes(MB).", Toast.LENGTH_LONG).show();
            chooseImage(PICK_COVER_IMAGE);

        }));

        editDetails.setOnClickListener((view -> {
            setEditDetails();
        }));

        editAboutMeButton.setOnClickListener((view -> {
            setAboutMe();

        }));

    }

    private void setupInfo() {
        profileImage.setImageBitmap(CurrentUser.getInstance().getProfileImage());
        coverImage.setImageBitmap(CurrentUser.getInstance().getCoverImage());
        profileName.setText(String.format("%s %s", CurrentUser.getInstance().getFirstName(), CurrentUser.getInstance().getLastName()));
        email.setText(CurrentUser.getInstance().getEmail());

        if (CurrentUser.getInstance().getDescription() != null) {
            profileDescription.setText(CurrentUser.getInstance().getDescription());
        } else {
            profileDescription.setText(String.format("Hello my name is %s nice to meet you", CurrentUser.getInstance().getFirstName()));

        }

        if (CurrentUser.getInstance().getBirthDate() != 0) {
            //todo: are we going to add description to the user registration? if so, get it from current user too.
            profileBirthDay.setText(CurrentUser.getInstance().getBirthDateString());
        } else {
            profileBirthDay.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_COVER_IMAGE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    filePath = data.getData();
                    uploadImage("cover", coverImage);
                } else {
                    Toast.makeText(this, "Something wasn't done right.", Toast.LENGTH_SHORT).show();

                }
                break;
            case PICK_PROFILE_IMAGE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    filePath = data.getData();
                    uploadImage("profile", profileImage);
                } else {
                    Toast.makeText(this, "Something wasn't done right.", Toast.LENGTH_SHORT).show();

                }
                break;
        }

    }


    /**
     * UploadImage gave us the ability to choose or create a folder on the FireBase Storage
     * and upload the image that the user pick.
     *
     * @param folder -> represent the folder that we want create or upload to it.
     */
    private void uploadImage(String folder, ImageView imagePlace) {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Please Wait Uploading...");
            progressDialog.show();


            StorageReference ref = storageReference.child(folder).child(uid);
            ref.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        //only after the photo was uploaded successfully to the server im setting in the UI
                        imagePlace.setImageURI(filePath);

                        //set the current user photo too.
                        updateCurrentUserPhotoChange(folder);

                        progressDialog.dismiss();
                        Toast.makeText(Profile.this, "Your photo was uploaded successfully!", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(Profile.this, "Failed " + e.getMessage(), Toast.LENGTH_LONG).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploading " + (int) progress + "%");
                    });
        }
    }


    /**
     * This method purpose is to update the current user object with the new photo being picked.
     * Its being called only if the upload to the server was a success.
     *
     * @param folder -> represent the kind of photo to be uploaded.
     */

    private void updateCurrentUserPhotoChange(String folder) {
        switch (folder) {
            case "profile":
                try {
                    CurrentUser.getInstance().setProfileImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath));

                    //Todo: notify observers.
                    CurrentUser.getInstance().notifyProfilePictureObservers();
                    System.out.println(CurrentUser.getInstance().getProfileImage());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("File wasn't uploaded to Current user profile");
                }
            case "cover":
                try {
                    CurrentUser.getInstance().setCoverImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath));
                    System.out.println(CurrentUser.getInstance().getCoverImage());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("File wasn't uploaded to Current user profile");
                }
        }

    }

    /**
     * ChooseImage gave us the ability to choose one image
     * from the gallery phone , downloads  or google drive.
     *
     * @param pickImageRequest -> it's a number that change.
     */
    private void chooseImage(int pickImageRequest) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), pickImageRequest);
    }

    /**
     * Let the user set the values of his personal details,
     * like name, description or birth day.
     */
    @SuppressLint("InflateParams")
    private void setEditDetails() {
        List<String> editedDetails = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your new details");
        viewForAlert = LayoutInflater.from(this).inflate(R.layout.dialog_view, null);
        editDialogName = viewForAlert.findViewById(R.id.edit_dialog_firstName);
        editDialogLastName = viewForAlert.findViewById(R.id.edit_dialog_lastName);
        editPassword = viewForAlert.findViewById(R.id.edit_password);
        editConfirmPassword = viewForAlert.findViewById(R.id.edit_confirmPassword);

        editDialogName.setText(CurrentUser.getInstance().getFirstName());
        editDialogLastName.setText(CurrentUser.getInstance().getLastName());

        positiveButton = viewForAlert.findViewById(R.id.positiveButton);
        negativeButton = viewForAlert.findViewById(R.id.negativeButton);
        datePicker = viewForAlert.findViewById(R.id.datePicker);
        datePicker.setMaxDate(new Date().getTime());
        builder.setView(viewForAlert);

        viewForAlert.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                if (imm.isAcceptingText()) { // verify if the soft keyboard is open
                    imm.hideSoftInputFromWindow(viewForAlert.getWindowToken(), 0);
                }
                return false;
            }
        });

        //todo: init picker with the date of birth.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(CurrentUser.getInstance().getBirthDate()));
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), (view, year, monthOfYear, dayOfMonth) -> {

                    Calendar dateOfBirth = Calendar.getInstance();
                    dateOfBirth.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                    dateOfBirth.set(Calendar.MILLISECOND, 0);

                    Calendar minAdultAge = Calendar.getInstance();
                    minAdultAge.add(Calendar.YEAR, -16);
                    if (minAdultAge.before(dateOfBirth)) {
                        Toast.makeText(Profile.this, "You must be over 16 years old to use Radio BE", Toast.LENGTH_SHORT).show();
                        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));

                    }
                });


        builder.setIcon(R.drawable.pan_edit);
        AlertDialog alert = builder.create();


        alert.show();


        positiveButton.setOnClickListener((v) -> {
            String name = editDialogName.getText().toString();
            String lastName = editDialogLastName.getText().toString();
            String password = editPassword.getText().toString();
            String confirmPassword = editConfirmPassword.getText().toString();
            Calendar dateOfBirth = Calendar.getInstance();
            dateOfBirth.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0, 0);
            dateOfBirth.set(Calendar.MILLISECOND, 0);


            if (checkForChanges(name, lastName, dateOfBirth, password, confirmPassword)) {
                System.out.println("IN NEW IF ???");
                Toast.makeText(this, "No changes were made!", Toast.LENGTH_SHORT).show();
                alert.dismiss();

            }

            if (password.length() < 1 && confirmPassword.length() < 1) {

                if (name.length() < 1) {
                    editDialogName.setError("Your name must be longer than 0 characters");
                } else if (!name.equals(CurrentUser.getInstance().getFirstName())) {
                    if (!editedDetails.contains(FIRST_NAME))
                        editedDetails.add(FIRST_NAME);
                }

                if (lastName.length() < 1) {
                    editDialogLastName.setError("Your last name must be longer than 0 characters");
                } else if (!lastName.equals(CurrentUser.getInstance().getLastName())) {
                    if (!editedDetails.contains(LAST_NAME))
                        editedDetails.add(LAST_NAME);
                }

                if (dateOfBirth.getTimeInMillis() != CurrentUser.getInstance().getBirthDate()) {
                    if (!editedDetails.contains(DATE_OF_BIRTH))
                        editedDetails.add(DATE_OF_BIRTH);
                }


                if (editDialogName.getError() != null || editDialogLastName.getError() != null) {
                    Toast.makeText(Profile.this, "Check Errors", Toast.LENGTH_SHORT).show();

                } else {

                    User user = new User(CurrentUser.getInstance().getFireBaseID(), name, lastName, CurrentUser.getInstance().getEmail(), dateOfBirth.getTimeInMillis());
                    ref.child("users").child(CurrentUser.getInstance().getFireBaseID()).updateChildren(user.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            CurrentUser.getInstance().loadDetailsFromMap();
                            alert.dismiss();
                        }
                    });

                    if (editedDetails.size() > 0) {
                        StringBuilder message = new StringBuilder();
                        for (String editedDetail : editedDetails) {
                            message.append(editedDetail);
                        }
                        Toast.makeText(this, "You have changed your " + message + "successfully!", Toast.LENGTH_SHORT).show();

                    }
                }
            } else {

                //not empty and equal -- V
                if (password.equals(confirmPassword)) {

                    if (name.length() < 1) {
                        editDialogName.setError("Your name must be longer than 0 characters");
                    } else if (!name.equals(CurrentUser.getInstance().getFirstName())) {
                        if (!editedDetails.contains(FIRST_NAME))
                            editedDetails.add(FIRST_NAME);
                    }

                    if (lastName.length() < 1) {
                        editDialogLastName.setError("Your last name must be longer than 0 characters");
                    } else if (!lastName.equals(CurrentUser.getInstance().getLastName())) {
                        if (!editedDetails.contains(LAST_NAME))
                            editedDetails.add(LAST_NAME);
                    }

                    if (dateOfBirth.getTimeInMillis() != CurrentUser.getInstance().getBirthDate()) {
                        if (!editedDetails.contains(DATE_OF_BIRTH))
                            editedDetails.add(DATE_OF_BIRTH);
                    }


                    if (editDialogName.getError() == null && editDialogLastName.getError() == null) {


                        //updade pass
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        firebaseUser.updatePassword(password).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                editedDetails.add(PASSWORD);
                                User user = new User(CurrentUser.getInstance().getFireBaseID(), name, lastName, CurrentUser.getInstance().getEmail(), dateOfBirth.getTimeInMillis());

                                ref.child("users").child(CurrentUser.getInstance().getFireBaseID()).updateChildren(user.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        CurrentUser.getInstance().loadDetailsFromMap();
                                        alert.dismiss();
                                    }
                                });


                                if (editedDetails.size() > 0) {
                                    StringBuilder message = new StringBuilder();
                                    for (String editedDetail : editedDetails) {
                                        message.append(editedDetail);
                                    }
                                    Toast.makeText(Profile.this, "You have changed the following details: " + message, Toast.LENGTH_SHORT).show();

                                }

                            } else {
                                System.out.println("WEIRD");
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    editPassword.setError("הסיסמה חלשה מדי, אנא הכנס סיסמה עם 6 תווים ומעלה");
                                    editPassword.requestFocus();
                                    System.out.println("WEAK PASS");
                                } catch (FirebaseAuthRecentLoginRequiredException e) {
                                    editPassword.setError("Re-confirmation of your details is required");
                                    AlertDialog.Builder builderApprove = new AlertDialog.Builder(Profile.this);
                                    builderApprove.setTitle("Re-confirmation Details");
                                    builderApprove.setMessage("Please re-confirm your soon to be changed details");
                                    View newViewForAlert = LayoutInflater.from(Profile.this).inflate(R.layout.change_email_dialog, null);
                                    EditText authEmail = newViewForAlert.findViewById(R.id.editEmailText);
                                    EditText authPassword = newViewForAlert.findViewById(R.id.editPasswordText);
                                    builderApprove.setPositiveButton("Done", (d, v1) -> {

                                        AuthCredential credential = EmailAuthProvider.getCredential(authEmail.getText().toString(), authPassword.getText().toString());
                                        firebaseUser.reauthenticate(credential).addOnSuccessListener(aVoid -> firebaseUser.updatePassword(password).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                System.out.println("PASS SUPPOSE TO CHANGE");
                                                editPassword.setError(null);
                                                User user = new User(CurrentUser.getInstance().getFireBaseID(), name, lastName, CurrentUser.getInstance().getEmail(), dateOfBirth.getTimeInMillis());
                                                ref.child("users").child(CurrentUser.getInstance().getFireBaseID()).updateChildren(user.toMap());
                                                editedDetails.add(PASSWORD);


                                                if (editedDetails.size() > 0) {
                                                    StringBuilder message = new StringBuilder();
                                                    for (String editedDetail : editedDetails) {
                                                        message.append(editedDetail);
                                                    }
                                                    Toast.makeText(Profile.this, "You have changed the following details: " + message, Toast.LENGTH_SHORT).show();

                                                }

                                                CurrentUser.getInstance().loadDetailsFromMap();
                                                alert.dismiss();
                                            } else {
                                                try {
                                                    throw Objects.requireNonNull(task1.getException());
                                                } catch (FirebaseAuthWeakPasswordException e1) {
                                                    editPassword.setError("הסיסמה חלשה מדי, אנא הכנס סיסמה עם 6 תווים ומעלה");
                                                    editPassword.requestFocus();
                                                } catch (Exception ex) {
                                                    System.out.println(ex.getMessage());
                                                }
                                            }
                                        })).addOnFailureListener(e12 -> Toast.makeText(Profile.this, "Re-authentication failed!", Toast.LENGTH_SHORT).show());


                                    });

                                    builderApprove.setView(newViewForAlert);
                                    builderApprove.show();
                                } catch (Exception ex) {
                                    System.out.println(ex.getMessage());
                                }
                            }
                        });

                    } else {
                        Toast.makeText(Profile.this, "Check Errors", Toast.LENGTH_SHORT).show();

                    }

                } else {
                    //not empty and not equal
                    editConfirmPassword.setError("Your passwords must be the same");
                }
            }

        });

        negativeButton.setOnClickListener((v) -> {
            alert.dismiss();
            Toast.makeText(this, "No change were made!", Toast.LENGTH_SHORT).show();
        });

    }

    public void setAboutMe() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your new About Me");
        viewForAlertAboutMe = LayoutInflater.from(this).inflate(R.layout.dialog_about_me, null);
        dialogAboutMeEditText = viewForAlertAboutMe.findViewById(R.id.edit_about_me);
        positiveButton = viewForAlertAboutMe.findViewById(R.id.positiveButton);
        negativeButton = viewForAlertAboutMe.findViewById(R.id.negativeButton);

        viewForAlertAboutMe.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                if (imm.isAcceptingText()) { // verify if the soft keyboard is open
                    imm.hideSoftInputFromWindow(viewForAlertAboutMe.getWindowToken(), 0);
                }
                return false;
            }
        });
        builder.setView(viewForAlertAboutMe);
        builder.setIcon(R.drawable.pan_edit);
        AlertDialog alertDialog = builder.create();

        alertDialog.show();


        if (CurrentUser.getInstance().getDescription() != null) {
            dialogAboutMeEditText.setText(CurrentUser.getInstance().getDescription());
        } else {
            dialogAboutMeEditText.setText(String.format("Hello my name is %s nice to meet you", CurrentUser.getInstance().getFirstName()));
        }

        dialogAboutMeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= 300) {
                    dialogAboutMeEditText.setError("Max limit 300 characters");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        positiveButton.setOnClickListener((v) -> {
            String aboutMe = dialogAboutMeEditText.getText().toString();

            if (aboutMe.length() < 1) {
                dialogAboutMeEditText.setError("Your description must be longer than 0 characters");
            }

            if (dialogAboutMeEditText.getError() != null) {
                Toast.makeText(this, dialogAboutMeEditText.getError().toString(), Toast.LENGTH_SHORT).show();
            } else {
                ref.child("users").child(CurrentUser.getInstance().getFireBaseID()).child("description").setValue(aboutMe);
                CurrentUser.getInstance().setDescription(aboutMe);
                profileDescription.setText(aboutMe);
                Toast.makeText(this, "Your description was updated successfully!", Toast.LENGTH_SHORT).show();
                System.out.println(CurrentUser.getInstance().getDescription());
                alertDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener((v) -> {
            alertDialog.dismiss();
            Toast.makeText(this, "No change!", Toast.LENGTH_SHORT).show();
        });

    }

    /**
     * setup all the Views on Profile be equals them to there ID's
     */
    private void setupViews() {
        editImageProfile = findViewById(R.id.idProfileEditImage);
        profileImage = findViewById(R.id.idImageViewProfile);
        editCoverImage = findViewById(R.id.idCoverEditImage);
        coverImage = findViewById(R.id.idCoverTopProfile);
        profileName = findViewById(R.id.idFullNameProfile);
        profileDescription = findViewById(R.id.idDescriptionProfile);
        profileBirthDay = findViewById(R.id.idBirthDayProfile);
        editDetails = findViewById(R.id.idEditDetails);
        email = findViewById(R.id.idEmail);
        editAboutMeButton = findViewById(R.id.idEditDetailsDescription);
        dialogAboutMeEditText = findViewById(R.id.edit_about_me);


    }

    //check if there was any change to the details.
    private boolean checkForChanges(String firstName, String lastName, Calendar
            dateOfBirth, String password, String confirmPassword) {
        return (firstName.equals(CurrentUser.getInstance().getFirstName()) &&
                lastName.equals(CurrentUser.getInstance().getLastName()) &&
                dateOfBirth.getTimeInMillis() == CurrentUser.getInstance().getBirthDate() &&
                password.isEmpty() &&
                (confirmPassword.equals(password) || confirmPassword.isEmpty()));
    }

    @Override
    public void refresh() {
        profileName.setText(String.format("%s %s", CurrentUser.getInstance().getFirstName(), CurrentUser.getInstance().getLastName()));
        profileBirthDay.setText(CurrentUser.getInstance().getBirthDateString());
    }
}
