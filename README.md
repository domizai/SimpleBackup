# SimpleBackup

Never lose your work. Automatically back up your project with every export. A utility library for Processing.

## Basic Example

```java
import ch.domizai.simplebackup.*;

SimpleBackup backup;

void setup() {
    backup = new SimpleBackup(this).copy("/");
}

void draw() {
}

void mouseClicked() {
    backup.backupNow();
}
```

## Methods

```java
backup = new SimpleBackup(this)
    // Copy everything within the sketch directory (except the backup folder).
    .copy("/")
    
    // Alternatively, list specific files or directories to copy.
    .copy("mySketch.pde", "data", "dir/to/file.ttf")
    
    // Ignore specific files or directories within the copied directories.
    .ignore("data/image.jpg", "dir/to/ignore")
    
    // Specify the backup folder. Default is "simplebackup".
    .to("backup")
    
    // Enable or disable verbose mode (default is true).
    .verbose(false)

    // Limit the backup size for safety (default is 100_000 bytes).
    .sizelimit(10_000);

// Finally, back up your project. 
// This copies the files and directories to a subdirectory within the backup folder.
// The subdirectory name follows the format "yyMMddHHmmss" and is determined at backup time.
backup.backupNow();

// Or specify the subdirectory name yourself.
backup.backupNow(SimpleBackup.currentDateAndTime("yy-MM-dd_HH-mm-ss"));
```

Tested with Processing 4.3.2 (1295) on macOS 14.3.1 (Sonoma) Intel.
