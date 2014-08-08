package jetbrains.kant.translator;

public class Text extends Wrapper {
    private String text;

    public Text() {
        super("text", null, null);
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString(PropertyManager propertyManager) {
        return indent + "text {\n" +
                indent + TAB + "\"\"\"\n" +
                StringProcessor.processProperties(StringProcessor.escapeTemplates(text), propertyManager) + "\n" +
                indent + TAB + "\"\"\"\n" +
                indent + "}";
    }
}
