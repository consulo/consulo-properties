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
package com.intellij.lang.properties.refactoring;

import com.intellij.lang.properties.references.PropertyReferenceBase;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import consulo.dataContext.DataContext;
import consulo.language.editor.LangDataKeys;
import consulo.language.editor.TargetElementUtil;
import consulo.language.editor.refactoring.rename.PsiElementRenameHandler;
import consulo.language.psi.*;
import consulo.project.Project;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class PropertyRenameHandler extends PsiElementRenameHandler
{
	public boolean isAvailableOnDataContext(final DataContext dataContext)
	{
		final Editor editor = dataContext.getData(LangDataKeys.EDITOR);
		if(editor != null)
		{
			if(getPsiElement(editor) != null)
			{
				return true;
			}
		}
		return false;
	}

	@Nullable
	private static PsiElement getPsiElement(final Editor editor)
	{
		final PsiReference reference = TargetElementUtil.findReference(editor);
		if(reference instanceof PropertyReferenceBase)
		{
			final ResolveResult[] resolveResults = ((PropertyReferenceBase) reference).multiResolve(false);
			return resolveResults.length > 0 ? resolveResults[0].getElement() : null;
		}
		else if(reference instanceof PsiMultiReference)
		{
			final PsiReference[] references = ((PsiMultiReference) reference).getReferences();
			for(PsiReference psiReference : references)
			{
				if(psiReference instanceof PropertyReferenceBase)
				{
					final ResolveResult[] resolveResults = ((PropertyReferenceBase) psiReference).multiResolve(false);
					if(resolveResults.length > 0)
					{
						return resolveResults[0].getElement();
					}
				}
			}
		}
		return null;
	}

	@Override
	public void invoke(@Nonnull final Project project, final Editor editor, final PsiFile file, final DataContext dataContext)
	{
		PsiElement element = getPsiElement(editor);
		editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
		final PsiElement nameSuggestionContext = file.findElementAt(editor.getCaretModel().getOffset());
		invoke(element, project, nameSuggestionContext, editor);
	}
}
