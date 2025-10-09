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
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
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
public class RemovePropertyLocalFix implements LocalQuickFix {
    private static final Logger LOG = Logger.getInstance(RemovePropertyLocalFix.class);
    public static final RemovePropertyLocalFix INSTANCE = new RemovePropertyLocalFix();

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return PropertiesLocalize.removePropertyQuickFixName();
    }

    @Override
    @RequiredWriteAction
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        Property property = PsiTreeUtil.getParentOfType(element, Property.class, false);
        if (property == null) {
            return;
        }
        try {
            new RemovePropertyFix(property).invoke(project, null, property.getPropertiesFile().getContainingFile());
        }
        catch (IncorrectOperationException e) {
            LOG.error(e);
        }
    }
}
