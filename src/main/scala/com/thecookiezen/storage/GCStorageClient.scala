package com.thecookiezen.storage

import java.io.ByteArrayInputStream

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.storage.Storage
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.typesafe.scalalogging.StrictLogging

import scala.jdk.CollectionConverters._

case class Bucket(name: String)

trait StorageClient {
  def listProjectBuckets(): Iterator[Bucket]
}

class GCStorageClient(googleCloudConfig: GoogleCloudConfig) extends StorageClient with StrictLogging {

  val credentials: GoogleCredentials = GoogleCredentials
    .fromStream(new ByteArrayInputStream(googleCloudConfig.credentials.value.getBytes))
    .createScoped("https://www.googleapis.com/auth/devstorage.read_only")

  val requestInitializer = new HttpCredentialsAdapter(credentials)

  val storage: Storage = new Storage.Builder(GoogleNetHttpTransport.newTrustedTransport, JacksonFactory.getDefaultInstance, requestInitializer).build()

  override def listProjectBuckets(): Iterator[Bucket] = {
      val x = storage.buckets().list(googleCloudConfig.projectName).execute()
      x.getItems.iterator().asScala.map(b => Bucket(b.getName))
  }
}
