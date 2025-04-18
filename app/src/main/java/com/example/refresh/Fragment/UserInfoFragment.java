package com.example.refresh.Fragment;

import static com.example.refresh.Fragment.UserInfoFragment.States.ACCOUNT_DETAILS;
import static com.example.refresh.Fragment.UserInfoFragment.States.ADJUST_GOAL;
import static com.example.refresh.Fragment.UserInfoFragment.States.ALL;
import static com.example.refresh.Fragment.UserInfoFragment.States.FIRST_LOG;
import static com.example.refresh.Fragment.UserInfoFragment.States.LIFESTYLE_GOAL;
import static com.example.refresh.Fragment.UserInfoFragment.States.MULTIPLE;
import static com.example.refresh.Fragment.UserInfoFragment.States.PERSONAL_INFO;

import android.app.DatePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.refresh.Database.UsersTable;
import com.example.refresh.Helper.DatabaseHelper;
import com.example.refresh.Helper.UserInfoHelper;
import com.example.refresh.Model.User;
import com.example.refresh.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class UserInfoFragment extends Fragment {

    public interface OnUserDetailsFragmentListener {
        void onExitUserDetailsFragment();
    }

    public enum States {
        ALL("all"),
        FIRST_LOG("first_log"),
        PERSONAL_INFO("personal_info"),
        ACCOUNT_DETAILS("account_details"),
        LIFESTYLE_GOAL("life_style_goal"),
        ADJUST_GOAL("adjust_goal"),
        MULTIPLE("multiple");

        private final String stateName;

        States(String stateName) {
            this.stateName = stateName;
        }

        public String getStateName() {
            return stateName;
        }
    }

    private String state;
    private int userId;
    private User user;
    private OnUserDetailsFragmentListener fragmentListener;
    private ArrayList<String> displayedSections;
    private UserInfoHelper userInfoHelper;
    private DatabaseHelper dbHelper;

    private LinearLayout personalInfoLayout;
    private LinearLayout accountDetailsLayout;
    private LinearLayout lifestyleGoalLayout;
    private LinearLayout adjustGoalLayout;
    private LinearLayout nameLayout;
    private EditText nameET;
    private LinearLayout weightLayout;
    private EditText weightET;
    private LinearLayout heightLayout;
    private EditText heightET;
    private LinearLayout dateOfBirthLayout;
    private EditText dateOfBirthET;
    private LinearLayout genderLayout;
    private Spinner genderSpinner;
    private TextView genderErrorTV;
    private LinearLayout emailLayout;
    private EditText emailET;
    private LinearLayout phoneLayout;
    private EditText phoneET;
    private LinearLayout pwdLayout;
    private EditText pwdET;
    private LinearLayout goalLayout;
    private RadioGroup goalRadioGroup;
    private RadioButton loseWeightRB;
    private RadioButton maintainWeightRB;
    private RadioButton gainWeightRB;
    private RadioButton gainMuscleRB;
    private TextView goalErrorTV;
    private LinearLayout targetWeightLayout;
    private EditText targetWeightET;
    private LinearLayout activityLevelLayout;
    private Spinner activityLevelSpinner;
    private TextView activityLevelErrorTV;
    private LinearLayout dietTypeLayout;
    private RadioGroup dietTypeRadioGroup;
    private RadioButton carnivoreRB;
    private RadioButton vegetarianRB;
    private RadioButton veganRB;
    private TextView dietTypeErrorTV;
    private LinearLayout adjustCaloriesLayout;
    private EditText adjustCaloriesET;
    private LinearLayout adjustCarbsLayout;
    private EditText adjustCarbsET;
    private LinearLayout adjustProteinLayout;
    private EditText adjustProteinET;
    private LinearLayout adjustFatLayout;
    private EditText adjustFatET;
    private LinearLayout adjustWaterIntakeLayout;
    private EditText adjustWaterIntakeET;

    private TextView title;
    private ImageButton saveButton;
    private ImageButton backArrow;

    private final ArrayList<String> genderOptions = new ArrayList<>(Arrays.asList("Select Gender", "Male", "Female", "Other"));
    private final ArrayList<String> activityLevelOptions = new ArrayList<>(Arrays.asList("Select Level", "Low", "Medium", "High", "Very High"));


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Make sure the hosting activity implements the interface.
        if (context instanceof UserInfoFragment.OnUserDetailsFragmentListener) {
            fragmentListener = (UserInfoFragment.OnUserDetailsFragmentListener) context;
        } else {
            throw new RuntimeException(context
                    + " must implement OnUserDetailsFragmentListener");
        }
    }

    public UserInfoFragment() {
        // Required empty public constructor
    }
    public static UserInfoFragment newInstance(String state, int userId) {
        UserInfoFragment fragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putString("state", state);
        args.putInt("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            state = getArguments().getString("state");
            userId = getArguments().getInt("userId");
        }
        else {
            state = ALL.getStateName();
            userId = -1;
        }

        dbHelper = new DatabaseHelper(requireContext());
        if (userId != -1) user = dbHelper.getRecord(
                DatabaseHelper.Tables.USERS,
                UsersTable.Columns.ID,
                new String[]{userId + ""}
        );
        else user = new User();
        dbHelper.close();

        userInfoHelper = new UserInfoHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_info, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);

        configureFragmentState();

        assignValuesToViews();

        setListeners();
    }

    private void initializeViews(View view) {
        personalInfoLayout = view.findViewById(R.id.personal_info_layout);
        accountDetailsLayout = view.findViewById(R.id.account_details_layout);
        lifestyleGoalLayout = view.findViewById(R.id.lifestyle_goal_layout);
        adjustGoalLayout = view.findViewById(R.id.goal_adjustment_layout);

        nameLayout = view.findViewById(R.id.name_layout);
        nameET = view.findViewById(R.id.name_et);

        weightLayout = view.findViewById(R.id.weight_layout);
        weightET = view.findViewById(R.id.weight_et);

        heightLayout = view.findViewById(R.id.height_layout);
        heightET = view.findViewById(R.id.height_et);

        dateOfBirthLayout = view.findViewById(R.id.date_of_birth_layout);
        dateOfBirthET = view.findViewById(R.id.date_of_birth_et);

        genderLayout = view.findViewById(R.id.gender_layout);
        genderSpinner = view.findViewById(R.id.gender_spinner);
        genderErrorTV = view.findViewById(R.id.gender_error_tv);

        emailLayout = view.findViewById(R.id.email_layout);
        emailET = view.findViewById(R.id.email_et);

        phoneLayout = view.findViewById(R.id.phone_layout);
        phoneET = view.findViewById(R.id.phone_et);

        pwdLayout = view.findViewById(R.id.pwd_layout);
        pwdET = view.findViewById(R.id.pwd_et);

        goalLayout = view.findViewById(R.id.goal_layout);
        goalRadioGroup = view.findViewById(R.id.goal_rg);
        loseWeightRB = view.findViewById(R.id.lose_rBtn);
        maintainWeightRB = view.findViewById(R.id.maintain_rBtn);
        gainWeightRB = view.findViewById(R.id.gain_rBtn);
        gainMuscleRB = view.findViewById(R.id.gain_muscle_rBtn);
        goalErrorTV = view.findViewById(R.id.goal_error_tv);

        targetWeightLayout = view.findViewById(R.id.target_weight_layout);
        targetWeightET = view.findViewById(R.id.target_weight_et);

        activityLevelLayout = view.findViewById(R.id.activity_level_layout);
        activityLevelSpinner = view.findViewById(R.id.activity_level_spinner);
        activityLevelErrorTV = view.findViewById(R.id.activity_level_error_tv);

        dietTypeLayout = view.findViewById(R.id.diet_type_layout);
        dietTypeRadioGroup = view.findViewById(R.id.diet_type_rg);
        carnivoreRB = view.findViewById(R.id.carnivore_rBtn);
        vegetarianRB = view.findViewById(R.id.vegetarian_rBtn);
        veganRB = view.findViewById(R.id.vegan_rBtn);
        dietTypeErrorTV = view.findViewById(R.id.diet_type_error_tv);

        adjustCaloriesLayout = view.findViewById(R.id.adjust_calories_layout);
        adjustCaloriesET = view.findViewById(R.id.adjust_calories_et);

        adjustCarbsLayout = view.findViewById(R.id.adjust_carbs_layout);
        adjustCarbsET = view.findViewById(R.id.adjust_carbs_et);

        adjustProteinLayout = view.findViewById(R.id.adjust_protein_layout);
        adjustProteinET = view.findViewById(R.id.adjust_protein_et);

        adjustFatLayout = view.findViewById(R.id.adjust_fat_layout);
        adjustFatET = view.findViewById(R.id.adjust_fat_et);

        adjustWaterIntakeLayout = view.findViewById(R.id.adjust_water_intake_layout);
        adjustWaterIntakeET = view.findViewById(R.id.adjust_water_intake_et);

        backArrow = view.findViewById(R.id.backArrow);
        saveButton = view.findViewById(R.id.extra_button);
        saveButton.setImageResource(R.drawable.ic_save);
        title = view.findViewById(R.id.toolbarTitle);
    }

    private void configureFragmentState() {
        if (state.equals(MULTIPLE.getStateName())) {
            if (getArguments() != null) displayedSections = getArguments().getStringArrayList("displayedSections");
            else displayedSections = new ArrayList<>();
        }

        if (!state.equals(ALL.getStateName())) {
            if (state.equals(FIRST_LOG.getStateName())) {
                accountDetailsLayout.setVisibility(View.GONE);
                adjustGoalLayout.setVisibility(View.GONE);
            }
            else if (state.equals(PERSONAL_INFO.getStateName())) {
                accountDetailsLayout.setVisibility(View.GONE);
                lifestyleGoalLayout.setVisibility(View.GONE);
                adjustGoalLayout.setVisibility(View.GONE);
            }
            else if (state.equals(ACCOUNT_DETAILS.getStateName())) {
                personalInfoLayout.setVisibility(View.GONE);
                lifestyleGoalLayout.setVisibility(View.GONE);
                adjustGoalLayout.setVisibility(View.GONE);
            }
            else if (state.equals(LIFESTYLE_GOAL.getStateName())) {
                personalInfoLayout.setVisibility(View.GONE);
                accountDetailsLayout.setVisibility(View.GONE);
                adjustGoalLayout.setVisibility(View.GONE);
            }
            else if (state.equals(ADJUST_GOAL.getStateName())) {
                personalInfoLayout.setVisibility(View.GONE);
                accountDetailsLayout.setVisibility(View.GONE);
                lifestyleGoalLayout.setVisibility(View.GONE);
            }
            else if (state.equals(MULTIPLE.getStateName())) {
                personalInfoLayout.setVisibility(View.GONE);
                accountDetailsLayout.setVisibility(View.GONE);
                lifestyleGoalLayout.setVisibility(View.GONE);
                adjustGoalLayout.setVisibility(View.GONE);

                for (String section : displayedSections) {
                    switch (section) {
                        case "personal_info":
                            personalInfoLayout.setVisibility(View.VISIBLE);
                            break;
                        case "account_details":
                            accountDetailsLayout.setVisibility(View.VISIBLE);
                            break;
                        case "lifestyle_goal":
                            lifestyleGoalLayout.setVisibility(View.VISIBLE);
                            break;
                        case "adjust_goal":
                            adjustGoalLayout.setVisibility(View.VISIBLE);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + section);
                    }
                }
            }
            else throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    private void assignValuesToViews() {
        if (state.equals(FIRST_LOG.getStateName()))
            backArrow.setVisibility(View.GONE);

        title.setText(R.string.user_info);
        nameET.setText(user != null ? user.getName() : "");
        nameET.setEnabled(false);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.custom_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(R.layout.custom_spinner_item);
        genderSpinner.setAdapter(genderAdapter);
        genderSpinner.setSelection(0); // Set the default selection to the first item
        activityLevelSpinner.setPopupBackgroundDrawable(null);

        ArrayAdapter<String> activityLevelAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.custom_spinner_item, activityLevelOptions);
        activityLevelAdapter.setDropDownViewResource(R.layout.custom_spinner_item);
        activityLevelSpinner.setAdapter(activityLevelAdapter);
        activityLevelSpinner.setSelection(0); // Set the default selection to the first item
        activityLevelSpinner.setPopupBackgroundDrawable(null);
    }

    private void setListeners() {
        // Button Listeners:

        backArrow.setOnClickListener(v -> {
            dbHelper.close();
            fragmentListener.onExitUserDetailsFragment();
        });

        saveButton.setOnClickListener(v -> {
            onSave();
        });

        // Spinner Listeners:
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int optionPosition = genderSpinner.getSelectedItemPosition();
                genderSpinner.setSelection(optionPosition);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        activityLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int optionPosition = activityLevelSpinner.getSelectedItemPosition();
                activityLevelSpinner.setSelection(optionPosition);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        dateOfBirthET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the current date for default values
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH); // Note: Month is 0-indexed
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Create and show the DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                        (dp, y, m, d) -> {
                            // Format the selected date to dd/MM/yyyy (m+1 to adjust 0-indexed month)
                            String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
                            dateOfBirthET.setText(formattedDate);
                        }, year, month, day);

                datePickerDialog.show();
            }
        });

        goalRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (loseWeightRB.isChecked()) {
                    targetWeightLayout.setVisibility(View.VISIBLE);
                }
                else if (maintainWeightRB.isChecked()) {
                    targetWeightET.setText("");
                    targetWeightLayout.setVisibility(View.GONE);
                }
                else if (gainWeightRB.isChecked()) {
                    targetWeightET.setText("");
                    targetWeightLayout.setVisibility(View.VISIBLE);
                }
                else if (gainMuscleRB.isChecked()) {
                    targetWeightLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void onSave() {
        if (saveByState()) {
            fragmentListener.onExitUserDetailsFragment();
            dbHelper.close();
        }
    }

    private boolean saveByState() {
        if (state.equals(ALL.getStateName())) return saveStateAll();
        else if (state.equals(FIRST_LOG.getStateName())) return saveStateFirstLog();
        else if (state.equals(PERSONAL_INFO.getStateName())) return saveStatePersonalInfo();
        else if (state.equals(ACCOUNT_DETAILS.getStateName())) return saveStateAccountDetails();
        else if (state.equals(LIFESTYLE_GOAL.getStateName())) return saveStateLifestyleGoal();
        else if (state.equals(ADJUST_GOAL.getStateName())) return saveStateAdjustGoal();
        else if (state.equals(MULTIPLE.getStateName())) return saveStateMultiple();
        else throw new IllegalStateException("Unexpected value: " + state);
    }

    private boolean saveStateAll() {
        return saveStatePersonalInfo()
                && saveStateAccountDetails()
                && saveStateLifestyleGoal()
                && saveStateAdjustGoal();
    }

    private boolean saveStateFirstLog() {
        boolean flag = saveStatePersonalInfo() && saveStateLifestyleGoal();

        if (validatePersonalInfo() && userInfoHelper.getUserPreferences() != null) {
            userInfoHelper.getUserPreferences()
                    .edit()
                    .putInt("startWeight", Integer.parseInt(weightET.getText().toString()))
                    .apply();
        }
        else flag = false;

        return flag;
    }

    private boolean saveStatePersonalInfo() {
        if (validatePersonalInfo()) {
            user.setName(nameET.getText().toString().trim());
            dbHelper.editRecords(DatabaseHelper.Tables.USERS, user, UsersTable.Columns.ID, new String[]{user.getID() + ""});

            userInfoHelper.setWeight(Integer.parseInt(weightET.getText().toString()));
            userInfoHelper.setHeight(Integer.parseInt(weightET.getText().toString()));

            String dateOfBirthStr = (dateOfBirthET.getText() != null) ? dateOfBirthET.getText().toString().trim() : "";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr, formatter);
            userInfoHelper.setDateOfBirth(dateOfBirth);

            userInfoHelper.setGender(genderSpinner.getSelectedItem().toString());
            userInfoHelper.setActivityLevel(activityLevelSpinner.getSelectedItem().toString());

            return true;
        }

        return false;
    }

    private boolean saveStateAccountDetails() {
        if (validateAccountDetails()) {
            user.setPwd(pwdET.getText().toString());
            // user.setEmail(emailET.getText().toString());
            // user.setPhone(phoneET.getText().toString());
            dbHelper.editRecords(DatabaseHelper.Tables.USERS, user, UsersTable.Columns.ID, new String[]{user.getID() + ""});

            return true;
        }

        return false;
    }

    private boolean saveStateLifestyleGoal() {
        if (validateLifestyleGoal()) {

            String goalStr = "";
            if (loseWeightRB.isChecked()) goalStr = "lose";
            else if (maintainWeightRB.isChecked()) goalStr = "maintain";
            else if (gainWeightRB.isChecked()) goalStr = "gain";
            else if (gainMuscleRB.isChecked()) goalStr = "gain muscle";

            userInfoHelper.setGoal(goalStr);

            if (goalStr.equals("lose") || goalStr.equals("gain")) {
                userInfoHelper.setTargetWeight(Integer.parseInt(targetWeightET.getText().toString()));
            }

            userInfoHelper.setActivityLevel(activityLevelSpinner.getSelectedItem().toString());

            String dietTypeStr = "";
            if (carnivoreRB.isChecked()) dietTypeStr = "carnivore";
            else if (vegetarianRB.isChecked()) dietTypeStr = "vegetarian";
            else if (veganRB.isChecked()) dietTypeStr = "vegan";

            userInfoHelper.setDietType(dietTypeStr);

            return true;
        }

        return false;
    }

    private boolean saveStateAdjustGoal() {
        if (validateAdjustGoal()) {
            userInfoHelper.setCalorieGoal(Integer.parseInt(adjustCaloriesET.getText().toString()));
            userInfoHelper.setCarbsGoal(Integer.parseInt(adjustCarbsET.getText().toString()));
            userInfoHelper.setProteinGoal(Integer.parseInt(adjustProteinET.getText().toString()));
            userInfoHelper.setFatGoal(Integer.parseInt(adjustFatET.getText().toString()));
            userInfoHelper.setWaterIntakeGoal(Integer.parseInt(adjustWaterIntakeET.getText().toString()));

            return true;
        }

        return false;
    }

    private boolean saveStateMultiple() {

        boolean flag = true;

        for (String section : displayedSections) {
            switch (section) {
                case "personal_info":
                    flag = flag && saveStatePersonalInfo();
                    break;
                case "account_details":
                    flag = flag && saveStateAccountDetails();
                    break;
                case "lifestyle_goal":
                    flag = flag && saveStateLifestyleGoal();
                    break;
                case "adjust_goal":
                    flag = flag && saveStateAdjustGoal();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + section);
            }
        }

        return flag;
    }

    private boolean validatePersonalInfo() {
        boolean valid = true;

        // --- Validate Name ---
        String name = (nameET.getText() != null) ? nameET.getText().toString().trim() : "";
        if (name.isEmpty()) {
            nameET.setError("Name is required");
            valid = false;
        } else if (!name.contains(" ") || name.length() < 6) {
            nameET.setError("Invalid name (must include a space and be at least 6 characters)");
            valid = false;
        } else {
            nameET.setError(null);
        }

        // --- Validate Weight ---
        String weightStr = (weightET.getText() != null) ? weightET.getText().toString().trim() : "";
        if (weightStr.isEmpty()) {
            weightET.setError("Weight is required");
            valid = false;
        } else {
            try {
                int weight = Integer.parseInt(weightStr);
                if (weight < 40 || weight > 250) {
                    weightET.setError("Weight must be between 40-250 kg");
                    valid = false;
                } else {
                    weightET.setError(null);
                }
            } catch (NumberFormatException e) {
                weightET.setError("Invalid weight format");
                valid = false;
            }
        }

        // --- Validate Height ---
        String heightStr = (heightET.getText() != null) ? heightET.getText().toString().trim() : "";
        if (heightStr.isEmpty()) {
            heightET.setError("Height is required");
            valid = false;
        } else {
            try {
                int height = Integer.parseInt(heightStr);
                if (height < 100 || height > 220) {
                    heightET.setError("Height must be between 100-220 cm");
                    valid = false;
                } else {
                    heightET.setError(null);
                }
            } catch (NumberFormatException e) {
                heightET.setError("Invalid height format");
                valid = false;
            }
        }

        // --- Validate Date of Birth ---
        String dobStr = (dateOfBirthET.getText() != null) ? dateOfBirthET.getText().toString().trim() : "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (dobStr.isEmpty()) {
            dateOfBirthET.setError("Date of birth is required");
            valid = false;
        } else {
            try {
                LocalDate dob = LocalDate.parse(dobStr, formatter);
                if (dob.isAfter(LocalDate.now())) {
                    dateOfBirthET.setError("Date of birth must be in the past");
                    valid = false;
                }
                else if (LocalDate.now().getYear() - dob.getYear() < 14 || LocalDate.now().getYear() - dob.getYear() > 120) {
                    dateOfBirthET.setError("This app is for ages 14-120");
                    valid = false;
                }
                else {
                    dateOfBirthET.setError(null);
                }
            } catch (DateTimeParseException e) {
                dateOfBirthET.setError("Invalid date format (dd/MM/yyyy)");
                valid = false;
            }
        }


        // --- Validate Gender Spinner ---
        if (genderSpinner.getSelectedItemPosition() == 0) {
            genderErrorTV.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            genderErrorTV.setVisibility(View.GONE);
        }

        return valid;
    }

    private boolean validateAccountDetails() {
        boolean valid = true;

        // Get and trim the text for each field
        String email = (emailET.getText() != null) ? emailET.getText().toString().trim() : "";
        String phone = (phoneET.getText() != null) ? phoneET.getText().toString().trim() : "";
        String pwd = (pwdET.getText() != null) ? pwdET.getText().toString().trim() : "";

        // Validate Email
        if (email.isEmpty()) {
            emailET.setError("Email is required");
            valid = false;
        } else {
            emailET.setError(null);
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                 emailET.setError("Enter a valid email address");
                 valid = false;
            }
        }

        // Validate Phone
        if (phone.isEmpty()) {
            phoneET.setError("Phone is required");
            valid = false;
        } else {
            phoneET.setError(null);
            // Additional phone validation rules can be added here if needed.
        }

        // Validate Password
        if (pwd.isEmpty()) {
            pwdET.setError("Password is required");
            valid = false;
        } else {
            pwdET.setError(null);
            // Additional password validation (e.g., length or complexity) can be added here.
        }

        return valid;
    }


    private boolean validateLifestyleGoal() {
        boolean valid = true;

        // Validate Goal RadioGroup (check that a radio button is selected)
        if (goalRadioGroup.getCheckedRadioButtonId() == -1) {
            goalErrorTV.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            goalErrorTV.setVisibility(View.GONE);
        }

        // Validate Target Weight EditText If Needed
        if (loseWeightRB.isChecked() || gainWeightRB.isChecked()) {
            String targetWeightText = targetWeightET.getText() != null ? targetWeightET.getText().toString().trim() : "";
            if (targetWeightText.isEmpty()) {
                targetWeightET.setError("Target weight is required");
                valid = false;
            } else {
                targetWeightET.setError(null);
                try {
                    float targetWeight = Float.parseFloat(targetWeightText);
                    if (targetWeight < 40 || targetWeight > 250) {
                        targetWeightET.setError("Target weight must be between 40 and 300 kg");
                        valid = false;
                    }
                    else {
                        float weight = Float.parseFloat(weightET.getText().toString());
                        if (loseWeightRB.isChecked() && targetWeight > weight) {
                            targetWeightET.setError("Target weight must be less than current weight");
                            valid = false;
                        }
                        else if (gainWeightRB.isChecked() && targetWeight < weight) {
                            targetWeightET.setError("Target weight must be greater than current weight");
                            valid = false;
                        }
                    }
                } catch (NumberFormatException e) {
                    targetWeightET.setError("Enter a valid number");
                    valid = false;
                }
            }
        }

        // Validate Activity Level Spinner
        if (activityLevelSpinner.getSelectedItemPosition() == 0) { // assuming position 0 is the default "Select..." entry
            activityLevelErrorTV.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            activityLevelErrorTV.setVisibility(View.GONE);
        }

        // Validate Diet Type RadioGroup
        if (dietTypeRadioGroup.getCheckedRadioButtonId() == -1) {
            dietTypeErrorTV.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            dietTypeErrorTV.setVisibility(View.GONE);
        }

        return valid;
    }

    private boolean validateAdjustGoal() {
        boolean valid = true;

        // Validate Adjust Calories
        String caloriesStr = adjustCaloriesET.getText() != null ? adjustCaloriesET.getText().toString().trim() : "";
        if (caloriesStr.isEmpty()) {
            adjustCaloriesET.setError("Adjust calories is required");
            valid = false;
        } else {
            try {
                int calories = Integer.parseInt(caloriesStr);
                if (calories < 0 || calories > 10000) {
                    adjustCaloriesET.setError("Calories must be between 0 and 10,000");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                adjustCaloriesET.setError("Invalid number");
                valid = false;
            }
        }

        // Validate Adjust Carbs
        String carbsStr = adjustCarbsET.getText() != null ? adjustCarbsET.getText().toString().trim() : "";
        if (carbsStr.isEmpty()) {
            adjustCarbsET.setError("Adjust carbs is required");
            valid = false;
        } else {
            try {
                int carbs = Integer.parseInt(carbsStr);
                if (carbs < 0 || carbs > 1000) {
                    adjustCarbsET.setError("Carbs must be between 0 and 1000");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                adjustCarbsET.setError("Invalid number");
                valid = false;
            }
        }

        // Validate Adjust Protein
        String proteinStr = adjustProteinET.getText() != null ? adjustProteinET.getText().toString().trim() : "";
        if (proteinStr.isEmpty()) {
            adjustProteinET.setError("Adjust protein is required");
            valid = false;
        } else {
            try {
                int protein = Integer.parseInt(proteinStr);
                if (protein < 0 || protein > 1000) {
                    adjustProteinET.setError("Protein must be between 0 and 1000");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                adjustProteinET.setError("Invalid number");
                valid = false;
            }
        }

        // Validate Adjust Fat
        String fatStr = adjustFatET.getText() != null ? adjustFatET.getText().toString().trim() : "";
        if (fatStr.isEmpty()) {
            adjustFatET.setError("Adjust fat is required");
            valid = false;
        } else {
            try {
                int fat = Integer.parseInt(fatStr);
                if (fat < 0 || fat > 1000) {
                    adjustFatET.setError("Fat must be between 0 and 500");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                adjustFatET.setError("Invalid number");
                valid = false;
            }
        }

        // Validate Adjust Water Intake
        String waterStr = adjustWaterIntakeET.getText() != null ? adjustWaterIntakeET.getText().toString().trim() : "";
        if (waterStr.isEmpty()) {
            adjustWaterIntakeET.setError("Adjust water intake is required");
            valid = false;
        } else {
            try {
                int water = Integer.parseInt(waterStr);
                if (water < 0 || water > 10000) {
                    adjustWaterIntakeET.setError("Water intake must be between 0 and 5000");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                adjustWaterIntakeET.setError("Invalid number");
                valid = false;
            }
        }

        return valid;
    }

}