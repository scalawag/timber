// timber -- Copyright 2012-2021 -- Justin Patterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.scalawag.timber.api.level

import org.scalawag.timber.api.BaseLogger.LogCallLocation
import org.scalawag.timber.api.{Level, BaseLogger, Tag, Message}

/**
  * Mixes in `finest` methods to a [[BaseLogger]].
  */
trait Finest { _: BaseLogger =>

  /** Override to customize the level at which calls to `finest` create entries. */
  protected val finestLevel: Level = Level.FINEST
  def finest(message: Message)(implicit location: LogCallLocation): Unit =
    log(finestLevel, Set.empty)(message)(location)
  def finest(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(finestLevel, tags)(message)(location)
}

/**
  * Mixes in `finer` methods to a [[BaseLogger]].
  */
trait Finer { _: BaseLogger =>

  /** Override to customize the level at which calls to `finer` create entries. */
  protected[this] val finerLevel: Level = Level.FINER
  def finer(message: Message)(implicit location: LogCallLocation): Unit = log(finerLevel, Set.empty)(message)(location)
  def finer(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(finerLevel, tags)(message)(location)
}

/**
  * Mixes in `fine` methods to a [[BaseLogger]].
  */
trait Fine { _: BaseLogger =>

  /** Override to customize the level at which calls to `fine` create entries. */
  protected[this] val fineLevel: Level = Level.FINE
  def fine(message: Message)(implicit location: LogCallLocation): Unit = log(fineLevel, Set.empty)(message)(location)
  def fine(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(fineLevel, tags)(message)(location)
}

/**
  * Mixes in `trace` methods to a [[BaseLogger]].
  */
trait Trace { _: BaseLogger =>

  /** Override to customize the level at which calls to `trace` create entries. */
  protected[this] val traceLevel: Level = Level.TRACE
  def trace(message: Message)(implicit location: LogCallLocation): Unit = log(traceLevel, Set.empty)(message)(location)
  def trace(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(traceLevel, tags)(message)(location)
}

/**
  * Mixes in `debug` methods to a [[BaseLogger]].
  */
trait Debug { _: BaseLogger =>

  /** Override to customize the level at which calls to `debug` create entries. */
  protected[this] val debugLevel: Level = Level.DEBUG
  def debug(message: Message)(implicit location: LogCallLocation): Unit = log(debugLevel, Set.empty)(message)(location)
  def debug(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(debugLevel, tags)(message)(location)
}

/**
  * Mixes in `config` methods to a [[BaseLogger]].
  */
trait Config { _: BaseLogger =>

  /** Override to customize the level at which calls to `config` create entries. */
  protected[this] val configLevel: Level = Level.CONFIG
  def config(message: Message)(implicit location: LogCallLocation): Unit =
    log(configLevel, Set.empty)(message)(location)
  def config(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(configLevel, tags)(message)(location)
}

/**
  * Mixes in `info` methods to a [[BaseLogger]].
  */
trait Info { _: BaseLogger =>

  /** Override to customize the level at which calls to `info` create entries. */
  protected[this] val infoLevel: Level = Level.INFO
  def info(message: Message)(implicit location: LogCallLocation): Unit = log(infoLevel, Set.empty)(message)(location)
  def info(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(infoLevel, tags)(message)(location)
}

/**
  * Mixes in `notice` methods to a [[BaseLogger]].
  */
trait Notice { _: BaseLogger =>

  /** Override to customize the level at which calls to `notice` create entries. */
  protected[this] val noticeLevel: Level = Level.NOTICE
  def notice(message: Message)(implicit location: LogCallLocation): Unit =
    log(noticeLevel, Set.empty)(message)(location)
  def notice(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(noticeLevel, tags)(message)(location)
}

/**
  * Mixes in `warn` methods to a [[BaseLogger]].
  */
trait Warn { _: BaseLogger =>

  /** Override to customize the level at which calls to `warn` create entries. */
  protected[this] val warnLevel: Level = Level.WARN
  def warn(message: Message)(implicit location: LogCallLocation): Unit = log(warnLevel, Set.empty)(message)(location)
  def warn(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(warnLevel, tags)(message)(location)
}

/**
  * Mixes in `warning` methods to a [[BaseLogger]].
  */
trait Warning { _: BaseLogger =>

  /** Override to customize the level at which calls to `warning` create entries. */
  protected[this] val warningLevel: Level = Level.WARNING
  def warning(message: Message)(implicit location: LogCallLocation): Unit =
    log(warningLevel, Set.empty)(message)(location)
  def warning(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(warningLevel, tags)(message)(location)
}

/**
  * Mixes in `error` methods to a [[BaseLogger]].
  */
trait Error { _: BaseLogger =>

  /** Override to customize the level at which calls to `error` create entries. */
  protected[this] val errorLevel: Level = Level.ERROR
  def error(message: Message)(implicit location: LogCallLocation): Unit = log(errorLevel, Set.empty)(message)(location)
  def error(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(errorLevel, tags)(message)(location)
}

/**
  * Mixes in `severe` methods to a [[BaseLogger]].
  */
trait Severe { _: BaseLogger =>

  /** Override to customize the level at which calls to `severe` create entries. */
  protected[this] val severeLevel: Level = Level.SEVERE
  def severe(message: Message)(implicit location: LogCallLocation): Unit =
    log(severeLevel, Set.empty)(message)(location)
  def severe(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(severeLevel, tags)(message)(location)
}

/**
  * Mixes in `fatal` methods to a [[BaseLogger]].
  */
trait Fatal { _: BaseLogger =>

  /** Override to customize the level at which calls to `fatal` create entries. */
  protected[this] val fatalLevel: Level = Level.FATAL
  def fatal(message: Message)(implicit location: LogCallLocation): Unit = log(fatalLevel, Set.empty)(message)(location)
  def fatal(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(fatalLevel, tags)(message)(location)
}

/**
  * Mixes in `critical` methods to a [[BaseLogger]].
  */
trait Critical { _: BaseLogger =>

  /** Override to customize the level at which calls to `critical` create entries. */
  protected[this] val criticalLevel: Level = Level.CRITICAL
  def critical(message: Message)(implicit location: LogCallLocation): Unit =
    log(criticalLevel, Set.empty)(message)(location)
  def critical(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(criticalLevel, tags)(message)(location)
}

/**
  * Mixes in `alert` methods to a [[BaseLogger]].
  */
trait Alert { _: BaseLogger =>

  /** Override to customize the level at which calls to `alert` create entries. */
  protected[this] val alertLevel: Level = Level.ALERT
  def alert(message: Message)(implicit location: LogCallLocation): Unit = log(alertLevel, Set.empty)(message)(location)
  def alert(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(alertLevel, tags)(message)(location)
}

/**
  * Mixes in `emergency` methods to a [[BaseLogger]].
  */
trait Emergency { _: BaseLogger =>

  /** Override to customize the level at which calls to `emergency` create entries. */
  protected[this] val emergencyLevel: Level = Level.EMERGENCY
  def emergency(message: Message)(implicit location: LogCallLocation): Unit =
    log(emergencyLevel, Set.empty)(message)(location)
  def emergency(tags: TraversableOnce[Tag])(message: Message)(implicit location: LogCallLocation): Unit =
    log(emergencyLevel, tags)(message)(location)
}
