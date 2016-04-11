tcProxy
=======

A simple TCP proxy and a few other pieces. The TCP proxy is embeddable in other Java applications. Two front ends are
provided a CLI and a GUI. The TCP proxy was originally implemented after using a TCP proxy in tests so it is possible
to do things not usually desirable such as dropping or delaying writes.

An alternative networking API is provided that wraps the Java standard library implementation. This was done to
simplify unit testing. The opportunity was taken to clean up some of the rough edges around the socket API. It is
focused on non-blocking I/O but does not differ significantly from the JSL.

Motivation
==========

A TCP proxy is not very original and there are many implementations. I inherited the code for one at work which wasn't
very good. We also had three different versions of it lying around. I spent a morning replacing them with a cleaner,
single implementation. I wasn't very happy with it though because it took the same approach to threading as the one it
replaced. Spawn two threads for each proxied connection.

I decided to implement a proxy that used Java's NIO library using non-blocking I/O to allow multiple connections to be
handled by the same threads.

Implementation
==============

There are eight separate modules. An I/O API module, an I/O implementation module, selector module, a proxy
implementation module, a command line interface  argument parsing module, a command line interface module, a graphical
user interface module and an examples module.

The TCP proxy uses Java NIO. The socket reads and writes do not block, accepting new connections still block. A single
thread is used to accepting connections, reading and writing data. Data read from a socket is placed on a queue
that is consumed by the thread responsible for writing data. Since very small amounts of data can be returned by NIO
reads, individual reads are batched before attempting to write them.

Modules
=======

tcProxy IO API
==============

Module that provides an API for performing socket I/O. It is focused on providing non-blocking operations. It provides
interfaces for classes responsible for performing network operations. It also provides some concrete classes for value
objects used to configure sockets.

tcProxy IO JSL
==============

Module that provides an implementation of the tcProxy IO API module that delegates to the Java Standard Library.

tcProxy Threads
================

Module that provides an extension to threads and workers that can be stopped and restarted.

tcProxy Selector
================

Module that provides a layer over the tcProxy IO API module to provide more friendly and common selector operations.

tcProxy Proxy
=============

Module that implements a simple reverse proxy server. It uses non-blocking I/O, selectors and a limited number of
threads to support multiple sockets.

tcProxy Graphical User Interface
================================

Module that provides a GUI frontend to the proxy server.

tcProxy Command Line Interface
==============================

Module that provides a CLI frontend to the proxy server.

tcProxy Command Line Argument Parser
====================================

Module that supports parsing CLI arguments.

tcProxy Examples
================

Module that provides examples of the other modules. It provides a HelloExample that shows how to use sockets from the
I/O API. It provides an EchoServer that shows how to build on top of the selectors module. It provides the examples
SimpleExample and BiDirectionalExampe to show the use of the proxy server.
