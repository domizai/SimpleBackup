# omit

```java
SimpleBackup omit(List<String> paths)
SimpleBackup omit(String... paths)
```
Add files or directories to omit and prevent them from being copied.

**Parameters:**

paths - A list of files or directories to omit.

**Returns:**

`this` object (allows for method chaining).

## Examples

```java
backup = new SimpleBackup(this)
    .copy("/")
    .omit("data/image.jpg", "dir/to/omit");
```

```java
List<String> paths = List.of("data/image.jpg", "dir/to/omit");

backup = new SimpleBackup(this)
    .copy("/")
    .omit(paths);
```

---

For more information, see the `reference` documentation folder in the [SimpleBackup](https://github.com/domizai/SimpleBackup) repository.

<br>
