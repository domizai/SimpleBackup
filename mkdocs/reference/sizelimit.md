# sizelimit

```java
SimpleBackup sizelimit(long sizelimit)
```
Set the size limit for copying files. Default is `100000L` bytes. This is a safety measure to prevent copying large files by accident.

**Parameters:**

sizelimit - The size limit in bytes.

**Returns:**

`this` object (allows method chaining).

## Example

```java
backup = new SimpleBackup(this)
    .copy("/")
    .sizelimit(10_000);     
```

---

For more information, see the `reference` documentation folder in the [SimpleBackup](https://github.com/domizai/SimpleBackup) repository.

<br>
