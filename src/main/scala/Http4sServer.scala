import java.util.Collections

import cats.data.OptionT
import com.google.api.client.googleapis.auth.oauth2.{GoogleIdToken, GoogleIdTokenVerifier}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import fs2.Task
import fs2.interop.cats._
import io.circe.Json
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze._
import org.http4s.twirl.TwirlInstances
import org.http4s.util.StreamApp
import play.twirl.api.Html

object Http4sServer extends StreamApp with TwirlInstances {

  lazy val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport
  lazy val jsonFactory: JacksonFactory = JacksonFactory.getDefaultInstance

  val GOOGLE_API_KEY = "<ADD YOUR API KEY HERE>.apps.googleusercontent.com"

  val indexService = HttpService {
    case GET -> Root =>
      val idx: Html = html.index(GOOGLE_API_KEY)
      Ok(idx)
  }

  val tokenService = HttpService {
    case request@POST -> Root / "tokensignin" =>

      import io.circe.generic.auto._
      import io.circe.syntax._
      import org.http4s.circe._

      case class AuthResponse(userId: Option[String], name: Option[String])

      val verifier: GoogleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
        .setAudience(Collections.singletonList(GOOGLE_API_KEY))
        .build()

      val reqform: Task[UrlForm] = request.as[UrlForm]

      val result: OptionT[Task, Json] = for {
        tokenString: String <- OptionT(reqform.map(form => form.getFirst("idtoken")))

        verfiedToken: GoogleIdToken = verifier.verify(tokenString)
        payload = verfiedToken.getPayload

        name = payload.get("name").asInstanceOf[String]
        userId = payload.getSubject
        authResponse = AuthResponse(Option(name), Option(userId))

        jsonResp: Json = authResponse.asJson
      } yield jsonResp


      result.semiflatMap(r => Ok(r))
          .getOrElseF(BadRequest("Unable to complete Google Auth"))
  }

  val builder = BlazeBuilder.bindHttp(8080, "localhost").mountService(indexService, "/").mountService(tokenService, "/")

  override def stream(args: List[String]): fs2.Stream[Task, Unit] = builder.serve
}
