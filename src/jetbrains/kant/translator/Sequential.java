package jetbrains.kant.translator;

import jetbrains.kant.ImportManager;
import org.xml.sax.SAXException;

public class Sequential extends Wrapper {
    public Sequential() {
        super((String) null, null);
    }

    public Sequential(String name) {
        super(name, null);
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
    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        return renderChildren(propertyManager, importManager);
    }
}
