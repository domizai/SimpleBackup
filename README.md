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
    .copy("advanced.pde", "data", "dir/to/file.ttf")
    // Omit specific files or directories within the copied directories.
    .omit("data/image.jpg", "dir/to/omit")
    // Specify the backup folder. Default is "simplebackup".
    .to("backup")
    // Enable or disable verbose mode (default is true).
    .verbose(false)
    // Limit the backup size for safety (default is 100_000 bytes).
    .sizelimit(10_000);              

// Get a list of files which will be copied.
println(backup.getFiles());
// Get a list of files which are omitted by the user.
println(backup.getOmittedFiles());
// Get a list of files which will be ignored (system files and files in the backup folder).
println(backup.getIgnoredFiles());
// Get the size of the backup
println(backup.getSize() + " bytes");

// Finally, back up your project. 
// This copies the files and directories to a subdirectory within the backup folder.
// The subdirectory name follows the format "yyMMddHHmmss" and is determined at backup time.
backup.backupNow();

// Or specify the subdirectory name yourself.
backup.backupNow(SimpleBackup.currentDateAndTime("yy-MM-dd_HH-mm-ss"));
```

Just remember to save your sketch before backing up.

---

Tested with Processing 4.3.2 (1295) on macOS 14.3.1 (Sonoma) and Windows 10 (21H2).
