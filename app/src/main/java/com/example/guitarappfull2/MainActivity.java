package com.example.guitarappfull2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private ToneGenerator toneGenerator;
    private Vibrator vibrator;

    private TextView tvStatus, tvNote;
    private View[] strings = new View[6];

    // Ноты открытых струн гитары (частоты в MIDI-тонах для ToneGenerator)
    private String[] noteNames = {"E2", "A2", "D3", "G3", "B3", "E4"};
    private int[] toneFreqs = {
            ToneGenerator.TONE_DTMF_1,
            ToneGenerator.TONE_DTMF_2,
            ToneGenerator.TONE_DTMF_3,
            ToneGenerator.TONE_DTMF_4,
            ToneGenerator.TONE_DTMF_5,
            ToneGenerator.TONE_DTMF_6
    };

    private int currentString = 0; // текущая струна
    private boolean wasNear = false; // был ли палец близко

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Находим элементы UI
        tvStatus = findViewById(R.id.tv_status);
        tvNote = findViewById(R.id.tv_note);
        strings[0] = findViewById(R.id.string1);
        strings[1] = findViewById(R.id.string2);
        strings[2] = findViewById(R.id.string3);
        strings[3] = findViewById(R.id.string4);
        strings[4] = findViewById(R.id.string5);
        strings[5] = findViewById(R.id.string6);

        // Инициализируем датчик
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Инициализируем звук и вибрацию
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Кнопки выбора струны — тап по струне меняет текущую
        for (int i = 0; i < strings.length; i++) {
            final int index = i;
            strings[i].setOnClickListener(v -> {
                currentString = index;
                tvNote.setText(noteNames[index]);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            boolean isNear = distance < proximitySensor.getMaximumRange();

            // Срабатываем только при приближении (не при удалении)
            if (isNear && !wasNear) {
                playString(currentString);
            }
            wasNear = isNear;
        }
    }

    private void playString(int index) {
        // Звук
        toneGenerator.startTone(toneFreqs[index], 300);

        // Вибрация
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        // Анимация — струна "дрожит"
        strings[index].setBackgroundColor(0xFFFFFFFF); // белая вспышка
        strings[index].postDelayed(() -> {
            // возвращаем цвет обратно
            int[] colors = {0xFF8B4513, 0xFFA0522D, 0xFFCD853F, 0xFFDAA520, 0xFFFFD700, 0xFFFFFFE0};
            strings[index].setBackgroundColor(colors[index]);
        }, 150);

        // Показываем название ноты
        tvNote.setText(noteNames[index]);
        tvStatus.setText("Струна задета!");
        tvStatus.postDelayed(() -> tvStatus.setText("Проведи рукой над датчиком"), 1000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}