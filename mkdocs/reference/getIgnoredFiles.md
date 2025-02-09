# getIgnoredFiles

```java
List<String> getIgnoredFiles()
```
Get the list of relative paths to the files that will be ignored (system files and files in the backup folder) when calling `backupNow()`. 

**Returns:**

A list of relative paths of ignored files.

## Example

```java
println(backup.getIgnoredFiles());
```

---

For more information, see the `reference` documentation folder in the [SimpleBackup](https://github.com/domizai/SimpleBackup) repository.

<br>
