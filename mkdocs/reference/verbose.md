# verbose

```java
SimpleBackup verbose(boolean verbose)
```
Set the verbose flag. True by default.

**Parameters:**

verbose - The verbose flag.

**Returns:**

`this` object (allows method chaining).

## Example

```java
backup = new SimpleBackup(this)
    .copy("/")
    .verbose(true);  
```

---

For more information, see the `reference` documentation folder in the [SimpleBackup](https://github.com/domizai/SimpleBackup) repository.

<br>
