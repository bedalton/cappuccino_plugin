package cappuccino.ide.intellij.plugin.structure;
import cappuccino.ide.intellij.plugin.lang.ObjJFile;
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.*;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;
import icons.ObjJIcons;
import org.jetbrains.annotations.NotNull;

public class ObjJStructureViewModel  extends StructureViewModelBase implements
        StructureViewModel.ElementInfoProvider {

    ObjJStructureViewModel(PsiFile psiFile) {
        super(psiFile, new ObjJStructureViewElement((ObjJFile)psiFile, new PresentationData(ObjJPsiFileUtil.Companion.getFileNameSafe(psiFile, "Objective-J File"), "", ObjJIcons.DOCUMENT_ICON, null), ObjJPsiFileUtil.Companion.getFileNameSafe(psiFile, "Objective-J File")));
    }

    @NotNull
    public Sorter[] getSorters() {
        return new Sorter[]{Sorter.ALPHA_SORTER};
    }


    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return element instanceof PsiFile;
    }
}