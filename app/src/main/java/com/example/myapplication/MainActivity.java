package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Spinner listIdSpinner;
    private ListView listView;
    private ArrayAdapter<String> listIdAdapter;
    private HashMap<String, ArrayList<String>> listItemMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listIdSpinner = findViewById(R.id.listIdSpinner);
        listView = findViewById(R.id.listView);

        // Initialize listItemMap to store list items for each list ID
        listItemMap = new HashMap<>();
        ArrayList<String> nameArray = new ArrayList<>();
        listItemMap.put("All", nameArray);

        // Execute AsyncTask to fetch JSON data
        new FetchData().execute();
    }

    private void populateSpinner() {
        List<String> listIds = new ArrayList<>(listItemMap.keySet());
        Collections.sort(listIds);
        listIdAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listIds);
        listIdSpinner.setAdapter(listIdAdapter);

        listIdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedListId = listIdAdapter.getItem(position);
                displayListItems(selectedListId);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    private void displayListItems(String listId) {
        ArrayList<String> items = listItemMap.get(listId);
        if (items != null) {
            // Display list items in ListView
            ArrayAdapter<String> listItemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
            listView.setAdapter(listItemsAdapter);
        } else {
            Toast.makeText(MainActivity.this, "No items found for List ID " + listId, Toast.LENGTH_SHORT).show();
        }
    }

    public class FetchData extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            return HttpHandler.makeHttpRequest("https://fetch-hiring.s3.amazonaws.com/hiring.json");
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            if (jsonResponse != null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonResponse);
                    // Extract list IDs and corresponding items
                    extractListItems(jsonArray);
                    // Populate the spinner with list IDs
                    populateSpinner();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void extractListItems(JSONArray jsonArray) {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    int id = jsonArray.getJSONObject(i).getInt("id");
                    int listId = jsonArray.getJSONObject(i).getInt("listId");
                    String name = jsonArray.getJSONObject(i).getString("name");

                    // Store list items for each list ID in the HashMap
                    String listIdKey = String.valueOf(listId);
                    if (!listItemMap.containsKey(listIdKey)) {
                        listItemMap.put(listIdKey, new ArrayList<>());
                    }

                    // Add the item in sorted order based on id
                    ArrayList<String> items = listItemMap.get(listIdKey);
                    int index = 0;
                    while (true) {
                        assert items != null;
                        if (!(index < items.size() && id > Integer.parseInt(items.get(index).split(" : ")[0])))
                            break;
                        index++;
                    }
                    if (!name.equals("null") && !name.isEmpty()) {
                        items.add(index, id + " : " + name);
                    }
                    // Add to the "All" list
                    ArrayList<String> allItems = listItemMap.get("All");
                    if (!name.equals("null") && !name.isEmpty()) {
                        assert allItems != null;
                        allItems.add(name);
                    }
                }
                // Sort the "All" list
                Collections.sort(Objects.requireNonNull(listItemMap.get("All")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
