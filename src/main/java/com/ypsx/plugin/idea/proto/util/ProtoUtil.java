package com.ypsx.plugin.idea.proto.util;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ProtoUtil {

    public static List<RpcMethodNode> findAllProtoRpcMethod(Project project) {
        Collection<VirtualFile> virtualFiles = findFilesInAllScope(project);
        List<RpcMethodNode> results = new ArrayList<>();
        for (VirtualFile virtualFile : virtualFiles) {
            ProtoPsiFileRoot protoFile = (ProtoPsiFileRoot) PsiManager.getInstance(project).findFile(virtualFile);
            Collection<ProtoType> declaredTypes = protoFile.getProtoRoot().getDeclaredTypes();
            Optional<ProtoType> serviceNode = declaredTypes.stream().filter(protoType -> protoType instanceof ServiceNode).findAny();
            if (serviceNode.isPresent()) {
                ServiceNode node = (ServiceNode) serviceNode.get();
                List<RpcMethodNode> rpcMethods = node.getRpcMethods();
                results.addAll(rpcMethods);
            }
        }
        return results;
    }

    public static List<RpcMethodNode> findPropertiesInProject(Project project, String methodName, String className) {
        Collection<VirtualFile> virtualFiles = findFilesInProject(project);
        return findMethod(virtualFiles, project, methodName, className);
    }

    private static List<RpcMethodNode> findMethod(Collection<VirtualFile> virtualFiles, Project project, String methodName, String className) {
        List<RpcMethodNode> result = new ArrayList<>();
        for (VirtualFile virtualFile : virtualFiles) {
            ProtoPsiFileRoot protoFile = (ProtoPsiFileRoot) PsiManager.getInstance(project).findFile(virtualFile);
            Collection<ProtoType> declaredTypes = protoFile.getProtoRoot().getDeclaredTypes();
            Optional<ProtoType> serviceNode = declaredTypes.stream().filter(protoType -> protoType instanceof ServiceNode).findAny();
            if (serviceNode.isPresent()) {
                ServiceNode node = (ServiceNode) serviceNode.get();
                List<RpcMethodNode> rpcMethods = node.getRpcMethods();
                for (RpcMethodNode methodNode : rpcMethods) {
                    if (methodNode.getMethodName().equals(methodName) && className.startsWith(node.getName())) {
                        result.add(methodNode);
                    }
                }
                rpcMethods.stream().filter(method -> method.getMethodName().equals(methodName)).findAny();
            }
        }
        return result;
    }

    public static <T> Collection<T> findFilesInProject(Project project) {
        return (Collection<T>) FileTypeIndex.getFiles(ProtoFileType.INSTANCE, GlobalSearchScope.projectScope(project));
    }

    public static <T> Collection<T> findFilesInAllScope(Project project) {
        return (Collection<T>) FileTypeIndex.getFiles(ProtoFileType.INSTANCE, GlobalSearchScope.allScope(project));
    }
}
