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

import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.dataContext.DataContext;
import consulo.language.editor.CommonDataKeys;
import consulo.language.editor.refactoring.safeDelete.SafeDeleteHandler;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.ui.ex.DeleteProvider;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

/**
 * @author cdr
 */
class ResourceBundleDeleteProvider implements DeleteProvider
{
	private static final Function<PropertiesFile, PsiElement> MAPPER = PropertiesFile::getContainingFile;
	private final ResourceBundle myResourceBundle;

	public ResourceBundleDeleteProvider(ResourceBundle resourceBundle)
	{
		myResourceBundle = resourceBundle;
	}

	public void deleteElement(@Nonnull DataContext dataContext)
	{
		final Project project = dataContext.getData(CommonDataKeys.PROJECT);
		List<PropertiesFile> propertiesFiles = myResourceBundle.getPropertiesFiles(project);
		assert project != null;
		new SafeDeleteHandler().invoke(project, ContainerUtil.map2Array(propertiesFiles, PsiElement.class, MAPPER), dataContext);
	}

	public boolean canDeleteElement(@Nonnull DataContext dataContext)
	{
		final Project project = dataContext.getData(CommonDataKeys.PROJECT);
		return project != null;
	}
}
