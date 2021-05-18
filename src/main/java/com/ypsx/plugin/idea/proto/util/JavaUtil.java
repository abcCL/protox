package com.ypsx.plugin.idea.proto.util;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.java.stubs.impl.PsiClassStubImpl;
import com.intellij.psi.impl.java.stubs.impl.PsiMethodStubImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubElement;
import idea.plugin.protoeditor.lang.PbFileType;
import idea.plugin.protoeditor.lang.psi.PbFile;
import idea.plugin.protoeditor.lang.psi.PbServiceBody;
import idea.plugin.protoeditor.lang.psi.PbServiceMethod;
import idea.plugin.protoeditor.lang.psi.PbStatement;
import idea.plugin.protoeditor.lang.psi.impl.PbFileImpl;
import idea.plugin.protoeditor.lang.psi.impl.PbServiceDefinitionImpl;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.element.Element;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaUtil {

    /**
     * Find clazz optional.
     *
     * @param project   the project
     * @param clazzName the clazz name
     * @return the optional
     */
    public static Optional<PsiClass> findClass(@NotNull Project project, @NotNull String clazzName) {
        return Optional.ofNullable(JavaPsiFacade.getInstance(project).findClass(clazzName, GlobalSearchScope.projectScope(project)));
    }

    public static List<String> findClassNameList(Project project) {
        Collection<VirtualFile> virtualFiles =
                FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        List<PsiJavaFileImpl> psiJavaFiles = virtualFiles.stream().map(f -> (PsiJavaFileImpl) PsiManager.getInstance(project).findFile(f)).filter(Objects::nonNull).collect(Collectors.toList());
        return psiJavaFiles.stream().filter(f -> StringUtils.isNotEmpty(f.getPackageName())).map(f -> f.getPackageName() + "." + f.getName().substring(0, f.getName().length() - 5)).collect(Collectors.toList());
    }
}
