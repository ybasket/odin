package io.odin.slf4j

import cats.effect.kernel.Sync
import io.odin.formatter.Formatter
import io.odin.loggers.DefaultLogger
import io.odin.{Level, Logger, LoggerMessage}
import org.slf4j.{Logger => JLogger, LoggerFactory}
import cats.implicits._

final class Slf4jLogger[F[_]: Sync](
    logger: JLogger,
    level: Level,
    formatter: Formatter,
    syncType: Sync.Type = Sync.Type.Blocking
) extends DefaultLogger[F](level) {
  override def submit(msg: LoggerMessage): F[Unit] = {
    Sync[F].uncancelable { _ =>
      Sync[F].whenA(msg.level >= this.minLevel)(msg.level match {
        case Level.Trace => Sync[F].suspend(syncType)(logger.trace(formatter.format(msg)))
        case Level.Debug => Sync[F].suspend(syncType)(logger.debug(formatter.format(msg)))
        case Level.Info  => Sync[F].suspend(syncType)(logger.info(formatter.format(msg)))
        case Level.Warn  => Sync[F].suspend(syncType)(logger.warn(formatter.format(msg)))
        case Level.Error => Sync[F].suspend(syncType)(logger.error(formatter.format(msg)))
      })
    }
  }

  override def withMinimalLevel(level: Level): Logger[F] = new Slf4jLogger[F](logger, level, formatter)
}

object Slf4jLogger {
  def apply[F[_]: Sync](
      logger: JLogger = LoggerFactory.getLogger("OdinSlf4jLogger"),
      level: Level = Level.Info,
      formatter: Formatter = Formatter.default
  ) = new Slf4jLogger[F](logger, level, formatter)
}
