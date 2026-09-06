package com.focusflow.feature.session;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.focusflow.FocusFlowApplication;
import com.focusflow.R;
import com.focusflow.core.di.AppContainer;
import com.focusflow.core.util.TimeFormat;
import com.focusflow.databinding.FragmentSessionBinding;
import com.focusflow.domain.model.FocusSession;
import com.focusflow.service.FocusSessionService;

public class SessionFragment extends Fragment {

    private FragmentSessionBinding binding;
    private SessionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentSessionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppContainer container = ((FocusFlowApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, new SessionViewModelFactory(container)).get(SessionViewModel.class);
        viewModel.activeSession().observe(getViewLifecycleOwner(), this::render);
        binding.sessionToggle.setOnClickListener(v -> {
            FocusSession active = viewModel.activeSession().getValue();
            if (active != null && active.endedAtEpochMs() == null) {
                FocusSessionService.stop(requireContext());
            } else {
                FocusSessionService.start(requireContext());
            }
        });
        binding.usageAccess.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
    }

    private void render(@Nullable FocusSession session) {
        if (binding == null) {
            return;
        }
        boolean running = session != null && session.endedAtEpochMs() == null;
        binding.sessionStatus.setText(running ? R.string.session_running : R.string.session_idle);
        binding.sessionToggle.setText(running ? R.string.session_stop : R.string.session_start);
        if (running) {
            binding.sessionElapsed.setText(TimeFormat.elapsed(session.startedAtEpochMs()));
        } else if (session != null && session.endedAtEpochMs() != null) {
            binding.sessionElapsed.setText(TimeFormat.sessionWindow(session.startedAtEpochMs(), session.endedAtEpochMs()));
        } else {
            binding.sessionElapsed.setText("");
        }
        binding.usageAccess.setEnabled(!hasUsageAccess());
    }

    private boolean hasUsageAccess() {
        AppOpsManager appOps = (AppOpsManager) requireContext().getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                requireContext().getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
