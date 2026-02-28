package com.whispertflite;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import com.whispertflite.utils.ApiEndpointBuilder;
import com.whispertflite.utils.ThemeUtils;

public class ApiSettingsActivity extends AppCompatActivity {

    private SwitchCompat switchRemoteApi;
    private LinearLayout layoutApiSettings;
    private Spinner spinnerProvider;
    private EditText editApiKey;
    private EditText editEndpoint;
    private EditText editModel;
    private Button btnSave;
    private TextView tvStatus;
    private SharedPreferences sp;

    private static final String[] PROVIDER_NAMES = {"Groq", "OpenAI", "Custom"};
    private static final String[] PROVIDER_KEYS = {
            ApiEndpointBuilder.PROVIDER_GROQ,
            ApiEndpointBuilder.PROVIDER_OPENAI,
            ApiEndpointBuilder.PROVIDER_CUSTOM
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_settings);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        ThemeUtils.setStatusBarAppearance(this);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        switchRemoteApi = findViewById(R.id.switchRemoteApi);
        layoutApiSettings = findViewById(R.id.layoutApiSettings);
        spinnerProvider = findViewById(R.id.spinnerProvider);
        editApiKey = findViewById(R.id.editApiKey);
        editEndpoint = findViewById(R.id.editEndpoint);
        editModel = findViewById(R.id.editModel);
        btnSave = findViewById(R.id.btnSave);
        tvStatus = findViewById(R.id.tvSettingsStatus);

        // Set up provider spinner
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, PROVIDER_NAMES);
        providerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvider.setAdapter(providerAdapter);

        // Load saved settings
        loadSettings();

        // Toggle visibility of settings when switch changes
        switchRemoteApi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutApiSettings.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            // Save the toggle immediately
            sp.edit().putBoolean("useRemoteApi", isChecked).apply();
        });

        // Provider selection auto-fills endpoint and model
        spinnerProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String provider = PROVIDER_KEYS[position];
                if (!provider.equals(ApiEndpointBuilder.PROVIDER_CUSTOM)) {
                    editEndpoint.setText(ApiEndpointBuilder.getBaseUrl(provider));
                    editModel.setText(ApiEndpointBuilder.getDefaultModel(provider));
                    editEndpoint.setEnabled(false);
                    editModel.setEnabled(false);
                } else {
                    editEndpoint.setEnabled(true);
                    editModel.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Save button
        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        boolean useRemote = sp.getBoolean("useRemoteApi", false);
        switchRemoteApi.setChecked(useRemote);
        layoutApiSettings.setVisibility(useRemote ? View.VISIBLE : View.GONE);

        String provider = sp.getString("apiProvider", ApiEndpointBuilder.PROVIDER_GROQ);
        for (int i = 0; i < PROVIDER_KEYS.length; i++) {
            if (PROVIDER_KEYS[i].equals(provider)) {
                spinnerProvider.setSelection(i);
                break;
            }
        }

        editApiKey.setText(sp.getString("apiKey", ""));
        editEndpoint.setText(sp.getString("apiEndpoint", ApiEndpointBuilder.GROQ_BASE_URL));
        editModel.setText(sp.getString("apiModel", ApiEndpointBuilder.GROQ_DEFAULT_MODEL));
    }

    private void saveSettings() {
        String apiKey = editApiKey.getText().toString().trim();
        String endpoint = editEndpoint.getText().toString().trim();
        String model = editModel.getText().toString().trim();

        // Validate
        String error = ApiEndpointBuilder.validateSettings(apiKey, endpoint, model);
        if (error != null) {
            tvStatus.setText(error);
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }

        // Determine provider
        int providerIndex = spinnerProvider.getSelectedItemPosition();
        String provider = PROVIDER_KEYS[providerIndex];

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("useRemoteApi", switchRemoteApi.isChecked());
        editor.putString("apiProvider", provider);
        editor.putString("apiKey", apiKey);
        editor.putString("apiEndpoint", endpoint);
        editor.putString("apiModel", model);
        editor.apply();

        tvStatus.setText(getString(R.string.settings_saved));
        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }
}
