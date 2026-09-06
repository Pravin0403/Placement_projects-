package com.focusflow.core.di;

import android.content.Context;

import com.focusflow.core.common.AppExecutors;
import com.focusflow.data.local.AppDatabase;
import com.focusflow.data.local.AppSettings;
import com.focusflow.data.local.OnboardingPreferences;
import com.focusflow.data.remote.NetworkModule;
import com.focusflow.data.remote.SyncClient;
import com.focusflow.data.remote.api.FocusFlowApi;
import com.focusflow.data.local.dao.BlockedAppDao;
import com.focusflow.data.local.dao.EmergencyContactDao;
import com.focusflow.data.local.dao.FocusZoneDao;
import com.focusflow.data.local.dao.PredictionDao;
import com.focusflow.data.local.dao.ViolationDao;
import com.focusflow.data.repository.AppUsageRepositoryImpl;
import com.focusflow.data.repository.AuthRepositoryImpl;
import com.focusflow.data.repository.FeedbackRepositoryImpl;
import com.focusflow.data.repository.FocusRepositoryImpl;
import com.focusflow.data.repository.ModelRepositoryImpl;
import com.focusflow.data.repository.SessionRepositoryImpl;
import com.focusflow.data.repository.UserRepositoryImpl;
import com.focusflow.domain.repository.AppUsageRepository;
import com.focusflow.domain.repository.AuthRepository;
import com.focusflow.domain.repository.FeedbackRepository;
import com.focusflow.domain.repository.FocusRepository;
import com.focusflow.domain.repository.ModelRepository;
import com.focusflow.domain.repository.SessionRepository;
import com.focusflow.domain.repository.UserRepository;
import com.focusflow.domain.usecase.AuthenticateUseCase;
import com.focusflow.domain.usecase.CompleteOnboardingUseCase;
import com.focusflow.domain.usecase.EndFocusSessionUseCase;
import com.focusflow.domain.usecase.ObserveDashboardUseCase;
import com.focusflow.domain.usecase.StartFocusSessionUseCase;
import com.focusflow.domain.usecase.SubmitFeedbackUseCase;
import com.focusflow.domain.usecase.UpdateTelemetryConsentUseCase;
import com.focusflow.engine.DecisionEngineImpl;
import com.focusflow.engine.FocusEngine;
import com.focusflow.engine.FocusEngineImpl;
import com.focusflow.engine.FocusZoneEngine;
import com.focusflow.engine.FocusZoneEngineImpl;
import com.focusflow.engine.RecommendationEngineImpl;
import com.focusflow.engine.RiskCalculatorImpl;
import com.focusflow.ml.FeatureExtractorImpl;
import com.focusflow.ml.FeatureScalerImpl;
import com.focusflow.ml.LogisticPredictor;
import com.focusflow.ml.ModelManagerImpl;
import com.focusflow.ml.PredictionPostProcessorImpl;
import com.focusflow.ml.TFLitePredictor;
import com.focusflow.ml.TFLitePredictorImpl;
import com.focusflow.telemetry.aggregator.SignalAggregatorImpl;
import com.focusflow.telemetry.collectors.AppSwitchCollectorImpl;
import com.focusflow.telemetry.collectors.ScreenEventCollectorImpl;
import com.focusflow.telemetry.processor.TelemetryProcessor;
import com.focusflow.telemetry.processor.TelemetryProcessorImpl;
import com.focusflow.telemetry.store.TelemetryEventStore;

public final class AppContainer {

    private final AppExecutors executors;
    private final AppDatabase database;
    private final AppSettings appSettings;
    private final UserRepository userRepository;
    private final FocusRepository focusRepository;
    private final SessionRepository sessionRepository;
    private final AppUsageRepository appUsageRepository;
    private final FocusZoneEngine focusZoneEngine;
    private final ModelRepository modelRepository;
    private final ModelManagerImpl modelManager;
    private final FeedbackRepository feedbackRepository;
    private final CompleteOnboardingUseCase completeOnboardingUseCase;
    private final ObserveDashboardUseCase observeDashboardUseCase;
    private final StartFocusSessionUseCase startFocusSessionUseCase;
    private final EndFocusSessionUseCase endFocusSessionUseCase;
    private final SubmitFeedbackUseCase submitFeedbackUseCase;
    private final UpdateTelemetryConsentUseCase updateTelemetryConsentUseCase;
    private final OnboardingPreferences onboardingPreferences;
    private final FocusEngine focusEngine;
    private final TelemetryProcessor telemetryProcessor;
    private final FocusFlowApi api;
    private final SyncClient syncClient;
    private final AuthRepository authRepository;
    private final AuthenticateUseCase authenticateUseCase;

    public AppContainer(Context context) {
        Context appContext = context.getApplicationContext();
        executors = new AppExecutors();
        database = AppDatabase.getInstance(appContext);
        appSettings = new AppSettings(appContext);
        onboardingPreferences = new OnboardingPreferences(appContext);
        userRepository = new UserRepositoryImpl(database.userPreferencesDao(), onboardingPreferences);
        sessionRepository = new SessionRepositoryImpl(database.focusSessionDao(), database.telemetryEventDao());
        appUsageRepository = new AppUsageRepositoryImpl(appContext, executors, database.focusSessionDao(), database.telemetryEventDao());
        focusZoneEngine = new FocusZoneEngineImpl(appContext, database.focusSessionDao(), database.focusSnapshotDao(), database.telemetryEventDao(), appSettings);
        focusRepository = new FocusRepositoryImpl(
                database.userPreferencesDao(),
                database.focusSnapshotDao(),
                database.focusSessionDao()
        );
        modelManager = new ModelManagerImpl(appContext);
        modelRepository = new ModelRepositoryImpl(modelManager);
        feedbackRepository = new FeedbackRepositoryImpl(database.feedbackDao());
        completeOnboardingUseCase = new CompleteOnboardingUseCase(userRepository);
        observeDashboardUseCase = new ObserveDashboardUseCase(focusRepository);
        startFocusSessionUseCase = new StartFocusSessionUseCase(sessionRepository);
        endFocusSessionUseCase = new EndFocusSessionUseCase(sessionRepository);
        submitFeedbackUseCase = new SubmitFeedbackUseCase(feedbackRepository, appSettings);
        updateTelemetryConsentUseCase = new UpdateTelemetryConsentUseCase(userRepository);

        TelemetryEventStore eventStore = new TelemetryEventStore(database.focusSessionDao(), database.telemetryEventDao());
        telemetryProcessor = new TelemetryProcessorImpl(
                database.userPreferencesDao(),
                new AppSwitchCollectorImpl(appContext, eventStore, executors),
                new ScreenEventCollectorImpl(appContext, eventStore, executors)
        );
        FeatureExtractorImpl extractor = new FeatureExtractorImpl();
        SignalAggregatorImpl aggregator = new SignalAggregatorImpl(
                database.focusSessionDao(),
                database.telemetryEventDao(),
                extractor
        );
        PredictionPostProcessorImpl postProcessor = new PredictionPostProcessorImpl();
        TFLitePredictor predictor = new TFLitePredictorImpl(
                modelManager,
                new FeatureScalerImpl(),
                new LogisticPredictor(),
                postProcessor
        );
        focusEngine = new FocusEngineImpl(
                appContext,
                database.focusSessionDao(),
                database.focusSnapshotDao(),
                database.predictionDao(),
                aggregator,
                predictor,
                new RiskCalculatorImpl(),
                new DecisionEngineImpl(),
                new RecommendationEngineImpl(),
                postProcessor,
                appSettings
        );
        api = NetworkModule.create(appSettings);
        syncClient = new SyncClient(
                api,
                database.telemetryEventDao(),
                database.predictionDao(),
                database.feedbackDao(),
                appSettings
        );
        authRepository = new AuthRepositoryImpl(api, appSettings);
        authenticateUseCase = new AuthenticateUseCase(authRepository);
    }

    public AppExecutors executors() {
        return executors;
    }

    public AppDatabase database() {
        return database;
    }

    public AppSettings appSettings() {
        return appSettings;
    }

    public UserRepository userRepository() {
        return userRepository;
    }

    public FocusRepository focusRepository() {
        return focusRepository;
    }

    public SessionRepository sessionRepository() {
        return sessionRepository;
    }

    public AppUsageRepository appUsageRepository() {
        return appUsageRepository;
    }

    public FocusZoneEngine focusZoneEngine() {
        return focusZoneEngine;
    }

    public ModelRepository modelRepository() {
        return modelRepository;
    }

    public ModelManagerImpl modelManager() {
        return modelManager;
    }

    public FeedbackRepository feedbackRepository() {
        return feedbackRepository;
    }

    public CompleteOnboardingUseCase completeOnboardingUseCase() {
        return completeOnboardingUseCase;
    }

    public ObserveDashboardUseCase observeDashboardUseCase() {
        return observeDashboardUseCase;
    }

    public StartFocusSessionUseCase startFocusSessionUseCase() {
        return startFocusSessionUseCase;
    }

    public EndFocusSessionUseCase endFocusSessionUseCase() {
        return endFocusSessionUseCase;
    }

    public SubmitFeedbackUseCase submitFeedbackUseCase() {
        return submitFeedbackUseCase;
    }

    public UpdateTelemetryConsentUseCase updateTelemetryConsentUseCase() {
        return updateTelemetryConsentUseCase;
    }

    public OnboardingPreferences onboardingPreferences() {
        return onboardingPreferences;
    }

    public FocusEngine focusEngine() {
        return focusEngine;
    }

    public TelemetryProcessor telemetryProcessor() {
        return telemetryProcessor;
    }

    public FocusFlowApi api() {
        return api;
    }

    public SyncClient syncClient() {
        return syncClient;
    }

    public AuthenticateUseCase authenticateUseCase() {
        return authenticateUseCase;
    }

    public PredictionDao predictionDao() {
        return database.predictionDao();
    }

    public FocusZoneDao focusZoneDao() {
        return database.focusZoneDao();
    }

    public EmergencyContactDao emergencyContactDao() {
        return database.emergencyContactDao();
    }

    public ViolationDao violationDao() {
        return database.violationDao();
    }

    public BlockedAppDao blockedAppDao() {
        return database.blockedAppDao();
    }
}
