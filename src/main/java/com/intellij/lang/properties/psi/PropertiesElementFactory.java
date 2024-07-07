/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package com.intellij.lang.properties.psi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import jakarta.annotation.Nonnull;

import consulo.language.psi.PsiFileFactory;
import org.jetbrains.annotations.NonNls;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.lang.properties.editor.ResourceBundleUtil;
import consulo.project.Project;
import consulo.application.util.UserDataCache;
import consulo.util.lang.StringUtil;

/**
 * @author cdr
 */
public class PropertiesElementFactory {
  private static final UserDataCache<PropertiesFile,Project,Void> PROPERTIES = new UserDataCache<PropertiesFile, Project, Void>("system.properties.file") {

    protected PropertiesFile compute(Project project, Void p) {
      return createPropertiesFile(project, System.getProperties(), "system");
    }
  };

  @Nonnull
  public static IProperty createProperty(@Nonnull Project project, @NonNls @Nonnull String name, @NonNls @Nonnull String value) {
    String text = escape(name) + "=" + escapeValue(value);
    final PropertiesFile dummyFile = createPropertiesFile(project, text);
    return dummyFile.getProperties().get(0);
  }

  @Nonnull
  public static PropertiesFile createPropertiesFile(@Nonnull Project project, @NonNls @Nonnull String text) {
    @NonNls String filename = "dummy." + PropertiesFileType.INSTANCE.getDefaultExtension();
    return (PropertiesFile) PsiFileFactory.getInstance(project)
      .createFileFromText(filename, PropertiesFileType.INSTANCE, text);
  }

  @Nonnull
  public static PropertiesFile createPropertiesFile(@Nonnull Project project, Properties properties, String fileName) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    try {
      properties.store(stream, "");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    @NonNls String filename = fileName + "." + PropertiesFileType.INSTANCE.getDefaultExtension();
    return (PropertiesFile)PsiFileFactory.getInstance(project)
      .createFileFromText(filename, PropertiesFileType.INSTANCE, stream.toString());
  }

  @Nonnull
  public static PropertiesFile getSystemProperties(@Nonnull Project project) {
    return PROPERTIES.get(project, null);
  }

  @Nonnull
  private static String escape(@Nonnull String name) {
    if (StringUtil.startsWithChar(name, '#')) {
      name = escapeChar(name, '#');
    }
    if (StringUtil.startsWithChar(name, '!')) {
      name = escapeChar(name, '!');
    }
    name = escapeChar(name, '=');
    name = escapeChar(name, ':');
    name = escapeChar(name, ' ');
    name = escapeChar(name, '\t');
    return name;
  }

  @Nonnull
  private static String escapeChar(@Nonnull String name, char c) {
    int offset = 0;
    while (true) {
      int i = name.indexOf(c, offset);
      if (i == -1) return name;
      if (i == 0 || name.charAt(i - 1) != '\\') {
        name = name.substring(0, i) + '\\' + name.substring(i);
      }
      offset = i + 2;
    }
  }

  public static String escapeValue(String value) {
    return ResourceBundleUtil.fromValueEditorToPropertyValue(value);
  }
}
