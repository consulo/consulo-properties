package com.intellij.lang.properties;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicImpl;
import consulo.application.ApplicationManager;
import consulo.document.FileDocumentManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FileTypeIndex;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.encoding.EncodingManager;
import consulo.virtualFileSystem.encoding.EncodingManagerListener;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;

@TopicImpl(ComponentScope.PROJECT)
public class PropertiesFilesManagerEncodingListener implements EncodingManagerListener
{
	private final Project myProject;

	@Inject
	public PropertiesFilesManagerEncodingListener(Project project)
	{
		myProject = project;
	}

	@Override
	public void propertyChanged(@Nullable Object document, @Nonnull String propertyName, Object oldValue, Object newValue)
	{
		if(EncodingManager.PROP_NATIVE2ASCII_SWITCH.equals(propertyName) ||
				EncodingManager.PROP_PROPERTIES_FILES_ENCODING.equals(propertyName))
		{
			DumbService.getInstance(myProject).smartInvokeLater(new Runnable()
			{
				public void run()
				{
					ApplicationManager.getApplication().runWriteAction(() ->
					{
						if(myProject.isDisposed())
						{
							return;
						}
						Collection<VirtualFile> filesToRefresh = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PropertiesFileType.INSTANCE, GlobalSearchScope.allScope
								(myProject));
						VirtualFile[] virtualFiles = VirtualFileUtil.toVirtualFileArray(filesToRefresh);
						FileDocumentManager.getInstance().saveAllDocuments();

						//force to re-detect encoding
						for(VirtualFile virtualFile : virtualFiles)
						{
							virtualFile.setCharset(null);
						}
						FileDocumentManager.getInstance().reloadFiles(virtualFiles);
					});
				}
			});
		}
	}
}
