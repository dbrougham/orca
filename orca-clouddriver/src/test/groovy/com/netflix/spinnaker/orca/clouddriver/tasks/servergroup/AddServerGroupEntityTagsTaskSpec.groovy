/*
 * Copyright 2017 Netflix, Inc.
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

package com.netflix.spinnaker.orca.clouddriver.tasks.servergroup

import com.netflix.spinnaker.orca.api.operations.OperationsRunner
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus
import com.netflix.spinnaker.kork.core.RetrySupport
import com.netflix.spinnaker.orca.clouddriver.OortService
import com.netflix.spinnaker.orca.pipeline.model.PipelineExecutionImpl
import com.netflix.spinnaker.orca.pipeline.model.StageExecutionImpl
import spock.lang.Specification
import spock.lang.Subject

class AddServerGroupEntityTagsTaskSpec extends Specification {

  def operationsRunner = Mock(OperationsRunner)
  def oortService = Mock(OortService)
  def retrySupport = Spy(RetrySupport) {
    _ * sleep(_) >> { /* do nothing */ }
  }

  ServerGroupEntityTagGenerator tagGenerator = new SpinnakerMetadataServerGroupTagGenerator(oortService, retrySupport)

  @Subject
  AddServerGroupEntityTagsTask task = new AddServerGroupEntityTagsTask(operationsRunner: operationsRunner, tagGenerators: [tagGenerator])

  void "should return with failed/continue status if tagging operation fails"() {
    when:
    def stage = new StageExecutionImpl(PipelineExecutionImpl.newPipeline("orca"), "whatever", [
      "deploy.server.groups": [
        "us-east-1": ["foo-v001"]
      ]
    ])
    def result = task.execute(stage)

    then:
    result.status == ExecutionStatus.FAILED_CONTINUE
    1 * operationsRunner.run(_) >> { throw new RuntimeException("something went wrong") }
  }

  void "just completes when no tag generators or generators do not produce any tags"() {
    given:
    AddServerGroupEntityTagsTask emptyTask = new AddServerGroupEntityTagsTask(operationsRunner: operationsRunner, tagGenerators: [])

    when:
    def stage = new StageExecutionImpl(PipelineExecutionImpl.newPipeline("orca"), "whatever", [
      "deploy.server.groups": [
        "us-east-1": ["foo-v001"],
      ]
    ])
    def result = emptyTask.execute(stage)

    then:
    result.status == ExecutionStatus.SUCCEEDED
    0 * _
  }
}
