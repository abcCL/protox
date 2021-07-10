package com.ypsx.plugin.idea.proto.util;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.protostuff.jetbrains.plugin.ProtoFileType;
import io.protostuff.jetbrains.plugin.psi.ProtoPsiFileRoot;
import io.protostuff.jetbrains.plugin.psi.ProtoType;
import io.protostuff.jetbrains.plugin.psi.RpcMethodNode;
import io.protostuff.jetbrains.plugin.psi.ServiceNode;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProtoUtil {

    /**
     * Searches the entire project for Protobuf language files with instances of the PbServiceMethod name with the given methodName.
     *
     * @param project    current project
     * @param methodName to check
     * @return matching properties
     */
    public static List<RpcMethodNode> findProperties(Project project, String methodName, String classFullName) {
        List<RpcMethodNode> result = new ArrayList<>();
        Collection<VirtualFile> virtualFiles =
                FileTypeIndex.getFiles(ProtoFileType.INSTANCE, GlobalSearchScope.projectScope(project));

        virtualFiles.stream()
                .map(file -> (ProtoPsiFileRoot) PsiManager.getInstance(project).findFile(file))
                .filter(file -> classFullName.startsWith(file.getPackageName()))
                .forEach(file -> {
                    Collection<ProtoType> declaredTypes = file.getProtoRoot().getDeclaredTypes();
                    Optional<ProtoType> serviceNode = declaredTypes.stream().filter(protoType -> protoType instanceof ServiceNode).findAny();
                    if (serviceNode.isPresent()) {
                        ServiceNode node = (ServiceNode) serviceNode.get();
                        List<RpcMethodNode> rpcMethods = node.getRpcMethods();
                        for (RpcMethodNode methodNode : rpcMethods) {
                            if (methodNode.getMethodName().equals(methodName) && classFullName.startsWith(file.getPackageName() + "." + node.getName())) {
                                result.add(methodNode);
                            }
                        }
                        rpcMethods.stream().filter(method -> method.getMethodName().equals(methodName)).findAny();
                    }
                });
        return result;
    }
}
