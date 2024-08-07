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

/*
 * Created by IntelliJ IDEA.
 * User: Alexey
 * Date: 11.04.2005
 * Time: 1:26:45
 */
package com.intellij.lang.properties.psi;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.ResourceBundle;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface PropertiesFile {

  @Nonnull
  PsiFile getContainingFile();

  /**
   * @return All properties found in this file.
   */
  @Nonnull
  List<IProperty> getProperties();

  /**
   *
   * @param key the name of the property in the properties file
   * @return property corresponding to the key specified, or null if there is no property found.
   * If there are several properties with the same key, returns first from the top of the file property.
   */
  @Nullable
  IProperty findPropertyByKey(@Nonnull @NonNls String key);

  /**
   * @param key
   * @return All properties found in this file with the name specified.
   */
  @Nonnull
  List<IProperty> findPropertiesByKey(@Nonnull @NonNls String key);

  @Nonnull
  ResourceBundle getResourceBundle();
  @Nonnull
  Locale getLocale();

  /**
   * Adds property to the end of the file.
   *
   * @param property to add. Typically you create the property via {@link PropertiesElementFactory}.
   * @return newly added property.
   * It is this value you use to do actual PSI work, e.g. call {@link PsiElement#delete()} to remove this property from the file.
   * @throws IncorrectOperationException
   * @deprecated
   * @see #addProperty(String, String)
   */
  @Nonnull
  PsiElement addProperty(@Nonnull IProperty property) throws IncorrectOperationException;

  /**
   * Adds property to the the file after the specified property.
   * If anchor is null, property added to the beginning of the file.
   * @param property to add. Typically you create the property via {@link com.intellij.lang.properties.psi.PropertiesElementFactory}.
   * @param anchor property after which to add the new property
   * @return newly added property.
   * It is this value you use to do actual PSI work, e.g. call {@link PsiElement#delete()} to remove this property from the file.
   * @throws IncorrectOperationException
   */
  @Nonnull
  PsiElement addPropertyAfter(@Nonnull Property property, @Nullable Property anchor) throws IncorrectOperationException;

  IProperty addProperty(String key, String value);

  void removeProperties(@Nonnull String key);

  /**
   * @return Property key to the property value map.
   * Do not modify this map. It's no use anyway.
   */
  @Nonnull
  Map<String,String> getNamesMap();

  String getName();

  VirtualFile getVirtualFile();

  PsiDirectory getParent();

  Project getProject();

  String getText();
}