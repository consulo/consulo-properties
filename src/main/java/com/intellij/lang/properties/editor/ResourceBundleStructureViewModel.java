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
package com.intellij.lang.properties.editor;

import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.structureView.GroupByWordPrefixes;
import com.intellij.lang.properties.structureView.PropertiesSeparatorManager;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.event.FileEditorPositionListener;
import consulo.fileEditor.structureView.event.ModelListener;
import consulo.fileEditor.structureView.tree.Filter;
import consulo.fileEditor.structureView.tree.Grouper;
import consulo.fileEditor.structureView.tree.Sorter;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

/**
 * @author max
 */
public class ResourceBundleStructureViewModel implements PropertiesGroupingStructureViewModel {
  private final Project myProject;
  private final ResourceBundle myResourceBundle;
  private final GroupByWordPrefixes myGroupByWordPrefixes;

  public ResourceBundleStructureViewModel(final Project project, ResourceBundle root) {
    myProject = project;
    myResourceBundle = root;
    String separator = PropertiesSeparatorManager.getInstance().getSeparator(project, new ResourceBundleAsVirtualFile(myResourceBundle));
    myGroupByWordPrefixes = new GroupByWordPrefixes(separator);
  }

  public void setSeparator(String separator) {
    myGroupByWordPrefixes.setSeparator(separator);
    PropertiesSeparatorManager.getInstance().setSeparator(new ResourceBundleAsVirtualFile(myResourceBundle), separator);
  }

  public String getSeparator() {
    return myGroupByWordPrefixes.getSeparator();
  }

  @Nonnull
  public StructureViewTreeElement getRoot() {
    return new ResourceBundleFileStructureViewElement(myProject, myResourceBundle);
  }

  @Nonnull
  public Grouper[] getGroupers() {
    return new Grouper[]{myGroupByWordPrefixes};
  }

  @Nonnull
  public Sorter[] getSorters() {
    return new Sorter[] {Sorter.ALPHA_SORTER};
  }

  @Nonnull
  public Filter[] getFilters() {
    return Filter.EMPTY_ARRAY;
  }

  public Object getCurrentEditorElement() {
    return null;
  }

  public void addEditorPositionListener(FileEditorPositionListener listener) {

  }

  public void removeEditorPositionListener(FileEditorPositionListener listener) {

  }

  public void addModelListener(ModelListener modelListener) {

  }

  public void removeModelListener(ModelListener modelListener) {

  }

  public void dispose() {

  }

  public boolean shouldEnterElement(final Object element) {
    return false;
  }
}
