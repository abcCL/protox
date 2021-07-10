package com.ypsx.plugin.idea.proto.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.ypsx.plugin.idea.proto.Constants;
import com.ypsx.plugin.idea.proto.ProtoIcons;
import com.ypsx.plugin.idea.proto.util.JavaUtil;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import io.protostuff.jetbrains.plugin.psi.RpcMethodNode;
import io.protostuff.jetbrains.plugin.psi.ServiceNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ProtoLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        if (!(element instanceof RpcMethodNode)
                || !(element.getParent() instanceof ServiceNode)
                || !(element.getParent().getParent() instanceof ProtoRootNode)) {
            return;
        }
        String key = ((RpcMethodNode) element).getMethodName();
        ServiceNode serviceNode = (ServiceNode) (element.getParent());
        ProtoRootNode protoRootNode = (ProtoRootNode) element.getParent().getParent();
        String packageName = protoRootNode.getPackageName();
        String definitionName = serviceNode.getName();
        Project project = element.getProject();
        List<String> list = JavaUtil.findClassNameList(project);
        List<PsiIdentifier> methods = new ArrayList<>();
        for (String s : list) {
            Optional<PsiClass> psiClass = JavaUtil.findClass(project, s);
            psiClass.ifPresent(c -> {
                Arrays.stream(c.getAnnotations())
                        .filter(annotation -> annotation.getQualifiedName().equals(Constants.OSTRICH_ANNOTATION_NAME))
                        .findAny().ifPresent(annotation -> {
                    if (!annotation.findAttributeValue("value").getFirstChild().getFirstChild().getReference().getCanonicalText().startsWith(packageName + "." + definitionName)) {
                        return;
                    }
                    PsiMethod[] allMethods = c.getAllMethods();
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
