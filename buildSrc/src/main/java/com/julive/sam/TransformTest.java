package com.julive.sam;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;


public class TransformTest extends Transform {

    @Override
    public String getName() {
        // 随便起个名字
        return "TransformSam";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        //代表处理的 java 的 class 文件
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        //要处理所有的class字节码
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        // 是否增量编译，我们先不考虑，返回false
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        try {
            //待实现
            doTransform(transformInvocation); // hack
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doTransform(TransformInvocation transformInvocation) throws IOException {
        System.out.println("doTransform   =======================================================");
        //inputs中是传过来的输入流，其中有两种格式，一种是jar包格式一种是目录格式。
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        //获取到输出目录，最后将修改的文件复制到输出目录，这一步必须做不然编译会报错
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();

        //删除之前的输出
        if (outputProvider != null)
            outputProvider.deleteAll();

        inputs.forEach(transformInput -> {
            //遍历directoryInputs
            transformInput.getDirectoryInputs().forEach(directoryInput -> {
                ArrayList<File> list = new ArrayList<>();
                getFileList(directoryInput.getFile(), list);
                list.forEach(file -> {
                    System.out.println("getDirectoryInputs   =======================================================" + file.getName());
                    // 判断是.class文件
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        try {
                            //ASM提供的读取类信息的对象
                            ClassReader classReader = new ClassReader(new FileInputStream(file));
                            //ASM提供的类修改对象，并将读到的信息交给classWriter
                            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                            //创建修改规则，TestClassVisitor
                            ClassVisitor visitor = new TestClassVisitor(classWriter);
                            //将修改规则给classReader
                            classReader.accept(visitor, ClassReader.EXPAND_FRAMES);
                            //通过toByteArray方法，将变更后信息转成byte数组
                            byte[] bytes = classWriter.toByteArray();
                            //放入输出流中往原文件中写入
                            FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
                            fileOutputStream.write(bytes);
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                if (outputProvider != null) {
                    File dest = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                    try {
                        //将该文件放入到目标目录中，这步骤必须实现，否则会导致dex文件找不到该文件
                        FileUtils.copyDirectory(directoryInput.getFile(), dest);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            //jarInputs
            transformInput.getJarInputs().forEach(jarInput -> {
                ArrayList<File> list = new ArrayList<>();
                getFileList(jarInput.getFile(), list);
                list.forEach(file -> {
                    System.out.println("getJarInputs   =======================================================" + file.getName());
                });
                if (outputProvider != null) {
                    File dest = outputProvider.getContentLocation(
                            jarInput.getName(),
                            jarInput.getContentTypes(),
                            jarInput.getScopes(),
                            Format.JAR);
                    //将该文件放入到目标目录中，这步骤必须实现，否则会导致dex文件找不到该文件
                    try {
                        FileUtils.copyFile(jarInput.getFile(), dest);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    //递归查找该文件夹下所有文件，因为我们修改的是.class 文件，而不关系文件夹
    void getFileList(File file, ArrayList<File> fileList) {
        if (file.isFile()) {
            fileList.add(file);
        } else {
            File[] list = file.listFiles();
            for (File value : list) {
                getFileList(value, fileList);
            }
        }
    }
}
