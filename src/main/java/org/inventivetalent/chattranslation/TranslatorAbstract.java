package org.inventivetalent.chattranslation;

public abstract class TranslatorAbstract {

	public abstract void translate(String original, String sourceLanguage, String targetLanguage, TranslationCallback callback);

}
