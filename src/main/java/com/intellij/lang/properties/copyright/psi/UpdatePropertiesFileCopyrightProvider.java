package com.intellij.lang.properties.copyright.psi;

import com.intellij.lang.properties.PropertiesFileType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.copyright.UpdateCopyrightsProvider;
import consulo.language.copyright.UpdatePsiFileCopyright;
import consulo.language.copyright.config.CopyrightFileConfig;
import consulo.language.copyright.config.CopyrightProfile;
import consulo.language.copyright.ui.TemplateCommentPanel;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.virtualFileSystem.fileType.FileType;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class UpdatePropertiesFileCopyrightProvider extends UpdateCopyrightsProvider
{
	@Nonnull
	@Override
	public FileType getFileType()
	{
		return PropertiesFileType.INSTANCE;
	}

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
