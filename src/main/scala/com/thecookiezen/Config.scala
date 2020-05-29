package com.thecookiezen

import cats.effect.{Blocker, ContextShift, IO}
import com.thecookiezen.gcp.GoogleCloudConfig
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

case class Config(gcp: GoogleCloudConfig)

object Config {
  def load(blocker: Blocker)(implicit cs: ContextShift[IO]): IO[Config] =
    ConfigSource.defaultApplication.loadF[IO, Config](blocker)
}
