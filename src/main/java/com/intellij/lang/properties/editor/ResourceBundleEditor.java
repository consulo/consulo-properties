/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
 * @author Alexey
 */
package com.intellij.lang.properties.editor;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.application.Application;
import consulo.application.ApplicationManager;
import consulo.application.WriteAction;
import consulo.codeEditor.*;
import consulo.colorScheme.EditorColorsManager;
import consulo.colorScheme.EditorColorsScheme;
import consulo.dataContext.DataProvider;
import consulo.disposer.Disposer;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.document.event.DocumentAdapter;
import consulo.document.event.DocumentEvent;
import consulo.document.event.DocumentListener;
import consulo.fileEditor.*;
import consulo.fileEditor.event.FileEditorManagerAdapter;
import consulo.fileEditor.event.FileEditorManagerEvent;
import consulo.fileEditor.event.FileEditorManagerListener;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.ide.impl.idea.ide.structureView.newStructureView.StructureViewComponent;
import consulo.language.editor.highlight.LexerEditorHighlighter;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.event.PsiTreeChangeAdapter;
import consulo.language.psi.event.PsiTreeChangeEvent;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.project.ui.view.SelectInContext;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.JBColor;
import consulo.ui.ex.awt.IdeBorderFactory;
import consulo.ui.ex.awt.JBScrollPane;
import consulo.ui.ex.awt.JBSplitter;
import consulo.ui.ex.awt.UIUtil;
import consulo.ui.ex.awt.tree.AbstractTreeUi;
import consulo.ui.ex.awt.util.Alarm;
import consulo.undoRedo.CommandProcessor;
import consulo.util.collection.Stack;
import consulo.util.dataholder.Key;
import consulo.util.dataholder.UserDataHolderBase;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.event.VirtualFileAdapter;
import consulo.virtualFileSystem.event.VirtualFileEvent;
import consulo.virtualFileSystem.event.VirtualFileListener;
import consulo.virtualFileSystem.event.VirtualFilePropertyEvent;
import kava.beans.PropertyChangeListener;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;

public class ResourceBundleEditor extends UserDataHolderBase implements FileEditor
{
	private static final Logger LOG = Logger.getInstance(ResourceBundleEditor.class);
	@NonNls
	private static final String VALUES = "values";
	@NonNls
	private static final String NO_PROPERTY_SELECTED = "noPropertySelected";

	private final StructureViewComponent myStructureViewComponent;
	private final Map<PropertiesFile, Editor> myEditors;
	private final ResourceBundle myResourceBundle;
	private final Map<PropertiesFile, JPanel> myTitledPanels;
	private final JComponent myNoPropertySelectedPanel = new NoPropertySelectedPanel().getComponent();
	private final Map<Editor, DocumentListener> myDocumentListeners = new HashMap<Editor, DocumentListener>();
	private final Project myProject;
	private final DataProviderPanel myDataProviderPanel;
	// user pressed backslash in the corresponding editor.
	// we cannot store it back to properties file right now, so just append the backslash to the editor and wait for the subsequent chars
	private final Set<PropertiesFile> myBackSlashPressed = new HashSet<PropertiesFile>();
	private final Alarm myUpdateEditorAlarm = new Alarm();
	private final Alarm mySelectionChangeAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

	private JPanel myValuesPanel;
	private JPanel myStructureViewPanel;

	private boolean myDisposed;
	private VirtualFileListener myVfsListener;
	private Editor mySelectedEditor;

	public ResourceBundleEditor(Project project, @Nonnull ResourceBundle resourceBundle)
	{
		myProject = project;

		JPanel panel = new JPanel(new BorderLayout());
		JBSplitter splitter = new JBSplitter();
		splitter.setSplitterProportionKey(getClass().getName() + ".splitter");

		myStructureViewPanel = new JPanel();
		splitter.setFirstComponent(myStructureViewPanel);
		myValuesPanel = new JPanel();
		splitter.setSecondComponent(myValuesPanel);
		panel.add(splitter, BorderLayout.CENTER);

		myResourceBundle = resourceBundle;
		myStructureViewComponent = new ResourceBundleStructureViewComponent(project, myResourceBundle, this);
		myStructureViewPanel.setLayout(new BorderLayout());
		myStructureViewPanel.add(myStructureViewComponent, BorderLayout.CENTER);

		myStructureViewComponent.getTree().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
		{
			private String selectedPropertyName;
			private PropertiesFile selectedPropertiesFile;

			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				// filter out temp unselect/select events
				if(getSelectedPropertyName() == null)
				{
					return;
				}
				if(!Comparing.strEqual(selectedPropertyName, getSelectedPropertyName()) || !Comparing.equal(selectedPropertiesFile, getSelectedPropertiesFile()))
				{
					selectedPropertyName = getSelectedPropertyName();
					selectedPropertiesFile = getSelectedPropertiesFile();
					selectionChanged();
				}
			}
		});
		installPropertiesChangeListeners();

		myEditors = new HashMap<PropertiesFile, Editor>();
		myTitledPanels = new HashMap<PropertiesFile, JPanel>();
		recreateEditorsPanel();

		TreeElement[] children = myStructureViewComponent.getTreeModel().getRoot().getChildren();
		if(children.length != 0)
		{
			TreeElement child = children[0];
			String propName = ((ResourceBundlePropertyStructureViewElement) child).getValue();
			setState(new ResourceBundleEditorState(propName));
		}
		myDataProviderPanel = new DataProviderPanel(panel);

		project.getMessageBus().connect(project).subscribe(FileEditorManagerListener.class, new FileEditorManagerAdapter()
		{
			@Override
			public void selectionChanged(@Nonnull FileEditorManagerEvent event)
			{
				onSelectionChanged(event);
			}
		});
	}

	private void onSelectionChanged(@Nonnull FileEditorManagerEvent event)
	{
		// Ignore events which don't target current editor.
		FileEditor oldEditor = event.getOldEditor();
		FileEditor newEditor = event.getNewEditor();
		if(oldEditor != this && newEditor != this)
		{
			return;
		}

		// We want to sync selected property key on selection change.
		if(newEditor == this)
		{
			if(oldEditor instanceof TextEditor)
			{
				setStructureViewSelectionFromPropertiesFile(((TextEditor) oldEditor).getEditor());
			}
		}
		else if(newEditor instanceof TextEditor)
		{
			setPropertiesFileSelectionFromStructureView(((TextEditor) newEditor).getEditor());
		}
	}

	private void setStructureViewSelectionFromPropertiesFile(@Nonnull Editor propertiesFileEditor)
	{
		int line = propertiesFileEditor.getCaretModel().getLogicalPosition().line;
		Document document = propertiesFileEditor.getDocument();
		if(line >= document.getLineCount())
		{
			return;
		}
		final String propertyName = getPropertyName(document, line);
		if(propertyName == null)
		{
			return;
		}
		setStructureViewSelection(propertyName);
	}

	private void setStructureViewSelection(@Nonnull final String propertyName)
	{
		JTree tree = myStructureViewComponent.getTree();
		if(tree == null)
		{
			return;
		}

		Object root = tree.getModel().getRoot();
		if(AbstractTreeUi.isLoadingChildrenFor(root))
		{
			mySelectionChangeAlarm.cancelAllRequests();
			mySelectionChangeAlarm.addRequest(new Runnable()
			{
				@Override
				public void run()
				{
					mySelectionChangeAlarm.cancelAllRequests();
					setStructureViewSelection(propertyName);
				}
			}, 500);
			return;
		}

		Stack<DefaultMutableTreeNode> toCheck = new Stack<>();
		toCheck.push((DefaultMutableTreeNode) root);
		DefaultMutableTreeNode nodeToSelect = null;
		while(!toCheck.isEmpty())
		{
			DefaultMutableTreeNode node = toCheck.pop();
			String value = getNodeValue(node);
			if(propertyName.equals(value))
			{
				nodeToSelect = node;
				break;
			}
			else
			{
				for(int i = 0; i < node.getChildCount(); i++)
				{
					toCheck.push((DefaultMutableTreeNode) node.getChildAt(i));
				}
			}
		}
		if(nodeToSelect != null)
		{
			TreePath path = new TreePath(nodeToSelect.getPath());
			tree.setSelectionPath(path);
			tree.scrollPathToVisible(path);
		}
	}

	@Nullable
	private static String getPropertyName(@Nonnull Document document, int line)
	{
		int startOffset = document.getLineStartOffset(line);
		int endOffset = StringUtil.indexOf(document.getCharsSequence(), '=', startOffset, document.getLineEndOffset(line));
		if(endOffset <= startOffset)
		{
			return null;
		}
		String propertyName = document.getCharsSequence().subSequence(startOffset, endOffset).toString().trim();
		return propertyName.isEmpty() ? null : propertyName;
	}

	private void setPropertiesFileSelectionFromStructureView(@Nonnull Editor propertiesFileEditor)
	{
		String selectedPropertyName = getSelectedPropertyName();
		if(selectedPropertyName == null)
		{
			return;
		}

		Document document = propertiesFileEditor.getDocument();
		for(int i = 0; i < document.getLineCount(); i++)
		{
			String propertyName = getPropertyName(document, i);
			if(selectedPropertyName.equals(propertyName))
			{
				propertiesFileEditor.getCaretModel().moveToLogicalPosition(new LogicalPosition(i, 0));
				return;
			}
		}
	}

	@Nullable
	private static String getNodeValue(@Nonnull DefaultMutableTreeNode node)
	{
		Object userObject = node.getUserObject();
		if(!(userObject instanceof AbstractTreeNode))
		{
			return null;
		}
		Object value = ((AbstractTreeNode) userObject).getValue();
		return value instanceof ResourceBundlePropertyStructureViewElement ? ((ResourceBundlePropertyStructureViewElement) value).getValue() : null;
	}

	private void recreateEditorsPanel()
	{
		myUpdateEditorAlarm.cancelAllRequests();

		myValuesPanel.removeAll();
		myValuesPanel.setLayout(new CardLayout());

		if(!myProject.isOpen())
		{
			return;
		}
		JPanel valuesPanelComponent = new MyJPanel(new GridBagLayout());
		myValuesPanel.add(new JBScrollPane(valuesPanelComponent)
		{
			@Override
			public void updateUI()
			{
				super.updateUI();
				getViewport().setBackground(UIUtil.getPanelBackground());
			}
		}, VALUES);
		myValuesPanel.add(myNoPropertySelectedPanel, NO_PROPERTY_SELECTED);

		List<PropertiesFile> propertiesFiles = myResourceBundle.getPropertiesFiles(myProject);

		GridBagConstraints gc = new GridBagConstraints(0, 0, 0, 0, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0);
		releaseAllEditors();
		myTitledPanels.clear();
		int y = 0;
		for(final PropertiesFile propertiesFile : propertiesFiles)
		{
			final Editor editor = createEditor();
			final Editor oldEditor = myEditors.put(propertiesFile, editor);
			if(oldEditor != null)
			{
				EditorFactory.getInstance().releaseEditor(oldEditor);
			}

			editor.getContentComponent().addFocusListener(new FocusAdapter()
			{
				@Override
				public void focusGained(FocusEvent e)
				{
					mySelectedEditor = editor;
				}
			});
			gc.gridx = 0;
			gc.gridy = y++;
			gc.gridheight = 1;
			gc.gridwidth = GridBagConstraints.REMAINDER;
			gc.weightx = 1;
			gc.weighty = 1;
			gc.anchor = GridBagConstraints.CENTER;

			Locale locale = propertiesFile.getLocale();
			List<String> names = new ArrayList<String>();
			if(!Comparing.strEqual(locale.getDisplayLanguage(), null))
			{
				names.add(locale.getDisplayLanguage());
			}
			if(!Comparing.strEqual(locale.getDisplayCountry(), null))
			{
				names.add(locale.getDisplayCountry());
			}
			if(!Comparing.strEqual(locale.getDisplayVariant(), null))
			{
				names.add(locale.getDisplayVariant());
			}

			String title = propertiesFile.getName();
			if(!names.isEmpty())
			{
				title += " (" + StringUtil.join(names, "/") + ")";
			}
			JComponent comp = new JPanel(new BorderLayout())
			{
				@Override
				public Dimension getPreferredSize()
				{
					Insets insets = getBorder().getBorderInsets(this);
					return new Dimension(100, editor.getLineHeight() * 4 + insets.top + insets.bottom);
				}
			};
			comp.add(editor.getComponent(), BorderLayout.CENTER);
			comp.setBorder(IdeBorderFactory.createTitledBorder(title, true));
			myTitledPanels.put(propertiesFile, (JPanel) comp);

			valuesPanelComponent.add(comp, gc);
		}

		gc.gridx = 0;
		gc.gridy = y;
		gc.gridheight = GridBagConstraints.REMAINDER;
		gc.gridwidth = GridBagConstraints.REMAINDER;
		gc.weightx = 10;
		gc.weighty = 1;

		valuesPanelComponent.add(new JPanel(), gc);
		myValuesPanel.repaint();
	}

	private void installPropertiesChangeListeners()
	{
		final VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
		if(myVfsListener != null)
		{
			assert false;
			virtualFileManager.removeVirtualFileListener(myVfsListener);
		}
		myVfsListener = new VirtualFileAdapter()
		{
			@Override
			public void fileCreated(VirtualFileEvent event)
			{
				if(PropertiesUtil.isPropertiesFile(event.getFile(), myProject))
				{
					recreateEditorsPanel();
				}
			}

			@Override
			public void fileDeleted(VirtualFileEvent event)
			{
				for(PropertiesFile file : myEditors.keySet())
				{
					if(Comparing.equal(file.getVirtualFile(), event.getFile()))
					{
						recreateEditorsPanel();
						return;
					}
				}
			}

			@Override
			public void propertyChanged(VirtualFilePropertyEvent event)
			{
				if(PropertiesUtil.isPropertiesFile(event.getFile(), myProject))
				{
					if(VirtualFile.PROP_NAME.equals(event.getPropertyName()))
					{
						recreateEditorsPanel();
					}
					else
					{
						updateEditorsFromProperties();
					}
				}
			}
		};

		virtualFileManager.addVirtualFileListener(myVfsListener, this);
		PsiTreeChangeAdapter psiTreeChangeAdapter = new PsiTreeChangeAdapter()
		{
			@Override
			public void childAdded(@Nonnull PsiTreeChangeEvent event)
			{
				childrenChanged(event);
			}

			@Override
			public void childRemoved(@Nonnull PsiTreeChangeEvent event)
			{
				childrenChanged(event);
			}

			@Override
			public void childReplaced(@Nonnull PsiTreeChangeEvent event)
			{
				childrenChanged(event);
			}

			@Override
			public void childMoved(@Nonnull PsiTreeChangeEvent event)
			{
				childrenChanged(event);
			}

			@Override
			public void childrenChanged(@Nonnull PsiTreeChangeEvent event)
			{
				final PsiFile file = event.getFile();
				PropertiesFile propertiesFile = PropertiesUtil.getPropertiesFile(file);
				if(propertiesFile == null)
				{
					return;
				}
				if(!propertiesFile.getResourceBundle().equals(myResourceBundle))
				{
					return;
				}
				updateEditorsFromProperties();
			}
		};
		PsiManager.getInstance(myProject).addPsiTreeChangeListener(psiTreeChangeAdapter, this);
	}

	private void selectionChanged()
	{
		myBackSlashPressed.clear();
		UIUtil.invokeLaterIfNeeded(new Runnable()
		{
			@Override
			public void run()
			{
				updateEditorsFromProperties();
			}
		});
	}

	private void updateEditorsFromProperties()
	{
		myUpdateEditorAlarm.cancelAllRequests();
		myUpdateEditorAlarm.addRequest(new Runnable()
		{
			@Override
			public void run()
			{
				if(!isValid())
				{
					return;
				}
				// there is pending update which is going to change prop file anyway
				if(myUpdatePsiAlarm.getActiveRequestCount() != 0)
				{
					myUpdateEditorAlarm.cancelAllRequests();
					myUpdateEditorAlarm.addRequest(this, 200);
					return;
				}
				uninstallDocumentListeners();
				try
				{
					String propertyName = getSelectedPropertyName();
					((CardLayout) myValuesPanel.getLayout()).show(myValuesPanel, propertyName == null ? NO_PROPERTY_SELECTED : VALUES);
					if(propertyName == null)
					{
						return;
					}

					List<PropertiesFile> propertiesFiles = myResourceBundle.getPropertiesFiles(myProject);
					for(final PropertiesFile propertiesFile : propertiesFiles)
					{
						EditorEx editor = (EditorEx) myEditors.get(propertiesFile);
						if(editor == null)
						{
							continue;
						}
						reinitSettings(editor);
						IProperty property = propertiesFile.findPropertyByKey(propertyName);
						final String value;
						if(property == null)
						{
							value = "";
						}
						else
						{
							String rawValue = property.getValue();
							value = rawValue == null ? "" : ResourceBundleUtil.fromPropertyValueToValueEditor(rawValue);
						}
						final Document document = editor.getDocument();
						CommandProcessor.getInstance().executeCommand(null, new Runnable()
						{
							@Override
							public void run()
							{
								ApplicationManager.getApplication().runWriteAction(new Runnable()
								{
									@Override
									public void run()
									{
										updateDocumentFromPropertyValue(value, document, propertiesFile);
									}
								});
							}
						}, "", this);

						JPanel titledPanel = myTitledPanels.get(propertiesFile);
						((TitledBorder) titledPanel.getBorder()).setTitleColor(property == null ? JBColor.RED : UIUtil.getLabelTextForeground());
						titledPanel.repaint();
					}
				}
				finally
				{
					installDocumentListeners();
				}
			}
		}, 200);
	}

	private void updateDocumentFromPropertyValue(final String value, final Document document, final PropertiesFile propertiesFile)
	{
		@NonNls String text = value;
		if(myBackSlashPressed.contains(propertiesFile))
		{
			text += "\\";
		}
		document.replaceString(0, document.getTextLength(), text);
	}

	private void updatePropertyValueFromDocument(final String propertyName, final PropertiesFile propertiesFile, final String text)
	{
		if(PropertiesUtil.isUnescapedBackSlashAtTheEnd(text))
		{
			myBackSlashPressed.add(propertiesFile);
		}
		else
		{
			myBackSlashPressed.remove(propertiesFile);
		}
		IProperty property = propertiesFile.findPropertyByKey(propertyName);
		try
		{
			if(property == null)
			{
				propertiesFile.addProperty(propertyName, text);
			}
			else
			{
				property.setValue(text);
			}
		}
		catch(IncorrectOperationException e)
		{
			LOG.error(e);
		}
	}

	private void installDocumentListeners()
	{
		List<PropertiesFile> propertiesFiles = myResourceBundle.getPropertiesFiles(myProject);
		for(final PropertiesFile propertiesFile : propertiesFiles)
		{
			final EditorEx editor = (EditorEx) myEditors.get(propertiesFile);
			if(editor == null)
			{
				continue;
			}
			DocumentAdapter listener = new DocumentAdapter()
			{
				private String oldText;

				@Override
				public void beforeDocumentChange(DocumentEvent e)
				{
					oldText = e.getDocument().getText();
				}

				@Override
				public void documentChanged(DocumentEvent e)
				{
					Document document = e.getDocument();
					String text = document.getText();
					updatePropertyValueFor(document, propertiesFile, text, oldText);
				}
			};
			myDocumentListeners.put(editor, listener);
			editor.getDocument().addDocumentListener(listener);
		}
	}

	private void uninstallDocumentListeners()
	{
		List<PropertiesFile> propertiesFiles = myResourceBundle.getPropertiesFiles(myProject);
		for(final PropertiesFile propertiesFile : propertiesFiles)
		{
			Editor editor = myEditors.get(propertiesFile);
			uninstallDocumentListener(editor);
		}
	}

	private void uninstallDocumentListener(Editor editor)
	{
		DocumentListener listener = myDocumentListeners.remove(editor);
		if(listener != null)
		{
			editor.getDocument().removeDocumentListener(listener);
		}
	}

	private final Alarm myUpdatePsiAlarm = new Alarm();

	private void updatePropertyValueFor(final Document document, final PropertiesFile propertiesFile, final String text, final String oldText)
	{
		myUpdatePsiAlarm.cancelAllRequests();
		myUpdatePsiAlarm.addRequest(new Runnable()
		{
			@Override
			public void run()
			{
				if(!isValid())
				{
					return;
				}
				CommandProcessor.getInstance().runUndoTransparentAction(new Runnable()
				{
					@Override
					public void run()
					{
						ApplicationManager.getApplication().runWriteAction(new Runnable()
						{
							@Override
							public void run()
							{
								Project project = propertiesFile.getProject();
								PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
								documentManager.commitDocument(document);
								Document propertiesFileDocument = documentManager.getDocument(propertiesFile.getContainingFile());
								if(propertiesFileDocument == null)
								{
									return;
								}
								documentManager.commitDocument(propertiesFileDocument);

								if(!FileDocumentManager.getInstance().requestWriting(document, project))
								{
									uninstallDocumentListeners();
									try
									{
										document.replaceString(0, document.getTextLength(), oldText);
									}
									finally
									{
										installDocumentListeners();
									}
									return;
								}
								String propertyName = getSelectedPropertyName();
								if(propertyName == null)
								{
									return;
								}
								updatePropertyValueFromDocument(propertyName, propertiesFile, text);
							}
						});
					}
				});
			}
		}, 300, Application.get().getModalityStateForComponent(getComponent()));
	}

	@Nullable
	private String getSelectedPropertyName()
	{
		JTree tree = myStructureViewComponent.getTree();
		if(tree == null)
		{
			return null;
		}
		TreePath selected = tree.getSelectionModel().getSelectionPath();
		if(selected == null)
		{
			return null;
		}
		return getNodeValue((DefaultMutableTreeNode) selected.getLastPathComponent());
	}

	@Override
	@Nonnull
	public JComponent getComponent()
	{
		return myDataProviderPanel;
	}

	private Object getData(final Key<?> dataId)
	{
		if(SelectInContext.DATA_KEY == dataId)
		{
			return new SelectInContext()
			{
				@Override
				@Nonnull
				public Project getProject()
				{
					return myProject;
				}

				@Override
				@Nonnull
				public VirtualFile getVirtualFile()
				{
					PropertiesFile selectedFile = getSelectedPropertiesFile();

					VirtualFile virtualFile = selectedFile == null ? null : selectedFile.getVirtualFile();
					assert virtualFile != null;
					return virtualFile;
				}

				@Override
				public Object getSelectorInFile()
				{
					return getSelectedPropertiesFile();
				}

				@Override
				public Supplier<FileEditor> getFileEditorProvider()
				{
					final PropertiesFile selectedPropertiesFile = getSelectedPropertiesFile();
					if(selectedPropertiesFile == null)
					{
						return null;
					}
					return () ->
					{
						final VirtualFile file = selectedPropertiesFile.getVirtualFile();
						if(file == null)
						{
							return null;
						}
						return FileEditorManager.getInstance(getProject()).openFile(file, false)[0];

					};
				}
			};
		}
		return null;
	}

	private PropertiesFile getSelectedPropertiesFile()
	{
		if(mySelectedEditor == null)
		{
			return null;
		}
		PropertiesFile selectedFile = null;
		for(PropertiesFile file : myEditors.keySet())
		{
			Editor editor = myEditors.get(file);
			if(editor == mySelectedEditor)
			{
				selectedFile = file;
				break;
			}
		}
		return selectedFile;
	}

	@Override
	public JComponent getPreferredFocusedComponent()
	{
		return myStructureViewPanel;
	}

	@Override
	@Nonnull
	public String getName()
	{
		return "Resource Bundle";
	}

	@Override
	@Nonnull
	public ResourceBundleEditorState getState(@Nonnull FileEditorStateLevel level)
	{
		return new ResourceBundleEditorState(getSelectedPropertyName());
	}

	@Override
	public void setState(@Nonnull FileEditorState state)
	{
		ResourceBundleEditorState myState = (ResourceBundleEditorState) state;
		String propertyName = myState.myPropertyName;
		if(propertyName != null)
		{
			myStructureViewComponent.select(propertyName, true);
			selectionChanged();
		}
	}

	@Override
	public boolean isModified()
	{
		return false;
	}

	@Override
	public boolean isValid()
	{
		return !myDisposed && !myProject.isDisposed();
	}

	@Override
	public void selectNotify()
	{

	}

	@Override
	public void deselectNotify()
	{

	}

	@Override
	public void addPropertyChangeListener(@Nonnull PropertyChangeListener listener)
	{

	}

	@Override
	public void removePropertyChangeListener(@Nonnull PropertyChangeListener listener)
	{

	}

	@Override
	public FileEditorLocation getCurrentLocation()
	{
		return null;
	}

	@Override
	public void dispose()
	{
		VirtualFileManager.getInstance().removeVirtualFileListener(myVfsListener);

		myDisposed = true;
		Disposer.dispose(myStructureViewComponent);
		releaseAllEditors();
	}

	private void releaseAllEditors()
	{
		for(Editor editor : myEditors.values())
		{
			releaseEditor(editor);
		}
		myEditors.clear();
	}

	private void releaseEditor(Editor editor)
	{
		if(!editor.isDisposed())
		{
			uninstallDocumentListener(editor);
			EditorFactory.getInstance().releaseEditor(editor);
		}
	}

	/**
	 * Renames target property if the one is available.
	 * <p>
	 * <b>Note:</b> is assumed to be called under {@link WriteAction write action}.
	 *
	 * @param oldName old property name
	 * @param newName new property name
	 */
	public void renameProperty(@Nonnull String oldName, @Nonnull String newName)
	{
		for(PropertiesFile properties : myResourceBundle.getPropertiesFiles(myProject))
		{
			IProperty property = properties.findPropertyByKey(oldName);
			if(property != null)
			{
				property.setName(newName);
			}
		}
	}

	public static class ResourceBundleEditorState implements FileEditorState
	{
		private final String myPropertyName;

		public ResourceBundleEditorState(String propertyName)
		{
			myPropertyName = propertyName;
		}

		@Override
		public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level)
		{
			return false;
		}

		public String getPropertyName()
		{
			return myPropertyName;
		}
	}

	private static Editor createEditor()
	{
		EditorFactory editorFactory = EditorFactory.getInstance();
		Document document = editorFactory.createDocument("");
		EditorEx editor = (EditorEx) editorFactory.createEditor(document);
		reinitSettings(editor);
		return editor;
	}

	private static void reinitSettings(final EditorEx editor)
	{
		EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
		editor.setColorsScheme(scheme);
		EditorSettings settings = editor.getSettings();
		settings.setLineNumbersShown(false);
		settings.setWhitespacesShown(false);
		settings.setLineMarkerAreaShown(false);
		settings.setIndentGuidesShown(false);
		settings.setFoldingOutlineShown(false);
		settings.setAdditionalColumnsCount(0);
		settings.setAdditionalLinesCount(0);
		settings.setRightMarginShown(true);
		settings.setRightMargin(60);

		editor.setHighlighter(new LexerEditorHighlighter(new PropertiesValueHighlighter(), scheme));
		editor.setVerticalScrollbarVisible(true);
	}

	private class DataProviderPanel extends JPanel implements DataProvider
	{
		private DataProviderPanel(final JPanel panel)
		{
			super(new BorderLayout());
			add(panel, BorderLayout.CENTER);
		}

		@Override
		@Nullable
		public Object getData(@Nonnull Key<?> dataId)
		{
			return ResourceBundleEditor.this.getData(dataId);
		}
	}

	private class MyJPanel extends JPanel implements Scrollable
	{
		private MyJPanel(LayoutManager layout)
		{
			super(layout);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize()
		{
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
		{
			Editor editor = myEditors.values().iterator().next();
			return editor.getLineHeight() * 4;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
		{
			return visibleRect.height;
		}

		@Override
		public boolean getScrollableTracksViewportWidth()
		{
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight()
		{
			return false;
		}
	}

}
