package com.julive.sam;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class Plugins implements Plugin<Project> {
    @Override
    public void apply(Project project) {
            //registerTransform
            AppExtension android = project.getExtensions().getByType(AppExtension.class);
            android.registerTransform(new TransformTest());
        }
}
