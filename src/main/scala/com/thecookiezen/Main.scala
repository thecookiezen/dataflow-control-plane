package com.thecookiezen

import java.util.concurrent.Executors

import cats.effect._
import com.thecookiezen.gcp.storage.{GCStorageClient, StorageClient}
import com.thecookiezen.terminal.{Console, Terminal}

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  val consoleInterpreter = new Console[IO] {
    val putStrLn: String => IO[Unit] = value => IO(println(value))
    val putStr: String => IO[Unit]   = value => IO(print(value))
    val readLn: IO[String]           = IO(scala.io.StdIn.readLine)
  }

  val terminal = new Terminal[IO](consoleInterpreter)

  case class Environment[F[_]](config: Config, storage: StorageClient[F])

  def run(args: List[String]): IO[ExitCode] = fixedThreadPool[IO](1).use { pool =>
    for {
      config     <- Config.load(Blocker.liftExecutionContext(pool))
      gcsStorage = new GCStorageClient(config.gcp)
      env        = Environment[IO](config, gcsStorage)
      _          <- terminal.init(env)
    } yield ExitCode.Success
  }

  private def fixedThreadPool[F[_]](num: Int)(implicit F: Sync[F]): Resource[F, ExecutionContext] =
    Resource(F.delay {
      val executor = Executors.newFixedThreadPool(num)
      val ec       = ExecutionContext.fromExecutor(executor)
      (ec, F.delay(executor.shutdown()))
    })
}
