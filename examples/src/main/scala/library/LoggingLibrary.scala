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

package library

object LoggingLibrary {
  def go {
    val jul = new org.scalawag.timber.api.style.jul.Logger("JUL")
    jul.finest("jul.finest")
    jul.finer("jul.finer")
    jul.fine("jul.fine")
    jul.config("jul.config")
    jul.info("jul.info")
    jul.warning("jul.warning")
    jul.severe("jul.severe")

    val log4j = new org.scalawag.timber.api.style.log4j.Logger("LOG4J")
    log4j.trace("slf4j.trace")
    log4j.debug("slf4j.debug")
    log4j.info("slf4j.info")
    log4j.warn("slf4j.warn")
    log4j.error("slf4j.error")
    log4j.fatal("slf4j.fatal")

    val slf4j = new org.scalawag.timber.api.style.slf4j.Logger("SLF4J")
    slf4j.trace("slf4j.trace")
    slf4j.debug("slf4j.debug")
    slf4j.info("slf4j.info")
    slf4j.warn("slf4j.warn")
    slf4j.error("slf4j.error")

    val syslog = new org.scalawag.timber.api.style.syslog.Logger("SYSLOG")
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
