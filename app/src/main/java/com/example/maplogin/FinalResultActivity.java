package com.example.maplogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.example.maplogin.utils.Constants;
import com.example.maplogin.utils.DatabaseAdapter;

public class FinalResultActivity extends AppCompatActivity {

    private TextView tvSubject, tvCorrect, tvIncorrect, tvPercentage, tvResult, tvWellDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_result);

        Intent intent = getIntent();
        String id = intent.getStringExtra(Constants.LOCATION_ID);
        int correctAnswer = intent.getIntExtra(Constants.CORRECT, 0);
        int incorrectAnswer = intent.getIntExtra(Constants.INCORRECT, 0);
        String locationName = intent.getStringExtra(Constants.SUBJECT);

        tvWellDone = findViewById(R.id.tvWellDone);
        tvSubject = findViewById(R.id.textView16);
        tvCorrect = findViewById(R.id.textView19);
        tvIncorrect = findViewById(R.id.textView27);
        tvPercentage = findViewById(R.id.textView28);
        tvResult = findViewById(R.id.textView29);

        findViewById(R.id.imageViewFinalResultQuiz).setOnClickListener(view -> {
            Intent intent1 = new Intent(this, MainActivity.class);
            startActivity(intent1);
            finish();
        });

        findViewById(R.id.btnFinishQuiz).setOnClickListener(view -> {
            Intent intent12 = new Intent(this, MainActivity.class);
            startActivity(intent12);
            finish();
        });

        displayData(id, correctAnswer, incorrectAnswer, locationName);
    }

    private void displayData(String id, int correctAnswer, int incorrectAnswer, String locationName) {
        int percentage = 100 * correctAnswer / (correctAnswer + incorrectAnswer);
        boolean isPassed = percentage >= 80;
        String result =  isPassed ? "Passed" : "Failed";
        String description = isPassed ? "Excellent. Now you can share the result to your friends.":
                "Discover the location and try again next time.";
        int color = isPassed ? Color.GREEN : Color.RED;

        if (isPassed) {
            DatabaseAdapter.getInstance().addCapturedLocation(id, (long) correctAnswer);
        } else {
            DatabaseAdapter.getInstance().addFailedLocation(id, (long) correctAnswer);
        }

        tvWellDone.setText(description);
        tvSubject.setText(locationName);
        tvCorrect.setText(String.valueOf(correctAnswer));
        tvIncorrect.setText(String.valueOf(incorrectAnswer));
        tvPercentage.setText(String.valueOf(percentage));

        tvResult.setText(result);
        tvResult.setTextColor(color);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}