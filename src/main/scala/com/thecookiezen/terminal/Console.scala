package com.thecookiezen.terminal

import cats.implicits._
import cats.{Monad, Show}
import com.softwaremill.quicklens._
import com.thecookiezen.Config
import com.thecookiezen.Main.Environment
import com.thecookiezen.gcp.storage.Bucket
import com.thecookiezen.terminal.Commands._
import com.thecookiezen.terminal.Messages.{showBucket, showConfig}

object Commands {
  sealed trait Command
  case object Quit              extends Command
  case object ShowHelp          extends Command
  case object ShowConfiguration extends Command
  case object ListBuckets       extends Command
  case object InvalidInput      extends Command
}

object Messages {
  val intro: String =
    """|
       | Hello in Dataflow Control Plane
       | Type :? for help
       |""".stripMargin

  val help: String =
    """
      |  Command      Arguments     Purpose
      |
      |  :b :buckets                Lists all the buckets assigned in the GCP project
      |  :c :config                 Display current configuration
      |  :q :quit                   Exit the app
      |  :? :h :help                Display this help text
      |""".stripMargin

  val invalidInputMsg: String =
    """
      |Unrecognised command or option
      |Type :? for help""".stripMargin

  implicit val showList: Show[List[_]]  = Show.show(list => list.mkString("\n"))
  implicit val showBucket: Show[Bucket] = Show.show(b => b.toString)
  implicit val showConfig: Show[Config] = Show.show(config => config.modify(_.gcp.credentials.value).using(_ => "*******").toString)
}

object InputParser {
  import atto.Atto._
  import atto._

  val quit: Parser[Command]    = (string("quit") | string("q")) >| Quit
  val config: Parser[Command]  = (string("config") | string("c")) >| ShowConfiguration
  val buckets: Parser[Command] = (string("buckets") | string("b")) >| ListBuckets
  val help: Parser[Command]    = (string("?") | string("h") | string("help")) >| ShowHelp
  val cmd: Parser[Command]     = char(':') ~> choice(quit, help, config, buckets)

  def parse(input: String): Command =
    cmd
      .parse(input.trim)
      .done
      .either
      .leftMap(_ => InvalidInput)
      .merge
}

trait Console[F[_]] {
  val putStrLn: String => F[Unit]
  val putStr: String => F[Unit]
  val readLn: F[String]
}

class Terminal[F[_]: Monad](console: Console[F]) {
  def init: Environment[F] => F[Command] = { env =>
    val storage = env.storage

    def loop(msg: String): F[Command] =
      for {
        command <- promptWithMsg(msg)
        result <- command match {
                   case Quit              => Monad[F].pure(Quit)
                   case ShowHelp          => loop(Messages.help)
                   case ShowConfiguration => loop(Show[Config].show(env.config))
                   case InvalidInput      => loop(Messages.invalidInputMsg)
                   case ListBuckets =>
                     storage.listProjectBuckets().flatMap { b =>
                       loop(Show[List[Bucket]].show(b))
                     }
                 }
      } yield result

    loop(Messages.intro)
  }

  private val prompt: F[Command] =
    for {
      _        <- console.putStr("control-plane> ")
      strInput <- console.readLn
    } yield InputParser.parse(strInput)

  private val promptWithMsg: String => F[Command] = msg =>
    for {
      _   <- console.putStrLn(msg)
      msg <- prompt
    } yield msg
}
