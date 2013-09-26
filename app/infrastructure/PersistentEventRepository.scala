package infrastructure

import com.jglobal.tardis.EventContainer
import domain.EventRepository

import java.util.UUID
import java.io.{File, PrintWriter}
import scala.io.Source._

class PersistentEventRepository(path: String) extends EventRepository {
  require(!path.endsWith("/"), s"Path must not end with a /, but $path does")
  val eventDir = new File(s"$path/events/")
  if (!eventDir.exists) eventDir.mkdirs()
  assert(eventDir.exists && eventDir.isDirectory, s"Directory ${eventDir.getPath} does not exist or is not a directory!")
  assert(eventDir.canRead && eventDir.canWrite, s"Directory ${eventDir.getPath} cannot be read from and written to!")
  
  override def store(container: EventContainer): EventContainer = {
    Some(new PrintWriter(s"${eventDir}/${container.id}")).foreach{p => p.write(SerializableEventContainer.toStr(container)); p.close}
    container
  }
  override def find(id: UUID): Option[EventContainer] = {
    val eventFilePath = s"$eventDir/${id.toString}"
    val eventFile = new File(eventFilePath)
    if (eventFile.exists())
      Some(SerializableEventContainer.fromStr(fromFile(eventFile).getLines.mkString("\n")))
    else None
  }
}

