package com.ypsx.plugin.idea.proto.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.ypsx.plugin.idea.proto.Constants;
import com.ypsx.plugin.idea.proto.ProtoIcons;
import com.ypsx.plugin.idea.proto.util.ProtoUtil;
import io.protostuff.jetbrains.plugin.psi.RpcMethodNode;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
        if (!annotationNames.contains(Constants.OSTRICH_ANNOTATION_NAME) && !annotationNames.contains(Constants.GRPC_ANNOTATION_NAME)) {
            return;
        }
        String classFullName = "";
        Project project = element.getProject();
        String methodName = element.getText();
        if (psiClass.getExtendsListTypes().length > 0) {
            PsiClassType referencedType = psiClass.getExtendsListTypes()[0];
//            referencedType.getName()
            classFullName = referencedType.getClassName();
        }
        Optional<String> annoClassFullName = Arrays.stream(annotations).filter(annotation -> annotation.getQualifiedName().equals(Constants.OSTRICH_ANNOTATION_NAME))
                .map(annotation ->
                                annotation.findAttributeValue("value").getFirstChild().getFirstChild().getReference().getCanonicalText())
                .findFirst();
        //优先根据注解里的value赋值
        if (annoClassFullName.isPresent()) {
            classFullName = annoClassFullName.get();
        }
        if (StringUtils.isBlank(classFullName)) {
            return;
        }

        List<RpcMethodNode> methods = ProtoUtil.findProperties(project, methodName, classFullName);
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
