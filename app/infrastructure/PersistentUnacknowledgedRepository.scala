package infrastructure

import domain._
import akka.actor._
import com.jglobal.tardis.EventContainer

import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PersistentUnacknowledgedRepository(path: String, clientRepo: ClientRepository, eventRepository: EventRepository) extends UnacknowledgedRepository(clientRepo) {
  
  require(!path.endsWith("/"), s"Path must not end with a /, but $path does")
  val unackDir = new File(path + "/unacknowledged/")
  if (!unackDir.exists) unackDir.mkdirs()
  assert(unackDir.exists && unackDir.isDirectory, s"Directory ${unackDir.getPath} does not exist or is not a directory!")
  assert(unackDir.canRead && unackDir.canWrite, s"Directory ${unackDir.getPath} cannot be read from and written to!")

  initialLoad

  def initialLoad { //TODO: Probably want a limit on how many we attempt to load

    unackDir.listFiles().toList.filter(_.isDirectory).foreach(clientDir => {
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
    val target  = new File(s"${unackDir.getPath}/${clientAndEventId.clientId}/${clientAndEventId.eventId}")
    if (target.exists) target.delete
  }

  override def store(clientAndEventId: ClientIdAndEventId, containerAndTimeStamp: EventContainerAndTimeStamp) {
    super.store(clientAndEventId, containerAndTimeStamp)
    val dir = new File(s"${unackDir.getPath}/${clientAndEventId.clientId}")
    if (!dir.exists) dir.mkdirs()
    val unackFile = new File(s"${dir.getPath}/${clientAndEventId.eventId}")
    new FileOutputStream(unackFile).close()
    unackFile.setLastModified(containerAndTimeStamp.timestamp)
  }
}


