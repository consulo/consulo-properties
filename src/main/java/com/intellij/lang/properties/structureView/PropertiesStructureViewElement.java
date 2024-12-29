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
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;

/**
 * @author max
 */
public class PropertiesStructureViewElement implements StructureViewTreeElement {
  private final Property myProperty;
  private String myPresentableName;

  public PropertiesStructureViewElement(final Property element) {
    myProperty = element;
  }

  @Override
  public Property getValue() {
    return myProperty;
  }

  @Override
  public void navigate(boolean requestFocus) {
    myProperty.navigate(requestFocus);
  }

  @Override
  public boolean canNavigate() {
    return myProperty.canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return myProperty.canNavigateToSource();
  }

  @Nonnull
  @Override
  public StructureViewTreeElement[] getChildren() {
    return EMPTY_ARRAY;
  }

  @Nonnull
  @Override
  public ItemPresentation getPresentation() {
    return new ItemPresentation() {
      @Override
      public String getPresentableText() {
        if (myPresentableName == null) {
          return myProperty.getUnescapedKey();
        }
        else {
          return myPresentableName;
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

  public void setPresentableName(final String presentableName) {
    myPresentableName = presentableName;
  }
}
