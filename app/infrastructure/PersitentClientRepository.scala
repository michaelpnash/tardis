package infrastructure

import domain.{TransientClientRepository, Client}

import scala.io.Source._
import java.io.{File, PrintWriter}

case class ClientDirectory(str: String)

class PersistentClientRepository(path: ClientDirectory) extends TransientClientRepository {
  
  require(!path.str.endsWith("/"), s"Path must not end with a /, but $path does")
  val clientDir = new File(s"$path/clients/")
  if (!clientDir.exists) clientDir.mkdirs()
  assert(clientDir.exists && clientDir.isDirectory, s"Directory ${clientDir.getPath} does not exist or is not a directory!")
  assert(clientDir.canRead && clientDir.canWrite, s"Directory ${clientDir.getPath} cannot be read from and written to!")

  initialLoad

  def initialLoad {
    clients ++= clientDir.listFiles().toList.filter(_.isFile).map(file => {
      val client = SerializableClient.fromStr(fromFile(file).getLines.mkString("\n"))
      (client.id, client)
    })
  }
  
  override def store(client: Client): Client = {
    Some(new PrintWriter(s"${clientDir.getPath}/${client.id}")).foreach{p => p.write(SerializableClient.toStr(client)); p.close}

    super.store(client)
  }
}

