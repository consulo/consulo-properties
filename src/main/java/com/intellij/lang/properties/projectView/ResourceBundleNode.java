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
package com.intellij.lang.properties.projectView;

import com.intellij.lang.properties.PropertiesBundle;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.editor.ResourceBundleAsVirtualFile;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.application.AllIcons;
import consulo.fileEditor.FileEditorManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.navigation.OpenFileDescriptor;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.project.ui.view.tree.ProjectViewNode;
import consulo.project.ui.view.tree.PsiFileNode;
import consulo.project.ui.view.tree.ViewSettings;
import consulo.ui.ex.tree.PresentationData;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResourceBundleNode extends ProjectViewNode<ResourceBundle> {
  public ResourceBundleNode(Project project, ResourceBundle resourceBundle, final ViewSettings settings) {
    super(project, resourceBundle, settings);
  }

  @Nonnull
  public Collection<AbstractTreeNode> getChildren() {
    List<PropertiesFile> propertiesFiles = getValue().getPropertiesFiles(myProject);
    Collection<AbstractTreeNode> children = new ArrayList<AbstractTreeNode>();
    for (PropertiesFile propertiesFile : propertiesFiles) {
      AbstractTreeNode node = new PsiFileNode(myProject, propertiesFile.getContainingFile(), getSettings());
      children.add(node);
    }
    return children;
  }

  public boolean contains(@Nonnull VirtualFile file) {
    if (!file.isValid()) return false;
    PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
    PropertiesFile propertiesFile = PropertiesUtil.getPropertiesFile(psiFile);
    return propertiesFile != null && getValue().getPropertiesFiles(myProject).contains(propertiesFile);
  }

  public VirtualFile getVirtualFile() {
    final List<PropertiesFile> list = getValue().getPropertiesFiles(myProject);
    if (!list.isEmpty()) {
      return list.get(0).getVirtualFile();
    }
    return null;
  }

  public void update(PresentationData presentation) {
    presentation.setIcon(AllIcons.Nodes.ResourceBundle);
    presentation.setPresentableText(PropertiesBundle.message("project.view.resource.bundle.tree.node.text", getValue().getBaseName()));
  }

  public boolean canNavigateToSource() {
    return true;
  }

  public boolean canNavigate() {
    return true;
  }

  public void navigate(final boolean requestFocus) {
    OpenFileDescriptor descriptor = OpenFileDescriptorFactory.getInstance(getProject()).builder(new ResourceBundleAsVirtualFile(getValue())).build();
    FileEditorManager.getInstance(getProject()).openTextEditor(descriptor, requestFocus);
  }

  public boolean isSortByFirstChild() {
    return true;
  }

  public Comparable getTypeSortKey() {
    return new PsiFileNode.ExtensionSortKey(PropertiesFileType.INSTANCE.getDefaultExtension());
  }
}
