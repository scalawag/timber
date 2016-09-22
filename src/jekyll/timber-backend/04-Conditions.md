---
layout: page
title: Conditions
permalink: timber-backend/Conditions
group: "timber-backend"
---

Conditions are objects that evaluate [entries](Entries) for acceptance.  They are an important part of a timber
[configuration graph](ConfigurationDSL). The result of a condition's evaluation is ternary:

 - __accept__ - The condition holds true for the entry.
 - __reject__ - The condition does not hold true for the entry.
 - __abstain__ - There's not enough information in the entry to know whether the condition holds true or not.

You can either take advantage of the built-in conditions, which will probably meet most of your needs, or create
your own.

## Built-in Conditions

There are currently four types of conditions built-in to timber:

 - `Int` conditions
 - `String` conditions
 - `Tag` conditions
 - Logical conditions

### Int Conditions

Int conditions support the following operations:

 - `<=`
 - `<`
 - `===` / `is`
 - `>`
 - `>=`

Hopefully, these are self-explanatory.

The use of `===` versus `is` is entirely up to you.  They behave exactly the same.  Choosing one over the other
depends on whether you'd rather be able to read your timber configuration like English or rather spend hours trying
to figure out that you only put two equals signs in your configuration instead of three. :)

#### Level

The only `Int` field currently built into timber is the `level` field.  You can use it to create a condition based
on the level of the entry.  If the entry does not have a level (`entry.level == None`), the condition will abstain.
The RHS of a level condition operator must be a `Level`.  It can be one of the standard levels or a level that
you've defined yourself. You can also specify the comparison level using an `Int` and it will be converted to a
`Level` for you.  All comparisons are done based on the integer value of the levels.  The names are completely
ignored.

#### Examples

~~~~
import org.scalawag.timber.api.level.Level._
import org.scalawag.timber.backend.dispatcher.configuration.dsl._

level > 5
level >= INFO
level is ERROR
~~~~
{: .language-scala}

### String Conditions

String conditions support the following operations:

 - `is` / `===` / `matches`
 - `contains`
 - `startsWith`
 - `endsWith`

The argument for each is a pattern that can be specified either as a `String`, a `scala.Regex` or a
`java.util.Pattern`.  The operators probably do exactly what they sound like they do.

Fields that are currently available to create `String` conditions are:

 - `loggingClass` - the name of the class from which the log method was called
 - `message` - the text of the message
 - `sourceFile` - the source file from which the log method was called
 - `logger(`_`attr`_`)` - the value of the logger attribute named `attr`
 - `thread(`_`attr`_`).any` - any value on the stack for the thread attribute named `attr`
 - `thread(`_`attr`_`).top` - the top value on the stack for the thread attribute named `attr`
 - `thread.name` - the name of the thread from which the log method was called

All of these are optional (may not be present in the entry) except for `thread.name`.  If the field does not have a
value for a given entry, all conditions will _abstain_ when evaluating the entry.

#### Examples

~~~~
import org.scalawag.timber.backend.dispatcher.configuration.dsl._

loggingClass startsWith "org.scalawag.timber"
message contains "[Ee][Rr][Rr][Oo][Rr]".r
logger("clientIpAddress") is "127.0.0.1"
thread.name endsWith "-test"
thread("subsystem").any is "db
~~~~
{: .language-scala}

### Tag Conditions

Tag conditions are not so much a group as a single condition.  You can use a Tag condition to determine if an entry
has been tagged with a specific Tag.

#### Example

~~~~
import org.scalawag.timber.backend.dispatcher.configuration.dsl._

object MyTag extends Tag

tagged(MyTag)
~~~~
{: .language-scala}

### Logical Conditions

In addition to the above conditions, timber also allows you to combine conditions to make more complex decisions
using logical operations on other conditions.  The logical operations that timber supports are:

 - `and` / `&&`
 - `or` / `||`
 - `not` / `!`

Again, they should do what you expect them to.  The only thing that may need clarification is that if any operand of
a logical condition abstains, the logical condition itself abstains. The English words and operators are equivalent.
They should operate exactly the same.  Which one to use is up to you, based on your style preferences. Scala's
operator precedence may also help you decide on one versus the other, as the rules are different for symbols versus letters.

#### Examples

~~~~
import org.scalawag.timber.backend.dispatcher.configuration.dsl._

object AlertTag extends Tag

! tagged(AlertTag)
( level > INFO ) or ( loggingClass startsWith "org.scalawag" )
~~~~
{: .language-scala}

## Custom Conditions

### From Scratch

Conditions are represented by the `org.scalawag.timber.backend.dispatcher.configuration.dsl.Condition` trait.
The single abstract method it declares is:

~~~~
def accepts(entry:EntryFacets):Option[Boolean]
~~~~
{: .language-scala}

This method should return one of the following three responses.

 - `Some(true)` - accept
 - `Some(false)` - reject
 - `None` - abstain

It's important that you handle the abstain case (`None`) properly because it's used internally by timber to optimize
the configuration graph.  It may pass a hypothetical `EntryFacets` to your condition prior to entry evaluation during
dispatch to determine whether it can prune or bypass a path in the DAG.

### Piggy-back Implementations

If you just want to create `Int` or `String` conditions based on a field that doesn't currently have an extractor
in timber, you can piggy-back on some of the existing timber code both to make your job easier and to ensure that
it behaves the same as other timber condition fields of the same type (i.e., supporting the same comparison operators).

Here's an example of using timber's `IntConditionFactory` to create a new field that contains the depth of a
thread attribute stack.

~~~~
val loggerAttributeCount = IntConditionFactory("loggerAttributeCount") { entry =>
  entry.loggerAttributes.map(_.size)
}
~~~~
{: .language-scala}

All you have to provide is an extractor function that extracts your field's value from a partial entry.  This
function should return `None` if the field is absent from the entry and `Some` if it has a definite value.  Note
that this condition field is not very useful.  That's why it's not built into timber!
