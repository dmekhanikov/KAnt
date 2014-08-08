package jetbrains.kant.translator;

import org.xml.sax.SAXException;

public class Sequential extends Wrapper {
    public Sequential() {
        super("sequential", null, null);
    }

    public Sequential(String name) {
        super(name, null, null);
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
    public String toString(PropertyManager propertyManager) {
        return renderChildren(propertyManager);
    }
}
