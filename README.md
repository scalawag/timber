# timber

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/scalawag/timber.svg?branch=develop)](https://travis-ci.org/scalawag/timber)
[![Coverage Status](https://coveralls.io/repos/scalawag/timber/badge.svg?branch=develop&service=github)](https://coveralls.io/github/scalawag/timber?branch=develop)

Timber is a logging system for Scala applications. It is written in Scala and takes advantage of its unique language features. It's not a wrapper for an existing Java logging library.

For more information, check out :

* the [documentation site](http://scalawag.org/timber/)
* the [API docs](http://scalawag.org/timber/docs/timber-api/) for the timber logging API
* the [API docs](http://scalawag.org/timber/docs/timber-backend/) for the timber logging backend

## Building from Source

### Building the Documentation

To build the documentation site, you need to install the Jekyll Ruby gem:
```
sudo gem install jekyll
```

Then, to build the documentation site:
```
sbt makeSite
```
