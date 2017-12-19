package com.novoda.downloadmanager;

import android.support.annotation.WorkerThread;

interface Migrator {

    @WorkerThread
    void migrate();

    interface Callback {
        void onUpdate(MigrationStatus migrationStatus);
    }

    Migrator NO_OP = new Migrator() {
        @Override
        public void migrate() {
            // no-op.
        }
    };
}
