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

import com.intellij.lang.properties.psi.Property;
import consulo.codeEditor.Editor;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

/**
 * @author cdr
 */
class RemovePropertyFix implements SyntheticIntentionAction {
  private final Property myProperty;

  public RemovePropertyFix(@Nonnull final Property origProperty) {
    myProperty = origProperty;
  }

  @Nonnull
  public String getText() {
    return PropertiesBundle.message("remove.property.intention.text");
  }

  public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
    return file.isValid()
           && myProperty != null
           && myProperty.isValid()
           && myProperty.getManager().isInProject(myProperty)
      ;
  }

  public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
  {
    if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
    myProperty.delete();
  }

  public boolean startInWriteAction() {
    return true;
  }
}
