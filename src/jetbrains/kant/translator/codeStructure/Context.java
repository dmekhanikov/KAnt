package jetbrains.kant.translator.codeStructure;

import jetbrains.kant.gtcommon.ImportManager;
import jetbrains.kant.translator.PropertyManager;
import jetbrains.kant.translator.wrappers.Wrapper;

import java.util.*;

public class Context {
    private Context parent;
    private List<Context> children = new ArrayList<>();
    private Wrapper wrapper;
    private Map<String, Variable> variables = new HashMap<>(); // ant name in lower case -> variable with name in camel case
    private PropertyManager propertyManager;
    private ImportManager importManager;
    private Map<String, Reference> referencesPool;
    private int offset;
    private String packageName;

    public Context(String packageName,
                   PropertyManager propertyManager,
                   ImportManager importManager,
                   Map<String, Reference> referencesPool) {
        this.packageName = packageName != null ? packageName : "_DefaultPackage";
        this.propertyManager = propertyManager;
        this.importManager = importManager;
        this.referencesPool = referencesPool;
    }

    public Context(Context parent) {
        this.parent = parent;
        propertyManager = parent.propertyManager;
        importManager = parent.importManager;
        referencesPool = parent.referencesPool;
        this.packageName = parent.packageName;
    }

    public Context getParent() {
        return parent;
    }

    public void setParent(Context parent) {
        this.parent = parent;
    }

    public void addChild(Context child) {
        children.add(child);
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public void addVariable(String name, String type, int offset) {
        Variable variable = new Variable(name, type);
        variable.setOffset(offset);
        variables.put(name, variable);
        Reference ref = getGlobalReference(name);
        ref.setDeclarationContext(this);
    }

    public Variable getVariable(String name) {
        Variable var = variables.get(name);
        if (var == null && parent != null) {
            return parent.getVariable(name);
        } else {
            return var;
        }
    }

    public void addFunctionArgument(String name, String type) {
        Variable variable = new Variable(name, type);
        variable.setFunctionArgument(true);
        variables.put(name.toLowerCase(), variable);
    }

    public boolean hasFunctionArgument(String name) {
        name = name.toLowerCase();
        Variable var = variables.get(name);
        return var != null && var.isFunctionArgument() || parent != null && parent.hasFunctionArgument(name);
    }

    public Variable getFunctionArgument(String name) {
        Variable var = getVariable(name.toLowerCase());
        if (var != null && var.isFunctionArgument()) {
            return var;
        }
        return null;
    }

    public boolean hasReference(String name) {
        Variable var = getVariable(name);
        return var != null && !var.isFunctionArgument() || parent != null && parent.hasReference(name);
    }

    private Reference getGlobalReference(String name) {
        Reference ref = referencesPool.get(name);
        if (ref == null) {
            ref = new Reference(name);
            referencesPool.put(name, ref);
        }
        return ref;
    }

    public void unresolvedReferenceAccess(String name) {
        Reference ref = getGlobalReference(name);
        ref.addUnresolvedAccess(this);
    }

    public void referenceAccess(String name) {
        if (!hasReference(name)) {
            unresolvedReferenceAccess(name);
        }
    }

    public PropertyManager getPropertyManager() {
        return propertyManager;
    }

    public void setPropertyManager(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }

    public ImportManager getImportManager() {
        return importManager;
    }

    public void setImportManager(ImportManager importManager) {
        this.importManager = importManager;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getPackageName() {
        return packageName;
    }
}
