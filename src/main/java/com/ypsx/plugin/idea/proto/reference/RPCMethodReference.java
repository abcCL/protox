package com.ypsx.plugin.idea.proto.reference;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.ypsx.plugin.idea.proto.ProtoIcons;
import com.ypsx.plugin.idea.proto.util.ProtoUtil;
import idea.plugin.protoeditor.lang.psi.PbServiceMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RPCMethodReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    private final String key;

    public RPCMethodReference(@NotNull PsiElement element, TextRange textRange) {
        super(element, textRange);
        key = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
    }
    @Override

    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        Project project = myElement.getProject();
        final List<PbServiceMethod> methods = ProtoUtil.findProperties(project, key);
        List<ResolveResult> results = new ArrayList<>();
        for (PbServiceMethod methodNode : methods) {
            results.add(new PsiElementResolveResult(methodNode));
        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    @Override
    public Object @NotNull [] getVariants() {
        Project project = myElement.getProject();
        final List<PbServiceMethod> methods = ProtoUtil.findProperties(project, key);
        List<LookupElement> variants = new ArrayList<>();
        for (final PbServiceMethod method : methods) {
            if (method.getNameIdentifier().getText() != null && method.getNameIdentifier().getText().length() > 0) {
                variants.add(LookupElementBuilder
                        .create(method).withIcon(ProtoIcons.FILE)
                        .withTypeText(method.getContainingFile().getName())
                );
            }
        }
        return variants.toArray();
    }

    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }
}
