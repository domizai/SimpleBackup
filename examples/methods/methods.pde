import ch.domizai.simplebackup.*;

SimpleBackup backup;

void setup() {
  backup = new SimpleBackup(this)
    // Copy everything within the sketch directory (except the backup folder).
    .copy("/")
    
    // Alternatively, list specific files or directories to copy.
    .copy("advanced.pde", "data", "dir/to/file.ttf")

    // Ignore specific files or directories within the copied directories.
    .ignore("data/image.jpg", "dir/to/ignore")
    
    // Specify the backup folder. Default is "simplebackup".
    .to("backup")
    
    // Enable or disable verbose mode (default is true).
    .verbose(false)

    // Limit the backup size for safety (default is 100_000 bytes).
    .sizelimit(10_000);              

  // Get the list of files and directories which will be copied
  println(backup.getFilesToCopy());

  // Get the list of files and directories which are ignored
  println(backup.getFilesToIgnore());
  
  // Get the size of the backup
  println(backup.getSize() + " bytes");
}

void draw() {
}

void mouseClicked() {
  // Finally, back up your project. 
  // This copies the files and directories to a subdirectory within the backup folder.
  // The subdirectory name follows the format "yyMMddHHmmss" and is determined at backup time.
  backup.backupNow();

  // Or specify the subdirectory name yourself.
  backup.backupNow(SimpleBackup.currentDateAndTime("yy-MM-dd_HH-mm-ss"));
}
