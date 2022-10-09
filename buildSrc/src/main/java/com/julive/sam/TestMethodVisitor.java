package com.julive.sam;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

import groovyjarjarasm.asm.Opcodes;

public class TestMethodVisitor extends AdviceAdapter {

    private String className;
    private String superName;

    protected TestMethodVisitor(int i, MethodVisitor methodVisitor, int i1, String s, String s1, String className, String superName) {
        super(i, methodVisitor, i1, s, s1);
        this.className = className;
        this.superName = superName;
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        mv.visitLdcInsn("TAG");
        mv.visitLdcInsn(className + "---->" + superName);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(Opcodes.POP);
    }

    @Override
    protected void onMethodExit(int opcode) {
        mv.visitLdcInsn("TAG");
        mv.visitLdcInsn("this is end");
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(Opcodes.POP);
        super.onMethodExit(opcode);
    }
}
