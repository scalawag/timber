---
layout: page
title: Advanced Usage
permalink: api/AdvancedUsage
group: "timber-api"
---

## Creating Your Own Custom Level Methods

If the built-in level mixins don't satisfy your need to be different, you can create your own traits similar to the
included ones.  Here's an example of creating a super low-level method called `whisper`.

~~~~
import org.scalawag.timber.api._
import org.scalawag.timber.api.level._
import org.scalawag.timber.api.BaseLogger._

trait Whisper { _: BaseLogger =>
  /** Override to customize the level at which calls to `whisper` create entries. */
  protected[this] val whisperLevel:Level = 0
  def whisper(message:Message)(implicit location:LogCallLocation):Unit = log(whisperLevel,Set.empty)(message)(location)
  def whisper(tags:TraversableOnce[Tag])(message:Message)(implicit location:LogCallLocation):Unit = log(whisperLevel,tags)(message)(location)
}

val log = new BaseLogger with Emergency with Fatal with Finest with Whisper
~~~~
{: .language-scala}

Of course, you don't need to use this pattern.  This may just be easier because it's copy-and-paste from the built-in
mixin level traits.  It also allows you to mix in other built-in traits as well.  You could just as easily subclass
`BaseLogger` and add whatever methods you want, ignoring the above pattern altogether.

~~~~
import org.scalawag.timber.api._

class MyLogger extends BaseLogger {
  def scream() = log(10000)("AAAARGH!")
}

val log = new MyLogger
log.scream()
~~~~
{: .language-scala}
