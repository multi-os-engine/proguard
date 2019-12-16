package proguard.shrink;

import proguard.classfile.ProgramClass;

public class MemberClassUsageMarker extends UsageMarker {
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
