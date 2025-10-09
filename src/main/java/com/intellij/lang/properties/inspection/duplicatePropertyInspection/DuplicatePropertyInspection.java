/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.intellij.lang.properties.inspection.duplicatePropertyInspection;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesBundle;
import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.application.util.StringSearcher;
import consulo.application.util.concurrent.JobLauncher;
import consulo.application.util.function.CommonProcessors;
import consulo.component.ProcessCanceledException;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.language.Language;
import consulo.language.editor.inspection.*;
import consulo.language.editor.inspection.localize.InspectionLocalize;
import consulo.language.editor.inspection.reference.RefManager;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.LowLevelSearchUtil;
import consulo.language.psi.search.PsiSearchHelper;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.properties.localize.PropertiesLocalize;
import consulo.util.lang.CharArrayUtil;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@ExtensionImpl
public class DuplicatePropertyInspection extends GlobalSimpleInspectionTool {
    private static final Logger LOG = Logger.getInstance(DuplicatePropertyInspection.class);

    @RequiredReadAction
    @Override
    public void checkFile(
        @Nonnull PsiFile file,
        @Nonnull InspectionManager manager,
        @Nonnull ProblemsHolder problemsHolder,
        @Nonnull GlobalInspectionContext globalContext,
        @Nonnull ProblemDescriptionsProcessor problemDescriptionsProcessor,
        @Nonnull Object state
    ) {
        checkFile(
            file,
            manager,
            globalContext,
            globalContext.getRefManager(),
            problemDescriptionsProcessor,
            (DuplicatePropertyInspectionState) state
        );
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return PropertiesLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    //public HTMLComposerImpl getComposer() {
    //    return new DescriptorComposer(this) {
    //        protected void composeDescription(final CommonProblemDescriptor description, int i, StringBuffer buf,
    //    final RefEntity refElement) {
    //            @NonNls String descriptionTemplate = description.getDescriptionTemplate();
    //            descriptionTemplate = descriptionTemplate.replaceAll("#end", " ");
    //            buf.append(descriptionTemplate);
    //        }
    //    };
    //}

    @SuppressWarnings({"HardCodedStringLiteral"})
    @RequiredReadAction
    private static void surroundWithHref(StringBuffer anchor, PsiElement element, final boolean isValue) {
        if (element != null) {
            final PsiElement parent = element.getParent();
            PsiElement elementToLink = isValue ? parent.getFirstChild() : parent.getLastChild();
            if (elementToLink != null) {
                HTMLComposer.appendAfterHeaderIndention(anchor);
                HTMLComposer.appendAfterHeaderIndention(anchor);
                anchor.append("<a HREF=\"");
                try {
                    final PsiFile file = element.getContainingFile();
                    if (file != null) {
                        final VirtualFile virtualFile = file.getVirtualFile();
                        if (virtualFile != null) {
                            anchor.append(new URL(virtualFile.getUrl() + "#" + elementToLink.getTextRange()
                                .getStartOffset()));
                        }
                    }
                }
                catch (MalformedURLException e) {
                    LOG.error(e);
                }
                anchor.append("\">");
                anchor.append(elementToLink.getText().replaceAll("\\$", "\\\\\\$"));
                anchor.append("</a>");
                compoundLineLink(anchor, element);
                anchor.append("<br>");
            }
        }
        else {
            anchor.append("<font style=\"font-family:verdana; font-weight:bold; color:#FF0000\";>");
            anchor.append(InspectionLocalize.inspectionExportResultsInvalidatedItem());
            anchor.append("</font>");
        }
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    @RequiredReadAction
    private static void compoundLineLink(StringBuffer lineAnchor, PsiElement psiElement) {
        final PsiFile file = psiElement.getContainingFile();
        if (file != null) {
            final VirtualFile vFile = file.getVirtualFile();
            if (vFile != null) {
                Document doc = FileDocumentManager.getInstance().getDocument(vFile);
                final int lineNumber = doc.getLineNumber(psiElement.getTextOffset()) + 1;
                lineAnchor.append(" ").append(InspectionLocalize.inspectionExportResultsAtLine()).append(" ");
                lineAnchor.append("<a HREF=\"");
                try {
                    int offset = doc.getLineStartOffset(lineNumber - 1);
                    offset = CharArrayUtil.shiftForward(doc.getCharsSequence(), offset, " \t");
                    lineAnchor.append(new URL(vFile.getUrl() + "#" + offset));
                }
                catch (MalformedURLException e) {
                    LOG.error(e);
                }
                lineAnchor.append("\">");
                lineAnchor.append(Integer.toString(lineNumber));
                lineAnchor.append("</a>");
            }
        }
    }

    @RequiredReadAction
    private void checkFile(
        final PsiFile file,
        final InspectionManager manager,
        GlobalInspectionContext context,
        final RefManager refManager,
        final ProblemDescriptionsProcessor processor,
        final DuplicatePropertyInspectionState dupState
    ) {
        if (!(file instanceof PropertiesFile propertiesFile)) {
            return;
        }
        if (!context.isToCheckFile(file, this)) {
            return;
        }
        final PsiSearchHelper searchHelper = PsiSearchHelper.getInstance(file.getProject());
        final List<IProperty> properties = propertiesFile.getProperties();
        Module module = file.getModule();
        if (module == null) {
            return;
        }
        GlobalSearchScope scope = switch (dupState.SCOPE) {
            case FILE -> GlobalSearchScope.fileScope(file);
            case MODULE -> GlobalSearchScope.moduleWithDependenciesScope(module);
            case PROJECT -> GlobalSearchScope.projectScope(file.getProject());
            default -> throw new IllegalArgumentException(dupState.SCOPE.name());
        };

        final Map<String, Set<PsiFile>> processedValueToFiles = Collections.synchronizedMap(new HashMap<String, Set<PsiFile>>());
        final Map<String, Set<PsiFile>> processedKeyToFiles = Collections.synchronizedMap(new HashMap<String, Set<PsiFile>>());
        ProgressManager progressManager = ProgressManager.getInstance();

        final ProgressIndicator original = progressManager.getProgressIndicator();
        final ProgressIndicator progress = progressManager.wrapProgressIndicator(original);
        progressManager.runProcess(
            () -> {
                if (!JobLauncher.getInstance().invokeConcurrentlyUnderProgress(
                    properties,
                    progress,
                    false,
                    property -> {
                        if (original != null) {
                            if (original.isCanceled()) {
                                return false;
                            }
                            original.setText2Value(PropertiesLocalize.searchingForPropertyKeyProgressText(property.getUnescapedKey()));
                        }
                        processTextUsages(processedValueToFiles, property.getValue(), processedKeyToFiles, searchHelper, scope);
                        processTextUsages(processedKeyToFiles, property.getUnescapedKey(), processedValueToFiles,
                            searchHelper, scope
                        );
                        return true;
                    }
                )) {
                    throw new ProcessCanceledException();
                }

                List<ProblemDescriptor> problemDescriptors = new ArrayList<>();
                Map<String, Set<String>> keyToDifferentValues = new HashMap<>();
                if (dupState.CHECK_DUPLICATE_KEYS || dupState.CHECK_DUPLICATE_KEYS_WITH_DIFFERENT_VALUES) {
                    prepareDuplicateKeysByFile(
                        processedKeyToFiles,
                        manager,
                        keyToDifferentValues,
                        problemDescriptors,
                        file,
                        original,
                        dupState
                    );
                }
                if (dupState.CHECK_DUPLICATE_VALUES) {
                    prepareDuplicateValuesByFile(processedValueToFiles, manager, problemDescriptors, file, original);
                }
                if (dupState.CHECK_DUPLICATE_KEYS_WITH_DIFFERENT_VALUES) {
                    processDuplicateKeysWithDifferentValues(
                        keyToDifferentValues,
                        processedKeyToFiles,
                        problemDescriptors,
                        manager,
                        file,
                        original
                    );
                }
                if (!problemDescriptors.isEmpty()) {
                    processor.addProblemElement(refManager.getReference(file), problemDescriptors.toArray(new
                        ProblemDescriptor[problemDescriptors.size()]));
                }
            },
            progress
        );
    }

    private static void processTextUsages(
        final Map<String, Set<PsiFile>> processedTextToFiles,
        final String text,
        final Map<String, Set<PsiFile>> processedFoundTextToFiles,
        final PsiSearchHelper searchHelper,
        final GlobalSearchScope scope
    ) {
        if (!processedTextToFiles.containsKey(text)) {
            if (processedFoundTextToFiles.containsKey(text)) {
                final Set<PsiFile> filesWithValue = processedFoundTextToFiles.get(text);
                processedTextToFiles.put(text, filesWithValue);
            }
            else {
                final Set<PsiFile> resultFiles = new HashSet<>();
                findFilesWithText(text, searchHelper, scope, resultFiles);
                if (resultFiles.isEmpty()) {
                    return;
                }
                processedTextToFiles.put(text, resultFiles);
            }
        }
    }


    @RequiredReadAction
    private static void prepareDuplicateValuesByFile(
        final Map<String, Set<PsiFile>> valueToFiles,
        final InspectionManager manager,
        final List<ProblemDescriptor> problemDescriptors,
        final PsiFile psiFile,
        final ProgressIndicator progress
    ) {
        for (final String value : valueToFiles.keySet()) {
            if (progress != null) {
                progress.setText2Value(InspectionLocalize.duplicatePropertyValueProgressIndicatorText(value));
                progress.checkCanceled();
            }
            if (value.length() == 0) {
                continue;
            }
            StringSearcher searcher = new StringSearcher(value, true, true);
            final StringBuffer message = new StringBuffer();
            final int[] duplicatesCount = {0};
            Set<PsiFile> psiFilesWithDuplicates = valueToFiles.get(value);
            for (final PsiFile file : psiFilesWithDuplicates) {
                CharSequence text = file.getViewProvider().getContents();
                LowLevelSearchUtil.processTextOccurrences(
                    text,
                    0,
                    text.length(),
                    searcher,
                    progress,
                    offset -> {
                        PsiElement element = file.findElementAt(offset);
                        if (element != null && element.getParent() instanceof Property) {
                            final Property property = (Property) element.getParent();
                            if (Comparing.equal(property.getValue(), value) && element.getStartOffsetInParent() != 0) {
                                if (duplicatesCount[0] == 0) {
                                    message.append(InspectionLocalize.duplicatePropertyValueProblemDescriptor(property.getValue()));
                                }
                                surroundWithHref(message, element, true);
                                duplicatesCount[0]++;
                            }
                        }
                        return true;
                    }
                );
            }
            if (duplicatesCount[0] > 1) {
                problemDescriptors.add(manager.createProblemDescriptor(psiFile, message.toString(), false, null,
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                ));
            }
        }
    }

    @RequiredReadAction
    private void prepareDuplicateKeysByFile(
        final Map<String, Set<PsiFile>> keyToFiles,
        final InspectionManager manager,
        final Map<String, Set<String>> keyToValues,
        final List<ProblemDescriptor> problemDescriptors,
        final PsiFile psiFile,
        final ProgressIndicator progress,
        DuplicatePropertyInspectionState state
    ) {
        for (String key : keyToFiles.keySet()) {
            if (progress != null) {
                progress.setText2Value(InspectionLocalize.duplicatePropertyKeyProgressIndicatorText(key));
                if (progress.isCanceled()) {
                    throw new ProcessCanceledException();
                }
            }
            final StringBuffer message = new StringBuffer();
            int duplicatesCount = 0;
            Set<PsiFile> psiFilesWithDuplicates = keyToFiles.get(key);
            for (PsiFile file : psiFilesWithDuplicates) {
                if (!(file instanceof PropertiesFile)) {
                    continue;
                }
                PropertiesFile propertiesFile = (PropertiesFile) file;
                final List<IProperty> propertiesByKey = propertiesFile.findPropertiesByKey(key);
                for (IProperty property : propertiesByKey) {
                    if (duplicatesCount == 0) {
                        message.append(InspectionLocalize.duplicatePropertyKeyProblemDescriptor(key));
                    }
                    surroundWithHref(message, property.getPsiElement().getFirstChild(), false);
                    duplicatesCount++;
                    //prepare for filter same keys different values
                    Set<String> values = keyToValues.get(key);
                    if (values == null) {
                        values = new HashSet<>();
                        keyToValues.put(key, values);
                    }
                    values.add(property.getValue());
                }
            }
            if (duplicatesCount > 1 && state.CHECK_DUPLICATE_KEYS) {
                problemDescriptors.add(manager.createProblemDescriptor(
                    psiFile,
                    message.toString(),
                    false,
                    null,
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                ));
            }
        }

    }

    @RequiredReadAction
    private static void processDuplicateKeysWithDifferentValues(
        final Map<String, Set<String>> keyToDifferentValues,
        final Map<String, Set<PsiFile>> keyToFiles,
        final List<ProblemDescriptor> problemDescriptors,
        final InspectionManager manager,
        final PsiFile psiFile,
        final ProgressIndicator progress
    ) {
        for (String key : keyToDifferentValues.keySet()) {
            if (progress != null) {
                progress.setText2Value(InspectionLocalize.duplicatePropertyDiffKeyProgressIndicatorText(key));
                if (progress.isCanceled()) {
                    throw new ProcessCanceledException();
                }
            }
            final Set<String> values = keyToDifferentValues.get(key);
            if (values == null || values.size() < 2) {
                keyToFiles.remove(key);
            }
            else {
                StringBuffer message = new StringBuffer();
                final Set<PsiFile> psiFiles = keyToFiles.get(key);
                boolean firstUsage = true;
                for (PsiFile file : psiFiles) {
                    if (!(file instanceof PropertiesFile)) {
                        continue;
                    }
                    PropertiesFile propertiesFile = (PropertiesFile) file;
                    final List<IProperty> propertiesByKey = propertiesFile.findPropertiesByKey(key);
                    for (IProperty property : propertiesByKey) {
                        if (firstUsage) {
                            message.append(InspectionLocalize.duplicatePropertyDiffKeyProblemDescriptor(key));
                            firstUsage = false;
                        }
                        surroundWithHref(message, property.getPsiElement().getFirstChild(), false);
                    }
                }
                problemDescriptors.add(manager.createProblemDescriptor(
                    psiFile,
                    message.toString(),
                    false,
                    null,
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                ));
            }
        }
    }

    private static void findFilesWithText(
        String stringToFind,
        PsiSearchHelper searchHelper,
        GlobalSearchScope scope,
        final Set<PsiFile> resultFiles
    ) {
        final List<String> words = StringUtil.getWordsIn(stringToFind);
        if (words.isEmpty()) {
            return;
        }
        Collections.sort(words, (o1, o2) -> o2.length() - o1.length());
        for (String word : words) {
            final Set<PsiFile> files = new HashSet<>();
            searchHelper.processAllFilesWithWord(word, scope, new CommonProcessors.CollectProcessor<>(files),
                true
            );
            if (resultFiles.isEmpty()) {
                resultFiles.addAll(files);
            }
            else {
                resultFiles.retainAll(files);
            }
            if (resultFiles.isEmpty()) {
                return;
            }
        }
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return InspectionLocalize.duplicatePropertyDisplayName();
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return InspectionLocalize.groupNamesPropertiesFiles();
    }

    @Nonnull
    @Override
    public LocalizeValue[] getGroupPath() {
        return new LocalizeValue[]{getGroupDisplayName()};
    }

    @Override
    @Nonnull
    public String getShortName() {
        return "DuplicatePropertyInspection";
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    @Nonnull
    @Override
    public InspectionToolState<?> createStateProvider() {
        return new DuplicatePropertyInspectionState();
    }
}
