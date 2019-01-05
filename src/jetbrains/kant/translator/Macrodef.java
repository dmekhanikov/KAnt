package jetbrains.kant.translator;

import static jetbrains.kant.gtcommon.StringsKt.toCamelCase;
import static jetbrains.kant.gtcommon.constants.ConstantsKt.DSL_TASK_CONTAINER;

import jetbrains.kant.gtcommon.ImportManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Macrodef extends Wrapper {
    private String macrodefName;

    public Macrodef(Attributes attrs) throws SAXException {
        super((String) null, null);
        macrodefName = attrs.getValue("name");
        if (macrodefName == null) {
            throw new SAXException("Macrodef should have a name");
        }
    }

    public String getMacrodefName() {
        return macrodefName;
    }

    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        String taskContainerShorten = importManager.shorten(DSL_TASK_CONTAINER);
        return indent + "fun " + taskContainerShorten + "." + toCamelCase(macrodefName) +
                renderAttributes(true, propertyManager) + " {\n"
                + renderChildren(propertyManager, importManager) + "\n"
                + indent + "}";
    }
}
