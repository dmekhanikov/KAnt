package jetbrains.kant.translator.codeStructure;

public class Variable {
    private String name;
    private String type;
    private int offset; // TODO: drop. We can learn offset from a context.
    private boolean isFunctionArgument;

    public Variable(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isFunctionArgument() {
        return isFunctionArgument;
    }

    public void setFunctionArgument(boolean isFunctionArgument) {
        this.isFunctionArgument = isFunctionArgument;
    }
}
