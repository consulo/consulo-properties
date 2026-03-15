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


@ExtensionImpl
public class UpdatePropertiesFileCopyrightProvider extends UpdateCopyrightsProvider
{
	@Override
	public FileType getFileType()
	{
		return PropertiesFileType.INSTANCE;
	}

	@Override
	public UpdatePsiFileCopyright createInstance(PsiFile file, CopyrightProfile copyrightProfile)
	{
		return new UpdatePropertiesFileCopyright(file, copyrightProfile);
	}

	@Override
	public CopyrightFileConfig createDefaultOptions()
	{
		return new CopyrightFileConfig();
	}

	@Override
	public TemplateCommentPanel createConfigurable(Project project, TemplateCommentPanel parentPane, FileType fileType)
	{
		return new TemplateCommentPanel(fileType, parentPane, project);
	}
}
