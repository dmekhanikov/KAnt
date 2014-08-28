package jetbrains.kant.translator;

import jetbrains.kant.gtcommon.ImportManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ConditionTask extends Wrapper{
    private String propName;
    private String value;
    private String elseValue;
    private Condition condition;

    public ConditionTask(Attributes attributes) throws SAXException {
        super((String) null, attributes);
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
    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        IfStatement ifStatement = new IfStatement();
        ifStatement.setIndent(indent);
        try {
            ifStatement.addChild(condition);
            Sequential thenBranch = new Sequential("then");
            ifStatement.addChild(thenBranch);
            Property thenProperty = new Property(propName, value, null);
            thenBranch.addChild(thenProperty);
            propertyManager.writeAccess(thenProperty);
            if (elseValue != null) {
                Sequential elseBranch = new Sequential("else");
                Property elseProperty = new Property(propName, elseValue, null);
                ifStatement.addChild(elseBranch);
                elseBranch.addChild(elseProperty);
                propertyManager.writeAccess(elseProperty);
            }
        } catch (SAXException ignore) {}
        return ifStatement.toString(propertyManager, importManager);
    }
}
