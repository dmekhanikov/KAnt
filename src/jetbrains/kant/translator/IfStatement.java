package jetbrains.kant.translator;

import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.List;

public class IfStatement extends Wrapper {
    private Condition condition;
    private Sequential thenStatement;
    private Sequential elseStatement;
    private List<IfStatement> elseifStatements = new ArrayList<>();

    public IfStatement() {
        super("if", null, null);
    }

    public IfStatement(Wrapper wrapper) {
        super(wrapper);
    }

    @Override
    public Wrapper addChild(Wrapper child) throws SAXException{
        child.setParent(this);
        child.setIndent(indent + TAB);
        switch (child.name) {
            case "then":
                if (thenStatement != null) {
                    throw new SAXException("\"if\" cannot contain more than one \"then\" statements");
                }
                thenStatement = new Sequential(child);
                return thenStatement;
            case "else":
                if (parent != null && parent instanceof IfStatement && parent.name.equals("elseif")) {
                    throw new SAXException("\"elseif\" cannot contain \"else\" statements");
                }
                if (elseStatement != null) {
                    throw new SAXException("\"if\" cannot contain more than one \"else\" statements");
                }
                elseStatement = new Sequential(child);
                return elseStatement;
            case "elseif":
                IfStatement elseifStatement = new IfStatement(child);
                elseifStatements.add(elseifStatement);
                return elseifStatement;
            default:
                if (condition != null) {
                    throw new SAXException("\"if\" doesn't support more than one condition");
                }
                child.setIndent("");
                condition = new Condition(child);
                return condition;
        }
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
        StringBuilder result = new StringBuilder(indent);
        result.append("if (");
        result.append(condition.toString(propertyManager));
        result.append(") {\n");
        if (thenStatement != null) {
            result.append(thenStatement.toString(propertyManager)).append("\n");
        }
        result.append(indent).append("}");
        for (IfStatement elseifStatement : elseifStatements) {
            result.append(" else ");
            result.append(elseifStatement.toString(propertyManager).substring(elseifStatement.indent.length()));
        }
        if (elseStatement != null) {
            result.append(" else {\n");
            result.append(elseStatement.toString(propertyManager));
            result.append("\n").append(indent).append("}");
        }
        return result.toString();
    }
}
