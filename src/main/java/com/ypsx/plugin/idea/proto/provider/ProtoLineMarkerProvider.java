package com.ypsx.plugin.idea.proto.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.ypsx.plugin.idea.proto.Constants;
import com.ypsx.plugin.idea.proto.ProtoIcons;
import com.ypsx.plugin.idea.proto.util.JavaUtil;
import idea.plugin.protoeditor.lang.psi.impl.PbServiceDefinitionImpl;
import idea.plugin.protoeditor.lang.psi.impl.PbServiceMethodImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ProtoLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PbServiceMethodImpl) || !(element.getParent().getParent() instanceof PbServiceDefinitionImpl)) {
            return;
        }
        String key = ((PbServiceMethodImpl) element).getName();
        PbServiceDefinitionImpl serviceDefinition = (PbServiceDefinitionImpl) (element.getParent().getParent());
        String definitionName = serviceDefinition.getName();
        Project project = element.getProject();
        List<String> list = JavaUtil.findClassNameList(project);
        List<PsiIdentifier> methods = new ArrayList<>();
        list.stream().forEach(name -> {
            Optional<PsiClass> psiClass = JavaUtil.findClass(project, name);
            psiClass.ifPresent(c -> {
                Arrays.stream(c.getAnnotations())
                        .filter(annotation -> annotation.getQualifiedName().equals(Constants.ANNOTATION_NAME))
                        .findAny().ifPresent(annotation -> {
                    if (!annotation.findAttributeValue("value").getFirstChild().getText().startsWith(definitionName)) {
                        return;
                    }
                    PsiMethod[] allMethods = c.getAllMethods();
                    Arrays.stream(allMethods).filter(method -> method.getName().equals(key)).forEach(m -> methods.add(m.getNameIdentifier()));
                });
            });
        });
        if (methods.size() > 0) {
//            // Add the property to a collection of line marker info
            NavigationGutterIconBuilder<PsiElement> builder =
                    NavigationGutterIconBuilder.create(ProtoIcons.FILE)
                            .setTargets(methods)
                            .setTooltipText("Navigate to JAVA language method");
            result.add(builder.createLineMarkerInfo(element));
        }
    }
}
