package jetbrains.kant.translator.wrappers;

import jetbrains.kant.translator.codeStructure.Context;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ConditionTask extends Wrapper {
    private String propName;
    private String value;
    private String elseValue;
    private Condition condition;

    public ConditionTask(Attributes attributes, Context context) throws SAXException {
        super((String) null, attributes, context);
        propName = attributes.getValue("property");
        if (propName == null) {
            throw new SAXException("Condition task should have a \"property\" attribute");
        }
        value = attributes.getValue("value");
        if (value == null) {
            value = "true";
        }
        elseValue = attributes.getValue("else");
    }

    @Override
    public Wrapper addChild(Wrapper child) throws SAXException {
        if (condition != null) {
            throw new SAXException("\"Condition\" task doesn't support more than one condition");
        }
        condition = new Condition(child);
        return condition;
    }

    @Override
    public String toString() {
        IfStatement ifStatement = new IfStatement(context);
        ifStatement.setIndent(indent);
        try {
            ifStatement.addChild(condition);
            Sequential thenBranch = new Sequential("then", context);
            ifStatement.addChild(thenBranch);
            Property thenProperty = new Property(propName, value, null, context);
            thenBranch.addChild(thenProperty);
            context.getPropertyManager().writeAccess(thenProperty);
            if (elseValue != null) {
                Sequential elseBranch = new Sequential("else", context);
                Property elseProperty = new Property(propName, elseValue, null, context);
                ifStatement.addChild(elseBranch);
                elseBranch.addChild(elseProperty);
                context.getPropertyManager().writeAccess(elseProperty);
            }
        } catch (SAXException ignore) {}
        return ifStatement.toString();
    }
}
