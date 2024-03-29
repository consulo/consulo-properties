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

/**
 * @author Alexey
 */
package com.intellij.lang.properties.editor;

import consulo.application.AllIcons;
import consulo.colorScheme.TextAttributes;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import com.intellij.lang.properties.PropertiesHighlighter;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.ResourceBundle;
import consulo.navigation.ItemPresentation;
import consulo.ui.ex.ColoredItemPresentation;
import consulo.colorScheme.EditorColorsManager;
import consulo.colorScheme.TextAttributesKey;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.ui.style.StandardColors;

public class ResourceBundlePropertyStructureViewElement implements StructureViewTreeElement {
  private final String myPropertyName;
  private final Project myProject;
  private final ResourceBundle myResourceBundle;
  private String myPresentableName;

  private static final TextAttributesKey INCOMPLETE_PROPERTY_KEY;

  static {
    TextAttributes textAttributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(PropertiesHighlighter.PROPERTY_KEY).clone();
    textAttributes.setForegroundColor(StandardColors.RED);
    INCOMPLETE_PROPERTY_KEY = TextAttributesKey.createTextAttributesKey("INCOMPLETE_PROPERTY_KEY", textAttributes);

  }
  public ResourceBundlePropertyStructureViewElement(final Project project, final ResourceBundle resourceBundle, String propertyName) {
    myProject = project;
    myResourceBundle = resourceBundle;
    myPropertyName = propertyName;
  }

  public void setPresentableName(final String presentableName) {
    myPresentableName = presentableName;
  }

  public String getValue() {
    return myPropertyName;
  }

  public StructureViewTreeElement[] getChildren() {
    return EMPTY_ARRAY;
  }

  public ItemPresentation getPresentation() {
    return new ColoredItemPresentation() {
      public String getPresentableText() {
        return myPresentableName == null ? myPropertyName : myPresentableName;
      }

      public String getLocationString() {
        return null;
      }

      public Image getIcon() {
        return AllIcons.Nodes.Property;
      }

      @Override
      public TextAttributesKey getTextAttributesKey() {
        boolean isComplete = PropertiesUtil.isPropertyComplete(myProject, myResourceBundle, myPropertyName);

        if (isComplete) {
          return PropertiesHighlighter.PROPERTY_KEY;
        }
        return INCOMPLETE_PROPERTY_KEY;
      }
    };
  }

  public void navigate(boolean requestFocus) {
    //todo
  }

  public boolean canNavigate() {
    return false;
  }

  public boolean canNavigateToSource() {
    return false;
  }

}
