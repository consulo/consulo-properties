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
 * Date: 08.05.2005
 * Time: 0:17:52
 */
package com.intellij.lang.properties;

import java.util.List;

import javax.annotation.Nonnull;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;

public interface ResourceBundle {
  Key<ResourceBundle[]> ARRAY_DATA_KEY = Key.create("resource.bundle.array");

  @Nonnull
  List<PropertiesFile> getPropertiesFiles(final Project project);

  @Nonnull
  PropertiesFile getDefaultPropertiesFile(final Project project);

  @Nonnull
  String getBaseName();

  @Nonnull
  VirtualFile getBaseDirectory();
}
