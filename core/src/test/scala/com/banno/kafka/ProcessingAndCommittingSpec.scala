/*
 * Copyright 2019 Jack Henry & Associates, Inc.®
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.banno.kafka

import cats.syntax.all.*
import cats.effect.IO
import fs2.Stream
import munit.CatsEffectSuite
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import com.banno.kafka.producer.*
import com.banno.kafka.consumer.*
import scala.concurrent.duration.*

class ProcessingAndCommittingSpec extends CatsEffectSuite with KafkaSpec {

  def offsets(
      p: TopicPartition,
      o: Long,
  ): Map[TopicPartition, OffsetAndMetadata] =
    Map(p -> new OffsetAndMetadata(o))

  val empty = Map.empty[TopicPartition, OffsetAndMetadata]

  test("processingAndCommitting commits after number of records") {
    ProducerApi
      .resource[IO, Int, Int](
        BootstrapServers(bootstrapServer)
      )
      .use { producer =>
        ConsumerApi
          .resource[IO, Int, Int](
            BootstrapServers(bootstrapServer),
            GroupId(genGroupId),
            AutoOffsetReset.earliest,
            EnableAutoCommit(false),
          )
          .use { consumer =>
            for {
              topic <- createTestTopic[IO]()
              p = new TopicPartition(topic, 0)
              ps = Set(p)
              values = (0 to 9).toList
              _ <- producer.sendAsyncBatch(
                values.map(v => new ProducerRecord(topic, v, v))
              )
              () <- consumer.subscribe(topic)
              c0 <- consumer.partitionQueries.committed(ps)
              // commit offsets every 2 records (and never commit after elapsed time)
              pac = consumer.processingAndCommitting(
                100.millis,
                2,
                Long.MaxValue,
              )(_.value.pure[IO])
              committed = Stream.repeatEval(
                consumer.partitionQueries.committed(ps)
              )
              results <- pac
                .take(values.size.toLong)
                .interleave(committed)
                .compile
                .toList
            } yield {
              assertEquals(c0, empty)
              assertEquals(results.size, values.size * 2)
              // TODO rewrite this to use values, not so hard-coded
              assertEquals(results(0), 0)
              assertEquals(results(1), empty)
              assertEquals(results(2), 1)
              assertEquals(results(3), offsets(p, 2))
              assertEquals(results(4), 2)
              assertEquals(results(5), offsets(p, 2))
              assertEquals(results(6), 3)
              assertEquals(results(7), offsets(p, 4))
              assertEquals(results(8), 4)
              assertEquals(results(9), offsets(p, 4))
              assertEquals(results(10), 5)
              assertEquals(results(11), offsets(p, 6))
              assertEquals(results(12), 6)
              assertEquals(results(13), offsets(p, 6))
              assertEquals(results(14), 7)
              assertEquals(results(15), offsets(p, 8))
              assertEquals(results(16), 8)
              assertEquals(results(17), offsets(p, 8))
              assertEquals(results(18), 9)
              assertEquals(results(19), offsets(p, 10))
            }
          }
      }
  }

  test("processingAndCommitting commits after elapsed time") {
    /*
    assert no committed offsets
    process records
    ...
    after elapsed time
    assert offsets got committed
     */

  }

  test(
    "on failure, processingAndCommitting commits successful offsets, but not the failed offset"
  ) {}

}
