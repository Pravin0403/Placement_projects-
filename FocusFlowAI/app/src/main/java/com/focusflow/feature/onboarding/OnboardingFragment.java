package com.focusflow.feature.onboarding;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.focusflow.FocusFlowApplication;
import com.focusflow.R;
import com.focusflow.core.di.AppContainer;
import com.focusflow.databinding.FragmentOnboardingBinding;

public class OnboardingFragment extends Fragment {

    private FragmentOnboardingBinding binding;
    private OnboardingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppContainer appContainer = ((FocusFlowApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, new OnboardingViewModelFactory(appContainer))
                .get(OnboardingViewModel.class);

        binding.nameInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.onDisplayNameChanged(s == null ? "" : s.toString());
            }
        });
        binding.privacyAck.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.onPrivacyAcknowledged(isChecked));
        binding.telemetryConsent.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.onTelemetryConsented(isChecked));
        binding.continueButton.setOnClickListener(v -> viewModel.completeOnboarding());

        viewModel.canContinue().observe(getViewLifecycleOwner(), enabled ->
                binding.continueButton.setEnabled(Boolean.TRUE.equals(enabled)));
        viewModel.completed().observe(getViewLifecycleOwner(), completed -> {
            if (Boolean.TRUE.equals(completed)) {
                NavHostFragment.findNavController(this).navigate(R.id.action_onboarding_to_dashboard);
            }
        });
        viewModel.errorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
