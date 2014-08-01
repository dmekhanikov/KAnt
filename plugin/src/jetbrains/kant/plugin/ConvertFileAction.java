package jetbrains.kant.plugin;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import jetbrains.kant.translator.Translator;

import java.io.File;

public class ConvertFileAction extends AnAction {

    private PsiElement getPsiElement(AnActionEvent event) {
        return CommonDataKeys.PSI_FILE.getData(event.getDataContext());
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        String path = getPsiElement(event).getContainingFile().getVirtualFile().getPath();
        File srcFile = new File(path);
        String srcFileName = srcFile.getName();
        String parentPath = srcFile.getParent();
        int pos = srcFileName.lastIndexOf(".xml");
        String dstFileName = srcFileName.substring(0, pos) + ".kt";
        File dstFile = new File(parentPath + "/" + dstFileName);
        Translator.main(srcFile.toString(), dstFile.toString());
        LocalFileSystem.getInstance().refresh(false);
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        PsiElement psiElement = getPsiElement(event);
        presentation.setVisible(psiElement instanceof XmlFile);
    }
}
