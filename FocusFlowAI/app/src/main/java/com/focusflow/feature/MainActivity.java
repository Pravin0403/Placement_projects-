package com.focusflow.feature;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.focusflow.FocusFlowApplication;
import com.focusflow.R;
import com.focusflow.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, 0);
            binding.bottomNav.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });

        boolean onboardingComplete = ((FocusFlowApplication) getApplication())
                .getAppContainer()
                .onboardingPreferences()
                .isCompleted();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("Nav host is missing");
        }
        NavController navController = navHostFragment.getNavController();
        if (savedInstanceState == null) {
            NavGraph graph = navController.getNavInflater().inflate(R.navigation.nav_graph);
            graph.setStartDestination(onboardingComplete ? R.id.dashboardFragment : R.id.onboardingFragment);
            navController.setGraph(graph);
        }
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean showNav = destination.getId() != R.id.onboardingFragment;
            binding.bottomNav.setVisibility(showNav ? View.VISIBLE : View.GONE);
        });
    }
}
