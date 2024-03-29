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
package com.intellij.lang.properties.projectView;

import consulo.annotation.component.ExtensionImpl;
import consulo.project.ui.view.tree.AbstractTreeNode;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.language.editor.PlatformDataKeys;
import consulo.application.dumb.DumbAware;
import consulo.project.Project;
import consulo.language.psi.PsiFile;
import consulo.project.ui.view.tree.TreeStructureProvider;
import consulo.project.ui.view.tree.ViewSettings;
import consulo.util.dataholder.Key;
import jakarta.inject.Inject;

import java.util.*;

@ExtensionImpl
public class ResourceBundleGrouper implements TreeStructureProvider, DumbAware {
  private final Project myProject;

  @Inject
  public ResourceBundleGrouper(Project project) {
    myProject = project;
  }

  public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings) {
    if (parent instanceof ResourceBundleNode) return children;

    Map<ResourceBundle,Collection<PropertiesFile>> childBundles = new HashMap<>();
    for (AbstractTreeNode child : children) {
      Object f = child.getValue();
      if (f instanceof PsiFile) {
        PropertiesFile propertiesFile = PropertiesUtil.getPropertiesFile((PsiFile)f);
        if (propertiesFile != null) {
          ResourceBundle bundle = propertiesFile.getResourceBundle();
          Collection<PropertiesFile> files = childBundles.get(bundle);
          if (files == null) {
            files = new ArrayList<>();
            childBundles.put(bundle, files);
          }
          files.add(propertiesFile);
        }
      }
    }

    List<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>();
    for (Map.Entry<ResourceBundle, Collection<PropertiesFile>> entry : childBundles.entrySet()) {
      ResourceBundle resourceBundle = entry.getKey();
      Collection<PropertiesFile> files = entry.getValue();
      if (files.size() != 1) {
        result.add(new ResourceBundleNode(myProject, resourceBundle, settings));
      }
    }
    for (AbstractTreeNode child : children) {
      Object f = child.getValue();
      if (f instanceof PsiFile) {
        PropertiesFile propertiesFile = PropertiesUtil.getPropertiesFile((PsiFile)f);
        if (propertiesFile != null) {
          ResourceBundle bundle = propertiesFile.getResourceBundle();
          if (childBundles.get(bundle).size() != 1) {
            continue;
          }
        }
      }
      result.add(child);
    }

    return result;
  }

  public Object getData(Collection<AbstractTreeNode> selected, Key<?> dataName) {
    if (selected == null) return null;
    for (AbstractTreeNode selectedElement : selected) {
      Object element = selectedElement.getValue();
      if (PlatformDataKeys.DELETE_ELEMENT_PROVIDER == dataName) {
        if (element instanceof ResourceBundle) {
          return new ResourceBundleDeleteProvider((ResourceBundle)element);
        }
      }
    }
    if (ResourceBundle.ARRAY_DATA_KEY == dataName) {
      final List<ResourceBundle> selectedElements = new ArrayList<ResourceBundle>();
      for (AbstractTreeNode node : selected) {
        final Object value = node.getValue();
        if (value instanceof ResourceBundle) {
          selectedElements.add((ResourceBundle)value);
        }
      }
      return selectedElements.isEmpty() ? null : selectedElements.toArray(new ResourceBundle[selectedElements.size()]);
    }
    return null;
  }
}
