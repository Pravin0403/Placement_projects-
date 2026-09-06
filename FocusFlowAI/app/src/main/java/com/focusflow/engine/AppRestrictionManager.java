package com.focusflow.engine;

import android.content.Context;

import com.focusflow.data.local.dao.BlockedAppDao;
import com.focusflow.data.local.entity.BlockedAppEntity;

import java.util.List;

/**
 * Manager for app restriction functionality using legitimate Android mechanisms.
 */
public interface AppRestrictionManager {

    /**
     * Check if an app is currently blocked.
     * @param packageName Package name of the app
     * @return true if the app is blocked
     */
    boolean isAppBlocked(String packageName);

    /**
     * Add an app to the blocked list.
     * @param packageName Package name of the app
     * @param appName Display name of the app
     * @param distractionLevel Distraction level
     */
    void addBlockedApp(String packageName, String appName, String distractionLevel);

    /**
     * Remove an app from the blocked list.
     * @param packageName Package name of the app
     */
    void removeBlockedApp(String packageName);

    /**
     * Get all blocked apps.
     * @return List of blocked apps
     */
    List<BlockedAppEntity> getBlockedApps();

    /**
     * Check if the app restriction service is available.
     * @return true if service can function properly
     */
    boolean isRestrictionServiceAvailable();
}

/**
 * Implementation using UsageStatsManager for detection and educational interventions.
 */
class AppRestrictionManagerImpl implements AppRestrictionManager {

    private final Context context;
    private final BlockedAppDao blockedAppDao;

    public AppRestrictionManagerImpl(Context context, BlockedAppDao blockedAppDao) {
        this.context = context.getApplicationContext();
        this.blockedAppDao = blockedAppDao;
    }

    @Override
    public boolean isAppBlocked(String packageName) {
        return blockedAppDao.isBlocked(packageName);
    }

    @Override
    public void addBlockedApp(String packageName, String appName, String distractionLevel) {
        BlockedAppEntity entity = new BlockedAppEntity(
                0,
                packageName,
                appName,
                distractionLevel,
                System.currentTimeMillis()
        );
        blockedAppDao.insert(entity);
    }

    @Override
    public void removeBlockedApp(String packageName) {
        blockedAppDao.delete(packageName);
    }

    @Override
    public List<BlockedAppEntity> getBlockedApps() {
        return blockedAppDao.observeAll().getValue();
    }

    @Override
    public boolean isRestrictionServiceAvailable() {
        // Check if UsageStats permission is available
        // This is the primary mechanism for app detection
        android.app.AppOpsManager appOps = (android.app.AppOpsManager) 
                context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }

        int mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.getPackageName()
        );

        return mode == android.app.AppOpsManager.MODE_ALLOWED;
    }
}