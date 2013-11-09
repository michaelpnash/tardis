package infrastructure

import domain._
import akka.actor._
import com.jglobal.tardis.EventContainer

import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PersistentUnacknowledgedRepository(dir: UnacknowledgedDirectory, clientRepo: ClientRepository, eventRepository: EventRepository) extends UnacknowledgedRepository(clientRepo) {
  
  initialLoad

  def initialLoad { //TODO: Probably want a limit on how many we attempt to load

    dir.dir.listFiles().toList.filter(_.isDirectory).foreach(clientDir => {
      clientDir.listFiles().toList.filter(_.isFile).foreach(eventFile => {
        val correspondingEvent: EventContainer = eventRepository.find(UUID.fromString(
          eventFile.getName)).getOrElse(throw new IllegalArgumentException(
            s"No such event stored with id ${UUID.fromString(eventFile.getName)} for client ${clientDir.getName}"))
        super.store(ClientIdAndEventId(clientDir.getName, UUID.fromString(eventFile.getName)),
          EventContainerAndTimeStamp(correspondingEvent, eventFile.lastModified))
      })
    })
  }

  override def remove(clientAndEventId: ClientIdAndEventId) {
    super.remove(clientAndEventId)
    val target  = new File(s"${dir.dir.getPath}/${clientAndEventId.clientId}/${clientAndEventId.eventId}")
    if (target.exists) target.delete
  }

  override def store(clientAndEventId: ClientIdAndEventId, containerAndTimeStamp: EventContainerAndTimeStamp) {
    super.store(clientAndEventId, containerAndTimeStamp)
    val target = new File(s"${dir.dir.getPath}/${clientAndEventId.clientId}")
    if (!target.exists) target.mkdirs()
    val unackFile = new File(s"${target.getPath}/${clientAndEventId.eventId}")
    new FileOutputStream(unackFile).close()
    unackFile.setLastModified(containerAndTimeStamp.timestamp)
  }
}


