package com.thecookiezen

case class GoogleCloudConfig(credentials: GoogleCloudCredentials, projectName: String, location: String)

case class GoogleCloudCredentials(value: String) extends AnyVal
