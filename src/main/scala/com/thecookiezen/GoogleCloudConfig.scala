package com.thecookiezen

import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import cats.effect.{ Blocker, ContextShift, IO }

case class Config(gcp: GoogleCloudConfig)
case class GoogleCloudConfig(credentials: GoogleCloudCredentials, projectName: String, location: String)

case class GoogleCloudCredentials(value: String) extends AnyVal

object GoogleCloudConfig {
  def load(blocker: Blocker)(implicit cs: ContextShift[IO]): IO[Config] = {
    ConfigSource.defaultApplication.loadF[IO, Config](blocker)
  }
}
