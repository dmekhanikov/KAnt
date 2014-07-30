import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ConditionTask extends Wrapper{
    private String propName;
    private String value;
    private String elseValue;
    private Condition condition;

    public ConditionTask(Attributes attributes) {
        super("condition", attributes);
        propName = attributes.getValue("property");
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
    public String toString(PropertyManager propertyManager) {
        IfStatement ifStatement = new IfStatement();
        ifStatement.setIndent(indent);
        try {
            ifStatement.addChild(condition);
            ConditionBranch thenBranch = new ConditionBranch(new Wrapper("then", null));
            ifStatement.addChild(thenBranch);
            Property thenProperty = new Property(propName, value);
            thenBranch.addChild(thenProperty);
            propertyManager.writeAccess(thenProperty);
            if (elseValue != null) {
                ConditionBranch elseBranch = new ConditionBranch(new Wrapper("else", null));
                Property elseProperty = new Property(propName, elseValue);
                ifStatement.addChild(elseBranch);
                elseBranch.addChild(elseProperty);
                propertyManager.writeAccess(elseProperty);
            }
        } catch (SAXException ignore) {}
        return ifStatement.toString();
    }
}
