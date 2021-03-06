package com.intellij.lang.properties.xml;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;
import com.intellij.icons.AllIcons;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.pom.PomRenameableTarget;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiTarget;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import consulo.ui.image.Image;

/**
 * @author Dmitry Avdeev
 *         Date: 7/26/11
 */
public class XmlProperty implements IProperty, PomRenameableTarget, PsiTarget {

  private final XmlTag myTag;
  private final XmlPropertiesFile myPropertiesFile;

  public XmlProperty(XmlTag tag, XmlPropertiesFile xmlPropertiesFile) {
    myTag = tag;
    myPropertiesFile = xmlPropertiesFile;
  }

  @Override
  public String getName() {
    return myTag.getAttributeValue("key");
  }

  @Override
  public boolean isWritable() {
    return myTag.isWritable();
  }

  @Override
  public PsiElement setName(@Nonnull String name) {
    return myTag.setAttribute("key", name);
  }

  @Override
  public String getKey() {
    return getName();
  }

  @Override
  public String getValue() {
    return myTag.getValue().getText();
  }

  @Override
  public String getUnescapedValue() {
    return getValue();
  }

  @Override
  public String getUnescapedKey() {
    return getKey();
  }

  @Override
  public void setValue(@NonNls @Nonnull String value) throws IncorrectOperationException {
    myTag.getValue().setText(value);
  }

  @Override
  public PropertiesFile getPropertiesFile() throws PsiInvalidElementAccessException {
    return myPropertiesFile;
  }

  @Override
  public String getDocCommentText() {
    return null;
  }

  @Override
  public PsiElement getPsiElement() {
    return myTag;
  }

  @Override
  public void navigate(boolean requestFocus) {

  }

  @Override
  public boolean canNavigate() {
    return true;
  }

  @Override
  public boolean canNavigateToSource() {
    return true;
  }

  @Override
  public Image getIcon(int flags) {
    return AllIcons.Nodes.Property;
  }

  @Override
  public boolean isValid() {
    return myTag.isValid();
  }

  @Nonnull
  @Override
  public PsiElement getNavigationElement() {
    return getPsiElement();
  }
}
