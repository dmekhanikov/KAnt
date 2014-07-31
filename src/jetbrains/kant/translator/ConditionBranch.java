package jetbrains.kant.translator;

public class ConditionBranch extends Wrapper {
    public ConditionBranch(Wrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String toString(PropertyManager propertyManager) {
        return renderChildren(propertyManager);
    }
}
