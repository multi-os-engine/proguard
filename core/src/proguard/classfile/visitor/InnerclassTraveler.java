package proguard.classfile.visitor;

import proguard.classfile.*;

public class InnerclassTraveler implements ClassVisitor {
    private final ClassVisitor classVisitor;

    public InnerclassTraveler(ClassVisitor classVisitor) {
        this.classVisitor = classVisitor;
    }


    public void visitProgramClass(ProgramClass programClass) {
        programClass.innerclassesAccept(this.classVisitor);
    }


    public void visitLibraryClass(LibraryClass libraryClass) {
        libraryClass.innerclassesAccept(this.classVisitor);
    }
}
