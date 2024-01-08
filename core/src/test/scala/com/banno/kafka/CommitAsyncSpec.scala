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

import cats.effect.IO
import munit.CatsEffectSuite
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import com.banno.kafka.consumer.*
import scala.concurrent.duration.*

class CommitAsyncSpec extends CatsEffectSuite with KafkaSpec {

  def offsets(
      p: TopicPartition,
      o: Long,
  ): Map[TopicPartition, OffsetAndMetadata] =
    Map(p -> new OffsetAndMetadata(o))

  val empty = Map.empty[TopicPartition, OffsetAndMetadata]

  def consumerResource =
    ConsumerApi
      .resource[IO, Int, Int](
        BootstrapServers(bootstrapServer),
        GroupId(genGroupId),
        AutoOffsetReset.earliest,
        EnableAutoCommit(false),
      )

  test("commit works") {
    consumerResource.use { consumer =>
      for {
        topic <- createTestTopic[IO]()
        p = new TopicPartition(topic, 0)
        ps = Set(p)
        _ = println("start")
        () <- consumer.subscribe(topic)
        _ = println("subscribed")
        c0 <- consumer.partitionQueries.committed(ps)
        _ = println(s"c0 = $c0")
        os = offsets(p, 0)
        _ = println(s"os = $os")
        // have to poll once to get consumer to join group, so it can commit offsets
        _ <- consumer.poll(1.second)
        _ = println("1")
        commit = consumer.commit(os)
        _ = println("2")
        a <- commit
        _ = println("3")
        () <- a
        _ = println("4")
        c1 <- consumer.partitionQueries.committed(ps)
      } yield {
        assertEquals(c0, empty)
        assertEquals(c1, os)
      }
    }
  }

}
