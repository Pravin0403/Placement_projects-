package com.focusflow.feature.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.focusflow.FocusFlowApplication;
import com.focusflow.R;
import com.focusflow.core.di.AppContainer;
import com.focusflow.data.local.entity.PredictionEntity;
import com.focusflow.databinding.FragmentAnalyticsBinding;

import java.util.List;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppContainer container = ((FocusFlowApplication) requireActivity().getApplication()).getAppContainer();
        AnalyticsViewModel viewModel = new ViewModelProvider(this, new AnalyticsViewModelFactory(container))
                .get(AnalyticsViewModel.class);
        binding.modelVersion.setText(getString(R.string.analytics_model, viewModel.modelVersion()));
        binding.appUsageAnalytics.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.appUsageAnalyticsFragment));
        binding.focusZone.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.focusZoneFragment));
        viewModel.predictions().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(@Nullable List<PredictionEntity> predictions) {
        if (binding == null) {
            return;
        }
        binding.predictionList.removeAllViews();
        boolean empty = predictions == null || predictions.isEmpty();
        binding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            binding.latency.setText(getString(R.string.analytics_latency, 0));
            return;
        }
        binding.latency.setText(getString(R.string.analytics_latency, predictions.get(0).getInferenceTimeMs()));
        LayoutInflater inflater = getLayoutInflater();
        for (PredictionEntity entity : predictions) {
            TextView row = (TextView) inflater.inflate(R.layout.item_signal, binding.predictionList, false);
            int riskPercent = Math.round(entity.getRiskScore() * 100f);
            row.setText(getString(
                    R.string.analytics_row,
                    riskPercent,
                    entity.getInferenceTimeMs(),
                    entity.getModelVersion()
            ));
            binding.predictionList.addView(row);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
