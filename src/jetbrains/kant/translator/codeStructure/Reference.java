package jetbrains.kant.translator.codeStructure;

import java.util.ArrayList;
import java.util.List;

public class Reference {
    private String name;
    private Context declarationContext;
    private List<Context> unresolvedAccess = new ArrayList<>();

    public Reference(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Context getDeclarationContext() {
        return declarationContext;
    }

    public void setDeclarationContext(Context declarationContext) {
        this.declarationContext = declarationContext;
    }

    public void addUnresolvedAccess(Context context) {
        unresolvedAccess.add(context);
    }
}
