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
package com.intellij.lang.properties.inspection.unused;

import com.intellij.lang.properties.PropertiesBundle;
import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.PropertySuppressableInspectionBase;
import com.intellij.lang.properties.RemovePropertyLocalFix;
import com.intellij.lang.properties.psi.Property;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.PsiSearchHelper;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author cdr
 */
@ExtensionImpl
public class UnusedPropertyInspection extends PropertySuppressableInspectionBase
{
	@Override
	@Nonnull
	public String getDisplayName()
	{
		return PropertiesBundle.message("unused.property.inspection.display.name");
	}

	@Override
	@Nonnull
	public String getShortName()
	{
		return "UnusedProperty";
	}

	@Nonnull
	@Override
	public HighlightDisplayLevel getDefaultLevel()
	{
		return HighlightDisplayLevel.WARNING;
	}

	@Nullable
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}

	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder,
										  final boolean isOnTheFly,
										  @Nonnull final LocalInspectionToolSession session,
										  Object state)
	{
		final PsiFile file = session.getFile();
		Module module = ModuleUtilCore.findModuleForPsiElement(file);
		if(module == null)
		{
			return super.buildVisitor(holder, isOnTheFly, session, state);
		}

		final GlobalSearchScope searchScope = GlobalSearchScope.moduleWithDependentsScope(module);
		final PsiSearchHelper searchHelper = PsiSearchHelper.getInstance(file.getProject());
		return new PsiElementVisitor()
		{
			@Override
			public void visitElement(PsiElement element)
			{
				if(!(element instanceof Property))
				{
					return;
				}
				Property property = (Property) element;

				final ProgressIndicator original = ProgressManager.getInstance().getProgressIndicator();
				if(original != null)
				{
					if(original.isCanceled())
					{
						return;
					}
					original.setText(PropertiesBundle.message("searching.for.property.key.progress.text", property.getUnescapedKey()));
				}

				if(ImplicitPropertyUsageProvider.isImplicitlyUsed(property))
				{
					return;
				}

				String name = property.getName();
				if(name == null)
				{
					return;
				}

				PsiSearchHelper.SearchCostResult cheapEnough = searchHelper.isCheapEnoughToSearch(name, searchScope, file, original);
				if(cheapEnough == PsiSearchHelper.SearchCostResult.TOO_MANY_OCCURRENCES)
				{
					return;
				}

				if(cheapEnough != PsiSearchHelper.SearchCostResult.ZERO_OCCURRENCES &&
						ReferencesSearch.search(property, searchScope, false).findFirst() != null)
				{
					return;
				}

				final ASTNode propertyNode = property.getNode();
				assert propertyNode != null;

				ASTNode[] nodes = propertyNode.getChildren(null);
				PsiElement key = nodes.length == 0 ? property : nodes[0].getPsi();
				String description = PropertiesBundle.message("unused.property.problem.descriptor.name");

				holder.registerProblem(key, description, ProblemHighlightType.LIKE_UNUSED_SYMBOL, RemovePropertyLocalFix.INSTANCE);
			}
		};
	}
}
