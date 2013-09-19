package controllers

import domain.ClientRepository
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

class Application(clientRepo: ClientRepository) extends Controller {

  def index = Action {
    implicit request =>
      Ok(clientRepo.list.mkString("\n"))
  }
}
