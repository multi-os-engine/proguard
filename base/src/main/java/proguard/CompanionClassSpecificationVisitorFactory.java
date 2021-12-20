package proguard;

import proguard.classfile.ClassPool;
import proguard.classfile.Clazz;
import proguard.classfile.LibraryClass;
import proguard.classfile.ProgramClass;
import proguard.classfile.visitor.ClassPoolVisitor;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.CompanionClassVisitor;
import proguard.classfile.visitor.MultiClassPoolVisitor;
import proguard.classfile.visitor.MultiClassVisitor;
import proguard.util.WildcardManager;

import java.util.List;

public class CompanionClassSpecificationVisitorFactory
extends ClassSpecificationVisitorFactory
{
    private final ClassPool programClassPool;
    private final ClassPool libraryClassPool;

    public CompanionClassSpecificationVisitorFactory(ClassPool programClassPool,
                                                     ClassPool libraryClassPool)
    {
        this.programClassPool = programClassPool;
        this.libraryClassPool = libraryClassPool;
    }

    public ClassPoolVisitor createClassPoolVisitor(List                  companionClassSpecifications,
                                                   ClassVisitor          classVisitor,
                                                   CompanionClassVisitor companionClassVisitor)
    {
        MultiClassPoolVisitor multiClassPoolVisitor = new MultiClassPoolVisitor();

        if (companionClassSpecifications != null)
        {
            for (int index = 0; index < companionClassSpecifications.size(); index++)
            {
                ClassSpecification companionClassSpecification =
                    (ClassSpecification)companionClassSpecifications.get(index);

                if (companionClassSpecification.classSpecifications != null) {
                    multiClassPoolVisitor.addClassPoolVisitor(
                        createClassPoolVisitor(companionClassSpecification,
                                               classVisitor,
                                               companionClassVisitor));
                }
            }
        }

        return multiClassPoolVisitor;
    }



    private ClassPoolVisitor createClassPoolVisitor(final ClassSpecification    companionClassSpecification,
                                                    final ClassVisitor          classVisitor,
                                                    final CompanionClassVisitor companionClassVisitor)
    {
        // Create a global wildcard manager, so wildcards can be referenced
        // from regular expressions. They are identified by their indices,
        // which imposes a number of tricky constraints:
        // - They need to be parsed in the right order, so the list is filled
        //   out in the expected order (corresponding to the text file
        //   configuration).
        // - They need to be matched in the right order, so the variable
        //   matchers are matched before they are referenced.
        final WildcardManager wildcardManager = new WildcardManager();

        // Parse the outer class. We need to parse it before the companion class
        // specification, to make sure the list of variable string matchers
        // is filled out in the right order.

        // Create a placeholder for the class pool visitor that
        // corresponds to the actual keep specification. Note that we
        // visit the entire class pool for each matched class.
        final MultiClassPoolVisitor companionClassPoolVisitor =
                new MultiClassPoolVisitor();

        final MultiClassVisitor outerClassVisitor = new MultiClassVisitor();

        // Parse the outer class.
        final ClassPoolVisitor outerClassPoolVisitor =
                createClassTester(companionClassSpecification,
                                  outerClassVisitor,
                                  wildcardManager);

        // Parse the companion classes specification and add it to the
        // placeholder.
        for (int index = 0; index < companionClassSpecification.classSpecifications.size(); index++) {
            ClassSpecification classSpecification =
                    (ClassSpecification)companionClassSpecification.classSpecifications.get(index);

            companionClassPoolVisitor.addClassPoolVisitor(
                    createClassTester(classSpecification, new ClassVisitor() {
                        @Override
                        public void visitAnyClass(Clazz clazz) {
                        }

                        @Override
                        public void visitProgramClass(ProgramClass programClass) {
                            companionClassVisitor.visitProgramCompanionClass(programClass);
                        }

                        @Override
                        public void visitLibraryClass(LibraryClass libraryClass) {
                            companionClassVisitor.visitLibraryCompanionClass(libraryClass);
                        }
                    }, wildcardManager)
            );
        }

        // Visit classes that match the outer specification
        outerClassVisitor.addClassVisitor(new ClassVisitor() {
            @Override
            public void visitAnyClass(Clazz clazz) {
                // Visit the outer class first
                if (classVisitor != null) {
                    clazz.accept(classVisitor);
                }

                // Then visit all companion classes
                programClassPool.accept(companionClassPoolVisitor);
                libraryClassPool.accept(companionClassPoolVisitor);
            }
        });

        return outerClassPoolVisitor;
    }
}
