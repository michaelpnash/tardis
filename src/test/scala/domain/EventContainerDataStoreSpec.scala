package domain

import application.InjectorFunSpec
import org.joda.time._

class EventContainerDataStoreSpec extends InjectorFunSpec {
  val dataStore = injector.getInstance(classOf[EventContainerDataStore])
  describe("The event container data store") {
    it ("should save a new event container data store entity") {
      val eventContainer = EventContainer("test data".getBytes, SenderIdentifier("foo"), EventIdentifier("x"), new DateTime)
      dataStore.save(eventContainer)
    }
  }  
}
