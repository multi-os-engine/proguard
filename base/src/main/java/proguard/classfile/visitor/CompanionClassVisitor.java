package proguard.classfile.visitor;

import proguard.classfile.Clazz;
import proguard.classfile.LibraryClass;
import proguard.classfile.ProgramClass;

public interface CompanionClassVisitor
{
    void visitAnyCompanionClass(Clazz clazz);

    default void visitProgramCompanionClass(ProgramClass programClass)
    {
        visitAnyCompanionClass(programClass);
    }

    default void visitLibraryCompanionClass(LibraryClass libraryClass)
    {
        visitAnyCompanionClass(libraryClass);
    }
}
