/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lang.properties;

import jakarta.annotation.Nonnull;

import consulo.dataContext.DataManager;
import consulo.ide.impl.idea.codeInsight.generation.actions.CommentByLineCommentAction;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.AnActionEvent;
import com.intellij.openapi.application.PluginPathManager;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;

/**
 * @author cdr
 */
public abstract class PropertiesCommenterTest extends LightPlatformCodeInsightTestCase {
  public void testProp1() throws Exception { doTest(); }
  public void testUncomment() throws Exception { doTest(); }

  @Nonnull
  @Override
  protected String getTestDataPath() {
    return PluginPathManager.getPluginHomePath("properties") + "/testData";
  }

  private void doTest() throws Exception {
    configureByFile("/propertiesFile/comment/before" + getTestName(false)+".properties");
    performAction();
    checkResultByFile("/propertiesFile/comment/after" + getTestName(false)+".properties");
  }

  private static void performAction() {
    CommentByLineCommentAction action = new consulo.ide.impl.idea.codeInsight.generation.actions.CommentByLineCommentAction();
    action.actionPerformed(new AnActionEvent(
      null,
      DataManager.getInstance().getDataContext(),
      "",
      action.getTemplatePresentation(),
      ActionManager.getInstance(),
      0)
    );
  }
}
