public class Text extends Wrapper {
    private String text;

    public Text() {
        super("text", null);
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return indent + "text {\n" +
                indent + TAB + "\"\"\"\n" +
                StringProcessor.processProperties(StringProcessor.escapeTemplates(text)) + "\n" +
                indent + TAB + "\"\"\"\n" +
                indent + "}";
    }
}
