package com.intellij.lang.properties;

import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.component.util.Iconable;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiInvalidElementAccessException;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.Navigatable;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nullable;

/**
 * @author Dmitry Avdeev
 *         Date: 7/25/11
 */
public interface IProperty extends Navigatable, Iconable {

  String getName();

  PsiElement setName(String name);

  @Nullable
  String getKey();

  @Nullable
  String getValue();

  /**
   * Returns the value with \n, \r, \t, \f and Unicode escape characters converted to their
   * character equivalents.
   *
   * @return unescaped value, or null if no value is specified for this property.
   */
  @Nullable
  String getUnescapedValue();

  /**
   * Returns the key with \n, \r, \t, \f and Unicode escape characters converted to their
   * character equivalents.
   *
   * @return unescaped key, or null if no key is specified for this property.
   */
  @Nullable
  String getUnescapedKey();

  void setValue(@NonNls @Nonnull String value) throws IncorrectOperationException;

  PropertiesFile getPropertiesFile() throws PsiInvalidElementAccessException;

  /**
   * @return text of comment preceding this property. Comment-start characters ('#' and '!') are stripped from the text.
   */
  @Nullable
  String getDocCommentText();

  PsiElement getPsiElement();
}
