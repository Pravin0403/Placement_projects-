package com.focusflow.feature.analytics;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.focusflow.FocusFlowApplication;
import com.focusflow.R;
import com.focusflow.core.di.AppContainer;
import com.focusflow.core.util.TimeFormat;
import com.focusflow.databinding.FragmentAppUsageAnalyticsBinding;
import com.focusflow.domain.model.AppUsageStats;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying app usage analytics and statistics.
 */
public class AppUsageAnalyticsFragment extends Fragment {

    private FragmentAppUsageAnalyticsBinding binding;
    private AppUsageAnalyticsViewModel viewModel;
    private AppUsageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentAppUsageAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        AppContainer container = ((FocusFlowApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, new AppUsageAnalyticsViewModelFactory(container))
                .get(AppUsageAnalyticsViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        
        // Load initial data
        viewModel.refreshAllData();
    }

    private void setupRecyclerView() {
        adapter = new AppUsageAdapter();
        binding.appUsageList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.appUsageList.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.grantPermission.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        });

        binding.refresh.setOnClickListener(v -> {
            viewModel.refreshAllData();
        });

        binding.tabDaily.setOnClickListener(v -> {
            selectTab(0);
            viewModel.loadDailyUsageStats();
        });

        binding.tabFocusSession.setOnClickListener(v -> {
            selectTab(1);
            viewModel.loadFocusSessionStats();
        });

        binding.tabDistracting.setOnClickListener(v -> {
            selectTab(2);
            viewModel.loadMostDistractingApps(10);
        });
    }

    private void selectTab(int position) {
        binding.tabDaily.setSelected(position == 0);
        binding.tabFocusSession.setSelected(position == 1);
        binding.tabDistracting.setSelected(position == 2);
    }

    private void observeViewModel() {
        viewModel.hasPermission().observe(getViewLifecycleOwner(), hasPermission -> {
            if (Boolean.TRUE.equals(hasPermission)) {
                binding.permissionCard.setVisibility(View.GONE);
                binding.contentLayout.setVisibility(View.VISIBLE);
            } else {
                binding.permissionCard.setVisibility(View.VISIBLE);
                binding.contentLayout.setVisibility(View.GONE);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
            binding.refresh.setEnabled(!Boolean.TRUE.equals(isLoading));
        });

        viewModel.dailyUsageStats().observe(getViewLifecycleOwner(), stats -> {
            if (binding.tabDaily.isSelected()) {
                adapter.setUsageStats(stats);
            }
        });

        viewModel.focusSessionStats().observe(getViewLifecycleOwner(), stats -> {
            if (binding.tabFocusSession.isSelected()) {
                adapter.setUsageStats(stats);
            }
        });

        viewModel.mostDistractingApps().observe(getViewLifecycleOwner(), stats -> {
            if (binding.tabDistracting.isSelected()) {
                adapter.setUsageStats(stats);
            }
        });

        viewModel.totalDailyUsage().observe(getViewLifecycleOwner(), totalUsage -> {
            if (totalUsage != null) {
                binding.totalUsage.setText(TimeFormat.formatDuration(totalUsage));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class AppUsageAdapter extends RecyclerView.Adapter<AppUsageAdapter.ViewHolder> {

        private List<AppUsageStats> usageStats = new ArrayList<>();

        public void setUsageStats(List<AppUsageStats> stats) {
            this.usageStats = stats != null ? stats : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app_usage, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppUsageStats stats = usageStats.get(position);
            holder.bind(stats);
        }

        @Override
        public int getItemCount() {
            return usageStats.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView appName;
            private final TextView packageName;
            private final TextView usageDuration;
            private final TextView usagePercentage;
            private final TextView focusSessionUsage;

            public ViewHolder(View itemView) {
                super(itemView);
                appName = itemView.findViewById(R.id.app_name);
                packageName = itemView.findViewById(R.id.package_name);
                usageDuration = itemView.findViewById(R.id.usage_duration);
                usagePercentage = itemView.findViewById(R.id.usage_percentage);
                focusSessionUsage = itemView.findViewById(R.id.focus_session_usage);
            }

            public void bind(AppUsageStats stats) {
                appName.setText(stats.appName() != null ? stats.appName() : stats.packageName());
                packageName.setText(stats.packageName());
                usageDuration.setText(TimeFormat.formatDuration(stats.totalUsageDurationMs()));
                usagePercentage.setText(String.format("%.1f%%", stats.percentageOfTotalUsage()));
                
                if (stats.usageDuringFocusSessionsMs() > 0) {
                    focusSessionUsage.setText(TimeFormat.formatDuration(stats.usageDuringFocusSessionsMs()) + " during focus");
                    focusSessionUsage.setVisibility(View.VISIBLE);
                } else {
                    focusSessionUsage.setVisibility(View.GONE);
                }
            }
        }
    }
}