#jeck
Java Easy Collection Kit

### What is it ?

Just a bunch of specialized Java collections (some quite generic too), with one thing in common : ** ease of use**.
Most of them are abstract classes, meant to ease implementing standard Java collections (mostly ones I actually needed).

### Examples

#### QuickMap

This is a simple wrapper that allows to easily create a "quick and dirty" map for testing, 
elements are simply listed to the constructor in a linear fashion (key1, value1, key2, value2, etc.)

```java
  Map<String, Object> m = new QuickMap<String, Object>("hello", 0, "world", true);
```

#### EasyMap

Allows implementing a `Map` by directly specifying the `get()`, `put()`, `remove()` and an additional `keys()` method 
(returning a non synchronized view of the map's keys at the moment it's called).

I find this implementation much easier than the default `AbstractMap`, which requires implementing a `Set`, 
and customizing its iterator.

#### FetchList

Allows implementing a `List` that doesn't need to know all it's elements to be used. 
Instead, elements are fetched when needed.
This is very useful, for example, when working with databases: A query results can be used as a list of records 
(similar to every other java list) without needing to fetch thousands of useless records.

Again, implementing this List is easy: only one `fetch()` method has to be implemented, with an optional `count()` method
for performance (in case it is possible to know the number of elemnts without fetching them all - such as a database COUNT statement).

All operations (even adding/removing elements) are supported without ever fetching unnecessary elements (yes, even removing 
elements that aren't fetched yet), and work exactly as if all elements were fetched in the first place (minus the performance hit).
