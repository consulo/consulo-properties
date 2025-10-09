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

import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.editor.localize.CodeInsightLocalize;
import consulo.language.editor.util.LanguageUndoUtil;
import consulo.language.psi.PsiAnchor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.properties.localize.PropertiesLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.undoRedo.CommandProcessor;
import consulo.util.lang.Couple;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.List;

public class CreatePropertyFix implements SyntheticIntentionAction, LocalQuickFix {
    private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.i18n.I18nizeQuickFix");
    private final PsiAnchor myElement;
    private final String myKey;
    private final List<PropertiesFile> myPropertiesFiles;

    public static final LocalizeValue NAME = PropertiesLocalize.createPropertyQuickfixText();

    @RequiredReadAction
    public CreatePropertyFix() {
        this(null, null, null);
    }

    @RequiredReadAction
    public CreatePropertyFix(PsiElement element, String key, final List<PropertiesFile> propertiesFiles) {
        myElement = element == null ? null : PsiAnchor.create(element);
        myKey = key;
        myPropertiesFiles = propertiesFiles;
    }

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return NAME;
    }

    @Override
    @RequiredUIAccess
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
        PsiElement psiElement = descriptor.getPsiElement();
        if (isAvailable(project, null, null)) {
            invoke(project, null, psiElement.getContainingFile());
        }
    }

    @Nonnull
    @Override
    public LocalizeValue getText() {
        return NAME;
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, @Nullable Editor editor, @Nullable PsiFile file) {
        return myElement != null && myElement.retrieve() != null;
    }

    @Override
    @RequiredUIAccess
    public void invoke(@Nonnull final Project project, @Nullable Editor editor, @Nonnull PsiFile file) {
        invokeAction(project, file, myElement.retrieve(), myKey, myPropertiesFiles);
    }

    @Nullable
    @RequiredUIAccess
    private Couple<String> invokeAction(
        @Nonnull final Project project,
        @Nonnull PsiFile file,
        @Nonnull PsiElement psiElement,
        @Nullable final String suggestedKey,
        @Nullable final List<PropertiesFile> propertiesFiles
    ) {
        final I18nizeQuickFixModel model;
        final I18nizeQuickFixDialog.DialogCustomization dialogCustomization = createDefaultCustomization(suggestedKey, propertiesFiles);

        if (project.getApplication().isUnitTestMode()) {
            model = new I18nizeQuickFixModel() {
                @Override
                public String getValue() {
                    return "";
                }

                @Override
                public String getKey() {
                    return dialogCustomization.getSuggestedName();
                }

                @Override
                public boolean hasValidData() {
                    return true;
                }

                @Override
                public Collection<PropertiesFile> getAllPropertiesFiles() {
                    return propertiesFiles;
                }
            };
        }
        else {
            model = new I18nizeQuickFixDialog(
                project,
                file,
                NAME.get(),
                dialogCustomization
            );
        }
        return doAction(project, psiElement, model);
    }

    protected static I18nizeQuickFixDialog.DialogCustomization createDefaultCustomization(
        String suggestedKey,
        List<PropertiesFile> propertiesFiles
    ) {
        return new I18nizeQuickFixDialog.DialogCustomization(
            NAME.get(),
            false,
            true,
            propertiesFiles,
            suggestedKey == null ? "" : suggestedKey
        );
    }

    @RequiredUIAccess
    protected Couple<String> doAction(Project project, PsiElement psiElement, I18nizeQuickFixModel model) {
        if (!model.hasValidData()) {
            return null;
        }
        final String key = model.getKey();
        final String value = model.getValue();

        final Collection<PropertiesFile> selectedPropertiesFiles = model.getAllPropertiesFiles();
        createProperty(project, psiElement, selectedPropertiesFiles, key, value);

        return Couple.of(key, value);
    }

    @RequiredUIAccess
    public static void createProperty(
        @Nonnull final Project project,
        @Nonnull final PsiElement psiElement,
        @Nonnull final Collection<PropertiesFile> selectedPropertiesFiles,
        @Nonnull final String key,
        @Nonnull final String value
    ) {
        for (PropertiesFile selectedFile : selectedPropertiesFiles) {
            if (!FileModificationService.getInstance().prepareFileForWrite(selectedFile.getContainingFile())) {
                return;
            }
        }
        LanguageUndoUtil.markPsiFileForUndo(psiElement.getContainingFile());

        CommandProcessor.getInstance().newCommand()
            .project(project)
            .name(CodeInsightLocalize.quickfixI18nCommandName())
            .groupId(project)
            .inWriteAction()
            .run(() -> {
                try {
                    I18nUtil.createProperty(project, selectedPropertiesFiles, key, value);
                }
                catch (IncorrectOperationException e) {
                    LOG.error(e);
                }
            });
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
