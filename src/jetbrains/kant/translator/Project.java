package jetbrains.kant.translator;

import static jetbrains.kant.KantPackage.toCamelCase;
import static jetbrains.kant.constants.ConstantsPackage.getDSL_PROJECT;
import static jetbrains.kant.constants.ConstantsPackage.getDSL_PROJECT_FUNCTION;
import jetbrains.kant.ImportManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Project extends Wrapper {
    private String defaultTarget;
    private Sequential init;

    public Project(Attributes attributes) {
        super(getDSL_PROJECT_FUNCTION(), null);
        defaultTarget = attributes.getValue("default");
    }

    @Override
    public Wrapper addChild(Wrapper child) throws SAXException {
        if (child instanceof Target) {
            return super.addChild(child);
        } else {
            if (init == null) {
                init = new Sequential();
                init.setParent(this);
                init.setIndent(indent + TAB + TAB);
            }
            return init.addChild(child);
        }
    }

    @Override
    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        StringBuilder result = new StringBuilder(indent);
        String dslProjectShorten = importManager.shorten(getDSL_PROJECT());
        result.append("object project : ").append(dslProjectShorten).append("() {\n");
        if (defaultTarget != null || init != null) {
            result.append(indent).append(TAB).append("{\n");
            result.append(indent).append(TAB).append(TAB).
                    append("default = ::").append(toCamelCase(defaultTarget)).append("\n");
            result.append(init.toString(propertyManager, importManager)).append("\n");
            result.append(indent).append(TAB).append("}\n");
        }
        for (Wrapper child : children) {
            result.append("\n").append(child.toString(propertyManager, importManager)).append("\n");
        }
        result.append(indent).append("}");
        return result.toString();
    }
}
