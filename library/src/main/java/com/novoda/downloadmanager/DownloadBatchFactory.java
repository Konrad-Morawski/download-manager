package com.novoda.downloadmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

final class DownloadBatchFactory {

    private static final boolean NOTIFICATION_NOT_SEEN = false;

    private DownloadBatchFactory() {
        // non instantiable factory class
    }

    static DownloadBatch newInstance(Batch batch,
                                     FileOperations fileOperations,
                                     DownloadsBatchPersistence downloadsBatchPersistence,
                                     DownloadsFilePersistence downloadsFilePersistence,
                                     CallbackThrottle callbackThrottle,
                                     ConnectionChecker connectionChecker) {
        DownloadBatchTitle downloadBatchTitle = DownloadBatchTitleCreator.createFrom(batch);
        DownloadBatchId downloadBatchId = batch.downloadBatchId();
        long downloadedDateTimeInMillis = System.currentTimeMillis();

        List<DownloadFile> downloadFiles = new ArrayList<>(batch.batchFiles().size());

        for (BatchFile batchFile : batch.batchFiles()) {
            String networkAddress = batchFile.networkAddress();

            InternalFileSize fileSize = InternalFileSizeCreator.unknownFileSize();

            FilePersistenceCreator filePersistenceCreator = fileOperations.filePersistenceCreator();
            FilePersistence filePersistence = filePersistenceCreator.create();

            String basePath = filePersistence.basePath().path();
            FilePath filePath = FilePathCreator.create(basePath, prependBatchIdTo(relativePathFrom(batchFile), downloadBatchId));
            FileName fileName = FileNameExtractor.extractFrom(filePath.path());

            DownloadFileId downloadFileId = FallbackDownloadFileIdProvider.downloadFileIdFor(batch.downloadBatchId(), batchFile);
            InternalDownloadFileStatus downloadFileStatus = new LiteDownloadFileStatus(
                    downloadBatchId,
                    downloadFileId,
                    InternalDownloadFileStatus.Status.QUEUED,
                    fileSize,
                    filePath
            );

            FileDownloader fileDownloader = fileOperations.fileDownloaderCreator().create();
            FileSizeRequester fileSizeRequester = fileOperations.fileSizeRequester();

            DownloadFile downloadFile = new DownloadFile(
                    downloadBatchId,
                    downloadFileId,
                    networkAddress,
                    downloadFileStatus,
                    fileName,
                    filePath,
                    fileSize,
                    fileDownloader,
                    fileSizeRequester,
                    filePersistence,
                    downloadsFilePersistence
            );
            downloadFiles.add(downloadFile);
        }

        InternalDownloadBatchStatus liteDownloadBatchStatus = new LiteDownloadBatchStatus(
                downloadBatchId,
                downloadBatchTitle,
                downloadedDateTimeInMillis,
                DownloadBatchStatus.Status.QUEUED,
                NOTIFICATION_NOT_SEEN
        );

        return new DownloadBatch(
                liteDownloadBatchStatus,
                downloadFiles,
                new HashMap<>(),
                downloadsBatchPersistence,
                callbackThrottle,
                connectionChecker
        );
    }

    private static String prependBatchIdTo(String filePath, DownloadBatchId downloadBatchId) {
        return sanitizeBatchIdPath(downloadBatchId.rawId()) + File.separatorChar + filePath;
    }

    private static String sanitizeBatchIdPath(String batchIdPath) {
        return batchIdPath.replaceAll("[:\\\\/*?|<>]", "_");
    }

    private static String relativePathFrom(BatchFile batchFile) {
        String fileNameFromNetworkAddress = FileNameExtractor.extractFrom(batchFile.networkAddress()).name();
        return batchFile.relativePath().or(fileNameFromNetworkAddress);
    }

}
