TARDIS
==============

Tardis is a simple distributed event router.

It allows your applications to decouple in both space and time from each other - in other words, it allows applications to communicate without having to be concerned about where the other application is, or when it might receive the communications.

This is an ideal foundation for writing server-side reactive applications (http://www.reactivemanifesto.org), for instance.

The communication between applications is in the form of events, that is, descriptions of occurrences in the past tense, such as "customer logged in" or "invoice paid".

Applications emit these events without needing to know about the consumers.

Tardis guarantees delivery of events eventually (usually within milliseconds) to all registered subscribers.
  