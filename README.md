TARDIS
==============

Tardis is a simple *distributed event router*.

It allows your applications modules to decouple in both space and time from each other - in other words, it allows applications to communicate without having to be concerned about *where* the other application is, or *when* it might receive the communications.

This is an ideal foundation for writing server-side reactive applications (http://www.reactivemanifesto.org), for instance.

The communication between applications is in the form of events, that is, descriptions of occurrences in the past tense, such as "customer logged in" or "invoice paid".

Applications emit these events without needing to know about the consumers.

Tardis guarantees delivery of events eventually (usually within milliseconds) to all registered subscribers.

Purpose
-------
When writing applications in a reactive style, you often need asynchronous event passing between executable applications: that's
what Tardis does, as simply as possible.

Events
------
Tardis transports *events* from a *publisher* to zero or more *subscribers*. The events themselves are identified by an event *type* (just a string), and have a *payload*. The payload is also a string, usually a serialized form of JSON, but it can be whatever you need.

This eliminates any need for fancy serialization/deserialization, and makes the whole system easy to diagnose (as strings can be made human-readable, especially JSON, without much effort).

Errors and Throttling
----------------------
In the event of a client publishing an event and the Tardis server failing to persist it, the client will not receive an acknowledgement of the publish, so it can retry, fail, or do whatever it should in that situation.

In the case of a client not successfully handling an event it subscribes to, it must not acknowledge that event to the Tardis server,
which will then resent the event (after a delay). If one event is not handled, it will not prevent other events from being sent, so
transient errors just create a "backlog" of unhandled events on the Tardis server.

Periodically (every 30 seconds by default, tunable), the client will send a message to the Tardis server declaring it's
client id (also a simple string) and what event types it subscribes to. The Tardis server will remove clients from it's list if
it hasn't hear from them in a short period of time, and not attempt to send events to them until they re-ping the server in
this way.

If a client is very busy processing events, then, it simply doesn't ping the server again until it's inbox is empty, at which
time any missed or timeed-out events are resent.

This provides a kind of built-in throttling, which turns out to be quite effective at not creating massive inboxes on the client applications.

It also means that the Tardis server doesn't need to know where clients are, it only needs to understand the client id and the
list of event types that client subscribes to: when a client with that id connects (and more than one node can use the same id), it will begin delivery of events to that node as long as it remains current.

Multiple nodes can share a client id, and the events for that id will be distributed amongst them - only one node of that id will
receive any one event (except in the case of a retry). This allows consuming clients to be easily clustered.

Internals
---------
Tardis is a Play 2.2 application, written in Scala and using Akka extensively under the hood.

It doesn't use a database, it uses the filesystem, and writes it's data in a simple human-readable JSON format to one common directory (which can be configured in the application.conf file to be wherever you like).

When you send Tardis an event, it stores it, then acknowledges the send to the publisher and starts trying to deliver that event
to any and all subscribers (via remote actor calls). If the server is stopped or crashes, it will keep trying to deliver
the event once it's brought back up, hence the need to persist the message itself.

Client
------
To publish or subscribe to events with Tardis, your application needs a *client*, that is, a small jar file on it's classpath
that defines a number of simple case classes also used by Tardis, and providing an easy connection point to the server.

You can build the client by using the "sbt" script in this directory, then say "project client" and "package". The jar file will be in the client/target directory.

You can also run a command-line runner for the client: say "project client" and "run" to start it from within SBT. Use "help" to get a list of available commands. This is a great way to test out the client, and the code serves as an example for your own client (until I get around to writing a good example app, that is).

Clients can then publish events and subscribe to events (or both).

Status Page
------------
The Tardis server is a Play application, and as such, presents an HTTP user interface on a port, by default 8000, when you run it.

This interface consists of a simple status page (more sophisticated one in the works), that gives you a list of clients
that are connected (or have been recently) and how many events have been sent to or received from them.

The status page uses server-side events, so it updates in near-real-time to show current counts.

This is handy when setting up a cluster to ensure each client is connecting properly.

Why Not...
==========
Tardis solves a problem that is similar to that which many different communication and middleware systems solve, so it's reasonable
to ask a number of "Why Not...X" when understanding how Tardis fits in the picture.

Why Not REST calls to other services?
-------------------------------------
Direct calls, either Socket-based or HTTP/REST based, are synchronous and blocking. Tardis isn't, either for publishing or subscribing.
Direct calls also require that each service be able to locate every other service, and to have knowledge of every other service that it needs to call, Tardis doesn't have this requirement.

Why Not JMS? RabbitMQ? ZeroMQ?
------------------------------
ActiveMQ and other JMS and AMQP solutions are excellent tools, and I've used them a lot. They are, however, more complex and heavyweight than Tardis, and support both queueing and publish/subscribe (topics). Tardis ONLY does publish subscribe, and is very lightweight (and based on Akka's remote actor protocol).


Roadmap
-------
* Sample client application, maybe a Typesafe Activator to build a client
* Server clustering: the client proxy should take a list of addresses, each of which is a Tardis server, and distribute it's publish calls between them.
* Better doc, diagrams, etc
  