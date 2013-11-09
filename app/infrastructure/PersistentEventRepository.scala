package infrastructure

import com.jglobal.tardis.EventContainer
import domain.EventRepository

import java.util.UUID
import java.io.{File, PrintWriter}
import scala.io.Source._

class PersistentEventRepository(eventDir: EventDirectory) extends EventRepository {
  
  override def store(container: EventContainer): EventContainer = {
    println(s"Storing event $container in persistent event repo")
    Some(new PrintWriter(s"${eventDir.dir.getPath}/${container.id.toString}")).foreach{p => p.write(SerializableEventContainer.toStr(container)); p.close}
    container
  }
  override def find(id: UUID): Option[EventContainer] = {
    val eventFilePath = s"${eventDir.dir.getPath}/events/${id.toString}"
    val eventFile = new File(eventFilePath)
    if (eventFile.exists())
      Some(SerializableEventContainer.fromStr(fromFile(eventFile).getLines.mkString("\n")))
    else None
  }
}

