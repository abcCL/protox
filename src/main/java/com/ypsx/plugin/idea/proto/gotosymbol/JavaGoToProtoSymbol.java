package com.ypsx.plugin.idea.proto.gotosymbol;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.ypsx.plugin.idea.proto.util.ProtoUtil;
import idea.plugin.protoeditor.lang.psi.PbServiceMethod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JavaGoToProtoSymbol implements ChooseByNameContributor {

    @Override
    public String @NotNull [] getNames(Project project, boolean includeNonProjectItems) {
        List<PbServiceMethod> methods = ProtoUtil.findAllProtoRpcMethod(project);
        List<String> names = new ArrayList<>(methods.size());
        for (PbServiceMethod method : methods) {
            if (method.getNameIdentifier().getText() != null && method.getNameIdentifier().getText().length() > 0) {
                names.add(method.getNameIdentifier().getText());
            }
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    public NavigationItem @NotNull [] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        List<PbServiceMethod> methods = ProtoUtil.findAllProtoRpcMethod(project);
        return methods.toArray(new NavigationItem[methods.size()]);
    }
}
