package org.inventivetalent.chattranslation;

public interface TranslationCallback {

	void call(String original, String translation);

	void error(Throwable throwable);

}
