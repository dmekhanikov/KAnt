package jetbrains.kant.translator.wrappers;

import jetbrains.kant.translator.codeStructure.Context;
import org.xml.sax.SAXException;

public class Sequential extends Wrapper {
    public Sequential(Context context) {
        super((String) null, null, context);
    }

    public Sequential(String name, Context context) {
        super(name, null, context);
    }

    public Sequential(Wrapper wrapper) {
        super(wrapper);
    }

    @Override
    public Wrapper addChild(Wrapper child) throws SAXException {
        child = super.addChild(child);
        child.setIndent(indent);
        return child;
    }

    @Override
    public String getDSLClassName() {
        if (parent != null) {
            return parent.getDSLClassName();
        } else {
            return super.getDSLClassName();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        renderChildren(sb);
        return sb.toString();
    }
}
