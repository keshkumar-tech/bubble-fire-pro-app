package com.example.bubblefirepro;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity implements GameView.GameListener {

    private GameView gameView;
    private TextView tvScore, tvMoves;
    private ProgressBar levelProgressBar;
    private ImageView btnSwap;
    private ImageButton btnPause;
    private FrameLayout btnBomb, btnNuclear, btnFire, btnElectric;
    private TextView badgeBomb, badgeNuclear, badgeFire, badgeElectric;

    private ConstraintLayout startLayout, gameLayout;
    private Button btnPlay, btnQuitMain, btnBackToMain;

    private SharedPreferences prefs;
    private int currentLevelReached = 1;
    private boolean isSoundOn = true;
    private ToneGenerator toneGenerator;

    private int countBomb = 0, countNuclear = 0, countFire = 0, countElectric = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("BubbleFirePrefs", Context.MODE_PRIVATE);
        currentLevelReached = prefs.getInt("currentLevel", 1);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        rebindMainViews();
    }

    private void rebindMainViews() {
        startLayout = findViewById(R.id.startLayout);
        gameLayout = findViewById(R.id.gameLayout);
        btnPlay = findViewById(R.id.btnPlay);
        btnQuitMain = findViewById(R.id.btnQuitMain);
        gameView = findViewById(R.id.gameView);
        tvScore = findViewById(R.id.tvScore);
        tvMoves = findViewById(R.id.tvMoves);
        levelProgressBar = findViewById(R.id.levelProgressBar);
        btnSwap = findViewById(R.id.btnSwap);
        btnPause = findViewById(R.id.btnPause);
        btnBomb = findViewById(R.id.btnBomb);
        btnNuclear = findViewById(R.id.btnNuclear);
        btnFire = findViewById(R.id.btnFire);
        btnElectric = findViewById(R.id.btnElectric);

        badgeBomb = findViewById(R.id.badgeBomb);
        badgeNuclear = findViewById(R.id.badgeNuclear);
        badgeFire = findViewById(R.id.badgeFire);
        badgeElectric = findViewById(R.id.badgeElectric);

        btnPlay.setOnClickListener(v -> showLevelMap());
        btnQuitMain.setOnClickListener(v -> finish());
        btnPause.setOnClickListener(v -> showPauseDialog());
        btnSwap.setOnClickListener(v -> gameView.swapBubbles());

        btnBomb.setOnClickListener(v -> {
            if (countBomb > 0) {
                gameView.activatePowerUp("BOMB");
                countBomb--;
                updateBadges();
            }
        });
        btnFire.setOnClickListener(v -> {
            if (countFire > 0) {
                gameView.activatePowerUp("FIRE");
                countFire--;
                updateBadges();
            }
        });
        btnNuclear.setOnClickListener(v -> {
            if (countNuclear > 0) {
                gameView.activatePowerUp("NUCLEAR");
                countNuclear--;
                updateBadges();
            }
        });
        btnElectric.setOnClickListener(v -> {
            if (countElectric > 0) {
                gameView.activatePowerUp("ELECTRIC");
                countElectric--;
                updateBadges();
            }
        });

        gameView.setGameListener(this);
        updateBadges();
    }

    private void updateBadges() {
        runOnUiThread(() -> {
            setBadge(badgeBomb, countBomb, btnBomb);
            setBadge(badgeNuclear, countNuclear, btnNuclear);
            setBadge(badgeFire, countFire, btnFire);
            setBadge(badgeElectric, countElectric, btnElectric);
        });
    }

    private void setBadge(TextView badge, int count, FrameLayout btn) {
        if (count > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(count));
            btn.setAlpha(1.0f);
            btn.setEnabled(true);
        } else {
            badge.setVisibility(View.GONE);
            btn.setAlpha(0.3f);
            btn.setEnabled(false);
        }
    }

    private void showLevelMap() {
        setContentView(R.layout.level_map);
        playSound("CLICK");

        GridLayout grid = findViewById(R.id.levelGrid);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        btnBackToMain.setOnClickListener(v -> {
            setContentView(R.layout.activity_main);
            rebindMainViews();
            startLayout.setVisibility(View.VISIBLE);
            gameLayout.setVisibility(View.GONE);
        });

        for (int i = 1; i <= 50; i++) {
            Button btn = new Button(this);
            btn.setText(String.valueOf(i));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 180;
            params.height = 180;
            params.setMargins(15, 15, 15, 15);
            btn.setLayoutParams(params);

            final int levelNum = i;
            if (i <= currentLevelReached) {
                btn.setBackgroundResource(R.drawable.play_button_bg);
                btn.setOnClickListener(v -> {
                    setContentView(R.layout.activity_main);
                    rebindMainViews();
                    showLevelStartDialog(levelNum);
                });
            } else {
                btn.setBackgroundResource(R.drawable.round_button);
                btn.setAlpha(0.5f);
                btn.setEnabled(false);
            }
            grid.addView(btn);
        }
    }

    private void showPauseDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pause_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.findViewById(R.id.btnRestart).setOnClickListener(v -> {
            gameView.initLevel(currentLevelReached);
            dialog.dismiss();
            playSound("CLICK");
        });

        dialog.findViewById(R.id.btnQuitPause).setOnClickListener(v -> {
            dialog.dismiss();
            showLevelMap();
            playSound("WHOOSH");
        });

        dialog.findViewById(R.id.btnClosePause).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void playSound(String type) {
        if (!isSoundOn) return;
        switch (type) {
            case "FIRE": toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 80); break;
            case "HIT": toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 40); break;
            case "POP": toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 120); break;
            case "SWAP": toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 60); break;
            case "CLICK": toneGenerator.startTone(ToneGenerator.TONE_SUP_PIP, 50); break;
            case "CHIME": toneGenerator.startTone(ToneGenerator.TONE_SUP_CONGESTION, 200); break;
            case "WHOOSH": toneGenerator.startTone(ToneGenerator.TONE_SUP_RADIO_ACK, 150); break;
        }
    }

    private void showLevelStartDialog(int level) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.level_start_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView txtLevel = dialog.findViewById(R.id.txtLevelTitle);
        txtLevel.setText("Lv. " + level);

        dialog.findViewById(R.id.btnStartGame).setOnClickListener(v -> {
            dialog.dismiss();
            startLayout.setVisibility(View.GONE);
            gameLayout.setVisibility(View.VISIBLE);
            gameView.initLevel(level);
            playSound("CLICK");
        });
        dialog.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onPerformanceAchieved(String performance) {
        runOnUiThread(() -> {
            if (performance.equals("GREAT")) {
                countBomb++;
                countFire++;
            } else if (performance.equals("EXCELLENT")) {
                countNuclear++;
                countElectric++;
            }
            updateBadges();
        });
    }

    @Override
    public void onScoreChanged(int score) {
        runOnUiThread(() -> tvScore.setText(String.valueOf(score)));
    }

    @Override
    public void onLevelChanged(int level) {
        runOnUiThread(() -> {
            if (level > currentLevelReached) {
                currentLevelReached = level;
                prefs.edit().putInt("currentLevel", currentLevelReached).apply();
            }
        });
    }

    @Override
    public void onMovesChanged(int moves) {
        runOnUiThread(() -> tvMoves.setText(String.valueOf(moves)));
    }

    @Override
    public void onLevelWin(int level, int score) {
        runOnUiThread(() -> {
            playSound("CHIME");
            showCustomDialog("LEVEL UP!", "Level " + level + " Cleared!", "Next Level", true, level);
        });
    }

    @Override
    public void onGameOver() {
        runOnUiThread(() -> showCustomDialog("GAME OVER", "No moves left!", "Retry", false, currentLevelReached));
    }

    private void showCustomDialog(String title, String message, String btnText, boolean isWin, int level) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ((TextView)dialog.findViewById(R.id.dialogTitle)).setText(title);
        ((TextView)dialog.findViewById(R.id.dialogMessage)).setText(message);
        Button btn = dialog.findViewById(R.id.btnAction);
        btn.setText(btnText);
        btn.setOnClickListener(v -> {
            dialog.dismiss();
            if (isWin) gameView.initLevel(level + 1);
            else gameView.initLevel(level);
            playSound("CLICK");
        });
        dialog.findViewById(R.id.btnQuit).setOnClickListener(v -> {
            dialog.dismiss();
            showLevelMap();
        });
        dialog.show();
    }
}
