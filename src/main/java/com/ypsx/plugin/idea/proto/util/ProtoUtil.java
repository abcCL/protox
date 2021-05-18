package com.ypsx.plugin.idea.proto.util;

import com.intellij.ide.highlighter.JavaFileType;
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

public class ProtoUtil {

    /**
     * Searches the entire project for Protobuf language files with instances of the PbServiceMethod name with the given methodName.
     *
     * @param project current project
     * @param methodName     to check
     * @return matching properties
     */
    public static List<PbServiceMethod> findProperties(Project project, String methodName,String className) {
        List<PbServiceMethod> result = new ArrayList<>();
        Collection<VirtualFile> virtualFiles =
                FileTypeIndex.getFiles(PbFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PbFile protoFile = (PbFileImpl) PsiManager.getInstance(project).findFile(virtualFile);
            if (protoFile != null) {
                List<PbStatement> statements = protoFile.getStatements();
                for (PbStatement statement : statements) {
                    if (statement instanceof PbServiceDefinitionImpl) {
                        String serviceName = ((PbServiceDefinitionImpl) statement).getName();
                        PbServiceBody serviceBody = ((PbServiceDefinitionImpl) statement).getBody();
                        List<PbServiceMethod> serviceMethods = serviceBody.getServiceMethodList();
                        for (PbServiceMethod method : serviceMethods) {
                            if (method.getNameIdentifier().getText().equals(methodName)
                            && className.startsWith(serviceName)) { ////约定实现类的类名以proto的service名称为前缀
                                result.add(method);
                            }
                        }
                    }
                }

            }
        }
        return result;
    }
}
