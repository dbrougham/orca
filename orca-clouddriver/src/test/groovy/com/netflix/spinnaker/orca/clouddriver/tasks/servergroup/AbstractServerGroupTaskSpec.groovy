/*
 * Copyright 2016 Google, Inc.
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

import com.netflix.spinnaker.orca.api.operations.OperationsInput
import com.netflix.spinnaker.orca.api.operations.OperationsRunner
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus
import com.netflix.spinnaker.kork.core.RetrySupport
import com.netflix.spinnaker.orca.clouddriver.model.KatoOperationsContext
import com.netflix.spinnaker.orca.clouddriver.model.TaskId
import com.netflix.spinnaker.orca.clouddriver.pipeline.servergroup.support.TargetServerGroup
import com.netflix.spinnaker.orca.clouddriver.pipeline.servergroup.support.TargetServerGroupResolver
import com.netflix.spinnaker.orca.pipeline.model.PipelineExecutionImpl
import com.netflix.spinnaker.orca.pipeline.model.StageExecutionImpl
import spock.lang.Specification
import spock.lang.Subject

class AbstractServerGroupTaskSpec extends Specification {

  static testAction = "testAction"

  static class TestServerGroupTask extends AbstractServerGroupTask {
    String serverGroupAction = testAction
  }

  @Subject
    task = new TestServerGroupTask(
      retrySupport: Spy(RetrySupport) {
        _ * sleep(_) >> { /* do nothing */ }
      }
    )
  def stage = new StageExecutionImpl(PipelineExecutionImpl.newPipeline("orca"), "whatever")
  def taskId = new TaskId(UUID.randomUUID().toString())

  def stageContext = [
    asgName      : "test-asg",
    regions      : ["us-west-1", "us-east-1"],
    credentials  : "fzlem",
    cloudProvider: "aws"
  ]

  def setup() {
    stage.context = stageContext
  }

  def "creates an enable ASG task based on job parameters"() {
    given:
    def operations
    task.operationsRunner = Mock(OperationsRunner) {
      1 * run(_) >> {
        OperationsInput operationsInput = it[0]
        operations = operationsInput.getOperations()
        new KatoOperationsContext(taskId, null)
      }
    }

    when:
    task.execute(stage)

    then:
    operations.size() == 1
    with(operations[0].testAction) {
      it instanceof Map
      asgName == this.stageContext.asgName
      serverGroupName == this.stageContext.asgName
      regions == this.stageContext.regions
      credentials == this.stageContext.credentials
    }
  }

  def "returns a success status with the kato task id"() {
    given:
    task.operationsRunner = Stub(OperationsRunner) {
      run(_) >> new KatoOperationsContext(taskId)
    }

    when:
    def result = task.execute(stage)

    then:
    result.status == ExecutionStatus.SUCCEEDED
    result.context."kato.last.task.id" == taskId
    result.context."deploy.account.name" == stageContext.credentials
  }

  void "should get target dynamically when configured"() {
    setup:
    stage.context.target = TargetServerGroup.Params.Target.ancestor_asg_dynamic
    task.operationsRunner = Stub(OperationsRunner) {
      run(_) >> new KatoOperationsContext(taskId)
    }
    GroovyMock(TargetServerGroupResolver, global: true)

    when:
    def result = task.execute(stage)

    then:
    2 * TargetServerGroupResolver.fromPreviousStage(stage) >> new TargetServerGroup(
      name: "foo-v001", region: "us-east-1"
    )
    result.context.asgName == "foo-v001"
    result.context.serverGroupName == "foo-v001"
    result.context."deploy.server.groups" == ["us-east-1": ["foo-v001"]]
  }

}
