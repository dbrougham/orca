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

import com.netflix.spinnaker.orca.pipeline.ExecutionRunner
import com.netflix.spinnaker.orca.pipeline.model.Execution
import com.netflix.spinnaker.q.Queue
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnMissingBean(Queue::class)
class NoopExecutionRunner() : ExecutionRunner {
  private val log = LoggerFactory.getLogger(this.javaClass)

  init {
    log.error("${this.javaClass.simpleName} was created - all queue operations will be NOOP'd. This is OK if the queue was intended to be disabled")
  }

  override fun start(execution: Execution) {
  }

  override fun reschedule(execution: Execution) {
  }

  override fun restart(execution: Execution, stageId: String) {
  }

  override fun unpause(execution: Execution) {
  }

  override fun cancel(execution: Execution, user: String, reason: String?) {
  }
}
