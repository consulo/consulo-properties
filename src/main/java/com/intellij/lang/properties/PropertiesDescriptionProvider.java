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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.ide.localize.IdeLocalize;
import consulo.language.editor.refactoring.util.DeleteTypeDescriptionLocation;
import consulo.language.psi.ElementDescriptionLocation;
import consulo.language.psi.ElementDescriptionProvider;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.usage.UsageViewLongNameLocation;

import org.jspecify.annotations.Nullable;

/**
 * @author yole
 */
@ExtensionImpl
public class PropertiesDescriptionProvider implements ElementDescriptionProvider {
    @Override
    @RequiredReadAction
    public LocalizeValue getElementDescription(PsiElement element, @Nullable ElementDescriptionLocation location) {
        if (element instanceof IProperty property) {
            if (location instanceof DeleteTypeDescriptionLocation deleteTypeDescriptionLocation) {
                int count = deleteTypeDescriptionLocation.isPlural() ? 2 : 1;
                return IdeLocalize.promptDeleteProperty(count);
            }
            if (location instanceof UsageViewLongNameLocation) {
                return LocalizeValue.ofNullable(property.getKey());
            }
        }
        return LocalizeValue.empty();
    }
}
