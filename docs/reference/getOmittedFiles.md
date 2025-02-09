# getOmittedFiles

```java
List<String> getOmittedFiles()
```
Get the list of relative paths to the files that will be ignored when calling `backupNow()`. This serves as a preview of the files that will be omitted.

**Returns:**

A list of relative paths of omitted files.

## Example

```java
println(backup.getOmittedFiles());
```

---

For more information, see the `reference` documentation folder in the [SimpleBackup](https://github.com/domizai/SimpleBackup) repository.

<br>
