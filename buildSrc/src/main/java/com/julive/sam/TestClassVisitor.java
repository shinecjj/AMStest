package com.julive.sam;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import groovyjarjarasm.asm.Opcodes;

public class TestClassVisitor extends ClassVisitor {

    private String className;
    private String superName;

    TestClassVisitor(final ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor);
    }

    /**
     * 这里可以拿到关于.class的所有信息，比如当前类所实现的接口类表等
     *
     * @param version    表示jdk的版本
     * @param access     当前类的修饰符 （这个和ASM 和 java有些差异，比如public 在这里就是ACC_PUBLIC）
     * @param name       当前类名
     * @param signature  泛型信息
     * @param superName  当前类的父类
     * @param interfaces 当前类实现的接口列表
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superName = superName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        //委托函数
        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
        //找到我们需要修改的类，注意这里是/ 斜杠来表示文件的路径，并不是java代码中的.
        System.out.println("visitMethod   =======================================================" + className);
        if (className.equals("com/julive/samtest/MainActivity")) {
            // 判断方法name是onCreate
            if (name.startsWith("onCreate")) {
                //插桩函数的实现，同样用到ASM提供的对象，下面看具体实现代码
                return new TestMethodVisitor(Opcodes.ASM6, methodVisitor, access, name, descriptor, className, superName);
            }
        }
        return methodVisitor;
    }
}
