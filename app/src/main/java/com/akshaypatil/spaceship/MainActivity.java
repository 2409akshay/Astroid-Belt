package com.akshaypatil.spaceship;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    float startX, endX = 0;
    final float minDelta = 150;
    float screenWidth;
    float _leftPosition = 0;
    float _rightPosition = 0;
    float _centralPosition = 0;
    long animDuration;
    float animTransY;
    long startTime;
    int counter;
    AtomicInteger score;
    int _hurdleMod = 0;
    AtomicBoolean stopLoop;
    AtomicInteger spaceShipX, spaceShipYTop, spaceShipYBottom;
    ConcurrentHashMap<String,ArrayList<Integer>> imgViewMap;

    Handler astroidHandler;
    Thread vibrationThread;
    final Runnable astroidRun = new Runnable() {

        @Override
        public void run() {
            final long millis = System.currentTimeMillis() - startTime;
            score.set(((int) (millis / 10)));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView scoreText = findViewById(R.id.scorecard);
                    scoreText.setText(String.valueOf(score.get()));
                }
            });

            if(counter % (_hurdleMod*10) == 0) {
                System.gc();
            }
            else if (counter % _hurdleMod == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GenerateHurdles();
                    }
                });
            }

            if (score.get() > 25000 ) {
                animDuration = 2500;
                _hurdleMod = 5;
            }
            else if (score.get() > 20000 ) {
                animDuration = 3000;
                _hurdleMod = 5;
            }
            else if(score.get() > 15000 ) {
                animDuration = 3500;
                _hurdleMod = 6;
            }
            else if(score.get() > 10000 ) {
                animDuration = 4000;
                _hurdleMod = 7;
            }
            else if(score.get() > 8000 ) {
                animDuration = 4500;
                _hurdleMod = 8;
            }
            else if(score.get() > 6000 ) {
                animDuration = 5000;
                _hurdleMod = 9;
            }
            else if(score.get() > 4000 ) {
                animDuration = 5500;
                _hurdleMod = 11;
            }
            else if(score.get() > 2000 ) {
                animDuration = 6000;
                _hurdleMod = 13;
            }
            counter++;

            if (stopLoop.get()) {
                astroidHandler.removeCallbacks(astroidRun);
            } else {
                astroidHandler.postDelayed(this, 100);
            }
        }

        private void GenerateHurdles() {
            if(spaceShipX.get() == 0) {
                ImageView spaceShipView = findViewById(R.id.spaceShip);
                int[] spaceShipViewPosition = new int[2];
                spaceShipView.getLocationOnScreen(spaceShipViewPosition);
                spaceShipX.set((int)spaceShipView.getX());
                spaceShipYTop.set(spaceShipViewPosition[1] + 20);
                spaceShipYBottom.set(spaceShipViewPosition[1] + spaceShipView.getMeasuredHeight() - 20);
            }
            double randomNum = Math.random();
            randomNum = randomNum * 10000;

            switch ((int) randomNum % 3) {
                case 0:
                    GenerateImageViews("LEFT");
                    break;

                case 1:
                    GenerateImageViews("CENTER");
                    break;

                case 2:
                    GenerateImageViews("RIGHT");
                    break;
            }
        }

        @SuppressLint("NewApi")
        private void GenerateImageViews(String position) {
            final RelativeLayout rLayout = findViewById(R.id.astroidNav);
            final ImageView imgView = new ImageView(rLayout.getContext());
            imgView.setId(View.generateViewId());
            imgView.setImageResource(PickAstroidImage());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(250, 250);
            final int _centerMarginOffset = 0;
            final int _leftMarginOffset = 0;
            final int _rightMarginOffset = 0;
            switch (position) {
                case "LEFT":
                    params.leftMargin = (int) _leftPosition - _leftMarginOffset;
                    break;

                case "CENTER":
                    params.leftMargin = (int) _centralPosition - _centerMarginOffset;
                    break;

                case "RIGHT":
                    params.leftMargin = (int) _rightPosition - _rightMarginOffset;

                    break;
            }
            imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            params.topMargin = -350;
            imgView.setLayoutParams(params);
            imgView.animate()
                    .translationY(animTransY)
                    .rotationBy(1080)
                    .setDuration(animDuration)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            imgView.setVisibility(View.GONE);
                            rLayout.removeView(imgView);
                            RelativeLayout.LayoutParams imgParams = (RelativeLayout.LayoutParams) imgView.getLayoutParams();
                            if (imgParams.leftMargin == ((int) _leftPosition) - _leftMarginOffset) {
                                //imgViewMap.get("LEFT").remove(imgView);
                                imgViewMap.get("LEFT").remove(0);
                            } else if (imgParams.leftMargin == ((int) _centralPosition) - _centerMarginOffset) {
                                //imgViewMap.get("CENTER").remove(imgView);
                                imgViewMap.get("CENTER").remove(0);
                            } else if (imgParams.leftMargin == ((int) _rightPosition) - _rightMarginOffset) {
                                //imgViewMap.get("RIGHT").remove(imgView);
                                imgViewMap.get("RIGHT").remove(0);
                            }
                        }
                    });
            rLayout.addView(imgView);
            Log.i("LayoutChildrenCount", "Active elements in Layout:" + String.valueOf(rLayout.getChildCount()));
            if (!imgViewMap.containsKey(position)) {
                //imgViewMap.put(position, new ArrayList<ImageView>());
                imgViewMap.put(position, new ArrayList<Integer>());
            }
            //imgViewMap.get(position).add(imgView);
            imgViewMap.get(position).add(imgView.getId());
        }


        private Integer PickAstroidImage()
        {
            double randomNum = Math.random();
            randomNum = randomNum*10000;
            int op = -1;

            switch ((int)randomNum%7)
            {
                case 0:
                    op = R.drawable.astroid;
                    break;

                case 1:
                    op = R.drawable.astroidred;
                    break;

                case 2:
                    op = R.drawable.astroidyellow;
                    break;

                case 3:
                    op = R.drawable.planetgray;
                    break;

                case 4:
                    op = R.drawable.planetyellow;
                    break;

                case 5:
                    op = R.drawable.astroidmgray;
                    break;

                case 6:
                    op = R.drawable.astroidredp;
                    break;
            }

            return op;
        }
    };
    Handler terminationHandler;
    final Runnable terminationRun = new Runnable() {
        @Override
        public void run() {
            ImageView spaceShipView = findViewById(R.id.spaceShip);
            String _tKey = "";
            if (Math.abs(spaceShipView.getX() - _leftPosition) < 1) {
                _tKey = "LEFT";
            }
            else if(Math.abs(spaceShipView.getX() - _centralPosition) < 1) {
                _tKey = "CENTER";
            }
            else if(Math.abs(spaceShipView.getX() - _rightPosition) < 1) {
                _tKey = "RIGHT";
            }

            if(spaceShipX.get() == 0) {
                int[] spaceShipViewPosition = new int[2];
                spaceShipView.getLocationOnScreen(spaceShipViewPosition);
                spaceShipX.set((int)spaceShipView.getX());
                spaceShipYTop.set(spaceShipViewPosition[1] + 20);
                spaceShipYBottom.set(spaceShipViewPosition[1] + spaceShipView.getMeasuredHeight() - 20);
            }
            if(imgViewMap.containsKey(_tKey)) {
                //ArrayList<ImageView> imgArrayList = new ArrayList<ImageView>(imgViewMap.get(_tKey));;
                for (int img:imgViewMap.get(_tKey)) {
                    if(isViewOverlapping(img)) {
                        stopLoop.set(true);
                    }
                }
            }

            if(stopLoop.get()) {
                terminationHandler.removeCallbacks(terminationRun);
                vibrationThread.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView spaceShipView = findViewById(R.id.spaceShip);
                        spaceShipView.setImageResource(R.drawable.blast);
                        spaceShipView.setScaleX(5);
                        spaceShipView.setScaleY(5);
                        spaceShipView.animate().alpha(0f).setDuration(1000).setListener(null).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                TextView scoreMsg = findViewById(R.id.scoreMsg);
                                TextView scoreCard = findViewById(R.id.scorecard);
                                scoreMsg.setText("Your score is " + scoreCard.getText());
                                scoreMsg.setVisibility(View.VISIBLE);
                                scoreMsg.animate().alpha(1f).setDuration(4000).withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        recreate();
                                    }
                                });
                            }
                        });
                    }
                });

            }
            else {
                terminationHandler.postDelayed(terminationRun, 10);
            }
        }

        private boolean isViewOverlapping(int astroidViewId) {
            int[] astroidPosition = new int[2];
            (findViewById(astroidViewId)).getLocationOnScreen(astroidPosition);
            //Log.i("Log","astroidY:" + String.valueOf(astroidPosition[1]) + "| astroidX:" + String.valueOf(astroidPosition[0]));
            return astroidPosition[1] >= (spaceShipYTop.get()) && astroidPosition[1] <= (spaceShipYBottom.get());
        }
    };
    final Runnable loggingRun = new Runnable() {
        @Override
        public void run() {

            Log.i("Log","spaceShipYTop:" + spaceShipYTop
                    + "| spaceShipYBottom:" + spaceShipYBottom
                    + "| spaceShipX:" + spaceShipX);
            if(imgViewMap.containsKey("LEFT")) {
                Log.i("Log", "LeftCount:" + imgViewMap.get("LEFT").size());
            }
            if(imgViewMap.containsKey("CENTER")) {
                Log.i("Log", "| CenterCount:" + imgViewMap.get("CENTER").size());
            }
            if(imgViewMap.containsKey("RIGHT")) {
                Log.i("Log", "| RightCount:" + imgViewMap.get("RIGHT").size());
            }

            if(stopLoop != null && stopLoop.get()) {
                loggingHandler.removeCallbacks(loggingRun);
            }
            else {
                loggingHandler.postDelayed(loggingRun, 100);
            }
        }
    };

    Handler loggingHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.welcome).setVisibility(View.INVISIBLE);
                InitiateNewGame();
            }
        });
    }

    @Override
    public void recreate() {
        super.recreate();
    }

    private void InitiateNewGame()
    {
        RelativeLayout astroidNav = findViewById(R.id.astroidNav);
        astroidNav.setVisibility(View.VISIBLE);
        score = new AtomicInteger();
        if(screenWidth == 0)
        {
            ImageView spaceShipView = findViewById(R.id.spaceShip);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float density  = getResources().getDisplayMetrics().density;
            screenWidth = metrics.widthPixels;
            _centralPosition = (screenWidth/2) - 125;
            _rightPosition = (5*screenWidth/6) - 125;
            _leftPosition = screenWidth/6 - 125;
//            _centralPosition = (screenWidth/2) - (spaceShipView.getMeasuredWidth()/2);
//            _rightPosition = (3*screenWidth/4) - (spaceShipView.getMeasuredWidth()/4);
//            _leftPosition = screenWidth/4 - (9*spaceShipView.getMeasuredWidth()/8);
            spaceShipView.setX(_centralPosition);
            spaceShipX = new AtomicInteger(0);
            spaceShipYBottom = new AtomicInteger(0);
            spaceShipYTop = new AtomicInteger(0);
            int[] spaceShipViewPosition = new int[2];
            spaceShipView.getLocationOnScreen(spaceShipViewPosition);
            spaceShipX.set((int)spaceShipView.getX());
            spaceShipYTop.set(spaceShipViewPosition[1] + 20);
            spaceShipYBottom.set(spaceShipViewPosition[1] + spaceShipView.getMeasuredHeight() - 20);
        }

        imgViewMap = new ConcurrentHashMap<String,ArrayList<Integer>>();
        startTime = System.currentTimeMillis();
        counter = 0;
        animTransY = 4000;
        animDuration = 7000;
        _hurdleMod = 15;

        astroidHandler = new Handler(Looper.getMainLooper());
        astroidHandler.postDelayed(astroidRun, 3000);
        //HandlerThread terminationHandlerThread = new HandlerThread("terminationHandlerThread");
        //terminationHandlerThread.start();
        //terminationHandler = new Handler(terminationHandlerThread.getLooper());
        terminationHandler = new Handler(Looper.getMainLooper());
        terminationHandler.postDelayed(terminationRun, 3000);

//        HandlerThread loggingThread = new HandlerThread("loggingThread");
//        loggingThread.start();
//        loggingHandler = new Handler(loggingThread.getLooper());
//        loggingHandler.postDelayed(loggingRun,0);

        vibrationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(30);
            }
        });
        stopLoop = new AtomicBoolean(false);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ImageView spaceShipView = findViewById(R.id.spaceShip);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                endX = event.getX();
                float deltaX = endX - startX;
                if(Math.abs(deltaX) > minDelta)
                {
                    if(endX > startX)
                    {
                        //rightSwipe
                        if(Math.abs(spaceShipView.getX() - _centralPosition) < 1) {
                            spaceShipView.setX(_rightPosition);
                        }
                        else if(Math.abs(spaceShipView.getX() - _leftPosition) < 1) {
                            spaceShipView.setX(_centralPosition);
                        }
                    }
                    else
                    {
                        //leftSwipe
                        if(Math.abs(spaceShipView.getX() - _rightPosition) < 1) {
                            spaceShipView.setX(_centralPosition);
                        }
                        else if(Math.abs(spaceShipView.getX() - _centralPosition) < 1) {
                            spaceShipView.setX(_leftPosition);
                        }
                    }
                    spaceShipX.set((int)spaceShipView.getX());
                }
                break;
        }
        if(spaceShipX == null) {
            spaceShipX = new AtomicInteger(0);
        }
        return super.onTouchEvent(event);
    }
}