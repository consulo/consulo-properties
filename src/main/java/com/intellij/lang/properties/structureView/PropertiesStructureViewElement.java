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
package com.intellij.lang.properties.structureView;

import com.intellij.lang.properties.psi.Property;
import consulo.annotation.access.RequiredReadAction;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;

/**
 * @author max
 */
public class PropertiesStructureViewElement implements StructureViewTreeElement {
    private final Property myProperty;
    private String myPresentableName;

    public PropertiesStructureViewElement(Property element) {
        myProperty = element;
    }

    @Override
    public Property getValue() {
        return myProperty;
    }

    @Override
    @RequiredReadAction
    public void navigate(boolean requestFocus) {
        myProperty.navigate(requestFocus);
    }

    @Override
    @RequiredReadAction
    public boolean canNavigate() {
        return myProperty.canNavigate();
    }

    @Override
    @RequiredReadAction
    public boolean canNavigateToSource() {
        return myProperty.canNavigateToSource();
    }

    @Override
    public StructureViewTreeElement[] getChildren() {
        return EMPTY_ARRAY;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public LocalizeValue getPresentableText() {
                if (myPresentableName == null) {
                    return LocalizeValue.ofNullable(myProperty.getUnescapedKey());
                }
                else {
                    return LocalizeValue.of(myPresentableName);
                }
            }

            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            @RequiredReadAction
            public Image getIcon() {
                return IconDescriptorUpdaters.getIcon(myProperty, 0);
            }
        };
    }

    public void setPresentableName(String presentableName) {
        myPresentableName = presentableName;
    }
}
