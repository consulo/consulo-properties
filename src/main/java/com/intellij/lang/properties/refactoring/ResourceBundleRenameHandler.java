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
package com.intellij.lang.properties.refactoring;

import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.editor.ResourceBundleAsVirtualFile;
import com.intellij.lang.properties.editor.ResourceBundleEditor;
import com.intellij.lang.properties.editor.ResourceBundleUtil;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.fileEditor.FileEditorStateLevel;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.editor.refactoring.rename.RenameHandler;
import consulo.language.editor.refactoring.rename.RenameProcessor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.properties.localize.PropertiesLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.InputValidator;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.UIUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;

import java.io.File;
import java.util.List;

/**
 * @author Alexey
 */
@ExtensionImpl
public class ResourceBundleRenameHandler implements RenameHandler {
    private static final Logger LOG = Logger.getInstance(ResourceBundleRenameHandler.class);

    @Nonnull
    @Override
    public LocalizeValue getActionTitleValue() {
        return LocalizeValue.localizeTODO("Resource Bundle Rename...");
    }

    @Override
    public boolean isAvailableOnDataContext(DataContext dataContext) {
        final Project project = dataContext.getData(Project.KEY);
        if (project == null) {
            return false;
        }
        final ResourceBundle bundle = ResourceBundleUtil.getResourceBundleFromDataContext(dataContext);
        if (bundle == null) {
            return false;
        }

        final VirtualFile virtualFile = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE);

        ResourceBundleEditor editor = ResourceBundleUtil.getEditor(dataContext);
        return (editor == null || editor.getState(FileEditorStateLevel.NAVIGATION)
            .getPropertyName() == null /* user selected non-bundle key element */)
            && bundle.getPropertiesFiles(project).size() > 1 && (virtualFile instanceof ResourceBundleAsVirtualFile || virtualFile == null);
    }

    @Override
    public boolean isRenaming(DataContext dataContext) {
        return isAvailableOnDataContext(dataContext);
    }

    @Override
    @RequiredUIAccess
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file, DataContext dataContext) {
        ResourceBundle resourceBundle = ResourceBundleUtil.getResourceBundleFromDataContext(dataContext);

        assert resourceBundle != null;
        Messages.showInputDialog(
            project,
            PropertiesLocalize.renameBundleEnterNewResourceBundleBaseNamePromptText().get(),
            PropertiesLocalize.renameResourceBundleDialogTitle().get(),
            UIUtil.getQuestionIcon(),
            resourceBundle.getBaseName(),
            new MyInputValidator(project, resourceBundle)
        );
    }

    @Override
    @RequiredUIAccess
    public void invoke(@Nonnull Project project, @Nonnull PsiElement[] elements, DataContext dataContext) {
        invoke(project, null, null, dataContext);
    }

    private static class MyInputValidator implements InputValidator {
        private final Project myProject;
        private final ResourceBundle myResourceBundle;

        public MyInputValidator(final Project project, final ResourceBundle resourceBundle) {
            myProject = project;
            myResourceBundle = resourceBundle;
        }

        @Override
        @RequiredUIAccess
        public boolean checkInput(String inputString) {
            return inputString.indexOf(File.separatorChar) < 0 && inputString.indexOf('/') < 0;
        }

        @Override
        @RequiredUIAccess
        public boolean canClose(final String inputString) {
            return doRename(inputString);
        }

        @RequiredUIAccess
        private boolean doRename(final String inputString) {
            final List<PropertiesFile> propertiesFiles = myResourceBundle.getPropertiesFiles(myProject);
            for (PropertiesFile propertiesFile : propertiesFiles) {
                if (!FileModificationService.getInstance().prepareFileForWrite(propertiesFile.getContainingFile())) {
                    return false;
                }
            }

            RenameProcessor renameProcessor = null;
            String baseName = myResourceBundle.getBaseName();
            for (PropertiesFile propertiesFile : propertiesFiles) {
                final VirtualFile virtualFile = propertiesFile.getVirtualFile();
                if (virtualFile == null) {
                    continue;
                }
                final String newName = inputString + virtualFile.getNameWithoutExtension().substring(baseName.length()) + "."
                    + virtualFile.getExtension();
                if (renameProcessor == null) {
                    renameProcessor = new RenameProcessor(myProject, propertiesFile.getContainingFile(), newName, false, false);
                    continue;
                }
                renameProcessor.addElement(propertiesFile.getContainingFile(), newName);
            }
            if (renameProcessor == null) {
                LOG.assertTrue(false);
                return true;
            }
            renameProcessor.setCommandName(PropertiesLocalize.renameResourceBundleDialogTitle());
            renameProcessor.doRun();
            return true;
        }
    }
}