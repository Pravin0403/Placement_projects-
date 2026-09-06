package com.focusflow.feature.focuszone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.focusflow.FocusFlowApplication;
import com.focusflow.R;
import com.focusflow.core.di.AppContainer;
import com.focusflow.databinding.FragmentFocusZoneBinding;
import com.focusflow.domain.model.FocusZone;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying focus zone timeline and recommendations.
 */
public class FocusZoneFragment extends Fragment {

    private FragmentFocusZoneBinding binding;
    private FocusZoneViewModel viewModel;
    private FocusZoneAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentFocusZoneBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContainer container = ((FocusFlowApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, new FocusZoneViewModelFactory(container))
                .get(FocusZoneViewModel.class);

        setupRecyclerView();
        observeViewModel();
        
        viewModel.loadFocusZones();
    }

    private void setupRecyclerView() {
        adapter = new FocusZoneAdapter();
        binding.focusZoneTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.focusZoneTimeline.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.focusZones().observe(getViewLifecycleOwner(), this::renderFocusZones);
        viewModel.bestFocusHour().observe(getViewLifecycleOwner(), this::renderBestHour);
        viewModel.recommendedHours().observe(getViewLifecycleOwner(), this::renderRecommendedHours);
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });
    }

    private void renderFocusZones(List<FocusZone> zones) {
        adapter.setZones(zones);
    }

    private void renderBestHour(FocusZone bestHour) {
        if (bestHour != null) {
            binding.bestHourText.setText(String.format("%02d:00 - %02d:00", 
                    bestHour.hourOfDay(), (bestHour.hourOfDay() + 1) % 24));
            binding.bestHourScore.setText(String.format("%d/100", bestHour.focusScore()));
            binding.bestHourRisk.setText(String.format("%.0f%%", bestHour.distractionRisk() * 100));
            binding.bestHourRecommendation.setText(bestHour.recommendation());
            binding.bestHourCard.setVisibility(View.VISIBLE);
        } else {
            binding.bestHourCard.setVisibility(View.GONE);
        }
    }

    private void renderRecommendedHours(List<FocusZone> hours) {
        if (hours != null && !hours.isEmpty()) {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < hours.size(); i++) {
                FocusZone zone = hours.get(i);
                text.append(String.format("%d. %02d:00-%02d:00 (%d/100)\n", 
                        i + 1, zone.hourOfDay(), (zone.hourOfDay() + 1) % 24, zone.focusScore()));
            }
            binding.recommendedHoursText.setText(text.toString());
            binding.recommendedHoursCard.setVisibility(View.VISIBLE);
        } else {
            binding.recommendedHoursCard.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class FocusZoneAdapter extends RecyclerView.Adapter<FocusZoneAdapter.ViewHolder> {

        private List<FocusZone> zones = new ArrayList<>();

        public void setZones(List<FocusZone> zones) {
            this.zones = zones != null ? zones : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_focus_zone, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(zones.get(position));
        }

        @Override
        public int getItemCount() {
            return zones.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView hourText;
            private final TextView scoreText;
            private final TextView riskText;
            private final TextView starsText;

            public ViewHolder(View itemView) {
                super(itemView);
                hourText = itemView.findViewById(R.id.hour_text);
                scoreText = itemView.findViewById(R.id.score_text);
                riskText = itemView.findViewById(R.id.risk_text);
                starsText = itemView.findViewById(R.id.stars_text);
            }

            public void bind(FocusZone zone) {
                hourText.setText(String.format("%02d:00", zone.hourOfDay()));
                scoreText.setText(String.format("%d/100", zone.focusScore()));
                riskText.setText(String.format("%.0f%%", zone.distractionRisk() * 100));
                
                // Convert score to stars (0-100 -> 0-5 stars)
                int stars = (int) Math.round(zone.focusScore() / 20.0);
                StringBuilder starString = new StringBuilder();
                for (int i = 0; i < 5; i++) {
                    starString.append(i < stars ? "★" : "☆");
                }
                starsText.setText(starString.toString());
            }
        }
    }
}