package com.jggdevelopment.randomnumbergenerator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Random;

public class NumberPicker extends Fragment {

    private TextView number;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private Button randomButton;
    private AdView mAdView;
    private BillingProcessor bp;
    private static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3UetPcIHYa8jiQycvSANUNeefN51RKcgocZKCJ8387LAjqrEkEXkdTsjXujCkPZz8lZ9yP5wAuxrXagU45lDSQ8VdBn0OQSfVMhHdEe8xpMxktZBglGBhUbg3fvuZ4EeDPFMS9/+1SvzhP21J0gme5/iyj1kwNVnbPxDUYtK0j3JmuLcFOlgIefrFrlhgUHP1kDb/lzqnyxTSGApjzl0bUEIbi0fIh6OsbG03OIhjdXV6qJiCTDtHpUHY9jO/2Il6PdHzDB1g5mn1N9dPoRLAmVF1VUsEDSJ4W9/08KKQlAeoEEum0O7qnjyXWAbUfFukGavXHE8hPDJjDXvPhbJvQIDAQAB";
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.number_picker, container, false);

        Button removeAds = rootView.findViewById(R.id.remove_ads);
        randomButton = rootView.findViewById(R.id.randomButton);

        firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        removeAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAds(v);
            }
        });

        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    randomButton(v);
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Click to randomize");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Click to randomize");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                if (validNumbers()) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Shake to randomize");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Shake to randomize");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                    random();
                } else {
                    Toast.makeText(getContext(), "Invalid numbers, try again", Toast.LENGTH_LONG).show();
                }
            }
        });

        MobileAds.initialize(getContext(), "ca-app-pub-7613732164066601~3004699844");
        mAdView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        bp = new BillingProcessor(getContext(), LICENSE_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
                Toast.makeText(getContext(), "Thanks for your support!", Toast.LENGTH_SHORT).show();
                mAdView.setVisibility(View.GONE);
            }

            @Override
            public void onPurchaseHistoryRestored() {
                if (!bp.listOwnedProducts().isEmpty()) {
                    mAdView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onBillingError(int errorCode, @Nullable Throwable error) {
                Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBillingInitialized() {

            }
        });
        bp.loadOwnedPurchasesFromGoogle();
        if (!bp.listOwnedProducts().isEmpty()) {
            mAdView.setVisibility(View.GONE);
            removeAds.setVisibility(View.GONE);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    public void randomButton(View view) throws InterruptedException {
        if (validNumbers()) {
            random();
        } else {
            Toast.makeText(getContext(), "Invalid numbers, try again", Toast.LENGTH_LONG).show();
        }
    }

    public boolean validNumbers() {
        EditText lowest = getView().findViewById(R.id.low);
        EditText highest = getView().findViewById(R.id.high);
        int low;
        int high;
        try {
            low = Integer.parseInt(lowest.getText().toString());
            high = Integer.parseInt(highest.getText().toString());
            if (low >= high) {
                lowest.setHint("Low");
                lowest.setText("");
                highest.setHint("High");
                highest.setText("");

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "low number > high number");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "low number > high number");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                return false;
            }
        } catch (NumberFormatException e) {
            lowest.setHint("Low");
            lowest.setText("");
            highest.setHint("High");
            highest.setText("");

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "number format exception");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "number format exception");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            return false;
        }

        return true;
    }

    public void random() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Successful random button click");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Successful random button click");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        randomButton.setEnabled(false);
        number = getView().findViewById(R.id.number);

        EditText lowest = getView().findViewById(R.id.low);
        EditText highest = getView().findViewById(R.id.high);

        int low = Integer.parseInt(lowest.getText().toString());
        int high = Integer.parseInt(highest.getText().toString());

        RandomNumberTask r = new RandomNumberTask();
        r.execute(low, high);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    public void removeAds(View view) {
        bp.purchase(getActivity(), "com.jggdevelopment.randomnumbergenerator.removeads");
    }

    @SuppressLint("StaticFieldLeak")
    private class RandomNumberTask extends AsyncTask<Integer, Integer, Integer> {


        @Override
        protected Integer doInBackground(Integer... ints) {
            Integer random = 0;

            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Random rand = new Random();
                random = rand.nextInt((ints[1] - ints[0]) + 1) + ints[0];

                publishProgress(random);
            }

            return random;
        }

        @Override
        protected void onPostExecute(Integer result) {
            randomButton.setEnabled(true);
            number.setText(result.toString());
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            number.setText(progress[0].toString());
        }
    }
}
