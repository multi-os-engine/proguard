package proguard.shrink;

import proguard.classfile.ProgramClass;
import proguard.classfile.visitor.ClassVisitor;

public class MemberClassUsageMarker extends UsageMarker {

    public static ClassVisitor wrapClassVisitor(ClassVisitor classVisitor) {
        if (classVisitor.getClass().equals(UsageMarker.class)) {
            return new MemberClassUsageMarker();
        }

        return classVisitor;
    }

    @Override
    public void visitProgramClass(ProgramClass programClass) {
        if (shouldBeMarkedAsUsed(programClass)) {
            if (isUsed(programClass)) {
                markAsUsed(programClass);
                markProgramClassBody(programClass);

            } else if (shouldBeMarkedAsPossiblyUsed(programClass)) {
                markAsPossiblyUsed(programClass);
            }
        }
    }
}
