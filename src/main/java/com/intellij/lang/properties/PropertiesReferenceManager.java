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
import com.intellij.lang.properties.xml.XmlPropertiesIndex;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FileTypeIndex;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.module.Module;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author max
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class PropertiesReferenceManager
{
	private final PsiManager myPsiManager;
	private final DumbService myDumbService;

	public static PropertiesReferenceManager getInstance(Project project)
	{
		return project.getInstance(PropertiesReferenceManager.class);
	}

	@Inject
	public PropertiesReferenceManager(PsiManager psiManager, DumbService dumbService)
	{
		myPsiManager = psiManager;
		myDumbService = dumbService;
	}

	@Nonnull
	public List<PropertiesFile> findPropertiesFiles(@Nonnull final Module module, final String bundleName)
	{
		return findPropertiesFiles(GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module), bundleName, BundleNameEvaluator.DEFAULT);
	}

	@Nonnull
	public List<PropertiesFile> findPropertiesFiles(@Nonnull final GlobalSearchScope searchScope,
													final String bundleName,
													BundleNameEvaluator bundleNameEvaluator)
	{


		final ArrayList<PropertiesFile> result = new ArrayList<PropertiesFile>();
		processPropertiesFiles(searchScope, new PropertiesFileProcessor()
		{
			public boolean process(String baseName, PropertiesFile propertiesFile)
			{
				if(baseName.equals(bundleName))
				{
					result.add(propertiesFile);
				}
				return true;
			}
		}, bundleNameEvaluator);
		return result;
	}

	@Nullable
	public PropertiesFile findPropertiesFile(final Module module,
											 final String bundleName,
											 final Locale locale)
	{
		List<PropertiesFile> propFiles = findPropertiesFiles(module, bundleName);
		if(locale != null)
		{
			for(PropertiesFile propFile : propFiles)
			{
				if(propFile.getLocale().equals(locale))
				{
					return propFile;
				}
			}
		}

		// fallback to default locale
		for(PropertiesFile propFile : propFiles)
		{
			if(propFile.getLocale().getLanguage().length() == 0 || propFile.getLocale().equals(Locale.getDefault()))
			{
				return propFile;
			}
		}

		// fallback to any file
		if(!propFiles.isEmpty())
		{
			return propFiles.get(0);
		}

		return null;
	}

	public String[] getPropertyFileBaseNames(@Nonnull final GlobalSearchScope searchScope, final BundleNameEvaluator bundleNameEvaluator)
	{
		final ArrayList<String> result = new ArrayList<String>();
		processPropertiesFiles(searchScope, new PropertiesFileProcessor()
		{
			public boolean process(String baseName, PropertiesFile propertiesFile)
			{
				result.add(baseName);
				return true;
			}
		}, bundleNameEvaluator);
		return ArrayUtil.toStringArray(result);
	}

	public boolean processAllPropertiesFiles(@Nonnull final PropertiesFileProcessor processor)
	{
		return processPropertiesFiles(GlobalSearchScope.allScope(myPsiManager.getProject()), processor, BundleNameEvaluator.DEFAULT);
	}

	public boolean processPropertiesFiles(@Nonnull final GlobalSearchScope searchScope,
										  @Nonnull final PropertiesFileProcessor processor,
										  @Nonnull final BundleNameEvaluator evaluator)
	{

		boolean result = FileBasedIndex.getInstance()
				.processValues(FileTypeIndex.NAME, PropertiesFileType.INSTANCE, null, (file, value) -> processFile(file, evaluator, processor), searchScope);
		if(!result)
		{
			return false;
		}

		return myDumbService.isDumb() || FileBasedIndex.getInstance()
				.processValues(XmlPropertiesIndex.NAME, XmlPropertiesIndex.MARKER_KEY, null, (file, value) -> processFile(file, evaluator, processor), searchScope);
	}

	private boolean processFile(VirtualFile file, BundleNameEvaluator evaluator, PropertiesFileProcessor processor)
	{
		final PsiFile psiFile = myPsiManager.findFile(file);
		PropertiesFile propertiesFile = PropertiesUtil.getPropertiesFile(psiFile);
		if(propertiesFile != null)
		{
			final String qName = evaluator.evaluateBundleName(psiFile);
			if(qName != null)
			{
				if(!processor.process(qName, propertiesFile))
				{
					return false;
				}
			}
		}
		return true;
	}
}
