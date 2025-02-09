# backupNow

```java
boolean backupNow()
```
Copy files to a subdirectory within the destination directory. If no destination directory is set using `to(String backupDir)`, the files will be copied to `simplebackup`. The subdirectory name follows the format `yyMMddHHmmss` and is determined at backup time.

**Returns:**

true if all files in the list were copied, false otherwise.

## Example

```java
backup.backupNow();
```

---

# backupNow

```java
boolean backupNow(String subDir)
```
Copy files to a subdirectory within the destination directory. If no destination directory is set using `to(String backupDir)`, the files will be copied to `simplebackup`.

**Parameters:**

subDir - The subdirectory in the destination directory.

**Returns:**

true if all files in the list were copied, false otherwise.

## Example

```java
backup.backupNow(SimpleBackup.currentDateAndTime("yy-MM-dd_HH-mm-ss"));
```

---

For more information, see the `reference` documentation folder in the [SimpleBackup](https://github.com/domizai/SimpleBackup) repository.

<br>
