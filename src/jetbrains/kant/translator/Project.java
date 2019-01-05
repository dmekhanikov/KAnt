package jetbrains.kant.translator;

import jetbrains.kant.gtcommon.ImportManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static jetbrains.kant.gtcommon.constants.ConstantsKt.DSL_PROJECT;
import static jetbrains.kant.gtcommon.constants.ConstantsKt.DSL_PROJECT_FUNCTION;

public class Project extends Wrapper {
    private String defaultTarget;
    private Sequential init;

    public Project(Attributes attributes) {
        super(DSL_PROJECT_FUNCTION, null);
        defaultTarget = attributes.getValue("default");
    }

    public String getDefaultTarget() {
        return defaultTarget;
    }

    @Override
    public Wrapper addChild(Wrapper child) throws SAXException {
        if (child instanceof Target) {
            return super.addChild(child);
        } else {
            if (init == null) {
                init = new Sequential();
                init.setParent(this);
            }
            child = init.addChild(child);
            if (child.id == null) {
                child.setIndent(indent + TAB + TAB);
            } else {
                child.setIndent(indent + TAB);
            }
            return child;
        }
    }

    @Override
    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        StringBuilder result = new StringBuilder(indent);
        String dslProjectShorten = importManager.shorten(DSL_PROJECT);
        result.append("object project : ").append(dslProjectShorten).append("() {");
        if (init != null) {
            boolean inInit = false;
            int n = init.children.size();
            for (int i = 0; i < n; i++) {
                Wrapper child = init.children.get(i);
                if (child.id == null && !inInit) {
                    if (i != 0) {
                        result.append(';');
                    }
                    result.append('\n');
                    result.append(indent).append(TAB).append("{");
                    inInit = true;
                } else if (child.id != null && inInit) {
                    result.append('\n').append(indent).append(TAB).append("}");
                    inInit = false;
                }
                result.append('\n').append(child.toString(propertyManager, importManager));
            }
            if (inInit) {
                result.append('\n').append(indent).append(TAB).append('}');
            }
            if (n != 0) {
                result.append('\n');
            }
        }
        for (Wrapper child : children) {
            result.append("\n").append(child.toString(propertyManager, importManager)).append("\n");
        }
        result.append(indent).append("}");
        return result.toString();
    }
}
