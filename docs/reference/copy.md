# copy

```java
SimpleBackup copy(List<String> paths)
SimpleBackup copy(String... paths)
```
Add files or directories to copy.

**Parameters:**

paths - A list of files or directories to copy.

**Returns:**

`this` object (allows for method chaining).

## Examples

```java
backup = new SimpleBackup(this).copy("mySketch.pde", "data", "dir/to/file.ttf");
```

```java
List<String> paths = List.of("mySketch.pde", "data", "dir/to/file.ttf");

backup = new SimpleBackup(this).copy(paths);
```

---

For more information, see the `reference` documentation folder in the [SimpleBackup](https://github.com/domizai/SimpleBackup) repository.

<br>
