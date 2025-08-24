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
package com.intellij.lang.properties.references;

import com.intellij.lang.properties.PropertiesBundle;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.language.editor.CodeInsightBundle;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.editor.util.LanguageUndoUtil;
import consulo.language.psi.PsiAnchor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.undoRedo.CommandProcessor;
import consulo.util.lang.Pair;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.List;

public class CreatePropertyFix implements SyntheticIntentionAction, LocalQuickFix
{
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.i18n.I18nizeQuickFix");
  private final PsiAnchor myElement;
  private final String myKey;
  private final List<PropertiesFile> myPropertiesFiles;

  public static final String NAME = PropertiesBundle.message("create.property.quickfix.text");

  public CreatePropertyFix() {
    this(null, null, null);
  }

  public CreatePropertyFix(PsiElement element, String key, final List<PropertiesFile> propertiesFiles) {
    myElement = element == null ? null : PsiAnchor.create(element);
    myKey = key;
    myPropertiesFiles = propertiesFiles;
  }

  @Nonnull
  public String getName() {
    return NAME;
  }

  @Nonnull
  public String getFamilyName() {
    return getText();
  }

  public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
    PsiElement psiElement = descriptor.getPsiElement();
    if (isAvailable(project, null, null)) {
      invoke(project, null, psiElement.getContainingFile());
    }
  }

  @Nonnull
  public String getText() {
    return NAME;
  }

  public boolean isAvailable(@Nonnull Project project, @Nullable Editor editor, @Nullable PsiFile file) {
    return myElement != null && myElement.retrieve() != null;
  }

  public void invoke(@Nonnull final Project project, @Nullable Editor editor, @Nonnull PsiFile file) {
    invokeAction(project, file, myElement.retrieve(), myKey, myPropertiesFiles);
  }

  @Nullable
  private Pair<String, String> invokeAction(@Nonnull final Project project,
                                            @Nonnull PsiFile file,
                                            @Nonnull PsiElement psiElement,
                                            @Nullable final String suggestedKey,
                                            @Nullable final List<PropertiesFile> propertiesFiles) {
    final I18nizeQuickFixModel model;
    final I18nizeQuickFixDialog.DialogCustomization dialogCustomization = createDefaultCustomization(suggestedKey, propertiesFiles);

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      model = new I18nizeQuickFixModel() {
        public String getValue() {
          return "";
        }

        public String getKey() {
          return dialogCustomization.getSuggestedName();
        }

        public boolean hasValidData() {
          return true;
        }

        public Collection<PropertiesFile> getAllPropertiesFiles() {
          return propertiesFiles;
        }
      };
    } else {
      model = new I18nizeQuickFixDialog(
        project,
        file,
        NAME, dialogCustomization
      );
    }
    return doAction(project, psiElement, model);
  }

  protected static I18nizeQuickFixDialog.DialogCustomization createDefaultCustomization(String suggestedKey, List<PropertiesFile> propertiesFiles) {
    return new I18nizeQuickFixDialog.DialogCustomization(NAME, false, true, propertiesFiles, suggestedKey == null ? "" : suggestedKey);
  }

  protected Pair<String, String> doAction(Project project, PsiElement psiElement, I18nizeQuickFixModel model) {
    if (!model.hasValidData()) {
      return null;
    }
    final String key = model.getKey();
    final String value = model.getValue();

    final Collection<PropertiesFile> selectedPropertiesFiles = model.getAllPropertiesFiles();
    createProperty(project, psiElement, selectedPropertiesFiles, key, value);

    return new Pair<String, String>(key, value);
  }

  public static void createProperty(@Nonnull final Project project,
                                    @Nonnull final PsiElement psiElement,
                                    @Nonnull final Collection<PropertiesFile> selectedPropertiesFiles,
                                    @Nonnull final String key,
                                    @Nonnull final String value) {
    for (PropertiesFile selectedFile : selectedPropertiesFiles) {
      if (!FileModificationService.getInstance().prepareFileForWrite(selectedFile.getContainingFile())) return;
    }
    LanguageUndoUtil.markPsiFileForUndo(psiElement.getContainingFile());

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
          public void run() {
            try {
              I18nUtil.createProperty(project, selectedPropertiesFiles, key, value);
            }
            catch (IncorrectOperationException e) {
              LOG.error(e);
            }
          }
        }, CodeInsightBundle.message("quickfix.i18n.command.name"), project);
      }
    });
  }

  public boolean startInWriteAction() {
    return false;
  }
}
