tcProxy
=======

A simple TCP proxy.

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

There are severn separate modules. An I/O API module, an I/O implementation module, a proxy implementation module, a
command line interface  argument parsing module, a command line interface module, a graphical user interface module and
an examples module.

The TCP proxy uses Java NIO. The socket reads and writes do not block, accepting new connections still block. There is
a separate thread for accepting connections, reading and writing data. Data read from a socket is placed on a queue
that is consumed by the thread responsible for writing data. Since very small amounts of data can be returned by NIO
reads, individual reads are batched before attempting to write them.
