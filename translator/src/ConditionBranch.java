
public class ConditionBranch extends Wrapper {
    public ConditionBranch(Wrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String toString() {
        return renderChildren();
    }
}
