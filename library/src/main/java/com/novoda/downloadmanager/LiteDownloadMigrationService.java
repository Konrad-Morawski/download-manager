package com.novoda.downloadmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiteDownloadMigrationService extends Service implements DownloadMigrationService {

    private static final String TAG = "MigrationService";
    private static volatile ExecutorService singleInstanceExecutor = Executors.newSingleThreadExecutor();

    private IBinder binder;
    private NotificationManager notificationManager;
    private NotificationChannelProvider notificationChannelProvider;
    private NotificationCreator<MigrationStatus> notificationCreator;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        binder = new MigrationDownloadServiceBinder();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        super.onCreate();
    }

    class MigrationDownloadServiceBinder extends Binder {
        DownloadMigrationService getService() {
            return LiteDownloadMigrationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void setNotificationChannelProvider(NotificationChannelProvider notificationChannelProvider) {
        this.notificationChannelProvider = notificationChannelProvider;
    }

    @Override
    public void setNotificationCreator(NotificationCreator<MigrationStatus> notificationCreator) {
        this.notificationCreator = notificationCreator;
    }

    @Override
    public void startMigration(String databaseFilename, MigrationCallback migrationCallback) {
        createNotificationChannel();
        MigrationJob migrationJob = new MigrationJob(getApplicationContext(), getDatabasePath(databaseFilename));
        migrationJob.addCallback(migrationCallback);
        migrationJob.addCallback(notificationMigrationCallback);
        singleInstanceExecutor.execute(migrationJob);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelProvider.registerNotificationChannel(getApplicationContext());
        }
    }

    private final MigrationCallback notificationMigrationCallback = new MigrationCallback() {
        @Override
        public void onUpdate(MigrationStatus migrationStatus) {
            NotificationInformation notification = notificationCreator.createNotification(migrationStatus);

            switch (notification.notificationStackState()) {
                case SINGLE_PERSISTENT_NOTIFICATION:
                    updateNotification(notification);
                    break;
                case STACK_NOTIFICATION_DISMISSIBLE:
                    stackNotification(notification);
                    break;
                case STACK_NOTIFICATION_NOT_DISMISSIBLE:
                default:
                    String message = String.format(
                            "%s: %s is not supported.",
                            NotificationCustomizer.NotificationStackState.class.getSimpleName(),
                            notification.notificationStackState()
                    );
                    throw new IllegalArgumentException(message);
            }
        }

        private void updateNotification(NotificationInformation notificationInformation) {
            startForeground(notificationInformation.getId(), notificationInformation.getNotification());
        }

        private void stackNotification(NotificationInformation notificationInformation) {
            stopForeground(true);
            Notification notification = notificationInformation.getNotification();
            notificationManager.notify(notificationInformation.getId(), notification);
        }
    };
}
