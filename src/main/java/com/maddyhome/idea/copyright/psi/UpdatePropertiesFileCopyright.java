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

package com.maddyhome.idea.copyright.psi;

import javax.annotation.Nonnull;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.maddyhome.idea.copyright.CopyrightProfile;
import com.maddyhome.idea.copyright.ui.TemplateCommentPanel;
import consulo.copyright.config.CopyrightFileConfig;

public class UpdatePropertiesFileCopyright extends UpdatePsiFileCopyright
{
	public static class UpdatePropertiesFileCopyrightProvider extends UpdateCopyrightsProvider
	{
		@Nonnull
		@Override
		public UpdatePsiFileCopyright createInstance(@Nonnull PsiFile file, @Nonnull CopyrightProfile copyrightProfile)
		{
			return new UpdatePropertiesFileCopyright(file, copyrightProfile);
		}

		@Nonnull
		@Override
		public CopyrightFileConfig createDefaultOptions()
		{
			return new CopyrightFileConfig();
		}

		@Nonnull
		@Override
		public TemplateCommentPanel createConfigurable(@Nonnull Project project, @Nonnull TemplateCommentPanel parentPane, @Nonnull FileType fileType)
		{
			return new TemplateCommentPanel(fileType, parentPane, project);
		}
	}

	public UpdatePropertiesFileCopyright(@Nonnull PsiFile psiFile, @Nonnull CopyrightProfile copyrightProfile)
	{
		super(psiFile, copyrightProfile);
	}

	@Override
	protected void scanFile()
	{
		PsiElement first = getFile().getFirstChild(); // PropertiesList
		PsiElement last = first;
		PsiElement next = first;
		while(next != null)
		{
			if(next instanceof PsiComment || next instanceof PsiWhiteSpace)
			{
				next = getNextSibling(next);
			}
			else
			{
				break;
			}
			last = next;
		}

		if(first != null)
		{
			checkComments(first, last, true);
		}
	}
}
