package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

class Application() extends Controller {

  def index = Action {
    implicit request =>
      Ok("Ok")
  }
}
