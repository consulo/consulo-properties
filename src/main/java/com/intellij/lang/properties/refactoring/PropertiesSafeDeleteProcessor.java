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

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.refactoring.RefactoringSettings;
import consulo.language.editor.refactoring.safeDelete.NonCodeUsageSearchInfo;
import consulo.language.editor.refactoring.safeDelete.SafeDeleteProcessor;
import consulo.language.editor.refactoring.safeDelete.SafeDeleteProcessorDelegate;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.usage.UsageInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author yole
 */
@ExtensionImpl
public class PropertiesSafeDeleteProcessor implements SafeDeleteProcessorDelegate {
    @Override
    public boolean handlesElement(PsiElement element) {
        return element instanceof PropertiesFile;
    }

    @Override
    @RequiredReadAction
    public NonCodeUsageSearchInfo findUsages(PsiElement element, PsiElement[] allElementsToDelete, List<UsageInfo> result) {
        PropertiesFile file = (PropertiesFile) element;
        List<PsiElement> elements = new ArrayList<>();
        elements.add(file.getContainingFile());
        for (IProperty property : file.getProperties()) {
            elements.add(property.getPsiElement());
        }
        for (PsiElement psiElement : elements) {
            SafeDeleteProcessor.findGenericElementUsages(psiElement, result, allElementsToDelete);
        }
        return new NonCodeUsageSearchInfo(SafeDeleteProcessor.getDefaultInsideDeletedCondition(allElementsToDelete), elements);
    }

    @Override
    public Collection<PsiElement> getElementsToSearch(PsiElement element, Collection<PsiElement> allElementsToDelete) {
        return Collections.singletonList(element);
    }

    @Override
    public Collection<PsiElement> getAdditionalElementsToDelete(
        PsiElement element,
        Collection<PsiElement> allElementsToDelete,
        boolean askUser
    ) {
        return null;
    }

    @Override
    public Collection<LocalizeValue> findConflicts(PsiElement element, PsiElement[] allElementsToDelete) {
        return null;
    }

    @Override
    public UsageInfo[] preprocessUsages(Project project, UsageInfo[] usages) {
        return usages;
    }

    @Override
    public void prepareForDeletion(PsiElement element) throws IncorrectOperationException {
    }

    @Override
    public boolean isToSearchInComments(PsiElement element) {
        return RefactoringSettings.getInstance().SAFE_DELETE_SEARCH_IN_COMMENTS;
    }

    @Override
    public boolean isToSearchForTextOccurrences(PsiElement element) {
        return RefactoringSettings.getInstance().SAFE_DELETE_SEARCH_IN_NON_JAVA;
    }

    @Override
    public void setToSearchInComments(PsiElement element, boolean enabled) {
        RefactoringSettings.getInstance().SAFE_DELETE_SEARCH_IN_COMMENTS = enabled;
    }

    @Override
    public void setToSearchForTextOccurrences(PsiElement element, boolean enabled) {
        RefactoringSettings.getInstance().SAFE_DELETE_SEARCH_IN_NON_JAVA = enabled;
    }
}
