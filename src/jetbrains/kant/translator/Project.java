package jetbrains.kant.translator;

import static jetbrains.kant.KantPackage.toCamelCase;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Project extends Wrapper {
    private HashMap<String, Target> targets = new HashMap<>();
    private String defaultTarget;

    public Project(Attributes attributes) {
        super("project", null, null);
        defaultTarget = attributes.getValue("default");
    }

    @Override
    public Wrapper addChild(Wrapper child) throws SAXException {
        if (child instanceof Target) {
            Target target = (Target) child;
            child.setIndent(indent + TAB);
            child.setParent(this);
            targets.put(target.getTargetName(), target);
        } else {
            super.addChild(child);
        }
        return child;
    }

    @Override
    public String toString(PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder(indent);
        result.append("project(args) {\n");
        if (!children.isEmpty()) {
            result.append(renderChildren(propertyManager)).append("\n");
        }
        List<Target> sortedTargets = sortTargets();
        for (Target target : sortedTargets) {
            result.append("\n").append(target.toString(propertyManager)).append("\n");
            if (target.getTargetName().equals(defaultTarget)) {
                result.append(indent).append(TAB).append("default = ");
                result.append(toCamelCase(defaultTarget)).append("\n");
            }
        }
        result.append(indent).append("}");
        return result.toString();
    }

    private void dfs(Target current, HashSet<String> visited, List<Target> result) {
        visited.add(current.getTargetName());
        String[] depends = current.getDepends();
        if (depends != null) {
            for (String dependName : depends) {
                if (!visited.contains(dependName)) {
                    Target depend = targets.get(dependName);
                    dfs(depend, visited, result);
                }
            }
        }
        result.add(current);
    }

    private List<Target> sortTargets() {
        HashSet<String> visited = new HashSet<>();
        List<Target> result = new ArrayList<>();
        for (Target target : targets.values()) {
            if (!visited.contains(target.getTargetName())) {
                dfs(target, visited, result);
            }
        }
        return result;
    }
}
