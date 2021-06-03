package com.ypsx.plugin.idea.proto.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class RPCMethodReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiElement.class),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext context) {
                        //element只能获取到PsiLiteralExpression和PsiCommentExpression ？需要探究
                        if (element instanceof PsiMethodCallExpression) {
                            System.out.println(element);
                        }
//                        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
//                        String value = literalExpression.getValue() instanceof String ?
//                                (String) literalExpression.getValue() : null;
//                        if ((value != null && value.length()>0)) {
//                            TextRange property = new TextRange(1,
//                                    value.length() + 1);
//                            return new PsiReference[]{new RPCMethodReference(element, property)};
//                        }

//                        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) element;
//                        methodCallExpression.getMethodExpression();
//                        if (referenceExpression.getParent() instanceof PsiMethodCallExpressionImpl) {
//                            PsiElement[] children = referenceExpression.getChildren();
//                            for (PsiElement ele : children) {
//                                if (ele instanceof PsiIdentifierImpl) {
//                                    TextRange property = new TextRange(0, ele.getTextOffset());
//                                    return new PsiReference[]{new RPCMethodReference(element, property)};
//                                }
//                            }
//                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                });
    }
}
