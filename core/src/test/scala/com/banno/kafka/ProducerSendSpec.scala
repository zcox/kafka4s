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

// import org.scalacheck.*
// import org.scalacheck.magnolia.*
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
// import org.scalatestplus.scalacheck.*

/*
verify outer effect succeeds as soon as send returns
verify inner effect succeeds as soon as kafka acks (callback is called/record is written)

verify outer effect fails after max.block.ms
verify inner effect fails after delivery.timeout.ms

verify outer effect fails on send throw
verify inner effect fails on callback with exception

verify batching? or sequencing/traversing multiple effects?

verify cancelation?
*/

class ProducerSendSpec
    extends AnyPropSpec
    with Matchers
    with EitherValues
    with DockerizedKafkaSpec {}
