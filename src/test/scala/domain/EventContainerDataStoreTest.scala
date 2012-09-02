package domain

import application.InjectorFunSpec

class EventContainerDataStoreTest extends InjectorFunSpec {
  val dataStore = injector.getInstance(classOf[EventContainerDataStore])
  describe("The event container data store") {
    it ("should convert an event container into a data store entity and back again") {

    }
    it ("should save a new event container data store entity") (pending)
  }  
}
