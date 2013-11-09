package infrastructure

import domain.{TransientClientRepository, Client}

import scala.io.Source._
import java.io.{File, PrintWriter}

class PersistentClientRepository(clientDir: ClientDirectory) extends TransientClientRepository {

  initialLoad

  def initialLoad {
    clients ++= clientDir.dir.listFiles().toList.filter(_.isFile).map(file => {
      val client = SerializableClient.fromStr(fromFile(file).getLines.mkString("\n"))
      (client.id, client)
    })
  }
  
  override def store(client: Client): Client = {
    Some(new PrintWriter(s"${clientDir.dir.getPath}/${client.id}")).foreach{p => p.write(SerializableClient.toStr(client)); p.close}

    super.store(client)
  }
}

