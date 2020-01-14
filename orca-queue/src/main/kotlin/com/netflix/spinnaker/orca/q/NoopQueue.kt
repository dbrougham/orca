/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.q

import com.netflix.spinnaker.q.DeadMessageCallback
import com.netflix.spinnaker.q.Message
import com.netflix.spinnaker.q.Queue
import com.netflix.spinnaker.q.QueueCallback
import org.slf4j.LoggerFactory
import org.threeten.extra.Minutes
import java.time.temporal.TemporalAmount

/**
 * A Noop queue do be used when no Queue bean is found (e.g. when Queue is disabled)
 */
class NoopQueue : Queue {
  private val log = LoggerFactory.getLogger(this.javaClass)

  init {
    log.error("${this.javaClass.simpleName} was created - all queue operations will be NOOP'd. This is OK if the queue was intended to be disabled")
  }

  override val ackTimeout: TemporalAmount
    get() = Minutes.of(1)
  override val canPollMany: Boolean
    get() = false
  override val deadMessageHandlers: List<DeadMessageCallback>
    get() = emptyList()

  override fun ensure(message: Message, delay: TemporalAmount) {
  }

  override fun poll(callback: QueueCallback) {
  }

  override fun poll(maxMessages: Int, callback: QueueCallback) {
  }

  override fun push(message: Message, delay: TemporalAmount) {
  }

  override fun reschedule(message: Message, delay: TemporalAmount) {
  }
}
