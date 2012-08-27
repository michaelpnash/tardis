tardis
======

Time-based event store

TARDIS is a project to provide the backbone for a lightweight zero-contention, highly scalable event store.

Events can be sourced from any application, and sent to the engine for distribution to listeners. All events are timestamped, so the store can regenerate state from any point in time as requested by listeners.

As the backbone of a CQRS system, TARDIS can recreate the state of objects at any point in time, and can be used as the event source for distributed models.

