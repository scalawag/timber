package library

object LoggingLibrary {
  def go {
    val jul = org.scalawag.timber.api.style.jul.LoggerFactory.getLogger("JUL")
    jul.finest("jul.finest")
    jul.finer("jul.finer")
    jul.fine("jul.fine")
    jul.config("jul.config")
    jul.info("jul.info")
    jul.warning("jul.warning")
    jul.severe("jul.severe")

    val log4j = org.scalawag.timber.api.style.log4j.LoggerFactory.getLogger("LOG4J")
    log4j.trace("slf4j.trace")
    log4j.debug("slf4j.debug")
    log4j.info("slf4j.info")
    log4j.warn("slf4j.warn")
    log4j.error("slf4j.error")
    log4j.fatal("slf4j.fatal")

    val slf4j = org.scalawag.timber.api.style.slf4j.LoggerFactory.getLogger("SLF4J")
    slf4j.trace("slf4j.trace")
    slf4j.debug("slf4j.debug")
    slf4j.info("slf4j.info")
    slf4j.warn("slf4j.warn")
    slf4j.error("slf4j.error")

    val syslog = org.scalawag.timber.api.style.syslog.LoggerFactory.getLogger("SYSLOG")
    syslog.debug("syslog.debug")
    syslog.info("syslog.info")
    syslog.notice("syslog.notice")
    syslog.warning("syslog.warning")
    syslog.error("syslog.error")
    syslog.critical("syslog.critical")
    syslog.alert("syslog.alert")
    syslog.emergency("syslog.emergency")
  }
}

/* timber -- Copyright 2012 Justin Patterson -- All Rights Reserved */
