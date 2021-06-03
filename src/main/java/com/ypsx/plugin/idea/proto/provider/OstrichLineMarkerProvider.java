package com.ypsx.plugin.idea.proto.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.impl.source.tree.java.PsiDeclarationStatementImpl;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.ypsx.plugin.idea.proto.Constants;
import com.ypsx.plugin.idea.proto.ProtoIcons;
import com.ypsx.plugin.idea.proto.util.ProtoUtil;
import idea.plugin.protoeditor.lang.psi.PbServiceMethod;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class OstrichLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        OstrichLineElementFilter filter = null;
        if (element instanceof PsiIdentifierImpl
                && element.getParent() instanceof PsiMethodImpl
                && element.getParent().getParent() instanceof PsiClassImpl) {
            filter = new PsiJavaMethodElementFilter();
        }

        if (filter != null) {
            filter.collectNavigationMarkers(element, result);
        }
    }

    private abstract static class OstrichLineElementFilter {
        protected abstract Collection<? extends PsiElement> getResults(@NotNull PsiElement element);

        private void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
            final Collection<? extends PsiElement> results = getResults(element);
            if (!results.isEmpty()) {
                NavigationGutterIconBuilder<PsiElement> builder =
                        NavigationGutterIconBuilder.create(ProtoIcons.FILE)
                                .setTargets(results)
                                .setTooltipText("Navigate to Proto language method");
                result.add(builder.createLineMarkerInfo(element));
            }
        }
    }

    private class PsiJavaMethodElementFilter extends OstrichLineElementFilter {
        @Override
        protected Collection<? extends PsiElement> getResults(@NotNull PsiElement element) {
            PsiClassImpl psiClass = (PsiClassImpl) element.getParent().getParent();
            PsiAnnotation[] annotations = psiClass.getAnnotations();
            List<String> annotationNames = Arrays.stream(annotations).map(PsiAnnotation::getQualifiedName).collect(Collectors.toList());
            if (!annotationNames.contains(Constants.OSTRICH_ANNOTATION_NAME) && !annotationNames.contains(Constants.GRPC_ANNOTATION_NAME)) {
                return new ArrayList<>();
            }
            String className = "";
            Project project = element.getProject();
            String methodName = element.getText();
            if (psiClass.getExtendsListTypes().length > 0) {
                PsiClassType referencedType = psiClass.getExtendsListTypes()[0];
                className = referencedType.getClassName();
            }
            Optional<String> annoClassName = Arrays.stream(annotations).filter(annotation -> annotation.getQualifiedName().equals(Constants.OSTRICH_ANNOTATION_NAME))
                    .map(annotation ->
                            annotation.findAttributeValue("value").getFirstChild().getText())
                    .findFirst();
            //优先根据注解里的value赋值
            if (annoClassName.isPresent()) {
                className = annoClassName.get();
            }
            if (StringUtils.isBlank(className)) {
                return new ArrayList<>();
            }

            List<PbServiceMethod> methods = ProtoUtil.findPropertiesInProject(project, methodName, className);
            return methods;
        }
    }
}
