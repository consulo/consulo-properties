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
package com.intellij.lang.properties.refactoring;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesBundle;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.editor.ResourceBundleEditor;
import com.intellij.lang.properties.editor.ResourceBundleUtil;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.fileEditor.FileEditorStateLevel;
import consulo.language.editor.refactoring.rename.RenameHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.InputBoxBuilder;
import consulo.ui.InputProblem;
import consulo.ui.InputValidator;
import consulo.ui.TextBox;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Encapsulates logic of renaming resource bundle property key.
 *
 * @author Denis Zhdanov
 * @since 11/9/10 4:13 PM
 */
@ExtensionImpl
public class ResourceBundleKeyRenameHandler implements RenameHandler {

    @Override
    public boolean isAvailableOnDataContext(DataContext dataContext) {
        ResourceBundleEditor editor = ResourceBundleUtil.getEditor(dataContext);
        if (editor == null) {
            return false;
        }
        return editor.getState(FileEditorStateLevel.NAVIGATION).getPropertyName() != null;
    }

    @Override
    public boolean isRenaming(DataContext dataContext) {
        return isAvailableOnDataContext(dataContext);
    }

    @Override
    public LocalizeValue getActionTitleValue() {
        return LocalizeValue.localizeTODO("Resource Bundle Key Rename...");
    }

    @RequiredUIAccess
    @Override
    public void invoke(Project project, Editor editor, PsiFile file, DataContext dataContext) {
        ResourceBundleEditor bundleEditor = ResourceBundleUtil.getEditor(dataContext);
        if (bundleEditor == null) {
            return;
        }

        String propertyName = bundleEditor.getState(FileEditorStateLevel.NAVIGATION).getPropertyName();
        if (propertyName == null) {
            return;
        }

        ResourceBundle bundle = ResourceBundleUtil.getResourceBundleFromDataContext(dataContext);
        if (bundle == null) {
            return;
        }
        InputBoxBuilder.text()
            .asQuestion()
            .title(LocalizeValue.of(PropertiesBundle.message("rename.resource.bundle.key.dialog.title")))
            .text(LocalizeValue.of(PropertiesBundle.message("rename.bundle.enter.new.resource.bundle.key.name.prompt.text")))
            .value(propertyName)
            .setupComponent(TextBox::selectAll)
            .validator(new ResourceBundleKeyRenameValidator(project, bundleEditor, bundle, propertyName))
            .showAsync(project);
    }

    @RequiredUIAccess
    @Override
    public void invoke(Project project, PsiElement[] elements, DataContext dataContext) {
        invoke(project, null, null, dataContext);
    }

    private static class ResourceBundleKeyRenameValidator implements InputValidator<String> {

        private final Set<String> myExistingProperties = new HashSet<String>();

        private final ResourceBundleEditor myEditor;
        private final String myOldPropertyName;

        ResourceBundleKeyRenameValidator(Project project, ResourceBundleEditor editor, ResourceBundle bundle, String oldPropertyName) {
            myEditor = editor;
            myOldPropertyName = oldPropertyName;
            for (PropertiesFile file : bundle.getPropertiesFiles(project)) {
                for (IProperty property : file.getProperties()) {
                    myExistingProperties.add(property.getKey());
                }
            }
            myExistingProperties.remove(oldPropertyName);
        }

        @RequiredUIAccess
        @Override
        public @Nullable InputProblem validate(String inputString) {
            if (inputString.isEmpty() || myExistingProperties.contains(inputString)) {
                return InputProblem.error(LocalizeValue.empty());
            }
            return null;
        }

        @RequiredUIAccess
        @Override
        public CompletableFuture<@Nullable InputProblem> confirm(String inputString) {
            if (!myOldPropertyName.equals(inputString)) {
                Application.get().runWriteAction(() -> myEditor.renameProperty(myOldPropertyName, inputString));
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}
