# currentDateAndTime

```java
String currentDateAndTime()
```
Get the current date and time using the default pattern `yyMMddHHmmss`.

**Returns:**

The current date and time formatted according to the default pattern.

## Example

```java
backup.backupNow(SimpleBackup.currentDateAndTime());
```

--- 

# currentDateAndTime

```java
String currentDateAndTime(String pattern)
```
Copy files to a subdirectory within the destination directory. If no destination directory is set using `to(String backupDir)`, the files will be copied to `simplebackup`.

**Parameters:**

pattern - The pattern describing the date and time format.

**Returns:**

The current date and time formatted according to the pattern.

## Example

```java
backup.backupNow(SimpleBackup.currentDateAndTime("yy-MM-dd_HH-mm-ss"));
```

---

For more information, see the `reference` documentation folder in the [SimpleBackup](https://github.com/domizai/SimpleBackup) repository.

<br>
