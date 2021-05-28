package com.ypsx.plugin.idea.proto.util;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import com.intellij.psi.impl.java.stubs.impl.PsiClassStubImpl;
import com.intellij.psi.impl.java.stubs.impl.PsiMethodStubImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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
        List<PsiJavaFileImpl> psiJavaFiles = virtualFiles.stream().map(f -> PsiManager.getInstance(project).findFile(f))
                .filter(f -> f instanceof PsiJavaFileImpl)
                .map(f->(PsiJavaFileImpl)f)
                .filter(Objects::nonNull)
                .filter(f->{
                    if (f.getContainingDirectory() instanceof PsiJavaDirectoryImpl) {
                       return !f.getContainingDirectory().toString().contains("/generated-sources");
                    }
                    return true;
                })
                .collect(Collectors.toList());
         return psiJavaFiles.stream().filter(f -> StringUtils.isNotEmpty(f.getPackageName())).map(f -> f.getPackageName() + "." + f.getName().substring(0, f.getName().length() - 5)).collect(Collectors.toList());
    }
}
