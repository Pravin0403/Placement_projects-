package com.focusflow.feature.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.focusflow.FocusFlowApplication;
import com.focusflow.R;
import com.focusflow.core.di.AppContainer;
import com.focusflow.core.util.TimeFormat;
import com.focusflow.databinding.FragmentDashboardBinding;
import com.focusflow.domain.model.DashboardState;
import com.focusflow.domain.model.FocusSnapshot;
import com.focusflow.service.FocusSessionService;

import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppContainer appContainer = ((FocusFlowApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, new DashboardViewModelFactory(appContainer))
                .get(DashboardViewModel.class);
        viewModel.dashboardState().observe(getViewLifecycleOwner(), this::render);
        binding.startSession.setOnClickListener(v -> {
            FocusSessionService.start(requireContext());
            Navigation.findNavController(v).navigate(R.id.sessionFragment);
        });
        binding.openSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.settingsFragment));
        binding.feedbackHelpful.setOnClickListener(v -> {
            viewModel.submitFeedback(true);
            Toast.makeText(requireContext(), R.string.feedback_saved, Toast.LENGTH_SHORT).show();
        });
        binding.feedbackNotHelpful.setOnClickListener(v -> {
            viewModel.submitFeedback(false);
            Toast.makeText(requireContext(), R.string.feedback_saved, Toast.LENGTH_SHORT).show();
        });
    }

    private void render(@Nullable DashboardState state) {
        if (state == null || binding == null) {
            return;
        }
        String name = state.userPreferences().greetingName();
        if (name.isEmpty()) {
            binding.greeting.setText(R.string.dashboard_greeting_fallback);
        } else {
            binding.greeting.setText(getString(R.string.dashboard_greeting, name));
        }

        FocusSnapshot snapshot = state.snapshot();
        if (snapshot.sessionStartedAtEpochMs() != null && snapshot.sessionEndedAtEpochMs() != null) {
            binding.sessionWindow.setText(TimeFormat.sessionWindow(
                    snapshot.sessionStartedAtEpochMs(),
                    snapshot.sessionEndedAtEpochMs()
            ));
        } else {
            binding.sessionWindow.setText(R.string.dashboard_session_idle);
        }

        binding.scoreIndicator.setMax(100);
        if (snapshot.focusScore() == null) {
            binding.scoreValue.setText(R.string.dashboard_score_empty);
            binding.scoreIndicator.setProgress(0);
        } else {
            binding.scoreValue.setText(getString(R.string.score_out_of_100, snapshot.focusScore()));
            binding.scoreIndicator.setProgress(snapshot.focusScore());
        }

        binding.riskIndicator.setMax(100);
        if (snapshot.distractionRiskPercent() == null) {
            binding.riskValue.setText(R.string.dashboard_risk_empty);
            binding.riskIndicator.setProgress(0);
        } else {
            binding.riskValue.setText(getString(R.string.risk_percent, snapshot.distractionRiskPercent()));
            binding.riskIndicator.setProgress(snapshot.distractionRiskPercent());
        }

        List<String> signals = snapshot.topSignals();
        binding.signalsContainer.removeAllViews();
        boolean hasSignals = !signals.isEmpty();
        binding.signalsEmpty.setVisibility(hasSignals ? View.GONE : View.VISIBLE);
        if (hasSignals) {
            LayoutInflater inflater = getLayoutInflater();
            for (String signal : signals) {
                android.widget.TextView item = (android.widget.TextView) inflater.inflate(
                        R.layout.item_signal,
                        binding.signalsContainer,
                        false
                );
                item.setText("• " + signal);
                binding.signalsContainer.addView(item);
            }
        }

        if (snapshot.recommendation() == null || snapshot.recommendation().isEmpty()) {
            binding.recommendation.setText(R.string.dashboard_recommendation_empty);
        } else {
            binding.recommendation.setText(snapshot.recommendation());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
