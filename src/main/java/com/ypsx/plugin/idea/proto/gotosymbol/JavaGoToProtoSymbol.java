package com.ypsx.plugin.idea.proto.gotosymbol;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.ypsx.plugin.idea.proto.util.ProtoUtil;
import io.protostuff.jetbrains.plugin.psi.RpcMethodNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JavaGoToProtoSymbol implements ChooseByNameContributor {

    @Override
    public String @NotNull [] getNames(Project project, boolean includeNonProjectItems) {
        List<RpcMethodNode> methods = ProtoUtil.findAllProtoRpcMethod(project);
        List<String> names = new ArrayList<>(methods.size());
        for (RpcMethodNode method : methods) {
            if (method.getMethodName() != null && method.getMethodName().length() > 0) {
                names.add(method.getMethodName());
            }
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    public NavigationItem @NotNull [] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        List<RpcMethodNode> methods = ProtoUtil.findAllProtoRpcMethod(project);
        return methods.toArray(new NavigationItem[methods.size()]);
    }
}
