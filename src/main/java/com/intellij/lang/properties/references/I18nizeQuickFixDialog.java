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

/**
 * @author cdr
 */
package com.intellij.lang.properties.references;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.LastSelectedPropertiesFileStore;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.application.ApplicationManager;
import consulo.application.ApplicationPropertiesComponent;
import consulo.application.HelpManager;
import consulo.application.util.function.Computable;
import consulo.language.editor.CodeInsightBundle;
import consulo.language.editor.ui.TreeFileChooser;
import consulo.language.editor.ui.TreeFileChooserFactory;
import consulo.language.file.FileTypeManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.ex.awt.*;
import consulo.ui.ex.awt.event.DocumentAdapter;
import consulo.util.io.FileUtil;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import jakarta.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class I18nizeQuickFixDialog extends DialogWrapper implements I18nizeQuickFixModel
{
	protected static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.i18n.I18nizeQuickFixDialog");

	private static final Pattern PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	private JTextField myValue;
	private JComboBox myKey;
	private final TextFieldWithHistory myPropertiesFile;
	protected JPanel myPanel;
	private JCheckBox myUseResourceBundle;
	protected final Project myProject;
	protected final PsiFile myContext;

	private JPanel myPropertiesFilePanel;
	protected JPanel myExtensibilityPanel;

	protected final String myDefaultPropertyValue;
	protected final DialogCustomization myCustomization;

	public static class DialogCustomization
	{
		private final String title;
		private final boolean suggestExistingProperties;
		private final boolean focusValueComponent;
		private final List<PropertiesFile> propertiesFiles;
		private final String suggestedName;

		public DialogCustomization(String title, boolean suggestExistingProperties, boolean focusValueComponent,
								   List<PropertiesFile> propertiesFiles,
								   String suggestedName)
		{
			this.title = title;
			this.suggestExistingProperties = suggestExistingProperties;
			this.focusValueComponent = focusValueComponent;
			this.propertiesFiles = propertiesFiles;
			this.suggestedName = suggestedName;
		}

		public DialogCustomization()
		{
			this(null, true, false, null, null);
		}

		public String getSuggestedName()
		{
			return suggestedName;
		}
	}

	public I18nizeQuickFixDialog(@Nonnull Project project,
								 @Nonnull final PsiFile context,
								 String defaultPropertyValue,
								 DialogCustomization customization
	)
	{
		this(project, context, defaultPropertyValue, customization, false);
	}

	protected I18nizeQuickFixDialog(@Nonnull Project project,
									@Nonnull final PsiFile context,
									String defaultPropertyValue,
									DialogCustomization customization,
									boolean ancestorResponsible)
	{
		super(false);
		myProject = project;
		myContext = context;

		myDefaultPropertyValue = defaultPropertyValue;
		myCustomization = customization != null ? customization : new DialogCustomization();
		setTitle(myCustomization.title != null ? myCustomization.title : CodeInsightBundle.message("i18nize.dialog.title"));

		myPropertiesFile = new TextFieldWithHistory();
		myPropertiesFile.setHistorySize(-1);
		myPropertiesFilePanel.add(constructFieldWithBrowseButton(myPropertiesFile, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				TreeFileChooserFactory chooserFactory = TreeFileChooserFactory.getInstance(myProject);
				final PropertiesFile propertiesFile = getPropertiesFile();
				TreeFileChooser fileChooser = chooserFactory.createFileChooser(
						CodeInsightBundle.message("i18nize.dialog.property.file.chooser.title"), propertiesFile != null ? propertiesFile.getContainingFile() : null, PropertiesFileType.INSTANCE,
						null);
				fileChooser.showDialog();
				PsiFile selectedFile = fileChooser.getSelectedFile();
				if(selectedFile == null)
				{
					return;
				}
				myPropertiesFile.setText(FileUtil.toSystemDependentName(selectedFile.getVirtualFile().getPath()));
			}
		}, 0), BorderLayout.CENTER);

		myPropertiesFile.addDocumentListener(new DocumentAdapter()
		{
			protected void textChanged(DocumentEvent e)
			{
				propertiesFileChanged();
				somethingChanged();
			}
		});

		getKeyTextField().getDocument().addDocumentListener(new DocumentAdapter()
		{
			protected void textChanged(DocumentEvent e)
			{
				somethingChanged();
			}
		});

		myValue.getDocument().addDocumentListener(new DocumentAdapter()
		{
			protected void textChanged(DocumentEvent e)
			{
				somethingChanged();
			}
		});

		final String KEY = "I18NIZE_DIALOG_USE_RESOURCE_BUNDLE";
		final ApplicationPropertiesComponent applicationPropertiesComponent = ApplicationPropertiesComponent.getInstance();
		final boolean useBundleByDefault = !applicationPropertiesComponent.isValueSet(KEY) || applicationPropertiesComponent.isTrueValue(KEY);
		myUseResourceBundle.setSelected(useBundleByDefault);
		myUseResourceBundle.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				applicationPropertiesComponent.setValue(KEY, Boolean.valueOf(myUseResourceBundle.isSelected()).toString());
			}
		});

		if(!ancestorResponsible)
		{
			init();
		}
	}

	private static JPanel constructFieldWithBrowseButton(final JComponent aComponent, final ActionListener aActionListener, int delta)
	{
		JPanel result = new JPanel(new GridBagLayout());
		result.add(aComponent, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		FixedSizeButton browseButton = new FixedSizeButton(aComponent.getPreferredSize().height - delta);//ignore border in case of browse button
		TextFieldWithBrowseButton.MyDoClickAction.addTo(browseButton, aComponent);
		result.add(browseButton, new GridBagConstraints(1, 0, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		browseButton.addActionListener(aActionListener);

		return result;
	}

	@Override
	protected void init()
	{
		populatePropertiesFiles();
		propertiesFileChanged();
		somethingChanged();
		setKeyValueEditBoxes();

		super.init();
	}

	private JTextField getKeyTextField()
	{
		return (JTextField) myKey.getEditor().getEditorComponent();
	}

	@Nonnull
	protected List<String> getExistingValueKeys(String value)
	{
		if(!myCustomization.suggestExistingProperties)
		{
			return Collections.emptyList();
		}
		final ArrayList<String> result = new ArrayList<String>();

		// check if property value already exists among properties file values and suggest corresponding key
		PropertiesFile propertiesFile = getPropertiesFile();
		if(propertiesFile != null)
		{
			for(IProperty property : propertiesFile.getProperties())
			{
				if(Comparing.strEqual(property.getValue(), value))
				{
					result.add(0, property.getUnescapedKey());
				}
			}
		}
		return result;
	}

	protected String suggestPropertyKey(String value)
	{
		if(myCustomization.suggestedName != null)
		{
			return myCustomization.suggestedName;
		}

		// suggest property key not existing in this file
		String key = defaultSuggestPropertyKey(value);
		value = PATTERN.matcher(Normalizer.normalize(value, Normalizer.Form.NFD)).replaceAll("");
		if(key == null)
		{
			final StringBuilder result = new StringBuilder();
			boolean insertDotBeforeNextWord = false;
			for(int i = 0; i < value.length(); i++)
			{
				final char c = value.charAt(i);
				if(Character.isLetterOrDigit(c))
				{
					if(insertDotBeforeNextWord)
					{
						result.append('.');
					}
					result.append(Character.toLowerCase(c));
					insertDotBeforeNextWord = false;
				}
				else if(c == '&')
				{   //do not insert dot if there is letter after the amp
					if(insertDotBeforeNextWord)
					{
						continue;
					}
					if(i == value.length() - 1)
					{
						continue;
					}
					if(Character.isLetter(value.charAt(i + 1)))
					{
						continue;
					}
					insertDotBeforeNextWord = true;
				}
				else
				{
					if(result.length() > 0)
					{
						insertDotBeforeNextWord = true;
					}
				}
			}
			key = result.toString();
		}

		PropertiesFile propertiesFile = getPropertiesFile();
		if(propertiesFile != null)
		{
			if(propertiesFile.findPropertyByKey(key) == null)
			{
				return key;
			}

			int suffix = 1;
			while(propertiesFile.findPropertyByKey(key + suffix) != null)
			{
				suffix++;
			}
			return key + suffix;
		}
		else
		{
			return key;
		}
	}

	protected String defaultSuggestPropertyKey(String value)
	{
		return null;
	}

	private void propertiesFileChanged()
	{
		PropertiesFile propertiesFile = getPropertiesFile();
		boolean hasResourceBundle =
				propertiesFile != null && propertiesFile.getResourceBundle().getPropertiesFiles(propertiesFile.getProject()).size() > 1;
		myUseResourceBundle.setEnabled(hasResourceBundle);
	}

	private void setKeyValueEditBoxes()
	{
		final List<String> existingValueKeys = getExistingValueKeys(myDefaultPropertyValue);

		if(existingValueKeys.isEmpty())
		{
			getKeyTextField().setText(suggestPropertyKey(myDefaultPropertyValue));
		}
		else
		{
			for(String key : existingValueKeys)
			{
				myKey.addItem(key);
			}
			myKey.setSelectedItem(existingValueKeys.get(0));
		}


		myValue.setText(myDefaultPropertyValue);
	}

	protected void somethingChanged()
	{
		setOKActionEnabled(!StringUtil.isEmptyOrSpaces(getKey()));
	}

	private void populatePropertiesFiles()
	{
		List<String> paths = suggestPropertiesFiles();
		final String lastUrl = suggestSelectedFileUrl(paths);
		final String lastPath = lastUrl == null ? null : FileUtil.toSystemDependentName(VirtualFileUtil.urlToPath(lastUrl));
		Collections.sort(paths, new Comparator<String>()
		{
			public int compare(final String path1, final String path2)
			{
				if(lastPath != null && lastPath.equals(path1))
				{
					return -1;
				}
				if(lastPath != null && lastPath.equals(path2))
				{
					return 1;
				}
				int r = LastSelectedPropertiesFileStore.getUseCount(path2) - LastSelectedPropertiesFileStore.getUseCount(path1);
				return r == 0 ? path1.compareTo(path2) : r;
			}
		});
		myPropertiesFile.setHistory(paths);
		if(lastPath != null)
		{
			myPropertiesFile.setSelectedItem(lastPath);
		}
		if(myPropertiesFile.getSelectedIndex() == -1 && !paths.isEmpty())
		{
			myPropertiesFile.setText(paths.get(0));
		}
	}

	private String suggestSelectedFileUrl(List<String> paths)
	{
		if(myDefaultPropertyValue != null)
		{
			for(String path : paths)
			{
				VirtualFile file = LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(path));
				if(file == null)
				{
					continue;
				}
				PsiFile psiFile = myContext.getManager().findFile(file);
				if(!(psiFile instanceof PropertiesFile))
				{
					continue;
				}
				for(IProperty property : ((PropertiesFile) psiFile).getProperties())
				{
					if(property.getValue().equals(myDefaultPropertyValue))
					{
						return path;
					}
				}
			}
		}
		return LastSelectedPropertiesFileStore.getInstance().suggestLastSelectedPropertiesFileUrl(myContext);
	}

	private void saveLastSelectedFile()
	{
		PropertiesFile propertiesFile = getPropertiesFile();
		if(propertiesFile != null)
		{
			LastSelectedPropertiesFileStore.getInstance().saveLastSelectedPropertiesFile(myContext, propertiesFile);
		}
	}

	protected List<String> suggestPropertiesFiles()
	{
		if(myCustomization.propertiesFiles != null && !myCustomization.propertiesFiles.isEmpty())
		{
			ArrayList<String> list = new ArrayList<String>();
			for(PropertiesFile propertiesFile : myCustomization.propertiesFiles)
			{
				final VirtualFile virtualFile = propertiesFile.getVirtualFile();
				if(virtualFile != null)
				{
					list.add(virtualFile.getPath());
				}
			}
			return list;
		}
		return defaultSuggestPropertiesFiles();
	}

	protected List<String> defaultSuggestPropertiesFiles()
	{
		return I18nUtil.defaultGetPropertyFiles(myProject);
	}

	protected PropertiesFile getPropertiesFile()
	{
		String path = FileUtil.toSystemIndependentName(myPropertiesFile.getText());
		VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
		if(virtualFile != null)
		{
			PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile);
			if(psiFile instanceof PropertiesFile)
			{
				return (PropertiesFile) psiFile;
			}
		}
		return null;
	}

	private boolean createPropertiesFileIfNotExists()
	{
		if(getPropertiesFile() != null)
		{
			return true;
		}
		final String path = FileUtil.toSystemIndependentName(myPropertiesFile.getText());
		if(StringUtil.isEmptyOrSpaces(path))
		{
			String message = CodeInsightBundle.message("i18nize.empty.file.path", myPropertiesFile.getText());
			Messages.showErrorDialog(myProject, message, CodeInsightBundle.message("i18nize.error.creating.properties.file"));
			myPropertiesFile.requestFocusInWindow();
			return false;
		}
		FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(path);
		if(fileType != PropertiesFileType.INSTANCE)
		{
			String message = CodeInsightBundle.message("i18nize.cant.create.properties.file.because.its.name.is.associated",
					myPropertiesFile.getText(), fileType.getDescription());
			Messages.showErrorDialog(myProject, message, CodeInsightBundle.message("i18nize.error.creating.properties.file"));
			myPropertiesFile.requestFocusInWindow();
			return false;
		}

		final VirtualFile virtualFile;
		try
		{
			final File file = new File(path).getCanonicalFile();
			FileUtil.createParentDirs(file);
			final IOException[] e = new IOException[1];
			virtualFile = ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>()
			{
				public VirtualFile compute()
				{
					VirtualFile dir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file.getParentFile());
					try
					{
						if(dir == null)
						{
							throw new IOException("Error creating directory structure for file '" + path + "'");
						}
						return dir.createChildData(this, file.getName());
					}
					catch(IOException e1)
					{
						e[0] = e1;
					}
					return null;
				}
			});
			if(e[0] != null)
			{
				throw e[0];
			}
		}
		catch(IOException e)
		{
			Messages.showErrorDialog(myProject, e.getLocalizedMessage(), CodeInsightBundle.message("i18nize.error.creating.properties.file"));
			return false;
		}

		PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile);
		return psiFile instanceof PropertiesFile;
	}

	protected JComponent createCenterPanel()
	{
		return myPanel;
	}

	public JComponent getPreferredFocusedComponent()
	{
		return myCustomization.focusValueComponent ? myValue : myKey;
	}

	public void dispose()
	{
		saveLastSelectedFile();
		super.dispose();
	}

	protected void doOKAction()
	{
		if(!createPropertiesFileIfNotExists())
		{
			return;
		}
		Collection<PropertiesFile> propertiesFiles = getAllPropertiesFiles();
		for(PropertiesFile propertiesFile : propertiesFiles)
		{
			IProperty existingProperty = propertiesFile.findPropertyByKey(getKey());
			final String propValue = myValue.getText();
			if(existingProperty != null && !Comparing.strEqual(existingProperty.getValue(), propValue))
			{
				Messages.showErrorDialog(myProject, CodeInsightBundle.message("i18nize.dialog.error.property.already.defined.message", getKey(),
						propertiesFile.getName()),
						CodeInsightBundle.message("i18nize.dialog.error.property.already.defined.title"));
				return;
			}
		}

		super.doOKAction();
	}

	@Nonnull
	protected Action[] createActions()
	{
		return new Action[]{
				getOKAction(),
				getCancelAction(),
				getHelpAction()
		};
	}

	public void doHelpAction()
	{
		HelpManager.getInstance().invokeHelp("editing.propertyFile.i18nInspection");
	}


	public JComponent getValueComponent()
	{
		return myValue;
	}

	public String getValue()
	{
		return myValue.getText();
	}

	public String getKey()
	{
		return getKeyTextField().getText();
	}

	public boolean hasValidData()
	{
		assert !ApplicationManager.getApplication().isUnitTestMode();
		show();

		return getExitCode() == OK_EXIT_CODE;
	}

	private boolean isUseResourceBundle()
	{
		return myUseResourceBundle.isEnabled() && myUseResourceBundle.isSelected();
	}

	protected String getDimensionServiceKey()
	{
		return "#com.intellij.codeInsight.i18n.I18nizeQuickFixDialog";
	}

	public Collection<PropertiesFile> getAllPropertiesFiles()
	{
		PropertiesFile propertiesFile = getPropertiesFile();
		if(propertiesFile == null)
		{
			return Collections.emptySet();
		}
		Collection<PropertiesFile> propertiesFiles;
		if(isUseResourceBundle())
		{
			propertiesFiles = propertiesFile.getResourceBundle().getPropertiesFiles(myProject);
		}
		else
		{
			propertiesFiles = Collections.singleton(propertiesFile);
		}
		return propertiesFiles;
	}
}
