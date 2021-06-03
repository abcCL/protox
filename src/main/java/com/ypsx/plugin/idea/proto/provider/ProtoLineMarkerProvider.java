package com.ypsx.plugin.idea.proto.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.ypsx.plugin.idea.proto.Constants;
import com.ypsx.plugin.idea.proto.ProtoIcons;
import com.ypsx.plugin.idea.proto.util.JavaUtil;
import idea.plugin.protoeditor.lang.psi.PbServiceDefinition;
import idea.plugin.protoeditor.lang.psi.PbServiceMethod;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ProtoLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        JavaLineElementFilter filter = null;
        if (element instanceof PbServiceMethod) {
            filter = new PsiRpcMethodElementFilter();
        }
        if (filter != null) {
            filter.collectNavigationMarkers(element, result);
        }
    }

    private class PsiRpcMethodElementFilter extends JavaLineElementFilter {

        @Override
        protected Collection<? extends PsiElement> getResults(@NotNull PsiElement element) {
            String key = ((PbServiceMethod) element).getNameIdentifier().getText();
            PbServiceDefinition pbServiceDefinition = (PbServiceDefinition)element.getParent().getParent();
            String definitionName = pbServiceDefinition.getNameIdentifier().getText();
            Project project = element.getProject();
            List<String> list = JavaUtil.findClassNameList(project);
            List<PsiIdentifier> methods = new ArrayList<>();
            for (String s : list) {
                if (methods.size() > 0) {
                    break;
                }
                Optional<PsiClass> psiClass = JavaUtil.findClass(project, s);
                psiClass.ifPresent(c -> {
                    Arrays.stream(c.getAnnotations())
                            .filter(annotation -> annotation.getQualifiedName().equals(Constants.OSTRICH_ANNOTATION_NAME))
                            .findAny().ifPresent(annotation -> {
                        if (!annotation.findAttributeValue("value").getFirstChild().getText().startsWith(definitionName)) {
                            return;
                        }
                        PsiMethod[] allMethods = c.getMethods();
                        Arrays.stream(allMethods).filter(method -> method.getName().equals(key)).forEach(m -> methods.add(m.getNameIdentifier()));
                    });
                });
            }

            //若是按照OstrichService注解未找到 继续根据继承类进行定位
            if (methods.size() == 0) {
                for (String s : list) {
                    if (methods.size() > 0) {
                        break;
                    }
                    Optional<PsiClass> psiClass = JavaUtil.findClass(project, s);
                    psiClass.ifPresent(c -> {
                        if (c.getExtendsListTypes().length > 0) {
                            PsiClassType extendsType = c.getExtendsListTypes()[0];
                            String className = extendsType.getClassName();
                            if (!className.startsWith(definitionName)) {
                                return;
                            }
                            PsiMethod[] allMethods = c.getMethods();
                            Arrays.stream(allMethods).filter(method -> method.getName().equals(key)).forEach(m -> methods.add(m.getNameIdentifier()));
                        }
                    });
                }
            }
            return methods;
        }
    }


    private abstract static class JavaLineElementFilter {
        protected abstract Collection<? extends PsiElement> getResults(@NotNull PsiElement element);

        private void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
            final Collection<? extends PsiElement> results = getResults(element);
            if (!results.isEmpty()) {
                NavigationGutterIconBuilder<PsiElement> builder =
                        NavigationGutterIconBuilder.create(ProtoIcons.FILE)
                                .setTargets(results)
                                .setTooltipText("Navigate to JAVA language method");
                result.add(builder.createLineMarkerInfo(element));
            }
        }
    }
}
