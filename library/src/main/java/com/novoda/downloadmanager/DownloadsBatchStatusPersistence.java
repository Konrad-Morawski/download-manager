package com.novoda.downloadmanager;

interface DownloadsBatchStatusPersistence {

    void updateStatusAsync(DownloadBatchId downloadBatchId, DownloadBatchStatus.Status status);

    void persistCompletedBatch(Migration migration);
}
