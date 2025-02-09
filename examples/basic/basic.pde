import ch.domizai.simplebackup.*;
import java.nio.file.Path;
import java.nio.file.Paths;

SimpleBackup backup;

void setup() {
  backup = new SimpleBackup(this).copy("/");
}

void draw() {
}

void mouseClicked() {
  backup.backupNow();
}
