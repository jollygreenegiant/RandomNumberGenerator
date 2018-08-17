package com.jggdevelopment.randomnumbergenerator;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Random;

public class ChooseFromList extends Fragment {
    private ArrayList<String> listItems = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private Button addButton;
    private EditText enterListItem;
    private ListView listView;
    private Button selectButton;
    private TextView selectedItem;
    private AdView adView;
    private BillingProcessor bp;
    private static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3UetPcIHYa8jiQycvSANUNeefN51RKcgocZKCJ8387LAjqrEkEXkdTsjXujCkPZz8lZ9yP5wAuxrXagU45lDSQ8VdBn0OQSfVMhHdEe8xpMxktZBglGBhUbg3fvuZ4EeDPFMS9/+1SvzhP21J0gme5/iyj1kwNVnbPxDUYtK0j3JmuLcFOlgIefrFrlhgUHP1kDb/lzqnyxTSGApjzl0bUEIbi0fIh6OsbG03OIhjdXV6qJiCTDtHpUHY9jO/2Il6PdHzDB1g5mn1N9dPoRLAmVF1VUsEDSJ4W9/08KKQlAeoEEum0O7qnjyXWAbUfFukGavXHE8hPDJjDXvPhbJvQIDAQAB";
    private FirebaseAnalytics firebaseAnalytics;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View rootView = inflater.inflate(R.layout.choose_from_list, container, false);

        enterListItem = rootView.findViewById(R.id.enter_list_item);
        listView = rootView.findViewById(R.id.list_items);
        addButton = rootView.findViewById(R.id.add_button);
        selectButton = rootView.findViewById(R.id.select_button);
        selectedItem = rootView.findViewById(R.id.selected_item);
        firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStringToList();
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFromList();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItems.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        MobileAds.initialize(getContext(), "ca-app-pub-7613732164066601~3004699844");
        adView = rootView.findViewById(R.id.adView);

        bp = new BillingProcessor(getContext(), LICENSE_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
                Toast.makeText(getContext(), "Thanks for your support!", Toast.LENGTH_SHORT).show();
                adView.setVisibility(View.GONE);
            }

            @Override
            public void onPurchaseHistoryRestored() {
                if (!bp.listOwnedProducts().isEmpty()) {
                    adView.setVisibility(View.GONE);
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
            adView.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }



        return rootView;
    }

    public void addStringToList() {
        // get text from edittext, then use adapter to create view and set list adapter
        String textToAdd = enterListItem.getText().toString();

        if (textToAdd.equals("")){
            Toast.makeText(getActivity(), "Empty text", Toast.LENGTH_SHORT).show();
        } else {
            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listItems);
            listView.setAdapter(adapter);

            listItems.add(textToAdd);
            adapter.notifyDataSetChanged();
        }

        enterListItem.setText("");
    }

    public void selectFromList() {
        if (listItems.size() == 0) {
            return;
        }

        RandomStringTask random = new RandomStringTask();
        random.execute(listItems.toArray(new String[0]));

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Selected from list");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Selected from list");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @SuppressLint("StaticFieldLeak")
    public class RandomStringTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            selectButton.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String random = "";

            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Random rand = new Random();
                random = strings[rand.nextInt(strings.length)];

                publishProgress(random);
            }

            return random;
        }

        @Override
        protected void onPostExecute(String result) {
            selectButton.setEnabled(true);
            selectedItem.setText(result);
        }

        @Override
        protected void onProgressUpdate(String... progress){
            selectedItem.setText(progress[0]);
        }
    }
}
