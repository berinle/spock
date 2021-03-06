/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;

public class TestRuleInterceptor implements IMethodInterceptor {
  private final List<FieldInfo> ruleFields;

  public TestRuleInterceptor(List<FieldInfo> ruleFields) {
    this.ruleFields = ruleFields;
  }

  public void intercept(final IMethodInvocation invocation) throws Throwable {
    Statement stat = createBaseStatement(invocation);

    for (FieldInfo field : ruleFields) {
      Object target = field.isStatic() ? null :
          field.isShared() ? invocation.getSharedInstance() :
              invocation.getInstance();

      Object rule = field.readValue(target);
      if (!(rule instanceof TestRule)) continue;

      Description description = field.isStatic() || field.isShared() ?
          invocation.getSpec().getDescription() : invocation.getFeature().getDescription();
      stat = ((TestRule) rule).apply(stat, description);
    }

    stat.evaluate();
  }

  private Statement createBaseStatement(final IMethodInvocation invocation) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        invocation.proceed();
      }
    };
  }
}
