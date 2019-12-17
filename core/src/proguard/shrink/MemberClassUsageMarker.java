package proguard.shrink;

import proguard.classfile.*;
import proguard.classfile.attribute.InnerClassesInfo;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.visitor.DummyConstantVisitor;
import proguard.classfile.visitor.ClassVisitor;

public class MemberClassUsageMarker extends UsageMarker {

    public static ClassVisitor wrapClassVisitor(ClassVisitor classVisitor) {
        if (classVisitor.getClass().equals(UsageMarker.class)) {
            return new MemberClassUsageMarker();
        }

        return classVisitor;
    }

    private static class OuterClassFinder
    implements InnerClassesInfoVisitor {

        public ProgramClass outerClass = null;
        private ProgramClass innerClass = null;

        @Override
        public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo) {
            innerClassesInfo.innerClassConstantAccept(clazz, new DummyConstantVisitor() {
                @Override
                public void visitClassConstant(Clazz clazz, ClassConstant classConstant) {
                    innerClass = (ProgramClass) classConstant.referencedClass;
                }
            });

            if (clazz.equals(innerClass)) {
                innerClassesInfo.outerClassConstantAccept(clazz, new DummyConstantVisitor() {
                    @Override
                    public void visitClassConstant(Clazz clazz, ClassConstant classConstant) {
                        outerClass = (ProgramClass) classConstant.referencedClass;
                    }
                });
            }
        }
    }

    @Override
    public void visitProgramClass(ProgramClass programClass) {


        if (shouldBeMarkedAsUsed(programClass)) {
            boolean isOuterClassUsed = false;

            // Find the outer class
            OuterClassFinder outerClassFinder = new OuterClassFinder();
            programClass.accept(new AllAttributeVisitor(
                                new AllInnerClassesInfoVisitor(
                                outerClassFinder)));

            // Check if outer class is used
            if (outerClassFinder.outerClass != null)
            {
                isOuterClassUsed = isUsed(outerClassFinder.outerClass);
            }

            if (isOuterClassUsed)
            {
                markAsUsed(programClass);
                markProgramClassBody(programClass);
            }
            else if (shouldBeMarkedAsPossiblyUsed(programClass))
            {
                markAsPossiblyUsed(programClass);
            }
        }
    }
}
