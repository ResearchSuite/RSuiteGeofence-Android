package edu.cornell.tech.foundry.geofence;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;

import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.task.OrderedTask;
import org.researchstack.backbone.ui.PinCodeActivity;
import org.researchstack.backbone.ui.ViewTaskActivity;

import java.util.Date;

import edu.cornell.tech.foundry.ohmageomhsdkrs.CTFLogInStep;
import edu.cornell.tech.foundry.ohmageomhsdkrs.CTFLogInStepLayout;
import edu.cornell.tech.foundry.ohmageomhsdkrs.CTFOhmageLogInStepLayout;

public class OnboardingActivity extends PinCodeActivity {

    public static final int REQUEST_CODE_SIGN_IN  = 31473;

    public static final int REQUEST_CODE_PASSCODE = 41473;

    public static final String LOG_IN_STEP_IDENTIFIER = "login step identifier";
    public static final String LOG_IN_TASK_IDENTIFIER = "login task identifier";
    public static final String PASS_CODE_TASK_IDENTIFIER = "passcode task identifier";

    private Button loginButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_onboarding);

        this.loginButton = (Button) findViewById(R.id.loginButton);
        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginClicked(view);
            }
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }
    }




    @Override
    public void onDataAuth()
    {
        if(StorageAccess.getInstance().hasPinCode(this))
        {
            super.onDataAuth();
        }
        else // allow onboarding if no pincode
        {
            onDataReady();
        }
    }


    //pass code MUST be set when we launch this
    private void showLogInStep() {
        CTFLogInStep logInStep = new CTFLogInStep(
                OnboardingActivity.LOG_IN_STEP_IDENTIFIER,
                "Sign In",
                "Please enter your Ohmage-OMH credentials to sign in.",
                CTFOhmageLogInStepLayout.class
        );

        OrderedTask task = new OrderedTask(OnboardingActivity.LOG_IN_TASK_IDENTIFIER, logInStep);
        startActivityForResult(ViewTaskActivity.newIntent(this, task),
                REQUEST_CODE_SIGN_IN);
    }

    public void loginClicked(View view)
    {

        showLogInStep();

    }

    private void skipToMainActivity()
    {
        startMainActivity();
    }

    private void startMainActivity()
    {

        Intent intent = new Intent(this, RSGeofenceSplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if(requestCode == REQUEST_CODE_SIGN_IN && resultCode == RESULT_OK) {
            TaskResult result = (TaskResult) data.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT);

            Boolean isLoggedIn = (Boolean) result.getStepResult(OnboardingActivity.LOG_IN_STEP_IDENTIFIER)
                    .getResultForIdentifier(CTFLogInStepLayout.LoggedInResultIdentifier);

            RSGeofenceFileAccess.getInstance().setSignedInDate(this, new Date());

            if (isLoggedIn) {
                skipToMainActivity();
            }

        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
