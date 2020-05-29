package com.thecookiezen.gcp.dataflow

import java.io.ByteArrayInputStream

import cats.effect.IO
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.dataflow.Dataflow
import com.google.api.services.dataflow.model.GetTemplateResponse
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.thecookiezen.gcp.GoogleCloudConfig
import com.typesafe.scalalogging.StrictLogging

case class Template(name: String)

trait DataflowClient[F[_]] {
  def getTemplate(location: String): F[Template]
}

class GCPDataflowClient(googleCloudConfig: GoogleCloudConfig) extends DataflowClient[IO] with StrictLogging {

  val credentials: GoogleCredentials = GoogleCredentials.fromStream(new ByteArrayInputStream(googleCloudConfig.credentials.value.getBytes))
  val requestInitializer             = new HttpCredentialsAdapter(credentials)

  val dataflow: Dataflow = new Dataflow.Builder(GoogleNetHttpTransport.newTrustedTransport, JacksonFactory.getDefaultInstance, requestInitializer).build()

  override def getTemplate(location: String): IO[Template] =
    IO.delay {
        val response: GetTemplateResponse = dataflow
          .projects()
          .locations()
          .templates()
          .get(googleCloudConfig.projectName, googleCloudConfig.location)
          .setGcsPath(location)
          .execute()
        response
      }
      .map(t => Template(t.toPrettyString))
}
