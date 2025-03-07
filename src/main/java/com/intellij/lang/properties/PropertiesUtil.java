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

import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.PropertyKeyIndex;
import com.intellij.lang.properties.xml.XmlPropertiesFile;
import com.intellij.lang.properties.xml.XmlPropertiesIndex;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.ApplicationManager;
import consulo.application.util.function.Computable;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.module.Module;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author cdr
 */
public class PropertiesUtil {
  private PropertiesUtil() {
  }

  @RequiredReadAction
  public static boolean isPropertiesFile(VirtualFile file, Project project) {
    return getPropertiesFile(PsiManager.getInstance(project).findFile(file)) != null;
  }

  @RequiredReadAction
  public static boolean isPropertiesFile(PsiFile file) {
    return getPropertiesFile(file) != null;
  }

  @Nullable
  @RequiredReadAction
  public static PropertiesFile getPropertiesFile(@Nullable PsiFile file) {
    if (file == null) return null;
    return file instanceof PropertiesFile ? (PropertiesFile)file : XmlPropertiesFile.getPropertiesFile(file);
  }

  @Nonnull
  public static List<IProperty> findPropertiesByKey(final Project project, final String key) {
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    final ArrayList<IProperty> properties =
      new ArrayList<IProperty>(PropertyKeyIndex.getInstance().get(key, project, scope));
    final Set<VirtualFile> files = new HashSet<VirtualFile>();
    FileBasedIndex.getInstance().processValues(XmlPropertiesIndex.NAME, new XmlPropertiesIndex.Key(key), null, new FileBasedIndex.ValueProcessor<String>() {
      @Override
      public boolean process(VirtualFile file, String value) {
        if (files.add(file)) {
          PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
          PropertiesFile propertiesFile = XmlPropertiesFile.getPropertiesFile(psiFile);
          if (propertiesFile != null) {
            properties.addAll(propertiesFile.findPropertiesByKey(key));
          }
        }
        return false;
      }
    }, scope);
    return properties;
  }

  public static boolean isPropertyComplete(final Project project, ResourceBundle resourceBundle, String propertyName) {
    List<PropertiesFile> propertiesFiles = resourceBundle.getPropertiesFiles(project);
    for (PropertiesFile propertiesFile : propertiesFiles) {
      if (propertiesFile.findPropertyByKey(propertyName) == null) return false;
    }
    return true;
  }

  @Nonnull
  public static String getBaseName(@Nonnull VirtualFile virtualFile) {
    String name = virtualFile.getNameWithoutExtension();

    List<String> parts = StringUtil.split(name, "_");
    if (parts.size() == 1) return parts.get(0);
    if (parts.size() == 0) return "";

    String baseName = parts.get(0);
    for (int i = 1; i< parts.size(); i++) {
      String part = parts.get(i);
      if (part.length() == 2) {
        break;
      }
      baseName += "_";
      baseName += part;
    }

    return baseName;
  }

  /**
   * messages_en.properties is a parent of the messages_en_US.properties
   */
  @Nullable
  public static PropertiesFile getParent(PropertiesFile file, List<PropertiesFile> candidates) {
    VirtualFile virtualFile = file.getVirtualFile();
    if (virtualFile == null) return null;
    String name = virtualFile.getNameWithoutExtension();
    String[] parts = name.split("_");
    if (parts.length == 1) return null;
    List<String> partsList = Arrays.asList(parts);
    for (int i=parts.length-1; i>=1;i--) {
      String parentName = StringUtil.join(partsList.subList(0, i), "_") + "." + virtualFile.getExtension();
      for (PropertiesFile candidate : candidates) {
        if (parentName.equals(candidate.getName())) return candidate;
      }
    }
    return null;
  }

  @Nullable
  public static String getFullName(final PropertiesFile psiFile) {
    return ApplicationManager.getApplication().runReadAction(new Supplier<String>() {
      public String get() {
        PsiDirectory directory = psiFile.getParent();
        String packageQualifiedName = getPackageQualifiedName(directory);
        if (packageQualifiedName == null) {
          return null;
        }
        StringBuilder qName = new StringBuilder(packageQualifiedName);
          if (qName.length() > 0) {
            qName.append(".");
          }
        final VirtualFile virtualFile = psiFile.getVirtualFile();
        assert virtualFile != null;
        qName.append(getBaseName(virtualFile));
        return qName.toString();
      }
    });
  }

  @Nonnull
  public static Locale getLocale(VirtualFile propertiesFile) {
    String name = propertiesFile.getNameWithoutExtension();
    String tail = StringUtil.trimStart(name, getBaseName(propertiesFile));
    tail = StringUtil.trimStart(tail, "_");
    String[] parts = tail.split("_");
    String language = parts.length == 0 ? "" : parts[0];
    String country = "";
    String variant = "";
    if (parts.length >= 2 && parts[1].length() == 2) {
      country = parts[1];
      for (int i = 2; i < parts.length; i++) {
        String part = parts[i];
        if (variant.length() != 0) variant += "_";
        variant += part;
      }
    }

    return new Locale(language,country,variant);
  }

  @Nonnull
  public static List<IProperty> findAllProperties(Project project, @Nonnull ResourceBundle resourceBundle, String key) {
    List<IProperty> result = new SmartList<IProperty>();
    List<PropertiesFile> propertiesFiles = resourceBundle.getPropertiesFiles(project);
    for (PropertiesFile propertiesFile : propertiesFiles) {
      result.addAll(propertiesFile.findPropertiesByKey(key));
    }
    return result;
  }

  public static boolean isUnescapedBackSlashAtTheEnd (String text) {
    boolean result = false;
    for (int i = text.length()-1; i>=0; i--) {
      if (text.charAt(i) == '\\') {
        result = !result;
      }
      else {
        break;
      }
    }
    return result;
  }

  @Nullable
  public static PropertiesFile getPropertiesFile(@Nonnull String bundleName,
                                                 @Nonnull Module searchFromModule,
                                                 @Nullable Locale locale) {
    PropertiesReferenceManager manager = PropertiesReferenceManager.getInstance(searchFromModule.getProject());
    return manager.findPropertiesFile(searchFromModule, bundleName, locale);
  }

  @Nullable
  public static String getPackageQualifiedName(@Nonnull PsiDirectory directory) {
    return ProjectRootManager.getInstance(directory.getProject()).getFileIndex().getPackageNameByDirectory(directory.getVirtualFile());
  }

  public static ResourceBundle getResourceBundle(final PsiFile containingFile) {
    VirtualFile virtualFile = containingFile.getVirtualFile();
    if (!containingFile.isValid() || virtualFile == null) {
      return EmptyResourceBundle.getInstance();
    }
    String baseName = getBaseName(virtualFile);
    PsiDirectory directory = ApplicationManager.getApplication().runReadAction(new Computable<PsiDirectory>() {
      @Nullable
      public PsiDirectory compute() {
        return containingFile.getContainingDirectory();
    }});
    if (directory == null) return EmptyResourceBundle.getInstance();
    return new ResourceBundleImpl(directory.getVirtualFile(), baseName);
  }
}
