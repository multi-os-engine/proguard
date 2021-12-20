package proguard.classfile;

import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.CompanionClassVisitor;

public class ClassCompanionInitializer
implements ClassVisitor, CompanionClassVisitor {
    private Clazz currentClass;

    @Override
    public void visitAnyClass(Clazz clazz) {
        currentClass = clazz;
    }

    @Override
    public void visitAnyCompanionClass(Clazz clazz) {
        currentClass.addCompanionClass(clazz);
    }
}
