/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.PropertiesList;
import com.intellij.lang.properties.psi.Property;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.inspection.CustomSuppressableInspectionTool;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.intention.SuppressIntentionAction;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.properties.localize.PropertiesLocalize;
import jakarta.annotation.Nonnull;

/**
 * @author cdr
 */
public abstract class PropertySuppressableInspectionBase extends LocalInspectionTool implements CustomSuppressableInspectionTool {
    private static final Logger LOG = Logger.getInstance(PropertySuppressableInspectionBase.class);

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return PropertiesLocalize.propertiesFilesInspectionGroupDisplayName();
    }

    @Nonnull
    @Override
    public LocalizeValue[] getGroupPath() {
        return new LocalizeValue[]{getGroupDisplayName()};
    }

    @Override
    public SuppressIntentionAction[] getSuppressActions(final PsiElement element) {
        return new SuppressIntentionAction[]{
            new SuppressSinglePropertyFix(getShortName()),
            new SuppressForFile(getShortName())
        };
    }

    @Override
    @RequiredReadAction
    public boolean isSuppressedFor(@Nonnull PsiElement element) {
        Property property = PsiTreeUtil.getParentOfType(element, Property.class, false);
        PropertiesFile file;
        if (property == null) {
            PsiFile containingFile = element.getContainingFile();
            if (containingFile instanceof PropertiesFile propertiesFile) {
                file = propertiesFile;
            }
            else {
                return false;
            }
        }
        else {
            PsiElement prev = property.getPrevSibling();
            while (prev instanceof PsiWhiteSpace || prev instanceof PsiComment) {
                if (prev instanceof PsiComment prevComment) {
                    String text = prevComment.getText();
                    if (text.contains("suppress") && text.contains("\"" + getShortName() + "\"")) {
                        return true;
                    }
                }
                prev = prev.getPrevSibling();
            }
            file = property.getPropertiesFile();
        }
        PsiElement leaf = file.getContainingFile().findElementAt(0);
        while (leaf instanceof PsiWhiteSpace space) {
            leaf = space.getNextSibling();
        }

        while (leaf instanceof PsiComment comment) {
            String text = comment.getText();
            if (text.contains("suppress") && text.contains("\"" + getShortName() + "\"") && text.contains("file")) {
                return true;
            }
            leaf = leaf.getNextSibling();
            if (leaf instanceof PsiWhiteSpace space) {
                leaf = space.getNextSibling();
            }
            // comment before first property get bound to the file, not property
            if (leaf instanceof PropertiesList propList
                && propList.getFirstChild() == property
                && text.contains("suppress")
                && text.contains("\"" + getShortName() + "\"")) {
                return true;
            }
        }

        return false;
    }

    private static class SuppressSinglePropertyFix extends SuppressIntentionAction {
        private final String shortName;

        public SuppressSinglePropertyFix(String shortName) {
            this.shortName = shortName;
        }

        @Nonnull
        @Override
        public LocalizeValue getText() {
            return PropertiesLocalize.unusedPropertySuppressForProperty();
        }

        @Override
        @RequiredReadAction
        public boolean isAvailable(@Nonnull final Project project, final Editor editor, @Nonnull final PsiElement element) {
            final Property property = PsiTreeUtil.getParentOfType(element, Property.class);
            return property != null && property.isValid();
        }

        @Override
        @RequiredWriteAction
        public void invoke(
            @Nonnull final Project project,
            final Editor editor,
            @Nonnull final PsiElement element
        ) throws IncorrectOperationException {
            final PsiFile file = element.getContainingFile();
            if (!FileModificationService.getInstance().prepareFileForWrite(file)) {
                return;
            }

            final Property property = PsiTreeUtil.getParentOfType(element, Property.class);
            LOG.assertTrue(property != null);
            final int start = property.getTextRange().getStartOffset();

            final Document doc = PsiDocumentManager.getInstance(project).getDocument(file);
            LOG.assertTrue(doc != null);
            final int line = doc.getLineNumber(start);
            final int lineStart = doc.getLineStartOffset(line);

            doc.insertString(lineStart, "# suppress inspection \"" + shortName + "\"\n");
        }
    }

    private static class SuppressForFile extends SuppressIntentionAction {
        private final String shortName;

        public SuppressForFile(String shortName) {
            this.shortName = shortName;
        }

        @Nonnull
        @Override
        public LocalizeValue getText() {
            return PropertiesLocalize.unusedPropertySuppressForFile();
        }

        @Override
        @RequiredReadAction
        public boolean isAvailable(@Nonnull final Project project, final Editor editor, @Nonnull final PsiElement element) {
            return element.isValid() && element.getContainingFile() instanceof PropertiesFile;
        }

        @Override
        @RequiredWriteAction
        public void invoke(
            @Nonnull final Project project,
            final Editor editor,
            @Nonnull final PsiElement element
        ) throws IncorrectOperationException {
            final PsiFile file = element.getContainingFile();
            if (!FileModificationService.getInstance().prepareFileForWrite(file)) {
                return;
            }

            final Document doc = PsiDocumentManager.getInstance(project).getDocument(file);
            LOG.assertTrue(doc != null, file);

            doc.insertString(0, "# suppress inspection \"" + shortName + "\" for whole file\n");
        }
    }
}
