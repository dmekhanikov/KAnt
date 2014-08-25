package jetbrains.kant.translator;

import jetbrains.kant.gtcommon.ImportManager;

public class Text extends Wrapper {
    private StringBuilder text = new StringBuilder();

    public Text() {
        super((String) null, null);
    }

    public void append(String text) {
        this.text.append(text);
    }

    @Override
    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        return indent + "text {\n" +
                indent + TAB + "\"\"\"\n" +
                StringProcessor.processProperties(StringProcessor.escapeTemplates(text.toString()), propertyManager) + "\n" +
                indent + TAB + "\"\"\"\n" +
                indent + "}";
    }
}
