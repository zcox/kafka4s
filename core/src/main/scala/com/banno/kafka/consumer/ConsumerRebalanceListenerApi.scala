package com.banno.kafka.consumer

import cats.effect.{Effect, IO}
import scala.collection.JavaConverters._
import org.apache.kafka.common._
import org.apache.kafka.clients.consumer._
import java.util.{Collection => JCollection}

trait ConsumerRebalanceListenerApi[F[_]] {
  def onPartitionsRevoked(partitions: Iterable[TopicPartition]): F[Unit]
  def onPartitionsAssigned(partitions: Iterable[TopicPartition]): F[Unit]
}

object ConsumerRebalanceListenerApi {
  //an alternative might be to push this into the contract of ConsumerRebalanceListenerApi, to allow/force developers to handle in use-case-specific manner?
  //as it stands, a failed effect will throw on the java client's call to onPartitionsRevoked/Assigned, not sure how that bubbles up...
  def raiseError[A]: Either[Throwable, A] => IO[Unit] = {
    case Right(_) => IO.unit
    case Left(t) => IO.raiseError(t)
  }

  def unsafe[F[_]: Effect](api: ConsumerRebalanceListenerApi[F]): ConsumerRebalanceListener = 
    new ConsumerRebalanceListener() {
      override def onPartitionsRevoked(partitions: JCollection[TopicPartition]): Unit = 
        Effect[F].runAsync(api.onPartitionsRevoked(partitions.asScala))(raiseError).unsafeRunSync()
      override def onPartitionsAssigned(partitions: JCollection[TopicPartition]): Unit = 
        Effect[F].runAsync(api.onPartitionsAssigned(partitions.asScala))(raiseError).unsafeRunSync()
    }
}
