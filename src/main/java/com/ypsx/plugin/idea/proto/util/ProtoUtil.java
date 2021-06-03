package com.ypsx.plugin.idea.proto.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import idea.plugin.protoeditor.lang.PbFileType;
import idea.plugin.protoeditor.lang.psi.PbFile;
import idea.plugin.protoeditor.lang.psi.PbServiceBody;
import idea.plugin.protoeditor.lang.psi.PbServiceMethod;
import idea.plugin.protoeditor.lang.psi.PbStatement;
import idea.plugin.protoeditor.lang.psi.impl.PbFileImpl;
import idea.plugin.protoeditor.lang.psi.impl.PbServiceDefinitionImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ProtoUtil {

    public static List<PbServiceMethod> findAllProtoRpcMethod(Project project) {
        Collection<VirtualFile> virtualFiles = findFilesInAllScope(project);
        List<PbServiceMethod> results = new ArrayList<>();
        for (VirtualFile virtualFile : virtualFiles) {
            PbFileImpl protoFile = (PbFileImpl) PsiManager.getInstance(project).findFile(virtualFile);
            if (protoFile != null) {
                List<PbStatement> statements = protoFile.getStatements();
                for (PbStatement statement : statements) {
                    if (statement instanceof PbServiceDefinitionImpl) {
                        String serviceName = ((PbServiceDefinitionImpl) statement).getName();
                        PbServiceBody serviceBody = ((PbServiceDefinitionImpl) statement).getBody();
                        results.addAll(serviceBody.getServiceMethodList());
                    }

                }
            }
        }
        return results;
    }

    public static List<PbServiceMethod> findProperties(Project project, String methodName) {
        Collection<VirtualFile> virtualFiles = findFilesInAllScope(project);
        return findMethodByKey(virtualFiles, project, methodName);
    }

    private static List<PbServiceMethod> findMethodByKey(Collection<VirtualFile> virtualFiles, Project project, String key) {
        List<PbServiceMethod> result = new ArrayList<>();
        for (VirtualFile virtualFile : virtualFiles) {
            PbFileImpl protoFile = (PbFileImpl) PsiManager.getInstance(project).findFile(virtualFile);
            if (protoFile != null) {
                List<PbStatement> statements = protoFile.getStatements();
                for (PbStatement statement : statements) {
                    if (statement instanceof PbServiceDefinitionImpl) {
                        String serviceName = ((PbServiceDefinitionImpl) statement).getName();
                        PbServiceBody serviceBody = ((PbServiceDefinitionImpl) statement).getBody();
                        List<PbServiceMethod> methods = serviceBody.getServiceMethodList();
                        for (PbServiceMethod method : methods) {
                            if (method.getNameIdentifier().getText().equals(key)) {
                                result.add(method);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<PbServiceMethod> findPropertiesInProject(Project project, String methodName, String className) {
        Collection<VirtualFile> virtualFiles = findFilesInProject(project);
        return findMethod(virtualFiles, project, methodName, className);
    }

    private static List<PbServiceMethod> findMethod(Collection<VirtualFile> virtualFiles, Project project, String methodName, String className) {
        List<PbServiceMethod> result = new ArrayList<>();
        for (VirtualFile virtualFile : virtualFiles) {
            PbFileImpl protoFile = (PbFileImpl) PsiManager.getInstance(project).findFile(virtualFile);
            if (protoFile != null) {
                List<PbStatement> statements = protoFile.getStatements();
                for (PbStatement statement : statements) {
                    if (statement instanceof PbServiceDefinitionImpl) {
                        String serviceName = ((PbServiceDefinitionImpl) statement).getName();
                        PbServiceBody serviceBody = ((PbServiceDefinitionImpl) statement).getBody();
                        List<PbServiceMethod> methods = serviceBody.getServiceMethodList();
                        for (PbServiceMethod method : methods) {
                            if (method.getNameIdentifier().getText().equals(methodName) && className.startsWith(serviceName)) {
                                result.add(method);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static <T> Collection<T> findFilesInProject(Project project) {
        return (Collection<T>) FileTypeIndex.getFiles(PbFileType.INSTANCE, GlobalSearchScope.projectScope(project));
    }

    public static <T> Collection<T> findFilesInAllScope(Project project) {
        return (Collection<T>) FileTypeIndex.getFiles(PbFileType.INSTANCE, GlobalSearchScope.allScope(project));
    }
}
