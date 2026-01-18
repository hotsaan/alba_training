package kr.ac.uc.albago.common;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import kr.ac.uc.albago.R;
import kr.ac.uc.albago.api.RegisterRequest;
import kr.ac.uc.albago.api.RegisterResponse;
import kr.ac.uc.albago.api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText editTextName, editTextEmail, editTextUserId, editTextPassword, editTextConfirmPassword;
    private EditText etBusinessName, etBusinessContact, etBusinessNumber, etBusinessLocation, etJobType;
    private Button buttonSignUp, btnJobSeeker, btnEmployer, buttonCheckDuplicate;
    private TextView textGoToLogin, tvTerms;
    private LinearLayout layoutBusinessFields, layoutRoleButtons, layoutAgreeTerms;
    private CheckBox cbAgreeTerms;
    private TextView labelName, labelEmail, labelPassword, labelConfirmPassword;


    // 현재 선택된 역할 (기본: 일반 사용자)
    private String selectedRole = "user";
    private boolean isUserIdCheckedAndAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        bindViews();

        // 초기 화면 상태 설정
        // - 아이디 입력만 보이도록
        // - 회원가입 버튼 비활성화
        showFullForm(false);
        editTextUserId.setEnabled(true);
        buttonCheckDuplicate.setEnabled(true);
        updateSignUpButtonState();


        setupListeners();
    }

    /**
     * XML 레이아웃에 있는 View들을 변수와 연결
     */
    private void bindViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUserId = findViewById(R.id.editTextUserId);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        etBusinessName = findViewById(R.id.etBusinessName);
        etBusinessContact = findViewById(R.id.etBusinessContact);
        etBusinessNumber = findViewById(R.id.etBusinessNumber);
        etBusinessLocation = findViewById(R.id.etBusinessLocation);
        etJobType = findViewById(R.id.etJobType);
        layoutBusinessFields = findViewById(R.id.layoutBusinessFields);
        layoutRoleButtons = findViewById(R.id.layoutRoleButtons);
        layoutAgreeTerms = findViewById(R.id.layoutAgreeTerms);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        btnJobSeeker = findViewById(R.id.btnJobSeeker);
        btnEmployer = findViewById(R.id.btnEmployer);
        cbAgreeTerms = findViewById(R.id.cbAgreeTerms);
        textGoToLogin = findViewById(R.id.textGoToLogin);
        tvTerms = findViewById(R.id.tvTerms);
        buttonCheckDuplicate = findViewById(R.id.buttonCheckDuplicate);

        // Bind labels to manage their visibility as well
        labelName = findViewById(R.id.labelName);
        labelEmail = findViewById(R.id.labelEmail);
        labelPassword = findViewById(R.id.labelPassword);
        labelConfirmPassword = findViewById(R.id.labelConfirmPassword);
    }

    /**
     *  버튼 클릭, 텍스트 변경 등 이벤트 리스너 설정
     */
    private void setupListeners() {
        btnJobSeeker.setOnClickListener(v -> {
            selectedRole = "user";
            layoutBusinessFields.setVisibility(View.GONE);
        });

        btnEmployer.setOnClickListener(v -> {
            selectedRole = "EMPLOYER";
            // Only show business fields if the main form is already visible
            if (isUserIdCheckedAndAvailable) {
                layoutBusinessFields.setVisibility(View.VISIBLE);
            }
        });

        textGoToLogin.setOnClickListener(v -> finish());

        buttonCheckDuplicate.setOnClickListener(v -> {
            String userIdToCheck = editTextUserId.getText().toString().trim();
            if (userIdToCheck.isEmpty()) {
                editTextUserId.setError("ID를 입력해주세요.");
                return;
            }
            if (!userIdToCheck.matches("^[a-zA-Z][a-zA-Z0-9]{3,19}$")) {
                editTextUserId.setError("ID는 문자로 시작하고 4~20자 이내여야 합니다.");
                return;
            }
            performPartialRegisterCheck(userIdToCheck);
        });

        editTextUserId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUserIdCheckedAndAvailable) {
                    resetValidationState();
                }
                editTextUserId.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        cbAgreeTerms.setOnCheckedChangeListener((buttonView, isChecked) -> updateSignUpButtonState());

        buttonSignUp.setOnClickListener(v -> handleFinalRegistration());
    }

    /**
     * 아이디 중복 체크 성공 시
     * 나머지 회원가입 입력 폼을 보여줌
     */
    private void showFullForm(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;

        // Show/hide input fields
        editTextName.setVisibility(visibility);
        editTextEmail.setVisibility(visibility);
        editTextPassword.setVisibility(visibility);
        editTextConfirmPassword.setVisibility(visibility);

        // Show/hide labels
        labelName.setVisibility(visibility);
        labelEmail.setVisibility(visibility);
        labelPassword.setVisibility(visibility);
        labelConfirmPassword.setVisibility(visibility);

        // Show/hide layouts and buttons
        layoutRoleButtons.setVisibility(visibility);
        layoutAgreeTerms.setVisibility(visibility);
        buttonSignUp.setVisibility(visibility);

        // Special handling for business fields based on role
        if ("EMPLOYER".equals(selectedRole) && show) {
            layoutBusinessFields.setVisibility(View.VISIBLE);
        } else {
            layoutBusinessFields.setVisibility(View.GONE);
        }
    }

    /**
     * 아이디 변경 시 또는 실패 시
     * 회원가입 상태 전체 초기화
     */
    private void resetValidationState() {
        isUserIdCheckedAndAvailable = false;
        showFullForm(false); // Hide the main form

        // Reset the ID field and duplicate check button
        editTextUserId.setEnabled(true);
        editTextUserId.setText("");
        editTextUserId.setError(null);
        buttonCheckDuplicate.setEnabled(true);
        buttonCheckDuplicate.setAlpha(1f);

        // Clear all other input fields
        editTextName.setText("");
        editTextEmail.setText("");
        editTextPassword.setText("");
        editTextConfirmPassword.setText("");
        cbAgreeTerms.setChecked(false);
        etBusinessName.setText("");
        etBusinessContact.setText("");
        etBusinessNumber.setText("");
        etBusinessLocation.setText("");
        etJobType.setText("");

        updateSignUpButtonState(); // Disable the sign up button
    }

    /**
     * 회원가입 버튼 활성화 조건 제어
     */
    private void updateSignUpButtonState() {
        boolean allConditionsMet = isUserIdCheckedAndAvailable && cbAgreeTerms.isChecked();
        buttonSignUp.setEnabled(allConditionsMet);
        buttonSignUp.setAlpha(allConditionsMet ? 1f : 0.5f);
    }

    /**
     * 아이디 중복 체크 (부분 회원가입 요청)
     */
    private void performPartialRegisterCheck(String username) {

        buttonCheckDuplicate.setEnabled(false);
        buttonCheckDuplicate.setAlpha(0.5f);

        RegisterRequest partialReq = RegisterRequest.partial(username);

        RetrofitClient.getInstance(selectedRole).getApi()
                .register(partialReq)        // ⭐ register 하나만 사용
                .enqueue(new Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(RegisterActivity.this,
                                    "서버 응답 오류: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                            resetValidationState();
                            return;
                        }

                        RegisterResponse body = response.body();

                        if (body.isAvailable()) {   // 또는 body.success 구조에 맞게
                            Toast.makeText(RegisterActivity.this,
                                    "사용 가능한 아이디입니다.",
                                    Toast.LENGTH_SHORT).show();

                            isUserIdCheckedAndAvailable = true;
                            editTextUserId.setEnabled(false);
                            showFullForm(true);
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "이미 사용 중인 아이디입니다.",
                                    Toast.LENGTH_LONG).show();
                            resetValidationState();
                        }

                        updateSignUpButtonState();
                    }

                    @Override
                    public void onFailure(Call<RegisterResponse> call, Throwable t) {
                        Toast.makeText(RegisterActivity.this,
                                "서버 연결 실패: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        resetValidationState();
                    }
                });
    }
    /**
     * 입력값 검증 후 실제 회원가입 요청
     */
    private void handleFinalRegistration() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUserId.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("이름을 입력해주세요.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("이메일 형식이 올바르지 않습니다.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("비밀번호를 입력해주세요.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("비밀번호가 일치하지 않습니다.");
            return;
        }
        if (!cbAgreeTerms.isChecked()) {
            Toast.makeText(this, "이용약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest req =
                RegisterRequest.full(email, password, username, selectedRole);


    }

    /**
     * 최종 회원가입 API 호출
     */
    private void sendFinalRegisterRequest(RegisterRequest req) {
        buttonSignUp.setEnabled(false); // Prevent double-clicking

        RetrofitClient.getInstance(selectedRole).getApi()
                .register(req)
                .enqueue(new Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                        buttonSignUp.setEnabled(true); // Re-enable button

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(RegisterActivity.this, "서버 오류: " + response.code(), Toast.LENGTH_LONG).show();
                            // Don't reset state here, allow user to correct minor errors
                            return;
                        }

                        RegisterResponse body = response.body();
                        if (body.success) {
                            Toast.makeText(RegisterActivity.this, "회원가입 완료 되었습니다", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity and return to login
                        } else {
                            Map<String, String> errs = body.errors;
                            if (errs != null) {
                                if (errs.containsKey("email")) editTextEmail.setError(errs.get("email"));
                                if (errs.containsKey("password")) editTextPassword.setError(errs.get("password"));
                                if (errs.containsKey("username")) editTextName.setError(errs.get("username"));
                                // If the ID was somehow taken between the check and final registration
                                if (errs.containsKey("userId")) {
                                    Toast.makeText(RegisterActivity.this, errs.get("userId"), Toast.LENGTH_LONG).show();
                                    resetValidationState(); // Force user to start over
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "회원가입 실패: 알 수 없는 오류", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterResponse> call, Throwable t) {
                        buttonSignUp.setEnabled(true); // Re-enable button
                        Toast.makeText(RegisterActivity.this, "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}