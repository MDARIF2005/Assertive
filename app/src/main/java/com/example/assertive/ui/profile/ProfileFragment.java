package com.example.assertive.ui.profile;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.assertive.DatabaseHelper;
import com.example.assertive.LoginActivity;
import com.example.assertive.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail, tvDob, tvGender;
    private Button btnEditProfile, btnLogout;
    private DatabaseHelper databaseHelper;
    private FirebaseAuth firebaseAuth;
    private String userId;
    private final Calendar myCalendar = Calendar.getInstance(); // Initialize Calendar

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        tvDob = view.findViewById(R.id.tv_dob);
        tvGender = view.findViewById(R.id.tv_gender);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Firebase & DB
        firebaseAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(requireContext());

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            fetchUserDataFromSQLite();
        } else {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        // Edit Profile using custom dialog
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Logout
        btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(requireContext(), "Logged Out!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    // Fetch user data from SQLite DB
    private void fetchUserDataFromSQLite() {
        try (Cursor cursor = databaseHelper.getUser(userId)) {
            if (cursor != null && cursor.moveToFirst()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String dob = cursor.getString(cursor.getColumnIndexOrThrow("DOB"));
                String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));

                tvUsername.setText(username);
                tvEmail.setText("Email: " + email);
                tvDob.setText("DOB: " + dob);
                tvGender.setText("Gender: " + gender);
            } else {
                Toast.makeText(requireContext(), "No data found in SQLite", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showEditProfileDialog() {
        LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
        View dialogView = dialogInflater.inflate(R.layout.fragment_editprofile, null);

        EditText etUsername = dialogView.findViewById(R.id.et_username);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        EditText etDob = dialogView.findViewById(R.id.et_dob);
        RadioGroup rgGender = dialogView.findViewById(R.id.rg_gender);
        RadioButton rbMale = dialogView.findViewById(R.id.rb_male);
        RadioButton rbFemale = dialogView.findViewById(R.id.rb_female);
        RadioButton rbOther = dialogView.findViewById(R.id.rb_other);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        try (Cursor cursor = databaseHelper.getUser(userId)) {
            if (cursor != null && cursor.moveToFirst()) {
                etUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow("username")));
                etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                etDob.setText(cursor.getString(cursor.getColumnIndexOrThrow("DOB")));
                String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));

                if (gender.equalsIgnoreCase("Male")) {
                    rbMale.setChecked(true);
                } else if (gender.equalsIgnoreCase("Female")) {
                    rbFemale.setChecked(true);
                } else {
                    rbOther.setChecked(true);
                }
            }
        }

        // Set up Date Picker Dialog
        etDob.setOnClickListener(view -> {
            new DatePickerDialog(requireContext(), (datePicker, year, month, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                etDob.setText(sdf.format(myCalendar.getTime()));
            }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCancel.setOnClickListener(cancelView -> dialog.dismiss());

        btnSave.setOnClickListener(saveView -> {
            String updatedUsername = etUsername.getText().toString().trim();
            String updatedEmail = etEmail.getText().toString().trim();
            String updatedDob = etDob.getText().toString().trim();
            String updatedGender = "";

            int selectedGenderId = rgGender.getCheckedRadioButtonId();
            if (selectedGenderId == R.id.rb_male) {
                updatedGender = "Male";
            } else if (selectedGenderId == R.id.rb_female) {
                updatedGender = "Female";
            } else if (selectedGenderId == R.id.rb_other) {
                updatedGender = "Other";
            }

            if (updatedUsername.isEmpty() || updatedDob.isEmpty() || updatedGender.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean result = databaseHelper.updateUser(userId, updatedUsername, updatedDob, updatedEmail, updatedGender);
            if (result) {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                fetchUserDataFromSQLite();
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
