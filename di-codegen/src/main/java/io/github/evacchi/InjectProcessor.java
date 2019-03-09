package io.github.evacchi;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;

@SupportedAnnotationTypes({InjectProcessor.InjectT, InjectProcessor.InjectCandidateT})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class InjectProcessor extends AbstractProcessor {

    static final String InjectT = "javax.inject.Inject";
    static final String InjectCandidateT = "io.github.evacchi.InjectCandidate";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (annotations.isEmpty()) {
            return false;
        }
        Messager m = processingEnv.getMessager();
        m.printMessage(Diagnostic.Kind.WARNING, annotations.toString());

        Bindings bindings = processInjectionCandidates(
                env.getElementsAnnotatedWith(InjectCandidate.class));
        processInjectionSites(
                env.getElementsAnnotatedWith(Inject.class),
                bindings);
        m.printMessage(Diagnostic.Kind.WARNING, bindings.toString());

        generateJavaSources(bindings);

        return false;
    }

    private Bindings processInjectionCandidates(Set<? extends Element> candidates) {
        Bindings result = new Bindings();
        for (Element e : candidates) {
            TypeMirror tm = ((TypeElement) e).getInterfaces().get(0);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                                     ((TypeElement) e).getInterfaces() + "::" + e.toString());
            result.put(tm.toString(),
                       new ObjectCreationExpr(null, new ClassOrInterfaceType(null, e.toString()), NodeList.nodeList()));
        }
        return result;
    }

    private void processInjectionSites(Set<? extends Element> injectionSites, Bindings bindings) {
        Messager m = processingEnv.getMessager();
        m.printMessage(Diagnostic.Kind.WARNING, "BB:" + bindings);
        for (Element e : injectionSites) {
            m.printMessage(Diagnostic.Kind.WARNING,
                           e.asType().toString());
            String className = e.getEnclosingElement().getSimpleName().toString();

            ExecutableElement ee = (ExecutableElement) e;
            NodeList<Expression> args =
                    ee.getParameters().stream().map(ve -> ve.asType().toString()).peek(s -> m.printMessage(Diagnostic.Kind.WARNING, "AA:" + s))
                            .map(bindings::get).collect(Collectors.toCollection(NodeList::new));
            bindings.put(className,
                         new ObjectCreationExpr(
                                 null,
                                 new ClassOrInterfaceType(null, className),
                                 args
                         )
            );
//            bindings.put(
//
//            );
        }
    }

    private void generateJavaSources(Bindings bindings) {
        String packageName = "io.github.evacchi";
        String className = "GeneratedBinder";
        String sourceFileName = packageName + "." + className;
        CompilationUnit cu = new CompilationUnit();
        ClassOrInterfaceDeclaration cls = cu
                .setPackageDeclaration("io.github.evacchi")
                .addClass(className);
        MethodDeclaration methodDeclaration = cls
                .addMethod("createInstance")
                .setTypeParameters(new NodeList<>(new TypeParameter("T")))
                .setType("T")
                .setModifiers(Modifier.Keyword.PUBLIC)
                .addParameter(Class.class, "type");

        BlockStmt blockStmt = new BlockStmt();

        for (String intf : bindings.interfaces()) {
            ObjectCreationExpr cons = bindings.get(intf);
            IfStmt ifStmt = new IfStmt(
                    new BinaryExpr(
                            new ClassExpr(new ClassOrInterfaceType(null, intf)), new NameExpr("type"), BinaryExpr.Operator.EQUALS),
                    new ReturnStmt(new CastExpr(new ClassOrInterfaceType(null, "T"), cons)),
                    null);

            blockStmt.addStatement(ifStmt);
        }
        blockStmt.addStatement(new ThrowStmt(new ObjectCreationExpr().setType(new ClassOrInterfaceType(null, "UnsupportedOperationException"))));

        methodDeclaration.setBody(blockStmt);
        try {
            JavaFileObject newSourceFile =
                    processingEnv.getFiler().createSourceFile(sourceFileName);
            try (Writer writer = newSourceFile.openWriter()) {
                writer.write(cu.toString());
            }
        } catch (IOException e) {
            processingEnv.getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }
}
