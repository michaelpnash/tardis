package infrastructure

import akka.ClientInfo
import infrastructure.SerializableClient._
import org.scalatest.FreeSpec
import com.jglobal.tardis.ClientStats

class ClientInfoTest extends FreeSpec {
	"the client info object" - {
	  "can be serialized to json" in {
	    val result = ClientInfo.toJson(ClientDAO("id", Set(), Set()), ClientStats("id")).toString
	    assert(result.contains("""{"client":{"id":"id","subscribes":[],"publishes":[]},"stats":{"clientId":"id","eventsSentTo":{"count":0,"last":0},"acks":{"count":0,"last":0},"eventsReceivedFrom":{"count":0,"last":0}}}"""))
	  }
	}
}