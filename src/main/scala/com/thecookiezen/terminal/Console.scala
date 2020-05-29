package com.thecookiezen.terminal

import cats.Monad
import cats.implicits._
import com.thecookiezen.Config
import com.thecookiezen.terminal.Commands._

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

  val displayConfig: Config => String = _.toString()

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
  def init(config: Config): F[Command] = loop(config)(Messages.intro)

  private def loop(config: Config): String => F[Command] =
    msg =>
      for {
        command <- promptWithMsg(msg)
        result <- command match {
                   case ShowHelp          => loop(config)(Messages.help)
                   case ShowConfiguration => loop(config)(Messages.displayConfig(config))
                   case InvalidInput      => loop(config)(Messages.invalidInputMsg)
                   case Quit              => Monad[F].pure(Quit)
                 }
      } yield result

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

  private val quit: Command => Boolean = _ == Quit
}
