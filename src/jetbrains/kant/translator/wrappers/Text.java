package jetbrains.kant.translator.wrappers;

import jetbrains.kant.translator.StringProcessor;
import jetbrains.kant.translator.codeStructure.Context;

public class Text extends Wrapper {
    private StringBuilder text = new StringBuilder();

    public Text(Context context) {
        super((String) null, null, context);
    }

    public void append(String text) {
        this.text.append(text);
    }

    @Override
    public String toString() {
        return indent + "text {\n" +
                indent + TAB + "\"\"\"\n" +
                StringProcessor.processProperties(StringProcessor.escapeTemplates(text.toString()), context) + "\n" +
                indent + TAB + "\"\"\"\n" +
                indent + "}";
    }
}
