package com.thecookiezen.dataflow

import java.io.ByteArrayInputStream

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.dataflow.Dataflow
import com.google.api.services.dataflow.model.GetTemplateResponse
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.typesafe.scalalogging.StrictLogging

case class Template(name: String)

trait DataflowClient {
  def getTemplate(location: String): Task[Template]
}

class GCPDataflowClient(googleCloudConfig: GoogleCloudConfig) extends DataflowClient with StrictLogging {

  val credentials: GoogleCredentials = GoogleCredentials.fromStream(new ByteArrayInputStream(googleCloudConfig.credentials.value.getBytes))
  val requestInitializer = new HttpCredentialsAdapter(credentials)

  val dataflow: Dataflow = new Dataflow.Builder(GoogleNetHttpTransport.newTrustedTransport, JacksonFactory.getDefaultInstance, requestInitializer).build()

  override def getTemplate(location: String): Task[Template] = {
    Task {
      val response: GetTemplateResponse = dataflow
        .projects()
        .locations()
        .templates()
        .get(googleCloudConfig.projectName, googleCloudConfig.location)
        .setGcsPath(location)
        .execute()
      response
    }.map(t => Template(t.toPrettyString))
  }
}
