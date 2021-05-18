package com.ypsx.plugin.idea.proto.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.ypsx.plugin.idea.proto.Constants;
import com.ypsx.plugin.idea.proto.ProtoIcons;
import com.ypsx.plugin.idea.proto.util.ProtoUtil;
import idea.plugin.protoeditor.lang.psi.PbServiceMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OstrichLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PsiIdentifierImpl)
                || !(element.getParent() instanceof PsiMethodImpl)
                || !(element.getParent().getParent() instanceof PsiClassImpl)) {
            return;
        }
        PsiClassImpl psiClass = (PsiClassImpl) element.getParent().getParent();
        PsiAnnotation[] annotations = psiClass.getAnnotations();
        List<String> annotationNames = Arrays.stream(annotations).map(PsiAnnotation::getQualifiedName).collect(Collectors.toList());
        if (!annotationNames.contains(Constants.ANNOTATION_NAME)) {
            return;
        }
        Project project = element.getProject();
        String methodName = element.getText();
        Optional<String> className = Arrays.stream(annotations).filter(annotation -> annotation.getQualifiedName().equals(Constants.ANNOTATION_NAME))
                .map(annotation ->
                        annotation.findAttributeValue("value").getFirstChild().getText())
                .findFirst();
        if (!className.isPresent()) {
            return;
        }
        List<PbServiceMethod> methods = ProtoUtil.findProperties(project, methodName, className.get());
        if (methods.size() > 0) {
            // Add the property to a collection of line marker info
            NavigationGutterIconBuilder<PsiElement> builder =
                    NavigationGutterIconBuilder.create(ProtoIcons.FILE)
                            .setTargets(methods)
                            .setTooltipText("Navigate to Proto language method");
            result.add(builder.createLineMarkerInfo(element));
        }
    }
}
