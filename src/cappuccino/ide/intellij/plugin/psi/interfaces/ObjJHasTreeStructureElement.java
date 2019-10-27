package cappuccino.ide.intellij.plugin.psi.interfaces;

import cappuccino.ide.intellij.plugin.lang.ObjJFile;
import cappuccino.ide.intellij.plugin.psi.ObjJBodyVariableAssignment;
import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedReference;
import cappuccino.ide.intellij.plugin.psi.ObjJVariableDeclarationList;
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName;
import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.psi.NavigatablePsiElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public interface ObjJHasTreeStructureElement extends ObjJCompositeElement, NavigatablePsiElement {

    ObjJStructureViewElement createTreeStructureElement();

    default TreeElement[] getTreeStructureChildElements() {
        List<TreeElement> treeElements = new ArrayList<>();
        for (ObjJHasTreeStructureElement child : this.getChildrenOfType(ObjJHasTreeStructureElement.class)) {
            treeElements.add(child.createTreeStructureElement());
        }

        if (this instanceof ObjJFile) {
            List<ObjJBodyVariableAssignment> bodyVariableAssignments = this.getChildrenOfType(ObjJBodyVariableAssignment.class);
            List<ObjJVariableName> fileScopeVariables = new ArrayList<>();
            for(ObjJBodyVariableAssignment bodyVariableAssignment : bodyVariableAssignments) {
                if (bodyVariableAssignment.getVarModifier() == null)
                    continue;
                ObjJVariableDeclarationList declarationList = bodyVariableAssignment.getVariableDeclarationList();
                if (declarationList == null)
                    continue;
                fileScopeVariables.addAll(declarationList.getVariableNameList());
                List<ObjJVariableName> variablesInDecList = declarationList.getVariableDeclarationList().stream()
                        .flatMap((dec) -> {
                            return dec.getQualifiedReferenceList().stream()
                                    .map(ObjJQualifiedReference::getPrimaryVar)
                                    .filter(Objects::nonNull);
                        })
                        .collect(Collectors.toList());
                fileScopeVariables.addAll(variablesInDecList);
                treeElements.addAll(
                        fileScopeVariables.stream()
                        .map(ObjJVariableName::createTreeStructureElement)
                        .collect(Collectors.toList())
                );
            }
        }
        return treeElements.toArray(new TreeElement[0]);
    }

}
