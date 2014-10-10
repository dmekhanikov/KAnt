package jetbrains.kant.translator.wrappers;

import static jetbrains.kant.gtcommon.GtcommonPackage.toCamelCase;
import static jetbrains.kant.gtcommon.constants.ConstantsPackage.getDSL_TASK_CONTAINER;

import jetbrains.kant.translator.codeStructure.Context;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Macrodef extends Wrapper {
    private String macrodefName;

    public Macrodef(Attributes attrs, Context context) throws SAXException {
        super((String) null, null, context);
        macrodefName = attrs.getValue("name");
        if (macrodefName == null) {
            throw new SAXException("Macrodef should have a name");
        }
    }

    public String getMacrodefName() {
        return macrodefName;
    }

    @Override
    public String toString() {
        String taskContainerShorten = context.getImportManager().shorten(getDSL_TASK_CONTAINER());
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("fun ").append(taskContainerShorten).append(".").
                append(toCamelCase(macrodefName)).append(renderAttributes(true, context)).append(" {\n");
        renderChildren(sb);
        sb.append("\n").append(indent).append("}");
        return sb.toString();
    }
}
