# download-manager [![CI status](https://ci.novoda.com/buildStatus/icon?job=download-manager)](https://ci.novoda.com/job/download-manager/lastBuild/console) [![Download from Bintray](https://api.bintray.com/packages/novoda/maven/download-manager/images/download.svg)](https://bintray.com/novoda/maven/download-manager/_latestVersion)[![Apache 2.0 Licence](https://img.shields.io/github/license/novoda/no-player.svg)](https://github.com/novoda/no-player/blob/master/LICENSE)

A library that handles long-running downloads, handling the network interactions and retrying downloads automatically after failures. Clients can request
downloads in batches, receiving a single notification for all of the files allocated to a batch while being able to retrieve the single files after downloads complete.

## Adding to your project

To start using this library, add these lines to the `build.gradle` of your project:

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.novoda:download-manager:<latest-version>'
}
```

## Simple usage

1. Create a `LiteDownloadManagerCommands`:

```
LiteDownloadManagerCommands liteDownloadManagerCommands = DownloadManagerBuilder
        .newInstance(this, handler, R.mipmap.ic_launcher_round)
        .withLogHandle(new DemoLogHandle())
        .withStorageRequirementRules(StorageRequirementRuleFactory.createByteBasedRule(TWO_HUNDRED_MB_IN_BYTES))
        .build();
```

2. Create a `Batch` of files to download:

```
Batch batch = Batch.with(primaryStorageWithDownloadsSubpackage, DownloadBatchIdCreator.createSanitizedFrom("batch_id_1"), "batch one title")
        .downloadFrom("http://ipv4.download.thinkbroadband.com/5MB.zip").saveTo("foo/bar", "local-filename-5mb.zip").withIdentifier(DownloadFileIdCreator.createFrom("file_id_1")).apply()
        .downloadFrom("http://ipv4.download.thinkbroadband.com/10MB.zip").apply()
        .build();
```   

3. Schedule the batch for download:

```
liteDownloadManagerCommands.download(batch);
```
