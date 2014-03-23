Proxy
=====

Core proxy implementation. Deals with creating a connection and sending data between two endpoints.

Thread Model
============

There are three threads. The acceptor thread, the read selector thread and the write selector thread.

The acceptor waits for new inbound TCP connections and creates a proxied connection to the endpoint.

The read selector reads data from and queues the read data as actions.

The write selector takes actions from the queue and writes data to the other side of the proxied connection.

Connection Model
================

Each connection consists of two directions. Each direction consists of an inbound socket channel, an outbound socket
channel and an action queue. The two directions of a connection are opposite, inbound socket channel of one direction is
the outbound socket channel of the other.

For both directions data is read from the inbound socket channel, turned into a queued action and written to the
outbound socket channel.

Actions
=======

When data is read from the inbound socket channel of a direction it is turned into an action. These actions are passed
to the processor chain for the direction. The processor chain consists of a series of action processors that call the
next. This allows the behaviour of direction to be built up. The default action is to put the action on the directions
queue and if the queue was empty to put the direction on a queue of new directions.