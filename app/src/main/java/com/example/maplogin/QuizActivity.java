package com.example.maplogin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.maplogin.utils.Constants;
import com.example.maplogin.utils.DatabaseAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class QuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_main);

        TextView tvUsername = findViewById(R.id.tvUsernameHome);
        CardView cvStartQuiz = findViewById(R.id.cvStartQuiz);
        CardView cvRule = findViewById(R.id.cvRule);
        String id = getIntent().getStringExtra(Constants.LOCATION_ID);
        DatabaseAdapter db = DatabaseAdapter.getInstance();
        String locationName = Objects.requireNonNull(db.getAllLocations().get(id)).name;

        tvUsername.setText(locationName);

        findViewById(R.id.imageViewBack).setOnClickListener(view -> finish());

        cvStartQuiz.setOnClickListener(view -> {
            Intent intent = new Intent(QuizActivity.this, Quiz.class);
            intent.putExtra(Constants.LOCATION_ID, id);
            startActivity(intent);
        });

        cvRule.setOnClickListener(view ->
            startActivity(new Intent(QuizActivity.this, RuleActivity.class)));
    }
}