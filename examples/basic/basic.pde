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
