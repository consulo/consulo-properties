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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.Icon;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.Property;
import com.intellij.lang.properties.psi.impl.PropertiesFileImpl;
import com.intellij.navigation.ItemPresentation;
import consulo.awt.TargetAWT;
import consulo.ide.IconDescriptorUpdaters;

/**
 * @author max
 */
public class PropertiesFileStructureViewElement extends PsiTreeElementBase<PropertiesFileImpl> {

  protected PropertiesFileStructureViewElement(PropertiesFileImpl propertiesFile) {
    super(propertiesFile);
  }

  @Nonnull
  public Collection<StructureViewTreeElement> getChildrenBase() {
    List<? extends IProperty> properties = getElement().getProperties();

    Collection<StructureViewTreeElement> elements = new ArrayList<StructureViewTreeElement>(properties.size());
    for (IProperty property : properties) {
      elements.add(new PropertiesStructureViewElement((Property)property));
    }
    return elements;
  }

  public String getPresentableText() {
    return getElement().getName();
  }

  public ItemPresentation getPresentation() {
    return new ItemPresentation() {
      public String getPresentableText() {
        return PropertiesFileStructureViewElement.this.getPresentableText();
      }

      public String getLocationString() {
        return null;
      }

      public Icon getIcon(boolean open) {
        return TargetAWT.to(IconDescriptorUpdaters.getIcon(getElement(), 0));
      }
    };
  }
}
