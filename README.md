# TARDIS

A Time-based event store

TARDIS is a project to provide the backbone for a lightweight zero-contention, highly scalable event store.

Events can be sourced from any application, and sent to the engine for distribution to listeners. All events are timestamped, so the store can regenerate state from any point in time as requested by listeners.

As the backbone of a CQRS system, TARDIS can recreate the state of objects at any point in time, and can be used as the event source for distributed models.

## Building

Run the included ./sbt script to launch sbt, then use the usual sbt commands, e.g. test. Use "assembly" to generate the executable jar.

If you don't use Ensime, just comment out the plugin for ensime-sbt-cmd, otherwise you'll have to get the source for that plugin and publish-local it as well.

## Configuration

## Running


