---
layout: page
title: Levels
permalink: api/Levels
group: "timber-api"
---

Levels in timber are a bit different than levels in most other logging systems (sometimes known as "severity").
In timber, levels are treated internally as `Int`s for most purposes. Since they are normally just used to filter
entries in the logging backend configuration, relationships between levels are all that's really important.

Each level in timber also has a _name_, which is just a string representation of the level.  The logger assigns a
level (including a name) to its entries solely as a hint to the formatter that ends up converting the entry to text.
The formatter may or may not end up using the name at all.
If the entry ends up being handled by the timber backend, its formatters can decide whether to use the level's integer
value, the level's name, or ignore both and map the level to an entirely new string.  If the entry is handled by
another logging system's backend, it won't be taking advantage of the hint.  It probably doesn't even know its there.
The level name will probably be lost as part of the bridging process.

## Level Equivalency

Timber has a mapping that is used to relate its built-in levels to the levels of other logging systems.  This is
relevant whenever timber needs to map a level from one system to a level in another.

|int|timber     |log4j      |slf4j      |jul        |syslog     |
|:-:|:---------:|:---------:|:---------:|:---------:|:---------:|
| 10|           |           |           |`FINEST`   |           |
| 20|`TRACE`    |`TRACE`    |`TRACE`    |`FINER`    |           |
| 30|`DEBUG`    |`DEBUG`    |`DEBUG`    |`FINE`     |`DEBUG`    |
| 40|           |           |           |`CONFIG`   |           |
| 50|`INFO`     |`INFO`     |`INFO`     |`INFO`     |`INFO`     |
| 60|           |           |           |           |`NOTICE`   |
| 70|`WARN`     |`WARN`     |`WARN`     |`WARNING`  |`WARNING`  |
| 80|`ERROR`    |`ERROR`    |`ERROR`    |`SEVERE`   |`ERROR`    |
| 90|           |`FATAL`    |           |           |`CRITICAL` |
|100|           |           |           |           |`ALERT`    |
|110|           |           |           |           |`EMERGENCY`|

Some examples of when the mapping is used are when:

* one of the `org.scalawag.timber.api.style` loggers is used to mimic a logger from another system,
* entries are bridged from the timber API to another logging backend, or
* entries are bridged from another logging API to the timber backend.

For example, if an entry is logged at the `ALERT` level using a `syslog` style logger, the entry is assigned a level
equivalent to 100 in timber.  If that entry is then bridged to an slf4j backend, it will appear to the slf4j backend
as if it had been logged at the `ERROR` level.  That's because slf4j doesn't have enough levels to distinguish between
level 80 and level 100.

When there are not levels low enough to represent the entry's level, the lowest level is used.  So, an entry logged at
timber level 0 would appear, after bridging to a java.util.logging backend as `FINEST` (10) while it would appear to a
slf4j backend as `TRACE` (20).

The level naming strategy gives timber the ability to bridge entries from other logging APIs while maintaining their
original level names all the way to the log file (or whatever their final destination may be).  So, if you end up
using the timber logging backend with libraries that use java.util.logging and slf4j, the bridges can be configured
to assign `Level(20,"FINER")` for the former and `Level(20,"DEBUG")` for the latter.  Both of those entries will be
filtered similarly by the dispatcher but will also maintain the level name from their original logging API.

There is some space left between the built-in levels to allow room for additions without having to move everything
around.  Of course, if you choose to, you can also just move everything around!

## Implicit Conversions

Since a level is essentially just an integer, timber provides implicit conversions from `Level` to and from `Int`.
To convert a `Level` to an `Int`, the level's `intValue` is used.  To convert an `Int` to a `Level` a new level is
created with the expected intValue and no name.  Levels with no name just use the string representation of the integer.
