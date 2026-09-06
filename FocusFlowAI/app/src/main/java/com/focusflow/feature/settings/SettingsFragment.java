package com.focusflow.feature.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.focusflow.FocusFlowApplication;
import com.focusflow.R;
import com.focusflow.core.di.AppContainer;
import com.focusflow.databinding.FragmentSettingsBinding;
import com.focusflow.domain.model.UserPreferences;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private boolean ignoreConsentCallback;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppContainer container = ((FocusFlowApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, new SettingsViewModelFactory(container)).get(SettingsViewModel.class);

        binding.login.setOnClickListener(v -> viewModel.login(text(binding.email), text(binding.password)));
        binding.register.setOnClickListener(v -> viewModel.register(text(binding.email), text(binding.password)));
        binding.logout.setOnClickListener(v -> viewModel.logout());
        binding.syncNow.setOnClickListener(v -> viewModel.syncNow());
        binding.telemetryConsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!ignoreConsentCallback) {
                viewModel.setTelemetryConsent(isChecked);
            }
        });

        viewModel.preferences().observe(getViewLifecycleOwner(), this::bindPreferences);
        viewModel.signedIn().observe(getViewLifecycleOwner(), this::bindAuth);
        viewModel.thresholdPercent().observe(getViewLifecycleOwner(), value -> {
            if (value != null && binding != null) {
                binding.threshold.setText(getString(R.string.settings_threshold, value));
            }
        });
        viewModel.message().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.refreshAuthState();
    }

    private void bindPreferences(@Nullable UserPreferences preferences) {
        if (binding == null || preferences == null) {
            return;
        }
        ignoreConsentCallback = true;
        binding.telemetryConsent.setChecked(preferences.telemetryConsented());
        ignoreConsentCallback = false;
    }

    private void bindAuth(@Nullable Boolean signedIn) {
        if (binding == null) {
            return;
        }
        boolean in = Boolean.TRUE.equals(signedIn);
        binding.authStatus.setText(in ? R.string.settings_signed_in : R.string.settings_signed_out);
        binding.emailLayout.setVisibility(in ? View.GONE : View.VISIBLE);
        binding.passwordLayout.setVisibility(in ? View.GONE : View.VISIBLE);
        binding.authActions.setVisibility(in ? View.GONE : View.VISIBLE);
        binding.logout.setVisibility(in ? View.VISIBLE : View.GONE);
    }

    private static String text(com.google.android.material.textfield.TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
