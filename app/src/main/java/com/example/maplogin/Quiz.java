package com.example.maplogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.maplogin.struct.LocationInfo;
import com.example.maplogin.struct.QuestionInfo;
import com.example.maplogin.utils.Constants;
import com.example.maplogin.utils.DatabaseAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Quiz extends AppCompatActivity {
    private int currentQuestionIndex = 0;
    private TextView tvQuestion, tvQuestionNumber;
    private Button btnNext;
    private RadioGroup radioGroup;
    private RadioButton radioButton1, radioButton2, radioButton3, radioButton4;
    private Map<String, QuestionInfo> mQuestionMap;
    private List<String> questions;
    private int correctQuestion = 0;
    private ImageView img;

    private static final long COUNTDOWN_IN_MILLIS = 15000;
    private CountDownTimer countDownTimer;
    private  long timeLeftInMillis;
    private TextView textViewCountDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Intent intent = getIntent();
        String id = intent.getStringExtra(Constants.LOCATION_ID);

        TextView tvTitle = findViewById(R.id.textView26);
        LocationInfo info = DatabaseAdapter.getInstance().getAllLocations().getOrDefault(id, null);
        mQuestionMap = DatabaseAdapter.getInstance().getAllQuestions();

        if (info != null) {
            tvTitle.setText(info.name);
            questions = new ArrayList<>(info.questions);

        } else {
            finish();
        }

        tvQuestion = findViewById(R.id.textView78);
        tvQuestionNumber = findViewById(R.id.textView18);
        btnNext = findViewById(R.id.btnNextQuestionLiteratureAndGeography);
        radioGroup = findViewById(R.id.radioGroup);

        radioButton1 = findViewById(R.id.radioButton1);
        radioButton2 = findViewById(R.id.radioButton2);
        radioButton3 = findViewById(R.id.radioButton3);
        radioButton4 = findViewById(R.id.radioButton4);

        img = findViewById((R.id.imageQuiz));
        textViewCountDown = findViewById(R.id.textViewCountDown);

        findViewById(R.id.btnNextQuestionLiteratureAndGeography)
            .setOnClickListener(view -> {
                countDownTimer.cancel();
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int userAnswer = radioGroup.indexOfChild(radioButton);

                Long correctAnswer = Objects.requireNonNull(
                        mQuestionMap.get(questions.get(currentQuestionIndex))).answer;

                boolean answer = (userAnswer == (correctAnswer - 1));

                if (answer) {
                    correctQuestion++;
                }

                currentQuestionIndex++;

                if (btnNext.getText().equals("Next")){
                    displayNextQuestions(getQuestionInfo(currentQuestionIndex));

                } else{
                    Intent intentResult = new Intent(Quiz.this,FinalResultActivity.class);
                    intentResult.putExtra(Constants.SUBJECT, info.name);
                    intentResult.putExtra(Constants.CORRECT,correctQuestion);
                    intentResult.putExtra(Constants.INCORRECT,Constants.QUESTION_SHOWING - correctQuestion);
                    intentResult.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intentResult);
                    finish();
                }
        });

        findViewById(R.id.imageViewStartQuizGeographyOrLiterature).setOnClickListener(
                view -> finish());

        displayData(getQuestionInfo(currentQuestionIndex));
    }

    private QuestionInfo getQuestionInfo(int idx) {
        return mQuestionMap.get(questions.get(idx));
    }

    private void displayNextQuestions(QuestionInfo info) {
        setAnswersToRadioButton(info);
        tvQuestion.setText(questions.get(currentQuestionIndex));
        tvQuestionNumber.setText("Current Question: " + (currentQuestionIndex + 1));

        if (currentQuestionIndex == questions.size() - 1){
            btnNext.setText("Finish");
        }
    }

    private void displayData(QuestionInfo info) {
        tvQuestion.setText(questions.get(currentQuestionIndex));
        tvQuestionNumber.setText("Current Question: " + (currentQuestionIndex + 1));
        setAnswersToRadioButton(info);
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long milliUntileEnd) {
                timeLeftInMillis = milliUntileEnd;
                updateCountDown();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDown();
                btnNext.callOnClick();
            }
        }.start();
    }

    private void updateCountDown(){
        int minutes = (int)(timeLeftInMillis /1000)/60;
        int seconds = (int)(timeLeftInMillis /1000)%60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewCountDown.setText(timeFormatted);
        if(timeLeftInMillis <= 5000){
            textViewCountDown.setTextColor(Color.RED);
        }
        else{
            textViewCountDown.setTextColor(Color.BLACK);
        }
    }

    private void setAnswersToRadioButton(QuestionInfo info){
        radioButton1.setText(info.choice1);
        radioButton2.setText(info.choice2);
        radioButton3.setText(info.choice3);
        radioButton4.setText(info.choice4);

        Picasso.get().load(info.imageUrl).fit()
                .placeholder(R.mipmap.ic_launcher_round)
                .into((ImageView) findViewById(R.id.imageQuiz));

        timeLeftInMillis = COUNTDOWN_IN_MILLIS;
        startCountDown();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

}